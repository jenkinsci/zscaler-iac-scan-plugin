package io.jenkins.plugins.zscaler;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import hudson.AbortException;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import io.jenkins.plugins.zscaler.models.ScannerConfig;
import okhttp3.ResponseBody;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import retrofit2.Response;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;

public class ZscannerSetup {

  private static final Logger LOGGER = Logger.getLogger(ZscannerSetup.class.getName());

  TaskListener listener;
  CWPService cwpService;
  String binaryLoc;
  Configuration configuration;
  StandardUsernamePasswordCredentials credentials;
  ProxyConfiguration proxy;


  public ZscannerSetup(
      TaskListener listener,
      CWPService cwpService,
      Configuration configuration,
      String binaryLoc,
      StandardUsernamePasswordCredentials credentials,
      ProxyConfiguration proxy) {
    this.listener = listener;
    this.cwpService = cwpService;
    this.configuration = configuration;
    this.binaryLoc = binaryLoc;
    this.credentials = credentials;
    this.proxy = proxy;
  }

  public void setup() throws AbortException {

    Path zscanner = Paths.get(binaryLoc, "zscanner");
    boolean doesZscannerExists = Files.exists(zscanner);
    if (!doesZscannerExists) {
      try {
        downloadScanner();
      } catch (IOException e) {
        listener.getLogger().println("Failed to download zscanner due to - " + e.getMessage());
        // Delete the scanner binary if exists
        deleteBinary();
        throw new AbortException("Failed to download zscanner");
      }
    }
    try {
      initScanner();
      if(doesZscannerExists){
        try{
          checkAndUpdateBinary(zscanner);
          Files.setPosixFilePermissions(
                  zscanner,
                  Sets.newHashSet(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
        } catch (IOException e) {
          LOGGER.log(Level.ERROR, "Failed to update the current scanner", e);
          throw new AbortException("Failed to update the current scanner");
        }
      }
    } catch (IOException e) {
      LOGGER.log(Level.ERROR, "Failed to initialise the scanner", e);
      throw new AbortException("Failed to initialise the scanner");
    }
  }

  private void deleteBinary() {
    Path zscannerPath = Paths.get(binaryLoc, "zscanner");
    if (zscannerPath.toFile().exists()) {
      try {
        FileUtils.forceDelete(zscannerPath.toFile());
      } catch (IOException e) {
        LOGGER.log(Level.ERROR, "Failed to delete zscanner binary due to - " + e.getMessage());
      }
    }
  }

  private void downloadScanner() throws IOException {
    listener.getLogger().println("Downloading Zscaler IaC scanner");

    String body = "{\"platform\": \"%s\",\"arch\":\"%s\"}";

    String osArch = SystemUtils.OS_ARCH;
    if (osArch.equals("amd64")) {
      osArch = "x86_64";
    } else if (osArch.equals("aarch64")) {
      osArch = "arm64";
    }
    String osName = SystemUtils.OS_NAME;
    if (osName.toLowerCase(Locale.ROOT).contains("mac")) {
      osName = "Darwin";
    }
    body = String.format(body, osName, osArch);

    Response<ResponseBody> download = cwpService.downloadScanner(body).execute();
    String scannerCompressedFilePath = binaryLoc + File.separator + "zscanner.tar.gz";
    try {
      File scannerFile = new File(scannerCompressedFilePath);
      boolean isCreated = scannerFile.createNewFile();
      if (isCreated) {
        if (download.body() != null) {
          FileUtils.copyInputStreamToFile(download.body().byteStream(), scannerFile);
        } else {
          listener.getLogger().println("Failed to download scanner");
          throw new AbortException("Failed to download scanner");
        }
      }

      decompressTarGzipFile(Paths.get(scannerCompressedFilePath), Paths.get(binaryLoc));
    } finally {
      Files.delete(Paths.get(scannerCompressedFilePath));
    }

    createBaseDir();
  }

  private void createBaseDir() throws IOException {
    Path baseDir = Paths.get(binaryLoc, "zscaler");
    if (!baseDir.toFile().exists()) {
      Files.createDirectory(
          baseDir,
          PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr--r--")));
    }
  }

  private void initScanner() throws IOException {
    listener.getLogger().println("Initialising Zscaler IaC scanner");
    String clientId = credentials.getUsername();
    String clientSecret = credentials.getPassword().getPlainText();
    if ("custom".equalsIgnoreCase(configuration.getRegion())) {
      configureScanner(
          configuration.getRegion(), configuration.getApiUrl(), configuration.getAuthUrl());
    }
    ProcessBuilder processBuilder = new ProcessBuilder();
    String proxyString = ClientUtils.getProxyConfigString(proxy);
    String[] command = {
      "./zscanner",
      "login",
      "cc",
      "-m",
      "cicd",
      "--client-id",
      clientId,
      "--client-secret",
      clientSecret,
      "-r",
      configuration.getRegion().toUpperCase(Locale.ROOT)
    };
    if (proxyString != null) {
      ArrayUtils.add(command, "--proxy");
      ArrayUtils.add(command, proxyString);
    }
    LOGGER.log(Level.INFO,"Login Command:: " + ArrayUtils.toString(command));
    Process exec = processBuilder.command(command).directory(new File(binaryLoc)).start();

    try (InputStream errorStream = exec.getErrorStream();
        InputStream resultStream = exec.getInputStream()) {
      if (errorStream.available() > 0) {
        listener.getLogger().println(IOUtils.toString(errorStream, Charset.defaultCharset()));
      }

      listener.getLogger().println(IOUtils.toString(resultStream, Charset.defaultCharset()));
      exec.destroy();
    }
  }

  private void configureScanner(String region, String apiUrl, String authUrl) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder();
    ScannerConfig scannerConfig = new ScannerConfig();
    ScannerConfig.AuthConfig authConfig = new ScannerConfig.AuthConfig();
    authConfig.setHost(authUrl);
    authConfig.setScope("offline_access");
    authConfig.setAudience("https://api.zscwp.io/iac");
    scannerConfig.setAuthConfig(authConfig);
    scannerConfig.setHost(apiUrl);
    scannerConfig.setAuthConfig(authConfig);
    ObjectMapper objectMapper = new ObjectMapper();

    String[] command = {
      "./zscanner",
      "config",
      "add",
      "-m",
      "cicd",
      "-k",
      "custom_region",
      "-v",
      objectMapper.writeValueAsString(scannerConfig)
    };
    LOGGER.log(Level.INFO, "Custom Region String::" + objectMapper.writeValueAsString(scannerConfig));
    Process exec = processBuilder.command(command).directory(new File(binaryLoc)).start();
    try (InputStream errorStream = exec.getErrorStream();
        InputStream resultStream = exec.getInputStream()) {
      if (errorStream.available() > 0) {
        listener.getLogger().println(IOUtils.toString(errorStream, Charset.defaultCharset()));
      }

      listener.getLogger().println(IOUtils.toString(resultStream, Charset.defaultCharset()));
      exec.destroy();
    }
  }

  private static void decompressTarGzipFile(Path source, Path target) throws IOException {

    if (Files.notExists(source)) {
      throw new IOException("File doesn't exists!");
    }

    try (InputStream fi = Files.newInputStream(source);
        BufferedInputStream bi = new BufferedInputStream(fi);
        GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
        TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

      ArchiveEntry entry;
      while ((entry = ti.getNextEntry()) != null) {

        Path newPath = zipSlipProtect(entry, target);

        if (entry.isDirectory()) {
          Files.createDirectories(newPath);
        } else {

          Path parent = newPath.getParent();
          if (parent != null) {
            if (Files.notExists(parent)) {
              Files.createDirectories(parent);
            }
          }

          Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
          Files.setPosixFilePermissions(
              newPath,
              Sets.newHashSet(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
        }
      }
    }
  }

  private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir) throws IOException {

    Path targetDirResolved = targetDir.resolve(entry.getName());

    Path normalizePath = targetDirResolved.normalize();

    if (!normalizePath.startsWith(targetDir)) {
      throw new IOException("Bad entry: " + entry.getName());
    }

    return normalizePath;
  }

  public static void cleanup(String binaryLoc) throws IOException {
    ProcessBuilder process = new ProcessBuilder();
    process.command("./zscanner", "logout","-m","cicd").directory(new File(binaryLoc)).start();
  }

  private void checkAndUpdateBinary(Path binaryPath) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder();
    String proxyString = ClientUtils.getProxyConfigString(proxy);
    String[] command = {
            "./zscanner",
            "update",
            "-m",
            "cicd"
    };
    if (proxyString != null) {
      ArrayUtils.add(command, "--proxy");
      ArrayUtils.add(command, proxyString);
    }
    LOGGER.log(Level.INFO,"Update Command:: " + ArrayUtils.toString(command));
    Process exec = processBuilder.command(command).directory(new File(binaryLoc)).start();

    try (InputStream errorStream = exec.getErrorStream();
         InputStream resultStream = exec.getInputStream()) {
      if (errorStream.available() > 0) {
        LOGGER.log(Level.INFO, "Error during update");
        listener.getLogger().println(IOUtils.toString(errorStream, Charset.defaultCharset()));
      }
      listener.getLogger().println(IOUtils.toString(resultStream, Charset.defaultCharset()));
      exec.destroy();
    }
  }
}
