package io.jenkins.plugins;

import com.google.common.io.Resources;
import hudson.model.Run;
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
    URL buildNumberFolder = Resources.getResource("sample");
    Path resourceFolder = Paths.get(buildNumberFolder.getPath());
    Mockito.when(run.getParent().getBuildDir()).thenReturn(resourceFolder.toFile());
    Mockito.when(run.getNumber()).thenReturn(1);
    underTest = new Report(run);
  }

  @Test
  public void getBuildDone() {
    Mockito.when(run.isBuilding()).thenReturn(false);
    Assert.assertEquals("true", underTest.getBuildDone());

    Mockito.when(run.isBuilding()).thenReturn(true);
    Assert.assertEquals("false", underTest.getBuildDone());
  }

  @Test
  public void getResult() {
    String results = underTest.getResults();
    Assert.assertNotNull(results);
  }
}
