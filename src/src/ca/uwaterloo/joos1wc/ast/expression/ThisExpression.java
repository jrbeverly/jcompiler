package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ThisExpression extends Expression {

  // TODO Qualified this?

  public ThisExpression(Token token) {
    super(token);
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ThisExpression newInstance(Token token, Terminal n0) {
    return new ThisExpression(token);
  }

  public static ThisExpression newInstance(Token token, ThisExpression o) {
    return new ThisExpression(token);
  }

}
