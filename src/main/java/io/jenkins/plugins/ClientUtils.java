package io.jenkins.plugins;

import hudson.ProxyConfiguration;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ClientUtils {

  public static Retrofit createRetrofit(
      String url, Optional<Interceptor> interceptor, ProxyConfiguration proxy) {
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
    OkHttpClient.Builder httpClient =
        new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .callTimeout(10, TimeUnit.MINUTES)
            .followRedirects(true)
            .addInterceptor(loggingInterceptor)
            .proxy(getProxy(url, proxy));

    if (proxy != null && proxy.getUserName() != null) {
      String userName = proxy.getUserName();
      String password = proxy.getSecretPassword().getPlainText();
      httpClient.proxyAuthenticator(getProxyAuthenticator(userName, password));
    }

    interceptor.ifPresent(httpClient::addInterceptor);
    return new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create())
        .client(httpClient.build())
        .build();
  }

  public static Authenticator getProxyAuthenticator(String username, String password) {
    return (route, response) -> {
      String credential = Credentials.basic(username, password);
      return response.request().newBuilder().header("Proxy-Authorization", credential).build();
    };
  }

  public static Proxy getProxy(String apiUrl, ProxyConfiguration proxy) {
    if (proxy == null) {
      return Proxy.NO_PROXY;
    } else {
      try {
        return proxy.createProxy(new URL(apiUrl).getHost());
      } catch (MalformedURLException e) {
        return proxy.createProxy(apiUrl);
      }
    }
  }

  public static Optional<Interceptor> getAuthInterceptor(ClientCredAuthentication auth) {
    return java.util.Optional.of(
        chain -> {
          Request request = chain.request();
          Request newRequest =
              request
                  .newBuilder()
                  .addHeader("Authorization", "Bearer " + auth.getAccessToken())
                  .build();
          okhttp3.Response response = chain.proceed(newRequest);
          if (response.code() == 401) {
            response.close();
            auth.populateAccessToken();
            newRequest =
                request
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + auth.getAccessToken())
                    .build();
            response = chain.proceed(newRequest);
          }
          return response;
        });
  }

  public static String getProxyConfigString(ProxyConfiguration proxy) {
    if (proxy != null && proxy.getName() != null) {
      String proxyString = "http://";
      if (proxy.getUserName() != null) {
        proxyString = proxy.getUserName() + ":" + proxy.getSecretPassword().getPlainText() + "@";
      }
      proxyString = proxyString + proxy.getName() + ":" + proxy.getPort();
      return proxyString;
    }
    return null;
  }
}
