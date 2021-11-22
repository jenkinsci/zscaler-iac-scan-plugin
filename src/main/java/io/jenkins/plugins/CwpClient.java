package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.AbortException;
import hudson.ProxyConfiguration;
import org.apache.commons.lang.StringUtils;
import retrofit2.Retrofit;

public class CwpClient {

  private static volatile Retrofit client;

  private static void initRetrofit(
      Configuration configuration,
      ProxyConfiguration proxy,
      StandardUsernamePasswordCredentials credentials)
      throws AbortException {
    String apiUrl = configuration.getApiUrl();
    String clientId = credentials.getUsername();
    String clientSecret = credentials.getPassword().getPlainText();
    String authUrl = configuration.getAuthUrl();

    if (StringUtils.isBlank(apiUrl)
        || StringUtils.isBlank(clientId)
        || StringUtils.isBlank(clientSecret)
        || StringUtils.isBlank(authUrl)) {
      throw new AbortException("Invalid Zscaler IaC scanner plugin configuration");
    }

    ClientCredAuthentication auth =
        new ClientCredAuthentication(authUrl, clientId, clientSecret, proxy);
    auth.populateAccessToken();
    client = ClientUtils.createRetrofit(apiUrl, ClientUtils.getAuthInterceptor(auth), proxy);
  }

  public static Retrofit getClient(
      Configuration configuration,
      ProxyConfiguration proxy,
      StandardUsernamePasswordCredentials credentials)
      throws AbortException {
    if (client == null) {
      synchronized (CwpClient.class) {
        if (client == null) {
          initRetrofit(configuration, proxy, credentials);
        }
      }
    }
    return client;
  }
}
