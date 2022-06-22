package io.jenkins.plugins.zscaler.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class BuildDetails implements Serializable {

  public static final String scmType = "scm_type";

  @JsonProperty("build_number")
  private String buildNumber;

  @JsonProperty("repo_loc")
  private String repoLoc;

  @JsonProperty("build_triggered_by")
  private String buildTriggeredBy;

  @JsonProperty("sub_type")
  private int subType;

  private int status;

  @JsonProperty("additional_details")
  private Map<String, String> additionalDetails;
  
  @JsonProperty("branch")
  private String branchName;

  @JsonProperty("ref")
  private String commitSha;

  @JsonProperty("event_details")
  private Map<String, String> eventDetails;

  @JsonProperty("repo-details")
  private Map<String, String> repoDetails;

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

  public void addAdditionalDetails(String key, String value) {
    if (additionalDetails == null) {
      additionalDetails = new HashMap<>();
    }
    additionalDetails.put(key, value);
  }

  public Map<String, String> getAdditionalDetails() {
    return additionalDetails;
  }

  public String getBranchName() {
    return branchName;
  }

  public void addEventDetails(String key, String value){
    if(eventDetails==null){
      eventDetails = new HashMap<>();
    }
    eventDetails.put(key, value);
  }

  public Map<String, String> getEventDetails(){ return eventDetails; }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  public String getCommitSha() {
    return commitSha;
  }

  public void setCommitSha(String commitSha) {
    this.commitSha = commitSha;
  }

  public void addRepoDetails(String key, String value){
    if(repoDetails==null){
      repoDetails = new HashMap<>();
    }
    repoDetails.put(key, value);
  }

  public Map<String, String> getRepoDetails(){ return repoDetails; }
}
