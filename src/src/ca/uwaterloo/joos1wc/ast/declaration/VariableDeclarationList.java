package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class VariableDeclarationList extends ASTNode {

  private List<VariableDeclaration> variableDeclarations;

  public VariableDeclarationList(Token token, List<VariableDeclaration> variableDeclarations) {
    super(token);
    this.variableDeclarations = variableDeclarations;
  }

  public List<VariableDeclaration> getVariableDeclarations() {
    return Collections.unmodifiableList(variableDeclarations);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  // FormalParameterList(opt) -> FormalParameterList
  public static VariableDeclarationList newInstance(Token token, VariableDeclarationList o) {
    return new VariableDeclarationList(token, o.variableDeclarations);
  }

  // FormalParameterList(opt) -> epsilon
  public static VariableDeclarationList newInstance(Token token) {
    return new VariableDeclarationList(token, new ArrayList<VariableDeclaration>());
  }

  // FormalParameterList -> FormalParameter
  public static VariableDeclarationList newInstance(Token token, VariableDeclaration node) {
    List<VariableDeclaration> variableDeclarations = new ArrayList<VariableDeclaration>();
    variableDeclarations.add(node);
    return new VariableDeclarationList(token, variableDeclarations);
  }

  // FormalParameterList -> FormalParameterList , FormalParameter
  public static VariableDeclarationList newInstance(Token token, VariableDeclarationList o, Terminal n0,
      VariableDeclaration node) {
    List<VariableDeclaration> variableDeclarations = o.variableDeclarations;
    variableDeclarations.add(node);
    return new VariableDeclarationList(token, variableDeclarations);
  }

}
