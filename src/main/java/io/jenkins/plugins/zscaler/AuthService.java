package io.jenkins.plugins.zscaler;

import io.jenkins.plugins.zscaler.models.AuthenticationResponse;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthService {

  @POST("oauth/token")
  @Headers({"Content-Type: application/x-www-form-urlencoded"})
  Call<AuthenticationResponse> login(@Body RequestBody body);
}
