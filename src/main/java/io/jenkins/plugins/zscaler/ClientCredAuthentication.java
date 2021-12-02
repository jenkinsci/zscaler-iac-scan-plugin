package io.jenkins.plugins.zscaler;

import hudson.AbortException;
import hudson.ProxyConfiguration;
import io.jenkins.plugins.zscaler.models.AuthenticationResponse;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientCredAuthentication {

  private static final Logger LOG = Logger.getLogger(ClientCredAuthentication.class.getName());

  private final String baseUrl;
  private final String clientId;
  private final String clientSecret;
  private String accessToken;
  private final ProxyConfiguration proxy;

  public ClientCredAuthentication(
      String baseUrl, String clientId, String clientSecret, ProxyConfiguration proxy) {
    this.baseUrl = baseUrl;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.proxy = proxy;
  }

  public String populateAccessToken() throws AbortException {
    Retrofit client = ClientUtils.createRetrofit(baseUrl, Optional.empty(), proxy);
    AuthService authClient = client.create(AuthService.class);
    Call<AuthenticationResponse> loginReq = authClient.login(getAuthBody());
    Response<AuthenticationResponse> loginResp;
    try {
      loginResp = loginReq.execute();
      if (loginResp.code() == 200) {
        AuthenticationResponse body = loginResp.body();
        if (body != null) {
          accessToken = body.getAccess_token();
        }
        return accessToken;
      } else {
        LOG.log(
            Level.SEVERE,
            "Login failed due to - "
                + IOUtils.toString(loginResp.errorBody().byteStream(), Charset.defaultCharset()));
        throw new AbortException("Failed to Authenticate with zscaler platform");
      }
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Failed to populate token due to - " + e.getMessage());
      throw new AbortException("Login to zscaler platform Failed");
    }
  }

  public String getAccessToken() throws AbortException {
    if (StringUtils.isBlank(accessToken)) {
      populateAccessToken();
    }
    return accessToken;
  }

  private RequestBody getAuthBody() {
    return new FormBody.Builder()
        .add("audience", "https://api.zscwp.io/iac")
        .add("grant_type", "client_credentials")
        .add("client_id", clientId)
        .add("client_secret", clientSecret)
        .build();
  }
}
