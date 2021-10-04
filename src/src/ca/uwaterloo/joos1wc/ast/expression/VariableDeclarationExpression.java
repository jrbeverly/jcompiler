package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.declaration.IVariableDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class VariableDeclarationExpression extends Expression implements INamedEntityNode, IVariableDeclaration {

  public final Type type;
  public final String name;
  public final Expression initExpr;

  private int stackOffset;

  public VariableDeclarationExpression(Token token, Type type, String name, Expression initExpr) {
    super(token);
    this.type = type;
    this.name = name;
    this.initExpr = initExpr;
  }

  @Override
  public Literal constantValue() {
    return null;
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
  public EntityType getEntityType() {
    return EntityType.VARIABLE;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public void setStackOffset(int stackOffset) {
    this.stackOffset = stackOffset;
  }

  @Override
  public int getStackOffset() {
    return stackOffset;
  }

  public static VariableDeclarationExpression newInstance(Token token, VariableDeclarationExpression o) {
    return new VariableDeclarationExpression(token, o.type, o.name, o.initExpr);
  }

  // ForInit -> LocalVariableDeclaration
  public static VariableDeclarationExpression newInstance(Token token, VariableDeclaration o) {
    return new VariableDeclarationExpression(token, o.type, o.name, o.initExpr);
  }

}
