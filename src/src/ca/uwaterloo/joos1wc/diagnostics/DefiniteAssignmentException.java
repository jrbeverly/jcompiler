package ca.uwaterloo.joos1wc.diagnostics;

import ca.uwaterloo.joos1wc.scanner.Token;

public class DefiniteAssignmentException extends JoosException {
  private static final long serialVersionUID = 0L;
  
  public DefiniteAssignmentException(String message, Token token) {
    super(message, token);
  }

}
