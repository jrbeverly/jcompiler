package ca.uwaterloo.joos1wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class E2ETest {

  private static final File STDLIB_DIR = new File("test/resources/stdlib/3.0");
  private static final List<String> STDLIB_FILES;
  private static final File[] TESTCASE_DIRS = new File[] { new File("test/resources/e2e/web_examples") };

  static {
    STDLIB_FILES = recursivelyListFiles(STDLIB_DIR);
  }

  private static List<String> recursivelyListFiles(File dir) {
    List<String> ret = new ArrayList<String>();
    Queue<File> q = new LinkedList<File>();
    q.addAll(Arrays.asList(dir.listFiles()));
    while (!q.isEmpty()) {
      File f = q.poll();
      if (f.isDirectory()) {
        q.addAll(Arrays.asList(f.listFiles()));
      } else if (f.getName().matches(".*\\.java")) {
        ret.add(f.toString());
      }
    }
    return ret;
  }

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Parameters(name = "{index}: {0}")
  public static Collection<String> data() {
    List<String> data = new ArrayList<String>();
    for (File dir : TESTCASE_DIRS) {
      File[] inputFiles = dir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.matches(".*\\.status");
        }
      });
      if (inputFiles == null)
        continue;
      for (File file : inputFiles) {
        data.add(file.toString());
      }
    }
    return data;
  }

  private final String statusFile;

  public E2ETest(String inputFile) {
    this.statusFile = inputFile;
  }

  @Test
  public void run() throws IOException {
    FileInputStream stream = new FileInputStream(statusFile);
    InputStreamReader input = new InputStreamReader(stream);
    BufferedReader buffered = new BufferedReader(input);

    try {
      int status = Integer.parseInt(buffered.readLine().trim());
      if (status > 0) {
        exit.expectSystemExitWithStatus(status);
      }

      List<String> sourceFiles = new ArrayList<String>(STDLIB_FILES);
      String inputFile = statusFile.substring(0, statusFile.length() - ".status".length());
      if (inputFile.matches(".*\\.java")) {
        sourceFiles.add(inputFile);
      } else {
        sourceFiles.addAll(recursivelyListFiles(new File(inputFile)));
      }

      Joos1Wc.main(sourceFiles.toArray(new String[sourceFiles.size()]));
    } finally {
      buffered.close();
    }
  }

}
