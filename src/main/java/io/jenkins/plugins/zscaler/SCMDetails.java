package io.jenkins.plugins.zscaler;

import hudson.EnvVars;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.SCMConstants;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCMDetails {

  private static final Logger LOGGER = Logger.getLogger(SCMDetails.class.getName());
  public static void populateSCMDetails(EnvVars env, BuildDetails buildDetails){
    String gitUrl = env.get(SCMConstants.GitUrl);
    if (gitUrl != null) {
      String repoUrl = gitUrl.substring(0,gitUrl.length()-4);
      repoUrl = maskAccessToken(repoUrl);
      buildDetails.setRepoLoc(repoUrl);
      buildDetails.addAdditionalDetails(BuildDetails.scmType, gitUrl.contains("gitlab.com") ? SCMConstants.GITLAB : (gitUrl.contains("github.com") ? SCMConstants.GITHUB : "GIT"));
      buildDetails.setBranchName(env.get(SCMConstants.GitBranch));
      buildDetails.setCommitSha(env.get(SCMConstants.GitCommit));
      if (gitUrl.contains("gitlab.com") || gitUrl.contains("github.com")) {
        try {
          String repoFullName = new URL(repoUrl).getPath().replaceFirst("/","");
          buildDetails.addRepoDetails(SCMConstants.RepoFullName, repoFullName);
          buildDetails.addRepoDetails(SCMConstants.RepoName, repoFullName.split("/")[1]);
        } catch (Exception e){
          LOGGER.log(Level.SEVERE, "Unable to fetch the path for repo url ::" + repoUrl);
        }
        buildDetails.addRepoDetails(SCMConstants.RepoUrl, repoUrl);
      }
    }
    String svnUrl = env.get(SCMConstants.SvnUrl);
    if(svnUrl!=null){
      buildDetails.setRepoLoc(svnUrl);
      buildDetails.addAdditionalDetails(BuildDetails.scmType, SCMConstants.SVN);
      buildDetails.setCommitSha(env.get(SCMConstants.SvnRevision));
    }
  }

  /**
   * Masks access token in git URL if present
   *
   * @param repoUrl
   * @return
   */
  private static String maskAccessToken(String repoUrl) {
    /*
    If the repo URl is either of the following formats:
    https://<token>@github.com/<username>/<repo>
    or
    https://oauth2:<token>@gitlab/<groupname>/<repo>
    then, convert it to the form:
    https://github.com/<username>/<repo>
    or
    https://gitlab.com/<groupname>/<repo>
     */
    try {
      URL url = new URL(repoUrl);
      String path = url.getPath();
      String host = url.getHost();
      String protocol = url.getProtocol();
      StringBuilder result = new StringBuilder();
      result.append(protocol).append("://").append(host).append(path);
      return result.toString();
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, new StringBuilder("Unable to remove access token for the repo URL: ").append(repoUrl).toString());
      e.printStackTrace();
    }
    return repoUrl;
  }
}
