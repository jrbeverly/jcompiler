package ca.uwaterloo.joos1wc.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import ca.uwaterloo.joos1wc.utility.CharacterStream;

public class InputCursorTest {

  @Test
  public void testWholeString1() throws IOException {
    String testString = "Hello, world!\nThis is a test string.\n";
    CharacterStream inputCursor = getInputCursor(testString);

    StringBuilder sb = new StringBuilder();
    while (!inputCursor.isEndOfInput()) {
      sb.append(inputCursor.next());
    }
    Assert.assertEquals("Should be able to read the whole input stream", testString, sb.toString());
  }

  @Test
  public void testWholeString2() throws IOException {
    StringBuilder sb1 = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
      sb1.append("0123456789");
    }
    CharacterStream inputCursor = getInputCursor(sb1.toString());

    StringBuilder sb2 = new StringBuilder();
    while (!inputCursor.isEndOfInput()) {
      sb2.append(inputCursor.next());
    }
    Assert.assertEquals("Should be able to read the whole input stream", sb1.toString(), sb2.toString());

    inputCursor.resetCurrentPosition();
    sb2 = new StringBuilder();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10000; j++) {
        sb2.append(inputCursor.next());
      }
      inputCursor.advance(10000);
    }
    Assert.assertEquals("Should be able to read the whole input stream", sb1.toString(), sb2.toString());
  }

  @Test
  public void testAdvance() throws IOException {
    String testString = "Hello, world!\nThis is a test string.\n";
    CharacterStream inputCursor = getInputCursor(testString);

    int skip = testString.indexOf('\n');
    inputCursor.advance(skip);
    StringBuilder sb = new StringBuilder();
    while (!inputCursor.isEndOfInput()) {
      sb.append(inputCursor.next());
    }
    Assert.assertEquals("Should be able to read the remainder of the input stream", testString.substring(skip),
        sb.toString());
  }

  @Test
  public void testLineCol() throws IOException {
    String testString = "0123\n4567\n";
    CharacterStream inputCursor = getInputCursor(testString);

    int[] expectedLine = new int[] { 1, 1, 1, 2, 2, 3 };
    int[] expectedCol = new int[] { 1, 3, 5, 2, 4, 1 };

    StringBuilder sb = new StringBuilder();
    int[] line = new int[6];
    int[] col = new int[6];
    for (int i = 0; i < 5; i++) {
      sb.append(inputCursor.next()).append(inputCursor.next());
      line[i] = inputCursor.getCurrentLine();
      col[i] = inputCursor.getLinePosition();
      inputCursor.advance(2);
    }
    line[5] = inputCursor.getCurrentLine();
    col[5] = inputCursor.getLinePosition();

    Assert.assertEquals("Should be able to read the whole input stream", testString, sb.toString());
    for (int i = 0; i < 6; i++) {
      Assert.assertEquals(expectedLine[i], line[i]);
      Assert.assertEquals(expectedCol[i], col[i]);
    }
  }

  private CharacterStream getInputCursor(String s) {
    return new CharacterStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII)));
  }

}
