package io.jenkins.plugins.zscaler;

import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.SCMConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SCMDetails {

  private static final Logger LOGGER = Logger.getLogger(SCMDetails.class.getName());

  public static void populateSCMDetails(BuildDetails buildDetails, String configXml) {
    JSONObject config = XML.toJSONObject(configXml);
    JSONObject project = config.optJSONObject("project");
    String scmType = "GIT";
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
            scmType = url.contains("gitlab.com") ? SCMConstants.GITLAB : SCMConstants.GITHUB;
          }
        }
        JSONObject svnLocations = scm.optJSONObject("locations");
        if (svnLocations != null) {
          JSONArray svnModules = svnLocations.optJSONArray("hudson.scm.SubversionSCM_-ModuleLocation");
          if (svnModules != null && !svnModules.isEmpty()) {
            scmType = SCMConstants.SVN;
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < svnModules.length(); i++) {
              String remoteUrl = svnModules.getJSONObject(i).optString("remote");
              list.add(remoteUrl);
            }
            buildDetails.setRepoLoc(list.toString());
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
              scmType = repo.contains("gitlab.com") ? SCMConstants.GITLAB : SCMConstants.GITHUB;
              buildDetails.setRepoLoc(repo);
            } catch (Exception e) {
              LOGGER.log(
                      Level.INFO, "No Repo configured for the Job - " + buildDetails.getJobName());
            }
          }
          Pattern svnPattern = Pattern.compile(".*remote: &apos;(.*)&apos;]],");
          Matcher svnMatcher = svnPattern.matcher(configXml);
          if (svnMatcher.find()) {
            try {
              String repo = svnMatcher.group(1);
              scmType = SCMConstants.SVN;
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
    if (scmType != null) {
      buildDetails.addAdditionalDetails(BuildDetails.scmType, scmType);
    }
  }
}
