package ca.uwaterloo.joos1wc.diagnostics;

public class InvalidGrammarException extends ParseException {
  private static final long serialVersionUID = 0L;

  public int numArg;

  public InvalidGrammarException(String message, int arg) {
    super(message);
    this.numArg = arg;
  }
}
