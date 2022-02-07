package io.jenkins.plugins.zscaler.models;

public enum Region {
  US_PROD("us-prod", "https://api.zscwp.io", "https://authourl.com"),
  EU_PROD("eu-prod", "https://api.zscwp.io", "https://authourl.com"),
  PREVIEW("preview", "https://int.api.zscwp.io", "https://preview-auth.zscwp.io");

  private final String name;
  private final String apiUrl;
  private final String authUrl;

  Region(String name, String apiUrl, String authUrl) {
    this.name = name;
    this.apiUrl = apiUrl;
    this.authUrl = authUrl;
  }

  public String getName() {
    return name;
  }

  public String getApiUrl() {
    return apiUrl;
  }

  public String getAuthUrl() {
    return authUrl;
  }
}
