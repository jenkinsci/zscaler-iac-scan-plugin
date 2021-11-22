package io.jenkins.plugins.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationsConfig {

  private Map<String, WebHook> notifications;

  public Map<String, WebHook> getNotifications() {
    return notifications;
  }

  public void setNotifications(Map<String, WebHook> notifications) {
    this.notifications = notifications;
  }

  public static class WebHook {
    private String type;
    private Config config;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public Config getConfig() {
      return config;
    }

    public void setConfig(Config config) {
      this.config = config;
    }
  }

  public static class Config {
    private String url;

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
  }
}
