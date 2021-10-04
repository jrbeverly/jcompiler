package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Null;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ReturnStatement extends Statement {
  public final Expression expr;

  public ReturnStatement(Token token, Expression expr) {
    super(token);
    this.expr = expr;
  }
  
  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }
  
  @Override
  public String toString() {
    return expr == null ? "Return" : String.format("Return (%s)", expr.toString());
  }
  
  // ReturnStatement -> RETURN Expression(opt) SEMICOLON
  public static ReturnStatement newInstance(Token token, Terminal n0, Expression expr, Terminal n1) {
    return new ReturnStatement(token, expr);
  }
  public static ReturnStatement newInstance(Token token, Terminal n0, Null expr, Terminal n1) {
    return new ReturnStatement(token, null);
  }
  public static ReturnStatement newInstance(Token token, ReturnStatement o) {
    return new ReturnStatement(token, o.expr);
  }

}
