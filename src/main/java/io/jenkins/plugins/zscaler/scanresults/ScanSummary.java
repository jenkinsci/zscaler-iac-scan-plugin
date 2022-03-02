package io.jenkins.plugins.zscaler.scanresults;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanSummary {

  @JsonProperty("scan_path")
  private String fileOrFolder;

  private String status;

  @JsonProperty("scanned_at")
  private String scannedAt;

  @JsonProperty("template_type")
  private List<String> iacType;

  @JsonProperty("total_policies")
  private int totalPolicies;

  @JsonProperty("passed_policies")
  private int passedPolicies;

  @JsonProperty("failed_policies")
  private ScanResultStats failedPolicies;

  @JsonProperty("skipped_policies")
  private ScanResultStats skippedPolicies;

  public String getFileOrFolder() {
    return fileOrFolder;
  }

  public void setFileOrFolder(String fileOrFolder) {
    this.fileOrFolder = fileOrFolder;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getScannedAt() {
    return scannedAt;
  }

  public void setScannedAt(String scannedAt) {
    this.scannedAt = scannedAt;
  }

  public List<String> getIacType() {
    return iacType;
  }

  public void setIacType(List<String> iacType) {
    this.iacType = iacType;
  }

  public int getTotalPolicies() {
    return totalPolicies;
  }

  public void setTotalPolicies(int totalPolicies) {
    this.totalPolicies = totalPolicies;
  }

  public int getPassedPolicies() {
    return passedPolicies;
  }

  public void setPassedPolicies(int passedPolicies) {
    this.passedPolicies = passedPolicies;
  }

  @Override
  public String toString() {
    return "ScanSummary [failedPolicies=" + failedPolicies + ", fileOrFolder=" + fileOrFolder + ", iacType=" + iacType
        + ", passedPolicies=" + passedPolicies + ", scannedAt=" + scannedAt + ", skippedPolicies=" + skippedPolicies
        + ", status=" + status + ", totalPolicies=" + totalPolicies + "]";
  }
}
