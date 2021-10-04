package ca.uwaterloo.joos1wc.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Exposes a Cursor around a stream, supporting read and seek operations on the underlying buffered stream.
 */
public class CharacterStream {

  private static final char NEWLINE = '\n';

  private final InputStream input;
  private final File file;
  private boolean isInputEof;
  private int startPosition;
  private int endPosition;
  private int readPosition;
  private int line = 1, col = 1;
  private byte[] buffer = new byte[1024];

  /**
   * Constructs an cursor on the specified file.
   * 
   * @param file
   *          The file to scan.
   * @throws FileNotFoundException
   */
  public CharacterStream(File file) throws FileNotFoundException {
    this.file = file;
    this.input = new FileInputStream(file);
  }

  /**
   * Constructs an cursor on the specified stream.
   * 
   * @param input
   *          The stream to scan.
   */
  public CharacterStream(InputStream input) {
    this.file = null; // the file is only kept around for reference anyway
    this.input = input;
  }

  /**
   * Closes this cursor.
   * 
   * @throws IOException
   */
  public void close() throws IOException {
    this.input.close();
  }

  /**
   * Sets the current position of this stream based on the given offset value.
   * 
   * @param distance
   *          The point relative to origin from which to begin seeking.
   * @throws IOException
   */
  public void advance(int distance) throws IOException {
    resetCurrentPosition();
    for (int i = 0; i < distance; i++) {
      char c = next();
      col++;
      if (c == NEWLINE) {
        line++;
        col = 1;
      }
    }
    startPosition = readPosition;
  }

  /**
   * Resets the position within the current stream.
   */
  public void resetCurrentPosition() {
    readPosition = startPosition;
  }

  /**
   * Returns true if this scanner has reached then end of its input.
   * 
   * @return Whether or not we have reached the end of the input stream.
   */
  public boolean isEndOfInput() {
    return isInputEof && readPosition == endPosition;
  }

  /**
   * Finds and returns the next character from this scanner.
   * 
   * @return The next character in the input stream.
   * @throws IOException
   */
  public char next() throws IOException {
    if (isEndOfInput()) {
      throw new IOException("Tried to read past end of file");
    }

    // If no bytes left in buffer, read from input stream
    if (readPosition == endPosition) {
      if (isInputEof) {
        throw new IOException("Tried to read past end of file");
      }

      // If we started reading from the first half of the buffer, expand the buffer first
      // Then copy everything after startPosition to the beginning of the buffer
      // The copy can be avoided with a cyclic buffer, but it's probably not worth the extra complexity
      if (startPosition < buffer.length / 2) {
        byte[] newBuffer = new byte[2 * buffer.length];
        System.arraycopy(buffer, startPosition, newBuffer, 0, endPosition - startPosition);
        buffer = newBuffer;
      } else {
        System.arraycopy(buffer, startPosition, buffer, 0, endPosition - startPosition);
      }

      // Update the indices and read
      endPosition -= startPosition;
      readPosition -= startPosition;
      startPosition = 0;
      readToBuffer();
    }
    assert(readPosition < endPosition);

    // Check for ASCII, advance position and return
    byte b = buffer[readPosition];
    if ((b & 0x80) > 0) {
      throw new IOException("Non-ASCII input detected");
    }
    readPosition++;
    return (char) b;
  }

  /**
   * The file currently being scanned.
   * 
   * @return A file to be scanned.
   */
  public File getFile() {
    return file;
  }

  /**
   * Get the current line number.
   * 
   * @return The current line number.
   */
  public int getCurrentLine() {
    return line;
  }

  /**
   * Get the current column position.
   * 
   * @return The current column position.
   */
  public int getLinePosition() {
    return col;
  }

  /**
   * Returns a buffer string representing recently read input.
   * 
   * @return A string representing buffered input.
   */
  public String getBufferedString() {
    byte[] subset = new byte[readPosition - startPosition];
    System.arraycopy(buffer, startPosition, subset, 0, readPosition - startPosition);
    return new String(subset);
  }

  private void readToBuffer() throws IOException {
    int numBytesToRead = buffer.length - endPosition;
    int bytesRead = input.read(buffer, endPosition, numBytesToRead);
    isInputEof = bytesRead < numBytesToRead;
    endPosition += bytesRead;
  }

}
