package io.jenkins.plugins;

import io.jenkins.plugins.models.BuildDetails;
import org.json.JSONObject;
import org.json.XML;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCMDetails {

  private static final Logger LOGGER = Logger.getLogger(SCMDetails.class.getName());

  public static void populateSCMDetails(BuildDetails buildDetails, String configXml) {
    JSONObject config = XML.toJSONObject(configXml);
    JSONObject project = config.optJSONObject("project");
    if (project != null) {
      JSONObject scm = project.optJSONObject("scm");
      if (scm != null) {
        JSONObject userRemoteConfigs = scm.optJSONObject("userRemoteConfigs");
        if (userRemoteConfigs != null) {
          JSONObject gitConfig =
              userRemoteConfigs.optJSONObject("hudson.plugins.git.UserRemoteConfig");
          if (gitConfig != null) {
            String url = gitConfig.optString("url");
            buildDetails.setRepoLoc(url);
          }
        }
      }
    }

    JSONObject flowDefinition = config.optJSONObject("flow-definition");
    if (flowDefinition != null) {
      JSONObject definition = flowDefinition.optJSONObject("definition");
      if (definition != null) {
        JSONObject script = definition.optJSONObject("script");
        if (script != null) {
          buildDetails.addAdditionalDetails(BuildDetails.jobType, "pipeline");

          // TODO need to handle for different version control systems like svn and etc.,
          Pattern pattern = Pattern.compile(".*git branch.*url.*&apos;(.*)&apos;");
          Matcher matcher = pattern.matcher(configXml);
          if (matcher.find()) {
            try {
              String repo = matcher.group(1);
              buildDetails.setRepoLoc(repo);
            } catch (Exception e) {
              LOGGER.log(
                  Level.INFO, "No Repo configured for the Job - " + buildDetails.getJobName());
            }
          }
        } else {
          buildDetails.addAdditionalDetails(BuildDetails.jobType, "free_style");
        }
      } else {
        buildDetails.addAdditionalDetails(BuildDetails.jobType, "free_style");
      }
    } else {
      buildDetails.addAdditionalDetails(BuildDetails.jobType, "free_style");
    }
  }
}
