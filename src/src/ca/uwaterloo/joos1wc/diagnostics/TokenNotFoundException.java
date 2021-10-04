package ca.uwaterloo.joos1wc.diagnostics;

public class TokenNotFoundException extends ParseException {
  private static final long serialVersionUID = 0L;
  private String name;

  public TokenNotFoundException(String message, String tokenName) {
    super(message);
    this.name = tokenName;
  }

  public String getTokenName() {
    return name;
  }
}
