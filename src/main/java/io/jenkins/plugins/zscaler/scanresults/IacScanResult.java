package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IacScanResult {

  @JsonProperty("passed_policies")
  @Nullable
  private List<PolicyResult> passed;

  @JsonProperty("failed_policies")
  @Nullable
  private List<PolicyResult> failed;

  @JsonProperty("skipped_policies")
  @Nullable
  private List<PolicyResult> skipped;

  @JsonProperty("summary")
  private ScanSummary scanSummary;

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
}
