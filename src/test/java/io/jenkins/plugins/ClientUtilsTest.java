package io.jenkins.plugins;

import hudson.ProxyConfiguration;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import org.junit.Assert;
import org.junit.Test;
import retrofit2.Retrofit;

import java.util.Optional;

public class ClientUtilsTest {

  @Test
  public void createRetrofit() {
    Interceptor interceptor = chain -> null;
    ProxyConfiguration proxyConfiguration = new ProxyConfiguration("http://example.com", 1);
    Retrofit client =
        ClientUtils.createRetrofit(
            "http://example.com", Optional.of(interceptor), proxyConfiguration);
    Assert.assertNotNull(client);
  }

  @Test
  public void getProxyAuthenticator() {
    Authenticator authenticator = ClientUtils.getProxyAuthenticator("user1", "pass1");
    Assert.assertNotNull(authenticator);
  }

  @Test
  public void getProxy() {
    ProxyConfiguration proxy = new ProxyConfiguration("http://proxy.com", 3003);
    Assert.assertNotNull(ClientUtils.getProxy("http://example.com", proxy));
  }

  @Test
  public void getAuthInterceptor() {
    ProxyConfiguration proxy = new ProxyConfiguration("http://proxy.com", 3003);
    ClientCredAuthentication auth =
        new ClientCredAuthentication("http://example.com", "clientId", "clientsecret", proxy);
    Assert.assertNotNull(ClientUtils.getAuthInterceptor(auth));
  }

  @Test
  public void getProxyConfigString() {
    ProxyConfiguration proxy = new ProxyConfiguration("http://proxy.com", 3003);
    Assert.assertNotNull(ClientUtils.getProxyConfigString(proxy));
  }
}
