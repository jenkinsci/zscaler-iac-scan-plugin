package io.jenkins.plugins.zscaler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import hudson.AbortException;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import io.jenkins.plugins.zscaler.models.NotificationsConfig;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

public class RunScanTask extends MasterToSlaveCallable<Object, RuntimeException> {
  TaskListener listener;
  FilePath workspace;
  Configuration configuration;
  String binaryLoc;
  ProxyConfiguration proxyConfiguration;
  String scanId;

  public RunScanTask(
      TaskListener listener,
      FilePath workspace,
      Configuration configuration,
      String binaryLoc,
      ProxyConfiguration proxyConfiguration,
      String scanId) {
    this.listener = listener;
    this.workspace = workspace;
    this.configuration = configuration;
    this.binaryLoc = binaryLoc;
    this.proxyConfiguration = proxyConfiguration;
    this.scanId = scanId;
  }

  @Override
  public Object call() throws RuntimeException {
    try {
      return runScan(listener, workspace.toURI().getPath());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String runScan(TaskListener listener, String workspace) throws IOException {
    listener.getLogger().println("Running Zscaler IaC scan");
    String jenkinsHome = binaryLoc;

    ProcessBuilder processBuilder = new ProcessBuilder();
    Process exec = null;
    String resp;
    String configFile = getConfigFile(jenkinsHome);
    String proxyString = ClientUtils.getProxyConfigString(proxyConfiguration);
    try {
      String[] command = {
        "./zscanner",
        "scan",
        "--scan-id",
        scanId,
        "--config-path",
        configFile,
        "--show-passed",
        "-o",
        "json",
        "-d",
        workspace
      };
      if (proxyString != null) {
        ArrayUtils.add(command, "--proxy");
        ArrayUtils.add(command, proxyString);
      }
      exec = processBuilder.command(command).directory(new File(jenkinsHome)).start();

      try (InputStream errorStream = exec.getErrorStream();
          InputStream resultStream = exec.getInputStream()) {
        resp = IOUtils.toString(resultStream, Charset.defaultCharset());
        String err = IOUtils.toString(errorStream, Charset.defaultCharset());
        if (StringUtils.isNotBlank(err)) {
          listener.getLogger().println(err);
        }
      }
    } finally {
      if (exec != null) {
        exec.destroy();
      }
      if (Paths.get(configFile).toFile().exists()) {
        FileUtils.forceDelete(Paths.get(configFile).toFile());
      }
      ZscannerSetup.cleanup(binaryLoc);
    }

    if (StringUtils.isBlank(resp)) {
      // if result is null then err in above try block will be populated
      listener.getLogger().println("Failed to run scan, the results are empty");
      throw new AbortException("Zscaler IaC scan failed");
    } else {
      return resp;
    }
  }

  private String getConfigFile(String jenkinsHome) throws IOException {
    String apiUrl = configuration.getApiUrl();
    Path configFile = Paths.get(jenkinsHome, "zscaler", "config.yaml");
    if (!configFile.toFile().exists()) {
      Files.createFile(
          configFile,
          PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr--r--")));
      NotificationsConfig cliConfig = new NotificationsConfig();

      Map<String, NotificationsConfig.WebHook> notifications = new HashMap<>();
      NotificationsConfig.WebHook webhook = new NotificationsConfig.WebHook();
      webhook.setType("webhook");
      NotificationsConfig.Config config = new NotificationsConfig.Config();
      config.setUrl(apiUrl + "/iac/findings-processor/v1/scan/results/" + scanId);
      webhook.setConfig(config);
      notifications.put("webhook", webhook);
      cliConfig.setNotifications(notifications);

      ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
      objectMapper.writeValue(configFile.toFile(), cliConfig);
    }

    return configFile.toAbsolutePath().toString();
  }
}