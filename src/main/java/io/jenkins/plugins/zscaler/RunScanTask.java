package io.jenkins.plugins.zscaler;

import hudson.AbortException;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunScanTask extends MasterToSlaveCallable<Object, RuntimeException> {

  TaskListener listener;
  FilePath workspace;
  Configuration configuration;
  String binaryLoc;
  ProxyConfiguration proxyConfiguration;
  BuildDetails buildDetails;
  private static final Logger LOGGER = Logger.getLogger(RunScanTask.class.getName());
  public RunScanTask(
      TaskListener listener,
      FilePath workspace,
      Configuration configuration,
      String binaryLoc,
      ProxyConfiguration proxyConfiguration,
      BuildDetails buildDetails) {
    this.listener = listener;
    this.workspace = workspace;
    this.configuration = configuration;
    this.binaryLoc = binaryLoc;
    this.proxyConfiguration = proxyConfiguration;
    this.buildDetails = buildDetails;
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
    String proxyString = ClientUtils.getProxyConfigString(proxyConfiguration);
    try {
      String[] command = {
              "./zscanner",
              "scan",
              "-o",
              "json",
              "-m",
              "cicd",
              "-d",
              workspace,
              "--sub-type",
              "JENKINS",
              "--event-type",
              "BUILD",
              "--event-id",
              buildDetails.getBuildNumber(),
      };

      if (buildDetails.getRepoLoc() != null) {
        ArrayUtils.add(command, "--repo");
        ArrayUtils.add(command, buildDetails.getRepoLoc());
      }
      if(buildDetails.getBuildTriggeredBy()!=null){
        ArrayUtils.add(command,"--triggered-by");
        ArrayUtils.add(command,buildDetails.getBuildTriggeredBy());
      }
      if (buildDetails.getAdditionalDetails() != null && buildDetails.getAdditionalDetails().get("scm_type") != null) {
        ArrayUtils.add(command, "--repo-type");
        ArrayUtils.add(command, buildDetails.getAdditionalDetails().get("scm_type"));
      }
      if (buildDetails.getAdditionalDetails() != null && buildDetails.getAdditionalDetails().get("log_level") != null) {
        ArrayUtils.add(command, "-l");
        ArrayUtils.add(command, buildDetails.getAdditionalDetails().get("log_level"));
      }
      if (proxyString != null) {
        ArrayUtils.add(command, "--proxy");
        ArrayUtils.add(command, proxyString);
      }
      LOGGER.log(Level.INFO, "Command ::" + Arrays.toString(command) + "  Jenkins Home::" + jenkinsHome);
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
}
