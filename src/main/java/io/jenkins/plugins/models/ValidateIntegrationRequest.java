package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidateIntegrationRequest {
  @JsonProperty("client_id")
  String clientId;

  @JsonProperty("unique_identifier")
  String uniqueIdentifier;

  public ValidateIntegrationRequest(String clientId, String uniqueIdentifier) {
    this.clientId = clientId;
    this.uniqueIdentifier = uniqueIdentifier;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  public void setUniqueIdentifier(String uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }
}
