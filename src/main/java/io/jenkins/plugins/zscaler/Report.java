package io.jenkins.plugins.zscaler;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.Job;
import hudson.model.ManagementLink;
import hudson.model.Run;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.ScanMetadata;
import io.jenkins.plugins.zscaler.scanresults.IacScanResult;
import jenkins.model.RunAction2;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

  public ScanMetadata getMetaData() {
    ScanMetadata metadata = new ScanMetadata();
    IacScanResult result = getBuildResults();

    try {
      if (result.getScanSummary() != null && result.getScanSummary().getScannedAt() != null) {
        String scannedAt = result.getScanSummary().getScannedAt();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSSSSXXX");
        Date scannedDate = sdf.parse(scannedAt);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        metadata.setDate(dateFormatter.format(scannedDate));
        SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
        metadata.setTime(timeFormatter.format(scannedDate));
      }
      metadata.setBuildNumber(String.valueOf(run.getNumber()));
      metadata.setBuildStatus(String.valueOf(run.getResult()));
      metadata.setProject(run.getParent().getName());
      BuildDetails details = new BuildDetails();
      SCMDetails.populateSCMDetails(details, getConfigXml(run));
      if (details.getRepoLoc() != null) {
        metadata.setRepo(details.getRepoLoc());
      }
      return metadata;
    } catch (Exception e) {
      LOG.error("Failed to build scan metadata" + e.getMessage(), e);
    }
    return null;
  }

  public String getResults() {
    try {
      IacScanResult scanResult = getBuildResults();
      ObjectMapper mapper = new ObjectMapper();
      if (scanResult.getSkipped() == null) {
        scanResult.setSkipped(new ArrayList<>());
      }
      if (scanResult.getPassed() == null) {
        scanResult.setPassed(new ArrayList<>());
      }
      if (scanResult.getFailed() == null) {
        scanResult.setFailed(new ArrayList<>());
      }
      ScanMetadata metadata = getMetaData();
      if (metadata != null) {
        scanResult.setMetadata(metadata);
      }
      LOG.info("Scan Results ::" + mapper.writeValueAsString(scanResult));
      return mapper.writeValueAsString(scanResult);
    } catch (Exception e) {
      LOG.error("Failed to map the scan results as string");
    }
    return null;
  }

  private IacScanResult getBuildResults() {
    Path resultFilePath = null;
    try {
      File buildDir = run.getParent().getBuildDir();
      resultFilePath = Paths.get(
              buildDir.getAbsolutePath().replaceFirst("^/(.:/)", "$1"),
          String.valueOf(run.getNumber()),
          "iac-scan-results",
          run.getNumber() + ".json");
      File resultFile = resultFilePath.toFile();
      ObjectMapper mapper = new ObjectMapper();
      IacScanResult scanResult = mapper.readValue(resultFile, IacScanResult.class);
      if (scanResult != null) {
        return scanResult;
      } else {
        LOG.error("Failed to read results from {}", resultFile.getAbsolutePath());
        return null;
      }

    } catch (Exception e) {
      LOG.error("Failed to read the file {} due to {}", resultFilePath, e.getMessage());
    }
    return null;
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
            LOG.info(
                String.format(
                    "Failed to read file - %s/%s ", file.getAbsolutePath(), file.getName()));
          }
        }
      }
    }
    return null;
  }
}
