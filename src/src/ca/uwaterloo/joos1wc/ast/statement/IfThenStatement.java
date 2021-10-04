package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class IfThenStatement extends Statement {
  public final Expression expr;
  public final Statement trueStatement;
  public final Statement falseStatement;
  
  public IfThenStatement(Token token, Expression expr, Statement trueStatement, Statement falseStatement) {
    super(token);
    this.expr = expr;
    this.trueStatement = trueStatement;
    this.falseStatement = falseStatement;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }
  
  // IfThenStatement -> IF LPAREN Expression RPAREN Statement
  // IfThenElseStatement -> IF LPAREN Expression RPAREN StatementNoShortIf ELSE Statement
  // IfThenElseStatementNoShortIf -> IF LPAREN Expression RPAREN StatementNoShortIf ELSE StatementNoShortIf
  public static IfThenStatement newInstance(Token token, Terminal n0, Terminal n1, Expression expr, Terminal n2, 
      Statement trueStatement) {
    return new IfThenStatement(token, expr, trueStatement, null);
  }
  
  public static IfThenStatement newInstance(Token token, Terminal n0, Terminal n1, Expression expr, Terminal n2, 
      Statement trueStatement, Terminal n3, Statement falseStatement) {
    return new IfThenStatement(token, expr, trueStatement, falseStatement);
  }
  
  public static IfThenStatement newInstance(Token token, IfThenStatement o) {
    return new IfThenStatement(token, o.expr, o.trueStatement, o.falseStatement);
  }
}
