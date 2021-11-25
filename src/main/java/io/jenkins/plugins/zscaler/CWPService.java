package io.jenkins.plugins.zscaler;

import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.CreateIntegrationResponse;
import io.jenkins.plugins.zscaler.models.ScanResponse;
import io.jenkins.plugins.zscaler.models.ValidateIntegrationRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CWPService {

  @POST("/iac/onboarding/v1/cicd/validate")
  Call<CreateIntegrationResponse> validateIntegration(@Body ValidateIntegrationRequest body);

  @POST("/iac/onboarding/v1/cli/download")
  Call<ResponseBody> downloadScanner(@Body String body);

  @POST("/iac/scans/v1/scan/create")
  Call<ScanResponse> createScan(@Body BuildDetails buildDetails);
}
