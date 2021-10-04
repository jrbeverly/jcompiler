package ca.uwaterloo.joos1wc;

public class JCompilerInfo {

  private boolean verboseDebug;
  private String[] libraries;
  private String[] files;

  public JCompilerInfo() {
    verboseDebug = false;
  }

  public boolean isVerbose() {
    return verboseDebug;
  }

  public String[] getLibraries() {
    return libraries;
  }

  public String[] getFiles() {
    return files;
  }
}
