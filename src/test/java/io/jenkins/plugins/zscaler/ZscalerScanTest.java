package io.jenkins.plugins.zscaler;

import com.google.common.io.Resources;
import hudson.AbortException;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

public class ZscalerScanTest {

  private static ZscalerScan underTest;

  @BeforeClass
  public static void setUp() {
    underTest = new ZscalerScan(false);
  }

  @Test
  public void testSetUp() {}

  @Test(expected = AbortException.class)
  public void validateAndFailBuild() throws IOException {
    underTest.setFailBuild(true);

    TaskListener taskListenerMock = Mockito.mock(TaskListener.class);
    PrintStream mocklogStream = Mockito.mock(PrintStream.class);
    when(taskListenerMock.getLogger()).thenReturn(mocklogStream);
    doNothing().when(mocklogStream).println(anyString());
    Run buildMock = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    when(buildMock.getParent().getDisplayName()).thenReturn("sample");
    when(buildMock.getNumber()).thenReturn(1);
    when(buildMock.getTimestampString()).thenReturn(String.valueOf(System.currentTimeMillis()));

    URL jobFolder = Resources.getResource("sample");
    Job mockJob = Mockito.mock(Job.class);

    when(buildMock.getParent()).thenReturn(mockJob);
    File resourceFolder = Paths.get(jobFolder.getPath()).toFile();
    when(mockJob.getRootDir()).thenReturn(resourceFolder);

    Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
    String iacScanResults = buildNumberFolder.toString();
    Path path = Paths.get(iacScanResults, "iac-scan-results", "1.json");
    String results = IOUtils.toString(path.toUri(), Charset.defaultCharset());

    underTest.validateAndFailBuild(results, taskListenerMock);
  }

  @Test(expected = AbortException.class)
  public void validateAndPostResultsToCWP_withfail() throws IOException {
    underTest.setFailBuild(true);
    TaskListener taskListenerMock = Mockito.mock(TaskListener.class);
    PrintStream mocklogStream = Mockito.mock(PrintStream.class);
    when(taskListenerMock.getLogger()).thenReturn(mocklogStream);
    doNothing().when(mocklogStream).println(anyString());
    Run buildMock = Mockito.mock(Run.class, Mockito.RETURNS_DEEP_STUBS);
    when(buildMock.getParent().getDisplayName()).thenReturn("sample");
    when(buildMock.getNumber()).thenReturn(1);
    when(buildMock.getTimestampString()).thenReturn(String.valueOf(System.currentTimeMillis()));

    URL jobFolder = Resources.getResource("sample");
    Job mockJob = Mockito.mock(Job.class);

    when(buildMock.getParent()).thenReturn(mockJob);
    File resourceFolder = Paths.get(jobFolder.getPath()).toFile();
    when(mockJob.getRootDir()).thenReturn(resourceFolder);

    Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
    String iacScanResults = buildNumberFolder.toString();
    Path path = Paths.get(iacScanResults, "iac-scan-results", "1.json");
    String results = IOUtils.toString(path.toUri(), Charset.defaultCharset());

    underTest.validateAndFailBuild(results, taskListenerMock);
  }

  @Test
  public void postResultsToWorkspace() throws IOException {
    Path path = null;
    try {
      URL jobFolder = Resources.getResource("sample");
      File resourceFolder = Paths.get(jobFolder.getPath()).toFile();
      Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
      String iacScanResults = buildNumberFolder.toString();
      underTest.postResultsToWorkspace(iacScanResults, jobFolder.getPath(), 2);

      path = Paths.get(jobFolder.getPath(), String.valueOf(2), "iac-scan-results", 2 + ".json");

      Assert.assertTrue(path.toFile().exists());
      String result = IOUtils.toString(path.toUri(), Charset.defaultCharset());
      Assert.assertNotNull(result);
    } finally {
      // cleanup
      if (path != null) FileUtils.deleteDirectory(path.getParent().getParent().toFile());
    }
  }
}
