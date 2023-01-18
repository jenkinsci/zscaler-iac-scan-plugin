package io.jenkins.plugins.zscaler;

import com.google.common.io.Resources;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.zscaler.models.BuildDetails;
import jenkins.branch.Branch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.checkerframework.checker.units.qual.A;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ZscalerScanTest {

    private static ZscalerScan underTest;

    @BeforeClass
    public static void setUp() {
        underTest = new ZscalerScan(false);
    }

    @Test
    public void testSetUp() {
    }

    @Test(expected = AbortException.class)
    public void validateAndFailBuild() throws IOException {
        underTest.setFailBuild(true);
        underTest.setLogLevel("debug");
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
        File resourceFolder = Paths.get(jobFolder.getPath().replaceFirst("^/(.:/)", "$1")).toFile();
        when(mockJob.getRootDir()).thenReturn(resourceFolder);

        Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
        String iacScanResults = buildNumberFolder.toString().replaceFirst("^/(.:/)", "$1");
        Path path = Paths.get(iacScanResults, "iac-scan-results", "1.json");
        String results = IOUtils.toString(path.toUri(), Charset.defaultCharset());

        underTest.validateAndFailBuild(results, taskListenerMock);
    }

    @Test
    public void GetBuildDetailsMultibranch() throws IOException, InterruptedException {
        underTest.setFailBuild(true);
        underTest.setLogLevel("debug");
        TaskListener taskListenerMock = Mockito.mock(TaskListener.class);
        PrintStream mocklogStream = Mockito.mock(PrintStream.class);
        when(taskListenerMock.getLogger()).thenReturn(mocklogStream);
        doNothing().when(mocklogStream).println(anyString());
        Run buildMock = Mockito.mock(Run.class);
        Job job = Mockito.mock(Job.class);
        Path resourceDirectory = Paths.get("src", "test", "resources", "multibranch", "config.xml");
        when(buildMock.getParent()).thenReturn(job);
        File file = Mockito.mock(File.class);
        when(job.getRootDir()).thenReturn(file);
        File configFile = new File(resourceDirectory.toFile().getAbsolutePath());
        File[] files = new File[]{configFile};
        when(file.listFiles()).thenReturn(files);
        Map<String, String> map = new HashMap<>();
        map.put("USER", "test");
        map.put("BUILD_URL", "https://localhost:8086/jenkins/job/multibranch/8");
        map.put("BRANCH_NAME", "main");
        EnvVars env = new EnvVars(map);
        when(buildMock.getEnvironment(any(TaskListener.class))).thenReturn(env);
        when(buildMock.getNumber()).thenReturn(8);
        when(buildMock.getDisplayName()).thenReturn("MultiBranch");
        BranchJobProperty jobPropertyMock = Mockito.mock(BranchJobProperty.class, RETURNS_DEEP_STUBS);
        when(job.getAllProperties()).thenReturn(Arrays.asList(jobPropertyMock));
        when(jobPropertyMock.getBranch().getName()).thenReturn("main");
        Branch branch = Mockito.mock(Branch.class, RETURNS_DEEP_STUBS);
        when(branch.getSourceId()).thenReturn("38vc692a-c52371b-vc692a-e52371b");
        BuildDetails details = underTest.getBuildDetails(buildMock, taskListenerMock);
        Assert.assertEquals("8", details.getBuildNumber());
        Assert.assertEquals("test", details.getBuildTriggeredBy());
        Assert.assertEquals("main", details.getBranchName());
        Assert.assertEquals("https://github.com/devworks751/zpc-test", details.getRepoLoc());
        Assert.assertEquals(2, details.getEventDetails().size());
        Assert.assertEquals(3, details.getRepoDetails().size());


    }

    @Test
    public void validateAndFailBuildForNoIaCResourceResult() throws IOException {
        underTest.setFailBuild(true);
        underTest.setLogLevel("debug");
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
        File resourceFolder = Paths.get(jobFolder.getPath().replaceFirst("^/(.:/)", "$1")).toFile();
        when(mockJob.getRootDir()).thenReturn(resourceFolder);

        String results = "{\"no_resources_message\" : \"No IaC resources found\"}";

        underTest.validateAndFailBuild(results, taskListenerMock);
    }

    @Test(expected = AbortException.class)
    public void validateAndPostResultsToCWP_withfail() throws IOException {
        underTest.setFailBuild(true);
        underTest.setLogLevel("debug");
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
        File resourceFolder = Paths.get(jobFolder.getPath().replaceFirst("^/(.:/)", "$1")).toFile();
        when(mockJob.getRootDir()).thenReturn(resourceFolder);

        Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
        String iacScanResults = buildNumberFolder.toString().replaceFirst("^/(.:/)", "$1");
        Path path = Paths.get(iacScanResults, "iac-scan-results", "1.json");
        String results = IOUtils.toString(path.toUri(), Charset.defaultCharset());

        underTest.validateAndFailBuild(results, taskListenerMock);
    }

    @Test
    public void postResultsToWorkspace() throws IOException {
        Path path = null;
        try {
            URL jobFolder = Resources.getResource("sample");
            File resourceFolder = Paths.get(jobFolder.getPath().replaceFirst("^/(.:/)", "$1")).toFile();
            Path buildNumberFolder = Paths.get(resourceFolder.getAbsolutePath(), "1");
            String iacScanResults = buildNumberFolder.toString();
            underTest.postResultsToWorkspace(iacScanResults, jobFolder.getPath().replaceFirst("^/(.:/)", "$1"), 2);

            path = Paths.get(jobFolder.getPath().replaceFirst("^/(.:/)", "$1"), String.valueOf(2), "iac-scan-results", 2 + ".json");

            Assert.assertTrue(path.toFile().exists());
            String result = IOUtils.toString(path.toUri(), Charset.defaultCharset());
            Assert.assertNotNull(result);
        } finally {
            // cleanup
            if (path != null) FileUtils.deleteDirectory(path.getParent().getParent().toFile());
        }
    }
}
