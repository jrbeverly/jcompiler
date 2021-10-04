package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class WhileStatement extends Statement {
  public final Expression expr;
  public final Statement statement;

  public WhileStatement(Token token, Expression expr, Statement statement) {
    super(token);
    this.expr = expr;
    this.statement = statement;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }
  
  // WhileStatement -> WHILE LPAREN Expression RPAREN Statement
  // WhileStatementNoShortIf -> WHILE LPAREN Expression RPAREN StatementNoShortIf
  public static WhileStatement newInstance(Token token, Terminal n0, Terminal n1, Expression expr, Terminal n2, Statement statement) {
    return new WhileStatement(token, expr, statement);
  }
  
  public static WhileStatement newInstance(Token token, WhileStatement o) {
    return new WhileStatement(token, o.expr, o.statement);
  }
}
