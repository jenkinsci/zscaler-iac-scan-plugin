package io.jenkins.plugins.zscaler.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateIntegrationResponse {

  @JsonProperty("integration_id")
  private String integrationId;

  public String getIntegrationId() {
    return integrationId;
  }

  public void setIntegrationId(String integrationId) {
    this.integrationId = integrationId;
  }
}
