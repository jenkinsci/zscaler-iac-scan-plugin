package io.jenkins.plugins.zscaler;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.*;
import hudson.model.*;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildWrapperDescriptor;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildWrapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ZscalerScan extends SimpleBuildWrapper {

  private static final Logger LOGGER = Logger.getLogger(ZscalerScan.class.getName());

  private boolean failBuild;

  @DataBoundConstructor
  public ZscalerScan(boolean failBuild) {
    this.failBuild = failBuild;
  }

  @Override
  public void setUp(
      Context context,
      Run<?, ?> build,
      FilePath workspace,
      Launcher launcher,
      TaskListener listener,
      EnvVars initialEnvironment)
      throws IOException, InterruptedException {

    try {
      BuildDetails buildDetails = getBuildDetails(build, listener);
      build.addAction(new Report(build));
      if (workspace != null) {

        FilePath rootDir = null;
        VirtualChannel channel = launcher.getChannel();
        if (channel != null) {
          Computer computer = Computer.currentComputer();
          if (computer != null) {
            Node node = computer.getNode();
            if (node != null) {
              rootDir = node.getRootPath();
            }
          }
          if (rootDir == null) {
            rootDir = workspace;
          }
          listener
              .getLogger()
              .println("Zscaler IaC binary location set to - " + rootDir.toURI().getPath());

          ProxyConfiguration proxy = Jenkins.get().proxy;
          StandardUsernamePasswordCredentials credentials = resolveCredentials();
          channel.call(
              new ScannerSetupTask(
                  listener, Configuration.get(), proxy, rootDir.toURI().getPath(), credentials));
          String results =
              (String)
                  channel.call(
                      new RunScanTask(
                          listener,
                          workspace,
                          Configuration.get(),
                          rootDir.toURI().getPath(),
                          proxy,
                          buildDetails));
          File buildDir = build.getParent().getBuildDir();
          postResultsToWorkspace(results, buildDir.getAbsolutePath(), build.getNumber());
          validateAndFailBuild(results, listener);
        }
      }
    } catch (Exception e) {
      // TODO Report failure back to platform
      listener.getLogger().println(e.getMessage());
      throw e;
    }
  }

  private BuildDetails getBuildDetails(Run build, TaskListener listener) {
    BuildDetails buildDetails = new BuildDetails();
    buildDetails.setIntegrationId(Configuration.get().getIntegrationId());
    buildDetails.setJobName(build.getParent().getDisplayName());
    buildDetails.setBuildNumber(String.valueOf(build.getNumber()));
    buildDetails.setBuildRunTimestamp(build.getTimestampString2());
    buildDetails.setSubType(1);
    buildDetails.setStatus(0);
    try {
      String configXml = getConfigXml(build);
      if (configXml != null) {
        SCMDetails.populateSCMDetails(buildDetails, configXml);

        JSONObject configJson = XML.toJSONObject(configXml);
        JSONObject project = configJson.optJSONObject("project");
        if (project != null) {
          JSONObject buildWrappers = project.optJSONObject("buildWrappers");
          if (buildWrappers != null) {
            JSONObject pluginConfig = buildWrappers.optJSONObject("io.jenkins.plugins.zscaler.ZscalerScan");
            if (pluginConfig != null) {
              String failBuild = pluginConfig.optString("failBuild");
              if (failBuild != null) {
                buildDetails.addAdditionalDetails("fail_build", failBuild);
              }
            }
          }
        }
      } else {
        listener
            .getLogger()
            .format("Config xml for the job %s not found.", build.getParent().getName());
      }
    } catch (Exception e) {
      listener
          .getLogger()
          .println("Failed to populate config information due to - " + e.getMessage());
    }
    return buildDetails;
  }

  private StandardUsernamePasswordCredentials resolveCredentials() throws AbortException {
    String credentialsId = Configuration.get().getCredentialsId();
    Optional<StandardUsernamePasswordCredentials> credentials =
        Configuration.resolveUserNamePassword(credentialsId);
    if (credentials.isPresent()) {
      return credentials.get();
    } else {
      throw new AbortException("Invalid credentials to connect to Zscaler");
    }
  }

  @VisibleForTesting
  void postResultsToWorkspace(String results, String workspace, int buildNumber)
      throws IOException {

    Path path =
        Paths.get(
            workspace, String.valueOf(buildNumber), "iac-scan-results", buildNumber + ".json");
    Path parent = path.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    Files.write(path, results.getBytes(StandardCharsets.UTF_8));
  }

  @VisibleForTesting
  void validateAndFailBuild(String results, TaskListener listener) throws IOException {
    listener.getLogger().println(results);
  LOGGER.log(Level.INFO, "Results::" + results);
    if (isFailBuild()) {
      JSONObject jsonObject = new JSONObject(results);
      JSONArray violations = jsonObject.optJSONArray("failed_policies");
      if (violations != null && violations.length() > 0) {
        for (int i = 0; i < violations.length(); i++) {
          JSONObject violation = violations.optJSONObject(i);
          String severity = violation.optString("severity");
          if (severity != null && ("HIGH".equals(severity.toUpperCase(Locale.ROOT)) || "CRITICAL".equals(severity.toUpperCase(Locale.ROOT)))) {
            throw new AbortException("Zscaler IaC scan found violations, they need to be fixed");
          }
        }
      }
    } else {
      listener.getLogger().println("Zscaler IaC scan found violations");
    }
  }

  private String getConfigXml(Run build) {
    Job job = build.getParent();
    File jobDir = job.getRootDir();
    File[] files = jobDir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.getName().equals("config.xml")) {
          try (FileInputStream fileStream = new FileInputStream(file)) {
            return IOUtils.toString(fileStream, Charset.defaultCharset());
          } catch (IOException e) {
            LOGGER.log(
                Level.SEVERE,
                String.format(
                    "Failed to read file - %s/%s ", file.getAbsolutePath(), file.getName()));
          }
        }
      }
    }
    return null;
  }

  public boolean isFailBuild() {
    return failBuild;
  }

  @DataBoundSetter
  public void setFailBuild(boolean failBuild) {
    this.failBuild = failBuild;
  }

  @Extension
  public static class DescriptorImpl extends BuildWrapperDescriptor {

    @NonNull
    @Override
    public String getDisplayName() {
      return "Zscaler IaC scan";
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> abstractProject) {
      return true;
    }
  }
}
