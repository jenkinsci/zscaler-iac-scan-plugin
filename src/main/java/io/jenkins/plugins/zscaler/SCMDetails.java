package io.jenkins.plugins.zscaler;

import hudson.EnvVars;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import io.jenkins.plugins.zscaler.models.SCMConstants;

public class SCMDetails {
  public static void populateSCMDetails(EnvVars env, BuildDetails buildDetails){
    String gitUrl = env.get(SCMConstants.GitUrl);
    if (gitUrl != null) {
      String repoUrl = gitUrl.substring(0,gitUrl.length()-4);
      buildDetails.setRepoLoc(repoUrl);
      buildDetails.addAdditionalDetails(BuildDetails.scmType, gitUrl.contains("gitlab.com") ? SCMConstants.GITLAB : (gitUrl.contains("github.com") ? SCMConstants.GITHUB : "GIT"));
      buildDetails.setBranchName(env.get(SCMConstants.GitBranch));
      buildDetails.setCommitSha(env.get(SCMConstants.GitCommit));
      if (gitUrl.contains("gitlab.com") || gitUrl.contains("github.com")) {
        String repoFullName = gitUrl.substring(19,gitUrl.length()-4);
        buildDetails.addRepoDetails(SCMConstants.RepoFullName, repoFullName);
        buildDetails.addRepoDetails(SCMConstants.RepoName, repoFullName.split("/")[1]);
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
