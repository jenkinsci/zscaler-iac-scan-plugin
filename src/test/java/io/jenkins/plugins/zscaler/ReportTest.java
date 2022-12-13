package io.jenkins.plugins.zscaler;

import com.google.common.io.Resources;
import hudson.model.Run;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class ReportTest {

  private static Report underTest;
  private static Run run;

  @BeforeClass
  public static void init() {
    run = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    underTest = new Report(run);
  }

  @Test
  public void getBuildDone() {
    URL buildNumberFolder = Resources.getResource("sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath().replaceFirst("^/(.:/)", "$1"));
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(1);
    Mockito.when(run.isBuilding()).thenReturn(false);
    Assert.assertEquals("true", underTest.getBuildDone());

    Mockito.when(run.isBuilding()).thenReturn(true);
    Assert.assertEquals("false", underTest.getBuildDone());
  }

  @Test
  public void getResult() {
    URL buildNumberFolder = Resources.getResource("sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath().replaceFirst("^/(.:/)", "$1"));
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(1);
    String results = underTest.getResults();
    Assert.assertNotNull(results);
  }

  @Test
  public void getResultNegative() {
    URL buildNumberFolder = Resources.getResource("negative_sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath().replaceFirst("^/(.:/)", "$1"));
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(1);
    String results = underTest.getResults();
    Assert.assertNotNull(results);
  }

  @Test
  public void getNoIaCResultNegative() {
    URL buildNumberFolder = Resources.getResource("negative_sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath().replaceFirst("^/(.:/)", "$1"));
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(1);
    String results = underTest.getResults();
    Assert.assertNotNull(results);
    Assert.assertTrue(StringUtils.contains(results, "\"noResourcesMessage\":\"No IaC resources were detected during the scan\""));
  }

  @Test
  public void testGetErrorResult() {
    URL buildNumberFolder = Resources.getResource("negative_sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath().replaceFirst("^/(.:/)", "$2"));
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(2);
    String results = underTest.getResults();
    Assert.assertNotNull(results);
    Assert.assertTrue(StringUtils.contains(results, "\"errorMessage\":\"Scan failed to complete\""));
  }
}
