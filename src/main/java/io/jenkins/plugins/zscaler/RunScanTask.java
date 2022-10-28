package io.jenkins.plugins.zscaler;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.AbortException;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunScanTask extends MasterToSlaveCallable<Object, RuntimeException> {

  TaskListener listener;
  FilePath workspace;
  Configuration configuration;
  String binaryLoc;
  ProxyConfiguration proxyConfiguration;
  BuildDetails buildDetails;
  String filePath;
  String dirPath;
  private static final Logger LOGGER = Logger.getLogger(RunScanTask.class.getName());
  private static final String NO_IAC_RESOURCES_FOUND_MSG = "No IaC resources found";
  public RunScanTask(
      TaskListener listener,
      FilePath workspace,
      Configuration configuration,
      String binaryLoc,
      ProxyConfiguration proxyConfiguration,
      BuildDetails buildDetails, String filePath, String dirPath) {
    this.listener = listener;
    this.workspace = workspace;
    this.configuration = configuration;
    this.binaryLoc = binaryLoc;
    this.proxyConfiguration = proxyConfiguration;
    this.buildDetails = buildDetails;
    this.filePath = filePath;
    this.dirPath = dirPath;
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
              "--sub-type",
              "JENKINS",
              "--event-type",
              "BUILD",
              "--event-id",
              buildDetails.getBuildNumber(),
              "--json-format-version",
              "v2"
      };

      List<String> commandPrefix = Arrays.asList(command);
      List<String> commandList = new ArrayList<>();
      commandList.addAll(commandPrefix);

      if(StringUtils.isNotEmpty(filePath)) {
        commandList.add("-f");
        commandList.add(filePath);
      } else if (StringUtils.isNotEmpty(dirPath)){
        commandList.add("-d");
        commandList.add(dirPath);
      } else {
        commandList.add("-d");
        commandList.add(workspace);
      }
      if (buildDetails.getRepoLoc() != null) {
        commandList.add("--repo");
        commandList.add(buildDetails.getRepoLoc());
      }

      if(buildDetails.getBranchName()!=null){
        commandList.add("--branch");
        commandList.add(buildDetails.getBranchName());
      }

      if(buildDetails.getCommitSha()!=null){
        commandList.add("--ref");
        commandList.add(buildDetails.getCommitSha());
      }

      if(buildDetails.getBuildTriggeredBy() != null){
        commandList.add("--triggered-by");
        commandList.add(buildDetails.getBuildTriggeredBy());
      }
      if (buildDetails.getAdditionalDetails() != null && buildDetails.getAdditionalDetails().get(BuildDetails.scmType) != null) {
        commandList.add("--repo-type");
        commandList.add(buildDetails.getAdditionalDetails().get("scm_type"));
      }
      if (buildDetails.getAdditionalDetails() != null && StringUtils.isNotEmpty(buildDetails.getAdditionalDetails().get("log_level"))) {
        commandList.add("--log-level");
        commandList.add(buildDetails.getAdditionalDetails().get("log_level"));
      }

      if (buildDetails.getEventDetails() != null && buildDetails.getEventDetails().size() > 0) {
        ObjectMapper objectMapper = new ObjectMapper();
        commandList.add("--event-details");
        commandList.add(objectMapper.writeValueAsString(buildDetails.getEventDetails()));
      }

      if (buildDetails.getRepoDetails() != null && buildDetails.getRepoDetails().size() > 0) {
        ObjectMapper repoDetailsMapper = new ObjectMapper();
        commandList.add("--repo-details");
        commandList.add(repoDetailsMapper.writeValueAsString(buildDetails.getRepoDetails()));
      }

      if (proxyString != null) {
        commandList.add("--proxy");
        commandList.add(proxyString);
      }
      LOGGER.log(Level.INFO, "Command ::" + commandList + "  Jenkins Home::" + jenkinsHome);
      exec = processBuilder.command(commandList).directory(new File(jenkinsHome)).start();

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

    if (StringUtils.isEmpty(resp) && StringUtils.equals(resp, NO_IAC_RESOURCES_FOUND_MSG)) {
      // if result is null then err in above try block will be populated
      listener.getLogger().println("Failed to run scan, the results are empty");
      throw new AbortException("Zscaler IaC scan failed");
    } else {
      return resp;
    }
  }
}
