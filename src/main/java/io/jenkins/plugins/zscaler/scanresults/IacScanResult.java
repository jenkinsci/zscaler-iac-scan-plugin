package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jenkins.plugins.zscaler.models.ScanMetadata;

import javax.annotation.Nullable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IacScanResult {

  @JsonProperty("passed_findings")
  @Nullable
  private List<PolicyResult> passed;

  @JsonProperty("failed_findings")
  @Nullable
  private List<PolicyResult> failed;

  @JsonProperty("skipped_findings")
  @Nullable
  private List<PolicyResult> skipped;

  @JsonProperty("passed_policies")
  @Nullable
  private List<PolicyResult> passedPolicies;

  @JsonProperty("failed_policies")
  @Nullable
  private List<PolicyResult> failedPolicies;

  @JsonProperty("skipped_policies")
  @Nullable
  private List<PolicyResult> skippedPolicies;

  @JsonProperty("summary")
  private ScanSummary scanSummary;

  @JsonProperty("metadata")
  private ScanMetadata metadata;

  @JsonProperty("errorMessage")
  @Nullable
  private String scanErrorMessage;

  public List<PolicyResult> getPassed() {
    return passed;
  }

  public void setPassed(List<PolicyResult> passed) {
    this.passed = passed;
  }

  public List<PolicyResult> getFailed() {
    return failed;
  }

  public void setFailed(List<PolicyResult> failed) {
    this.failed = failed;
  }

  public List<PolicyResult> getSkipped() {
    return skipped;
  }

  public void setSkipped(List<PolicyResult> skipped) {
    this.skipped = skipped;
  }

  public ScanSummary getScanSummary() {
    return scanSummary;
  }

  public void setScanSummary(ScanSummary scanSummary) {
    this.scanSummary = scanSummary;
  }

  public ScanMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(ScanMetadata metadata) {
    this.metadata = metadata;
  }

  @Nullable
  public List<PolicyResult> getFailedPolicies() {
    return failedPolicies;
  }

  public void setFailedPolicies(@Nullable List<PolicyResult> failedPolicies) {
    this.failedPolicies = failedPolicies;
  }

  @Nullable
  public List<PolicyResult> getSkippedPolicies() {
    return skippedPolicies;
  }

  public void setSkippedPolicies(@Nullable List<PolicyResult> skippedPolicies) {
    this.skippedPolicies = skippedPolicies;
  }

  @Nullable
  public List<PolicyResult> getPassedPolicies() {
    return passedPolicies;
  }

  public void setPassedPolicies(@Nullable List<PolicyResult> passedPolicies) {
    this.passedPolicies = passedPolicies;
  }

  @Nullable
  public String getScanErrorMessage() {
    return scanErrorMessage;
  }

  public void setScanErrorMessage(@Nullable String scanErrorMessage) {
    this.scanErrorMessage = scanErrorMessage;
  }
}
