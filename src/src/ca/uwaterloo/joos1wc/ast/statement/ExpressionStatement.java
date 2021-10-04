package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ExpressionStatement extends Statement {
  public final Expression expr;

  /**
   * This is a wrapper to convert Expressions to Statements
   */
  public ExpressionStatement(Token token, Expression expr) {
    super(token);
    this.expr = expr;
  }
  
  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  // ExpressionStatement -> StatementExpression SEMICOLON
  public static ExpressionStatement newInstance(Token token, Expression expr, Terminal n0) {
    return new ExpressionStatement(token, expr);
  }
  
  public static ExpressionStatement newInstance(Token token, ExpressionStatement o) {
    return new ExpressionStatement(token, o.expr);
  }
}
