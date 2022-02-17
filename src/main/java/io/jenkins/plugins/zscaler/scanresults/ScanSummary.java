package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScanSummary {

  @JsonProperty("target_path")
  private String fileOrFolder;

  @JsonProperty("iac_type")
  private String iacType;

  @JsonProperty("scanned_at")
  private String scannedAt;

  @JsonProperty("validated_policies")
  private int policiesValidated;

  @JsonProperty("violated_policies")
  private int violatedPolicies;

  private int low;

  private int medium;

  private int high;

  public String getFileOrFolder() {
    return fileOrFolder;
  }

  public void setFileOrFolder(String fileOrFolder) {
    this.fileOrFolder = fileOrFolder;
  }

  public String getIacType() {
    return iacType;
  }

  public void setIacType(String iacType) {
    this.iacType = iacType;
  }

  public String getScannedAt() {
    return scannedAt;
  }

  public void setScannedAt(String scannedAt) {
    this.scannedAt = scannedAt;
  }

  public int getPoliciesValidated() {
    return policiesValidated;
  }

  public void setPoliciesValidated(int policiesValidated) {
    this.policiesValidated = policiesValidated;
  }

  public int getViolatedPolicies() {
    return violatedPolicies;
  }

  public void setViolatedPolicies(int violatedPolicies) {
    this.violatedPolicies = violatedPolicies;
  }

  public int getLow() {
    return low;
  }

  public void setLow(int low) {
    this.low = low;
  }

  public int getMedium() {
    return medium;
  }

  public void setMedium(int medium) {
    this.medium = medium;
  }

  public int getHigh() {
    return high;
  }

  public void setHigh(int high) {
    this.high = high;
  }
}
