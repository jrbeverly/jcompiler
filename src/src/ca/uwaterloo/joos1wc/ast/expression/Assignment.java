package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class Assignment extends Expression {

  public final Expression lhs, rhs;

  public Assignment(Token token, Expression lhs, Expression rhs) {
    super(token);
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static Assignment newInstance(Token token, Expression lhs, Terminal n0, Expression rhs) {
    return new Assignment(token, lhs, rhs);
  }

  public static Assignment newInstance(Token token, Assignment o) {
    return new Assignment(token, o.lhs, o.rhs);
  }

}
