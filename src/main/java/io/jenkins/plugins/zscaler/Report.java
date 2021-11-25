package io.jenkins.plugins.zscaler;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.ManagementLink;
import hudson.model.Run;
import io.jenkins.plugins.zscaler.scanresults.ScanResultWrapper;
import jenkins.model.RunAction2;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@ExportedBean
public class Report extends ManagementLink implements RunAction2 {

  private static final Logger LOG = LoggerFactory.getLogger(Report.class.getName());
  public transient Run<?, ?> run;

  public Report(Run<?, ?> run) {
    this.run = run;
  }

  @Override
  public void onAttached(Run<?, ?> run) {
    this.run = run;
  }

  @Override
  public void onLoad(Run<?, ?> run) {
    this.run = run;
  }

  @Override
  public String getIconFileName() {
    return "/plugin/zscaler-iac-scan/images/icon.png";
  }

  @Override
  public String getDisplayName() {
    return "Zscaler IaC scan results";
  }

  @Override
  public String getUrlName() {
    return "zscaler-iac-scan";
  }

  public Run getRun() {
    return run;
  }

  public String getBuildDone() {
    if (run.isBuilding()) {
      return "false";
    } else {
      return "true";
    }
  }

  public String getResults() {
    Path resultFilePath = null;
    try {
      File buildDir = run.getParent().getBuildDir();
      resultFilePath =
          Paths.get(
              buildDir.getAbsolutePath(),
              String.valueOf(run.getNumber()),
              "iac-scan-results",
              run.getNumber() + ".json");
      File resultFile = resultFilePath.toFile();

      ObjectMapper mapper = new ObjectMapper();
      ScanResultWrapper scanResult = mapper.readValue(resultFile, ScanResultWrapper.class);
      if (scanResult != null) {
        if (scanResult.getResult().getSkipped() == null) {
          scanResult.getResult().setSkipped(new ArrayList<>());
        }
        if (scanResult.getResult().getPassed() == null) {
          scanResult.getResult().setPassed(new ArrayList<>());
        }
        if (scanResult.getResult().getFailed() == null) {
          scanResult.getResult().setFailed(new ArrayList<>());
        }

        return mapper.writeValueAsString(scanResult);
      } else {
        LOG.error("Failed to read results from {}", resultFile.getAbsolutePath());
        return null;
      }

    } catch (Exception e) {
      LOG.error("Failed to read the file {} due to {}", resultFilePath, e.getMessage());
    }
    return null;
  }
}
