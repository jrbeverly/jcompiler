package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class EmptyStatement extends Statement {

  public EmptyStatement(Token token) {
    super(token);
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static EmptyStatement newInstance(Token token, EmptyStatement o) {
    return new EmptyStatement(token);
  }

  // StatementWithoutTrailingSubstatement -> ;
  public static EmptyStatement newInstance(Token token, Terminal n0) {
    return new EmptyStatement(token);
  }

}
