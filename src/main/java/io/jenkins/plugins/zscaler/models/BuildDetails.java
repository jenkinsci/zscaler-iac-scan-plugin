package io.jenkins.plugins.zscaler.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class BuildDetails {

  public static final String jobType = "job_type";

  @JsonProperty("job_name")
  private String jobName;

  @JsonProperty("build_number")
  private String buildNumber;

  @JsonProperty("repo_loc")
  private String repoLoc;

  @JsonProperty("build_run_timestamp")
  private String buildRunTimestamp;

  @JsonProperty("build_triggered_by")
  private String buildTriggeredBy;

  @JsonProperty("sub_type")
  private int subType;

  @JsonProperty("integration_id")
  private String integrationId;

  private int status;

  @JsonProperty("additional_details")
  private Map<String, String> additionalDetails;

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getBuildNumber() {
    return buildNumber;
  }

  public void setBuildNumber(String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getRepoLoc() {
    return repoLoc;
  }

  public void setRepoLoc(String repoLoc) {
    this.repoLoc = repoLoc;
  }

  public String getBuildRunTimestamp() {
    return buildRunTimestamp;
  }

  public void setBuildRunTimestamp(String buildRunTimestamp) {
    this.buildRunTimestamp = buildRunTimestamp;
  }

  public String getBuildTriggeredBy() {
    return buildTriggeredBy;
  }

  public void setBuildTriggeredBy(String buildTriggeredBy) {
    this.buildTriggeredBy = buildTriggeredBy;
  }

  public int getSubType() {
    return subType;
  }

  public void setSubType(int subType) {
    this.subType = subType;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getIntegrationId() {
    return integrationId;
  }

  public void setIntegrationId(String integrationId) {
    this.integrationId = integrationId;
  }

  public void addAdditionalDetails(String key, String value) {
    if (additionalDetails == null) {
      additionalDetails = new HashMap<>();
    }
    additionalDetails.put(key, value);
  }
}
