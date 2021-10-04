package ca.uwaterloo.joos1wc.diagnostics;

import ca.uwaterloo.joos1wc.scanner.Token;

public class CastException extends JoosException {
  private static final long serialVersionUID = 0L;

  public CastException(String message, Token token) {
    super(message, token);
  }

}
