package io.jenkins.plugins;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.AbortException;
import hudson.ProxyConfiguration;
import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;

public class ScannerSetupTask extends MasterToSlaveCallable<Object, RuntimeException> {

  TaskListener listener;
  Configuration configuration;
  ProxyConfiguration proxy;
  String binaryLoc;
  private final StandardUsernamePasswordCredentials credentials;

  public ScannerSetupTask(
      TaskListener listener,
      Configuration configuration,
      ProxyConfiguration proxy,
      String binaryLoc,
      StandardUsernamePasswordCredentials credentials) {
    this.listener = listener;
    this.configuration = configuration;
    this.proxy = proxy;
    this.binaryLoc = binaryLoc;
    this.credentials = credentials;
  }

  @Override
  public Object call() throws RuntimeException {
    ZscannerSetup zscannerSetup = null;
    try {
      CWPService cwpService =
          CwpClient.getClient(configuration, proxy, credentials).create(CWPService.class);
      zscannerSetup =
          new ZscannerSetup(listener, cwpService, configuration, binaryLoc, credentials, proxy);
      zscannerSetup.setup();
    } catch (AbortException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
