package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScannerConfig {

  private String host;

  @JsonProperty("auth")
  private AuthConfig authConfig;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public AuthConfig getAuthConfig() {
    return authConfig;
  }

  public void setAuthConfig(AuthConfig authConfig) {
    this.authConfig = authConfig;
  }

  public static class AuthConfig {
    private String host;
    private String audience;
    private String scope;

    public String getHost() {
      return host;
    }

    public void setHost(String host) {
      this.host = host;
    }

    public String getAudience() {
      return audience;
    }

    public void setAudience(String audience) {
      this.audience = audience;
    }

    public String getScope() {
      return scope;
    }

    public void setScope(String scope) {
      this.scope = scope;
    }
  }
}
