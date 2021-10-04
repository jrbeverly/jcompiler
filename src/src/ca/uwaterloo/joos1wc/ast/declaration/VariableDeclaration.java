package ca.uwaterloo.joos1wc.ast.declaration;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.scanner.Token;

public class VariableDeclaration extends ASTNode implements INamedEntityNode, IVariableDeclaration {

  public final Type type;
  public final String name;
  public final Expression initExpr;

  private int stackOffset;

  public VariableDeclaration(Token token, Type type, String name, Expression initExpr) {
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

  // LocalVariableDeclaration -> Type ID EQ Expression
  public static VariableDeclaration newInstance(Token token, Type type, Terminal nameNode, Terminal n1,
      Expression initExpr) {
    return new VariableDeclaration(token, type, nameNode.token.getImage(), initExpr);
  }

  // FormalParameter -> Type ID
  public static VariableDeclaration newInstance(Token token, Type type, Terminal nameNode) {
    return new VariableDeclaration(token, type, nameNode.token.getImage(), null);
  }

}
