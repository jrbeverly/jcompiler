package ca.uwaterloo.joos1wc.scanner;

import java.io.IOException;
import java.io.InputStream;

import ca.uwaterloo.joos1wc.utility.CharacterStream;

/**
 * Used for testing code that needs InputCursors
 *
 */
public class MockInputCursor extends CharacterStream {
  public int resetCount = 0;
  public int length = 0;
  public int advanced = 0;
  InputStream mockInput;
  
  public MockInputCursor(InputStream input) throws IOException {
    super(input);
    mockInput = input;
    length = input.available();
  }
  
  public void setLength(int newLength) {
    length = newLength;
  }
  
  @Override
  public void resetCurrentPosition() {
    resetCount++;
  }
  
  @Override
  public void advance(int size) {
    advanced += size;
  }
  
  @Override
  public boolean isEndOfInput() {
    try {
      return advanced >= length || mockInput.available() == 0;
    } catch (IOException ioe) {
      return true;
    }
  }
  
  @Override
  public char next() throws IOException {
    return (char)mockInput.read();
  }
}
