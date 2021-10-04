package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ParenthesizedExpression extends Expression {

  public final Expression expr;

  public ParenthesizedExpression(Token token, Expression expr) {
    super(token);
    this.expr = expr;
  }
  
  @Override
  public Literal constantValue() throws ConstantEvaluationException, CastException {
    return expr.constantValue();
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ParenthesizedExpression newInstance(Token token, ParenthesizedExpression o) {
    return new ParenthesizedExpression(token, o.expr);
  }

  // PrimaryNoNewArray -> ( Expression )
  // We need to do this to distinguish between "((Object)) a" (invalid) and "(Object) a" (valid)
  public static ParenthesizedExpression newInstance(Token token, Terminal n0, Expression expr, Terminal n1) {
    return new ParenthesizedExpression(token, expr);
  }

}
