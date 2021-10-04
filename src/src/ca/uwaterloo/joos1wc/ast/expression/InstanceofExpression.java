package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class InstanceofExpression extends Expression {

  public final Expression expr;
  public final Type referenceType;

  public InstanceofExpression(Token token, Expression expr, Type referenceType) {
    super(token);
    this.expr = expr;
    this.referenceType = referenceType;
  }
  
  @Override
  public Literal constantValue() {
    // TODO: we might want or need to try to figure this out here
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static InstanceofExpression newInstance(Token token, InstanceofExpression o) {
    return new InstanceofExpression(token, o.expr, o.referenceType);
  }

  // InstanceofExpression -> Expression INSTANCEOF Type
  public static InstanceofExpression newInstance(Token token, Expression expr, Terminal n0, Type referenceType) {
    return new InstanceofExpression(token, expr, referenceType);
  }

}
