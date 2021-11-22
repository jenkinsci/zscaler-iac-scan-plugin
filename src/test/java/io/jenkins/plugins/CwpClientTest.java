package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.AbortException;
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import okhttp3.*;
import okio.Buffer;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientUtils.class, Configuration.class, Secret.class, Jenkins.class})
@PowerMockIgnore("javax.net.ssl.*")
public class CwpClientTest {

  @Test
  public void getClient() throws AbortException {
    ProxyConfiguration proxy = new ProxyConfiguration("http://example.com", 1);
    Configuration mockConfig = PowerMockito.mock(Configuration.class);
    PowerMockito.when(mockConfig.getAuthUrl()).thenReturn("http://authurl.com");
    PowerMockito.when(mockConfig.getApiUrl()).thenReturn("http://apiurl.com");

    StandardUsernamePasswordCredentials mockCred =
        Mockito.mock(StandardUsernamePasswordCredentials.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockCred.getUsername()).thenReturn("clientId");
    Secret mockSecret = PowerMockito.mock(Secret.class);
    PowerMockito.when(mockSecret.getPlainText()).thenReturn("ClientSecret");
    Mockito.when(mockCred.getPassword()).thenReturn(mockSecret);

    PowerMockito.mockStatic(ClientUtils.class);
    PowerMockito.when(ClientUtils.createRetrofit(anyString(), any(), any()))
        .thenReturn(mockClient());

    Retrofit client = CwpClient.getClient(mockConfig, proxy, mockCred);
    Assert.assertNotNull(client);
  }

  private Retrofit mockClient() {

    OkHttpClient client = new OkHttpClient();
    client =
        client
            .newBuilder()
            .addInterceptor(
                chain -> {
                  RequestBody body = chain.request().body();

                  Buffer bfs = new Buffer();
                  body.writeTo(bfs);
                  String bodyString = bfs.readUtf8();
                  if (bodyString.contains("clientId")) {
                    return new Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .message("ok")
                        .body(
                            ResponseBody.create(
                                "{\"access_token\":\"token1234\",\"scope\":\"scope\",\"expires_in\":\"64573\",\"token_type\":\"bearer\"}",
                                null))
                        .code(200)
                        .build();
                  }
                  return new Response.Builder()
                      .protocol(Protocol.HTTP_1_1)
                      .message("unauthorized")
                      .body(ResponseBody.create("unauthorized", null))
                      .request(chain.request())
                      .code(401)
                      .build();
                })
            .build();

    return new Retrofit.Builder()
        .baseUrl("http://localhost:3030")
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create())
        .client(client)
        .build();
  }
}
