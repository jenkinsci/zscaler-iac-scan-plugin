package io.jenkins.plugins.zscaler;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import com.google.common.collect.Maps;
import hudson.AbortException;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.zscaler.models.CreateIntegrationResponse;
import io.jenkins.plugins.zscaler.models.Region;
import io.jenkins.plugins.zscaler.models.ValidateIntegrationRequest;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.verb.POST;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;
import static org.apache.commons.lang.StringUtils.trimToEmpty;

@Extension
public class Configuration extends GlobalConfiguration implements Serializable, ExtensionPoint {

  private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

  public static Configuration get() {
    return ExtensionList.lookupSingleton(Configuration.class);
  }

  private static final String CUSTOM_REGION = "custom";

  private String region;
  private String credentialsId;
  private String IntegrationId;
  private String apiUrl;
  private String authUrl;
  private String reportUrl;
  private static final Map<String, Region> REGIONTOURLMAP = Maps.newHashMap();

  static {
    REGIONTOURLMAP.put("US", Region.US);
    REGIONTOURLMAP.put("EU", Region.EU);
  }

  public Configuration() {
    load();
  }

  public String getRegion() {
    return region;
  }

  @DataBoundSetter
  public void setRegion(String region) {
    this.region = region;
    save();
  }

  public String getCredentialsId() {
    return credentialsId;
  }

  @DataBoundSetter
  public void setCredentialsId(String credentialsId) {
    this.credentialsId = credentialsId;
    save();
  }

  @DataBoundSetter
  public void setApiUrl(String apiUrl) {
    this.apiUrl = apiUrl;
    save();
  }

  public String getApiUrl() {
    if (region == null) {
      return null;
    }
    if (CUSTOM_REGION.equals(region)) {
      return apiUrl;
    }
    return REGIONTOURLMAP.get(region).getApiUrl();
  }

  public String getAuthUrl() {
    if (region == null) {
      return null;
    }
    if (CUSTOM_REGION.equals(region)) {
      return authUrl;
    }
    return REGIONTOURLMAP.get(region).getAuthUrl();
  }

  @DataBoundSetter
  public void setAuthUrl(String authUrl) {
    this.authUrl = authUrl;
  }

  public String getIntegrationId() {
    return IntegrationId;
  }

  public void setIntegrationId(String integrationId) {
    IntegrationId = integrationId;
    save();
  }

  public String getReportUrl() {
    if (region == null) {
      return null;
    }
    if (CUSTOM_REGION.equals(region)) {
      return reportUrl != null ? reportUrl : Region.US.getReportUrl();
    }
    return REGIONTOURLMAP.get(region).getReportUrl();
  }

  public void setReportUrl(String reportUrl) {
    this.reportUrl = reportUrl;
  }

  @SuppressWarnings("unused")
  public ListBoxModel doFillCredentialsIdItems(
      @QueryParameter String apiUrl, @QueryParameter String credentialsId) {
    if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
      return new StandardListBoxModel().includeCurrentValue(credentialsId);
    }
    return new StandardListBoxModel()
        .includeEmptyValue()
        .includeMatchingAs(
            ACL.SYSTEM,
            Jenkins.get(),
            StandardUsernamePasswordCredentials.class,
            URIRequirementBuilder.create().build(),
            CredentialsMatchers.always());
  }

  @POST
  public FormValidation doValidate(
      @QueryParameter("region") String region,
      @QueryParameter("credentialsId") String credentialsId,
      @QueryParameter("apiUrl") String customApiUrl,
      @QueryParameter("authUrl") String customAuthUrl,
      @AncestorInPath Job job) {

    Jenkins.get().checkPermission(Jenkins.ADMINISTER);

    String apiUrl;
    String authUrl;
    if (CUSTOM_REGION.equals(region)) {
      apiUrl = customApiUrl;
      authUrl = customAuthUrl;
    } else {
      apiUrl = REGIONTOURLMAP.get(region).getApiUrl();
      authUrl = REGIONTOURLMAP.get(region).getAuthUrl();
    }
    if (apiUrl == null || apiUrl.isEmpty()) {
      return FormValidation.error("Invalid region");
    }

    String clientId;
    String clientSecret;
    Optional<StandardUsernamePasswordCredentials> credentials =
        resolveUserNamePassword(credentialsId);
    if (credentials.isPresent()) {
      clientId = credentials.get().getUsername();
      clientSecret = credentials.get().getPassword().getPlainText();
    } else {
      return FormValidation.error("Credentials not found");
    }

    ClientCredAuthentication auth =
        new ClientCredAuthentication(authUrl, clientId, clientSecret, Jenkins.get().proxy);

    try {
      String accessToken = auth.populateAccessToken();
      if (StringUtils.isNotBlank(accessToken)) {
        String rootUrl = Jenkins.get().getRootUrl();
        try {
          validateInstallation(apiUrl, clientId, rootUrl, auth);
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Validation failed due to - " + e.getMessage());
          return FormValidation.error("Validation Failed due to - " + e.getMessage());
        }
        return FormValidation.ok("Validation is successful");
      } else {
        return FormValidation.error("Validation failed");
      }
    } catch (AbortException e) {
      LOGGER.log(Level.SEVERE, "Validation failed due to - " + e.getMessage());
      return FormValidation.error("Validation failed");
    }
  }

  public static Optional<StandardUsernamePasswordCredentials> resolveUserNamePassword(
      String credentialsId) {
    List<StandardUsernamePasswordCredentials> creds =
        filter(
            lookupCredentials(
                StandardUsernamePasswordCredentials.class,
                Jenkins.get(),
                ACL.SYSTEM,
                Collections.emptyList()),
            withId(trimToEmpty(credentialsId)));

    if (creds.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(creds.get(0));
    }
  }

  private void validateInstallation(
      String apiUrl, String clientId, String rootUrl, ClientCredAuthentication auth)
      throws Exception {
    Retrofit retrofit =
        ClientUtils.createRetrofit(
            apiUrl, ClientUtils.getAuthInterceptor(auth), Jenkins.get().proxy);
    CWPService cwpService = retrofit.create(CWPService.class);
    ValidateIntegrationRequest validateIntegrationRequest =
        new ValidateIntegrationRequest(clientId, rootUrl);
    Response<CreateIntegrationResponse> response =
        cwpService.validateIntegration(validateIntegrationRequest).execute();

    if (response.code() != 200) {
      String errMsg = IOUtils.toString(response.errorBody().byteStream(), Charset.defaultCharset());
      throw new Exception(errMsg);
    } else {
      CreateIntegrationResponse integrationResponse = response.body();
      if (integrationResponse != null) {
        setIntegrationId(integrationResponse.getIntegrationId());
      }
    }
  }
}
