package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class Null extends ASTNode {

  /**
   * This is just used for qualified NonTerminals, as in Expression(opt)
   */
  public Null(Token token) {
    super(token);
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static Null newInstance(Token token) {
    return new Null(token);
  }

  // TypeDeclaration -> SEMICOLON
  public static Null newInstance(Token token, Terminal n0) {
    return new Null(token);
  }

}
