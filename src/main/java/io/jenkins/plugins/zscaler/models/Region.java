package io.jenkins.plugins.zscaler.models;

public enum Region {
  US("US", "https://api.zpccloud.net", "https://auth.us.zpccloud.net");

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
