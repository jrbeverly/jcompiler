package ca.uwaterloo.joos1wc.ast;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.statement.Statement;
import ca.uwaterloo.joos1wc.scanner.Token;

public class Block extends Statement {

  public final List<Statement> statements;

  public Block(Token token, List<Statement> statements) {
    super(token);
    this.statements = statements;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  // BlockStatements -> BlockStatement
  // BlockStatements(opt) -> BlockStatement
  public static Block newInstance(Token token, Block o) {
    return new Block(token, o.statements);
  }

  // BlockStatements(opt) -> epsilon
  public static Block newInstance(Token token) {
    return new Block(token, new ArrayList<Statement>());
  }

  // Block -> { BlockStatements(opt) }
  public static Block newInstance(Token token, Terminal n0, Block o, Terminal n1) {
    return new Block(token, o.statements);
  }

  // BlockStatement -> LocalVariableDeclaration
  // BlockStatement -> Statement
  public static Block newInstance(Token token, Statement node) {
    List<Statement> statements = new ArrayList<Statement>();
    statements.add(node);
    return new Block(token, statements);
  }

  // BlockStatements -> BlockStatements BlockStatement
  public static Block newInstance(Token token, Block o, Statement node) {
    List<Statement> statements = o.statements;
    statements.add(node);
    return new Block(token, statements);
  }

}
