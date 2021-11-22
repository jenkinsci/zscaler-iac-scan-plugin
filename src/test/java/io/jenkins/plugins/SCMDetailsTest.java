package io.jenkins.plugins;

import com.google.common.io.Resources;
import io.jenkins.plugins.models.BuildDetails;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SCMDetailsTest {

  @Test
  public void populateSCMDetails() throws IOException {
    BuildDetails buildDetails = new BuildDetails();
    buildDetails.setJobName("sample");
    URL builds = Resources.getResource("builds");

    Path configXmlFile = Paths.get(builds.getPath(), "freestyle.xml");
    String configxml = IOUtils.toString(configXmlFile.toUri(), Charset.defaultCharset());

    SCMDetails.populateSCMDetails(buildDetails, configxml);

    Assert.assertNotNull(buildDetails.getRepoLoc());

    configXmlFile = Paths.get(builds.getPath(), "pipeline.xml");
    configxml = IOUtils.toString(configXmlFile.toUri(), Charset.defaultCharset());

    SCMDetails.populateSCMDetails(buildDetails, configxml);

    Assert.assertNotNull(buildDetails.getRepoLoc());
  }
}
