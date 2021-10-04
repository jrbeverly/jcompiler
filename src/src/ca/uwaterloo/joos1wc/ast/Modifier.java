package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class Modifier extends ASTNode {

  public enum ModifierKeyword {
    PUBLIC, PROTECTED, STATIC, ABSTRACT, FINAL, NATIVE
  }

  public final ModifierKeyword keyword;

  public Modifier(Token token, ModifierKeyword keyword) {
    super(token);
    this.keyword = keyword;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public String toString() {
    return String.format("Modifier (%s)", keyword.toString());
  }

  public static Modifier newInstance(Token token, Terminal kwNode) {
    return new Modifier(token, ModifierKeyword.valueOf(kwNode.token.getKind().name()));
  }

}
