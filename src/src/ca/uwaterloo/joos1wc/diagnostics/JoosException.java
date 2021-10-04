package ca.uwaterloo.joos1wc.diagnostics;

import ca.uwaterloo.joos1wc.scanner.Token;

public class JoosException extends Exception {
  private static final long serialVersionUID = 0L;
  private static final String tokenMessage = "Exception in file(%s) at line(%d:%d) <%s>\n";

  private final Token token;
  
  public JoosException(String message) {
    super(message);
    token = null;
  }
  
  public JoosException(String message, Token token) {
    super(message);
    this.token = token;
  }
  
  @Override
  public String getMessage() {
    if (token != null) {
      return String.format(tokenMessage, token.getFile().getName(), token.getLineNumber(), 
          token.getPosition(), token.getImage()) + super.getMessage();
    }
    
    return super.getMessage();
  }
  
}
