package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class SimpleName extends Name {

  public SimpleName(Token token, String identifier) {
    super(token, identifier);
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public String toString() {
    return String.format("SimpleName (%s)", identifier);
  }

  public static SimpleName newInstance(Token token, Terminal idNode) {
    return new SimpleName(token, idNode.token.getImage());
  }

  public static SimpleName newInstance(Token token, SimpleName o) {
    return new SimpleName(token, o.identifier);
  }

}
