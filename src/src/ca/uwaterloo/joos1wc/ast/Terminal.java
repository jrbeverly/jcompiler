package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class Terminal extends TreeNode {

  public Terminal(Token token) {
    super(token);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public String toString() {
    return String.format("%s (%s)", token.getKind().name(), token.getImage());
  }
  
  public static Terminal newInstance(Token token) {
    return new Terminal(token);
  }

}
