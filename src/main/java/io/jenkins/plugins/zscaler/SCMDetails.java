package io.jenkins.plugins.zscaler;

import hudson.EnvVars;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.SCMConstants;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCMDetails {

  private static final Logger LOGGER = Logger.getLogger(SCMDetails.class.getName());
  public static void populateSCMDetails(EnvVars env, BuildDetails buildDetails){
    String gitUrl = env.get(SCMConstants.GitUrl);
    if (gitUrl != null) {
      String repoUrl = gitUrl.substring(0,gitUrl.length()-4);
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
}
