package ca.uwaterloo.joos1wc.parse;

import java.util.List;

import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class Production {
  private final TokenKind lhs;
  private final List<TokenKind> rhs;
  
  public Production(TokenKind lhs, List<TokenKind> rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public TokenKind getLHS() {
    return this.lhs;
  }
  
  public List<TokenKind> getRHS() {
    return this.rhs;
  }

}
