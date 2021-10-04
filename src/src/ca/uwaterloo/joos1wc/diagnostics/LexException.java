package ca.uwaterloo.joos1wc.diagnostics;

public class LexException extends JoosException {
  private static final long serialVersionUID = 0L;

  public int lineNumber;
  public int colNumber;
  public String buffer;

  public LexException(String message, String buffer, int lineNumber, int colNumber) {
    super(message);
    this.buffer = buffer;
    this.lineNumber = lineNumber;
    this.colNumber = colNumber;
  }
  
  
}
