package ca.uwaterloo.joos1wc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Assert;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class E2ETestHelper {

  private static final String E2EDIR = "test/resources/e2e/";
  private static final String ADIR = "test/resources/assignment_testcases/";

  public static final File BASE_OUTPUT_DIR = new File("target/test/lastrun");
  public static final File[] A1_TEST_DIRS = getAssignmentCase("a1");
  public static final File[] A2_TEST_DIRS = getAssignmentCase("a2");
  public static final File[] A3_TEST_DIRS = getAssignmentCase("a3");
  public static final File[] A4_TEST_DIRS = getAssignmentCase("a4");
  public static final File[] A5_TEST_DIRS = getAssignmentCase("a5");
  public static final File[] WEB_TEST_DIRS = new File[] { new File("test/resources/e2e/web_examples") };
  public static final File[] TESTCASE_DIRS = new File[] { new File("test/resources/e2e/web_examples"),
      new File(E2EDIR + "a1"), new File(ADIR + "a1"), new File(E2EDIR + "a2"), new File(ADIR + "a2"),
      new File(E2EDIR + "a3"), new File(ADIR + "a3") };

  private static final File STDLIB_DIR = new File("test/resources/stdlib/5.0");
  private static final List<String> STDLIB_FILES = getDirectoryStructure(STDLIB_DIR);

  private static final String ASSEMBLER_CMD = "tools/nasm -O1 -f elf -g -F dwarf %s -o %s";
  private static final String LINKER_CMD = "ld -melf_i386 -o %s/main %s/runtime.o %s";

  static {
    try {
      if (BASE_OUTPUT_DIR.exists()) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Recursively delete " + BASE_OUTPUT_DIR.getAbsolutePath() + "? (y/n) ");
        String input = in.readLine();
        if (input.equals("y")) {
          E2ETestHelper.recursivelyDelete(BASE_OUTPUT_DIR.toPath());
          Files.createDirectories(BASE_OUTPUT_DIR.toPath());
        }
      } else {
        Files.createDirectories(BASE_OUTPUT_DIR.toPath());
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static File[] getAssignmentCase(String name) {
    return new File[] { new File(E2EDIR + name), new File(ADIR + name) };
  }

  public static List<String> getDirectoryStructure(File dir) {
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

  public static List<String> getFiles(File[] testcases) {
    List<String> data = new ArrayList<String>();
    for (File dir : testcases) {
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

  public static void recursivelyDelete(Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (exc == null) {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        } else {
          throw exc;
        }
      }
    });
  }

  public static List<String> collectFiles(String inputFile) {
    List<String> sourceFiles = new ArrayList<>();
    if (inputFile.matches(".*\\.java")) {
      sourceFiles.add(inputFile);
    } else {
      sourceFiles.addAll(E2ETestHelper.getDirectoryStructure(new File(inputFile)));
    }
    sourceFiles.addAll(STDLIB_FILES);
    return sourceFiles;
  }

  public static void compile(List<String> sourceFiles, File outputDir) throws IOException {
    Files.createDirectories(outputDir.toPath());
    Joos1Wc.CWD = outputDir;
    Files.createDirectory(new File(outputDir, "output").toPath());
    Joos1Wc.main(sourceFiles.toArray(new String[sourceFiles.size()]));
  }

  private static void redirectToStderr(InputStream stream) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(stream));
    String input = in.readLine();
    while (input != null) {
      System.err.println(input);
      input = in.readLine();
    }
    in.close();
  }

  public static void assembleAndLink(File testcaseDir) throws IOException {
    Runtime runtime = Runtime.getRuntime();
    File[] assemblies = new File(testcaseDir, "output").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.matches(".*\\.s");
      }
    });
    StringBuilder objectFilesBuilder = new StringBuilder();
    for (File assembly : assemblies) {
      String filename = assembly.toString();
      String outputFilename = filename.substring(0, filename.length() - 2) + ".o";
      objectFilesBuilder.append(outputFilename).append(" ");
      String assemblerCmd = String.format(ASSEMBLER_CMD, filename, outputFilename);
      if (Joos1Wc.DEBUG) {
        System.err.println(assemblerCmd);
      }
      Process p = runtime.exec(assemblerCmd);
      try {
        int status = p.waitFor();
        if (status != 0) {
          redirectToStderr(p.getErrorStream());
          Assert.fail(String.format("Error during assembly of %s", filename));
        }
      } catch (InterruptedException e) {
      }
    }
    String linkerCmd = String.format(LINKER_CMD, testcaseDir.toString(), STDLIB_DIR, objectFilesBuilder.toString());
    if (Joos1Wc.DEBUG) {
      System.err.println(linkerCmd);
    }
    Process p = runtime.exec(linkerCmd);
    try {
      int status = p.waitFor();
      if (status != 0) {
        redirectToStderr(p.getErrorStream());
        Assert.fail("Error during linking");
      }
    } catch (InterruptedException e) {
    }
  }

  public static void executeAndDiff(File testcaseDir, int expectedStatus, BufferedReader expectedOutputReader)
      throws IOException {
    Process p = new ProcessBuilder("./" + new File(testcaseDir, "main").toString()).start();
    try {
      int status = p.waitFor();
      redirectToStderr(p.getErrorStream());
      Assert.assertEquals("Compiled program returned incorrect status", expectedStatus, status);
      BufferedReader outputReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String expectedOutput = expectedOutputReader.readLine();
      while (expectedOutput != null) {
        Assert.assertEquals("Compiled program gave incorrect output", expectedOutput, outputReader.readLine());
        expectedOutput = expectedOutputReader.readLine();
      }
      Assert.assertNull("Unexpected output from compiled program", outputReader.readLine());
    } catch (InterruptedException e) {
    }
  }

  public static void runTest(ExpectedSystemExit exit, String statusFile, File outputDir) throws IOException {
    String inputFile = statusFile.substring(0, statusFile.length() - ".status".length());
    BufferedReader statusReader = new BufferedReader(new InputStreamReader(new FileInputStream(statusFile)));
    BufferedReader outputReader = null;
    if (new File(inputFile + ".stdout").exists()) {
      outputReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile + ".stdout")));
    }

    try {
      int status = Integer.parseInt(statusReader.readLine().trim());
      if (outputReader == null && status > 0) {
        // Expect compilation error
        exit.expectSystemExitWithStatus(status);
      }

     /* List<String> sourceFiles = collectFiles(inputFile);
      File testcaseDir = new File(outputDir, inputFile.replace('/', '.'));
      compile(sourceFiles, testcaseDir);
      assembleAndLink(testcaseDir);

      if (outputReader != null) {
        executeAndDiff(testcaseDir, status, outputReader);
      }*/
    } finally {
      statusReader.close();
      if (outputReader != null) {
        outputReader.close();
      }
    }
  }
}
