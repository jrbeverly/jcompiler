package ca.uwaterloo.joos1wc.ast.statement;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class Statement extends ASTNode {
  /**
   * Used in static analysis by ReachabilityVisitor
   */
  public boolean isReachable = false;
  public boolean canCompleteNormally = false;

  public Statement(Token token) {
    super(token);
  }

  public static Statement newInstance(Token token, Statement o) {
    if (o instanceof Block) return Block.newInstance(token, (Block) o);
    if (o instanceof EmptyStatement) return EmptyStatement.newInstance(token, (EmptyStatement)o);
    if (o instanceof ExpressionStatement) return ExpressionStatement.newInstance(token, (ExpressionStatement) o);
    if (o instanceof ForStatement) return ForStatement.newInstance(token, (ForStatement) o);
    if (o instanceof IfThenStatement) return IfThenStatement.newInstance(token, (IfThenStatement) o);
    if (o instanceof ReturnStatement) return ReturnStatement.newInstance(token, (ReturnStatement) o);
    if (o instanceof VariableDeclarationStatement) return VariableDeclarationStatement.newInstance(token, (VariableDeclarationStatement) o);
    if (o instanceof WhileStatement) return WhileStatement.newInstance(token, (WhileStatement) o);

    return null;
  }
  
  /**
  Statement:
    StatementWithoutTrailingSubstatement
    IfThenStatement
    IfThenElseStatement
    WhileStatement
    ForStatement
  StatementNoShortIf:
    StatementWithoutTrailingSubstatement
    IfThenElseStatementNoShortIf
    WhileStatementNoShortIf
    ForStatementNoShortIf
  StatementWithoutTrailingSubstatement:
    Block
    EmptyStatement
    ExpressionStatement
    ReturnStatement
  EmptyStatement:
    SEMICOLON
  **/

}
