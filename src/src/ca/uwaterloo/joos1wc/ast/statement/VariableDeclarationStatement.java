package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.declaration.IVariableDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class VariableDeclarationStatement extends Statement implements INamedEntityNode, IVariableDeclaration {

  public final Type type;
  public final String name;
  public final Expression initExpr;

  private int stackOffset;

  public VariableDeclarationStatement(Token token, Type type, String name, Expression initExpr) {
    super(token);
    this.type = type;
    this.name = name;
    this.initExpr = initExpr;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public EntityType getEntityType() {
    return EntityType.VARIABLE;
  }

  @Override
  public void setStackOffset(int stackOffset) {
    this.stackOffset = stackOffset;
  }

  @Override
  public int getStackOffset() {
    return stackOffset;
  }

  public static VariableDeclarationStatement newInstance(Token token, VariableDeclarationStatement o) {
    return new VariableDeclarationStatement(token, o.type, o.name, o.initExpr);
  }

  // LocalVariableDeclarationStatement -> LocalVariableDeclaration SEMICOLON
  public static VariableDeclarationStatement newInstance(Token token, VariableDeclaration o, Terminal n0) {
    return new VariableDeclarationStatement(token, o.type, o.name, o.initExpr);
  }

}
