package io.jenkins.plugins.zscaler;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.User;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import okhttp3.*;
import okio.Buffer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientUtils.class, Configuration.class, Secret.class, Jenkins.class})
@PowerMockIgnore("javax.net.ssl.*")
public class ConfigurationTest {

  static Configuration underTest;

  @BeforeClass
  public static void init() {
    underTest = Mockito.mock(Configuration.class);

    Mockito.when(underTest.doValidate(anyString(), anyString(), anyString(),anyString(), any()))
        .thenCallRealMethod();
  }

  @Test
  public void doValidate() throws Exception {

    Jenkins jenkinsMock = PowerMockito.mock(Jenkins.class);
    PowerMockito.when(jenkinsMock.getRootUrl()).thenReturn("http://localhost:8080");

    User usermock = PowerMockito.mock(User.class);
    PowerMockito.when(usermock.getFullName()).thenReturn("user1");
    PowerMockito.when(jenkinsMock.getMe()).thenReturn(usermock);
    Whitebox.setInternalState(Jenkins.class, "theInstance", jenkinsMock);

    PowerMockito.mockStatic(ClientUtils.class);
    PowerMockito.when(ClientUtils.createRetrofit(anyString(), any(), any()))
        .thenReturn(mockClient());

    PowerMockito.mockStatic(Configuration.class);

    StandardUsernamePasswordCredentials mockCred =
        Mockito.mock(StandardUsernamePasswordCredentials.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(mockCred.getUsername()).thenReturn("clientId");
    Secret mockSecret = PowerMockito.mock(Secret.class);
    PowerMockito.when(mockSecret.getPlainText()).thenReturn("ClientSecret");
    Mockito.when(mockCred.getPassword()).thenReturn(mockSecret);
    PowerMockito.when(Configuration.resolveUserNamePassword(anyString()))
        .thenReturn(Optional.of(mockCred));

    ClientCredAuthentication authMock = PowerMockito.mock(ClientCredAuthentication.class);
    PowerMockito.when(authMock.populateAccessToken()).thenReturn("sampletoken");
    PowerMockito.whenNew(ClientCredAuthentication.class).withAnyArguments().thenReturn(authMock);

    FormValidation formValidation = underTest.doValidate("US", "1234", "", "",null);
    Assert.assertEquals(FormValidation.Kind.OK, formValidation.kind);

    Mockito.when(mockCred.getUsername()).thenReturn("errorId");
    formValidation = underTest.doValidate("US", "1234", "","", null);
    Assert.assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
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
                        .body(ResponseBody.create("{\"integration_id\":\"1234\"}", null))
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
