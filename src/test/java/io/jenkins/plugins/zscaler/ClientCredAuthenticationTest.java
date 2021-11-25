package io.jenkins.plugins.zscaler;

import hudson.AbortException;
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
@PrepareForTest({ClientUtils.class})
@PowerMockIgnore("javax.net.ssl.*")
public class ClientCredAuthenticationTest {

  @Test
  public void populateAccessToken() throws AbortException {

    PowerMockito.mockStatic(ClientUtils.class);
    PowerMockito.when(ClientUtils.createRetrofit(anyString(), any(), any()))
        .thenReturn(mockClient());

    ClientCredAuthentication underTest =
        new ClientCredAuthentication("https://example.com", "1234", "secret", null);
    String integrationId = underTest.populateAccessToken();
    Assert.assertNotNull(integrationId);
  }

  @Test(expected = AbortException.class)
  public void populateAccessToken_failure() throws AbortException {

    PowerMockito.mockStatic(ClientUtils.class);
    PowerMockito.when(ClientUtils.createRetrofit(anyString(), any(), any()))
        .thenReturn(mockClient());

    ClientCredAuthentication underTest =
        new ClientCredAuthentication("https://example.com", "5678", "secret", null);
    underTest.populateAccessToken();
  }

  @Test
  public void getAccessToken() throws AbortException {

    ClientCredAuthentication underTest = PowerMockito.mock(ClientCredAuthentication.class);
    PowerMockito.when(underTest.populateAccessToken()).thenReturn("");
    PowerMockito.when(underTest.getAccessToken()).thenCallRealMethod();
    underTest.getAccessToken();
    Mockito.verify(underTest, Mockito.atLeastOnce()).populateAccessToken();
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
                  if (bodyString.contains("1234")) {
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
