package ca.uwaterloo.joos1wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.ast.NonTerminal;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.diagnostics.JoosException;
import ca.uwaterloo.joos1wc.scanner.Token;

@RunWith(Parameterized.class)
public class E2EAbstractSyntaxTest {

  private static final File[] TESTCASE_DIRS = new File[] { new File("test/resources/e2e/a1"),
      new File("test/resources/e2e/web_examples"), new File("test/resources/assignment_testcases/a1") };

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  @Parameters(name = "{index}: {0}")
  public static Collection<String> data() throws IOException {
    List<String> data = new ArrayList<String>();
    for (File dir : TESTCASE_DIRS) {
      File[] inputFiles = dir.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.matches(".*\\.java.status");
        }
      });
      if (inputFiles == null) continue;
      for (File file : inputFiles) {
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        try {
          int status = Integer.parseInt(input.readLine().trim());
          if (status == 0) {
            String filename = file.toString();
            data.add(filename.substring(0, filename.length() - ".status".length()));
          }
        } finally {
          input.close();
        }
      }
    }
    return data;
  }

  private final String[] inputFile;

  public E2EAbstractSyntaxTest(String inputFile) {
    this.inputFile = new String[] { inputFile };
  }

  @Test
  public void run() throws IOException, JoosException {
    List<List<Token>> tokens = Joos1Wc.tokenize(inputFile);
    List<TreeNode> trees = Joos1Wc.parse(tokens);
    for (TreeNode root : trees) {
      root.accept(new RecursiveVisitor() {
        @Override
        public void preVisit(NonTerminal node) {
          Assert.fail(String.format("NonTerminal found: %s (%s)", node.token.getKind().name(), node.token.getImage()));
        }
        // Control
        @Override
        public void preVisit(Block node) {
          System.err.format("%s (%s)\n", node.token.getKind().name(), node.token.getImage());
        }
      });
    }
  }

}
