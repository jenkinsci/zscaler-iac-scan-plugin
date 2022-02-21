package io.jenkins.plugins.zscaler.scanresults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicyResult {

  @JsonProperty("name")
  private String policyName;

  private String description;

  @JsonProperty("policy_id")
  private String policyId;

  private String severity;

  private String category;

  @JsonProperty("resource_name")
  private String resourceName;

  @JsonProperty("resource_type")
  private String resourceType;

  @JsonProperty("file_path")
  private String file;

  @JsonProperty("start_line")
  private int startLine;

  @JsonProperty("end_line")
  private int endLine;

  @JsonProperty("skip_comment")
  @Nullable
  private String skipComment;

  public String getPolicyName() {
    return policyName;
  }

  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getPolicyId() {
    return policyId;
  }

  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getSkipComment() {
    return skipComment;
  }

  public void setSkipComment(String skipComment) {
    this.skipComment = skipComment;
  }

  public int getStartLine() {
    return startLine;
  }

  public void setStartLine(int startLine) {
    this.startLine = startLine;
  }

  public int getEndLine() {
    return endLine;
  }

  public void setEndLine(int endLine) {
    this.endLine = endLine;
  }

  
}
