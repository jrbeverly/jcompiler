package ca.uwaterloo.joos1wc.ast;

import java.util.List;

import ca.uwaterloo.joos1wc.scanner.Token;

public class NonTerminal extends TreeNode {

  public final List<TreeNode> children;

  // Would be nice to be able to statically check that we only get non-terminal tokens here
  public NonTerminal(Token token, List<TreeNode> children) {
    super(token);
    this.children = children;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
  
  public String toString() {
    return token.getKind().name();
  }

}
