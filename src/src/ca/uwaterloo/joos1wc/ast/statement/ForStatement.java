package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Null;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ForStatement extends Statement {

  public final Expression forInit; // statement expression list OR local variable declaration
  public final Expression condExpr;
  public final Expression forUpdate; // statement expression list
  public final Statement statement;

  public ForStatement(Token token, Expression forInit, Expression condExpr, Expression forUpdate,
      Statement statement) {
    super(token);
    this.forInit = forInit;
    this.condExpr = condExpr;
    this.forUpdate = forUpdate;
    this.statement = statement;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  // ForStatement -> FOR LPAREN ForInit(opt) SEMICOLON Expression(opt) SEMICOLON ForUpdate(opt) RPAREN Statement
  // ForStatementNoShortIf -> FOR LPAREN ForInit(opt) SEMICOLON Expression(opt) SEMICOLON ForUpdate(opt) RPAREN
  // StatementNoShortIf
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Expression forInit, Terminal n2,
      Expression expr, Terminal n3, Expression forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, forInit, expr, forUpdate, statement);
  }
  
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Null forInit, Terminal n2,
      Expression expr, Terminal n3, Expression forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, null, expr, forUpdate, statement);
  }
  
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Null forInit, Terminal n2,
      Null expr, Terminal n3, Expression forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, null, null, forUpdate, statement);
  }
  
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Null forInit, Terminal n2,
      Null expr, Terminal n3, Null forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, null, null, null, statement);
  }

  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Expression forInit, Terminal n2,
      Null expr, Terminal n3, Expression forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, forInit, null, forUpdate, statement);
  }
  
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Expression forInit, Terminal n2,
      Null expr, Terminal n3, Null forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, forInit, null, null, statement);
  }
  
  public static ForStatement newInstance(Token token, Terminal n0, Terminal n1, Expression forInit, Terminal n2,
      Expression expr, Terminal n3, Null forUpdate, Terminal n4, Statement statement) {
    return new ForStatement(token, forInit, expr, null, statement);
  }

  public static ForStatement newInstance(Token token, ForStatement o) {
    return new ForStatement(token, o.forInit, o.condExpr, o.forUpdate, o.statement);
  }

}
