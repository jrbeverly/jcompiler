package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class FieldAccess extends Expression {

  public final Expression expr;
  public final String name;

  public FieldDeclaration declNode;

  public FieldAccess(Token token, Expression expr, String name) {
    super(token);
    this.expr = expr;
    this.name = name;
  }

  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static FieldAccess newInstance(Token token, FieldAccess o) {
    return new FieldAccess(token, o.expr, o.name);
  }

  // FieldAccess -> Primary DOT ID
  public static FieldAccess newInstance(Token token, Expression expr, Terminal n0, Terminal id) {
    return new FieldAccess(token, expr, id.token.getImage());
  }

}
