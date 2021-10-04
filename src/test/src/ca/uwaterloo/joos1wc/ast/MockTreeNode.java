package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public class MockTreeNode extends TreeNode {
  public String name;

  public MockTreeNode(String name) {
    super(new Token(null, new NonTerminalTokenKind(name), name, 0, 0));
    this.name = name;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

}
