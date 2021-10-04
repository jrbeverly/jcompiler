package ca.uwaterloo.joos1wc.ast;

import java.util.List;

import ca.uwaterloo.joos1wc.analysis.SymbolTable;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class TreeNode {

  public final Token token;
  public SymbolTable symbols;

  public TreeNode(Token token) {
    this.token = token;
  }

  public abstract void accept(Visitor v);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((token == null) ? 0 : token.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", this.getClass().getSimpleName(), token.getKind().name());
  }

  public static TreeNode newNonTerminalNode(Token token, List<TreeNode> children) {
    ASTNode astNode = ASTNode.newNonTerminalNode(token, children);
    //if (astNode == null) throw new RuntimeException("No AST Node for token " + token.toString() + " children " + children.toString());
    return astNode == null ? new NonTerminal(token, children) : astNode;
  }

  public static TreeNode newTerminalNode(Token token) {
    ASTNode astNode = ASTNode.newTerminalNode(token);
    return astNode == null ? new Terminal(token) : astNode;
  }

}
