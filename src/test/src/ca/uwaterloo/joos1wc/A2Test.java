package ca.uwaterloo.joos1wc;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class A2Test {

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Parameters(name = "{index}: {0}")
  public static Collection<String> data() {
    return E2ETestHelper.getFiles(E2ETestHelper.A2_TEST_DIRS);
  }

  private final String statusFile;

  public A2Test(String inputFile) {
    this.statusFile = inputFile;
  }

  @Test
  public void run() throws IOException {
    E2ETestHelper.runTest(exit, statusFile, new File(E2ETestHelper.BASE_OUTPUT_DIR, "a2"));
  }

}
