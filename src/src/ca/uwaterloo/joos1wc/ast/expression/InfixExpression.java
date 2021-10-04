package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.Token;

public class InfixExpression extends Expression {

  public enum InfixOperator {
    MULT, DIV, MOD, PLUS, MINUS, LT, GT, LTE, GTE, DEQUAL, NEQ, BITAND, BITOR, LOGAND, LOGOR
  }

  public final Expression lhs, rhs;
  public final InfixOperator operator;

  public InfixExpression(Token token, Expression expr1, InfixOperator operator, Expression expr2) {
    super(token);
    this.lhs = expr1;
    this.operator = operator;
    this.rhs = expr2;
  }
  
  @Override
  public Literal constantValue() throws ConstantEvaluationException, CastException {
    Literal lhsLiteral = lhs.constantValue();
    Literal rhsLiteral = rhs.constantValue();
    
    // we only have a constant value if both sides of the expression have constant values
    if (lhsLiteral == null || rhsLiteral == null) {
      return null;
    }
    
    // now get the actual value we are working with
    // there's a lot of machinery in them there literals to do this...
    return lhsLiteral.compare(rhsLiteral, operator);
  }
  

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static InfixExpression newInstance(Token token, Expression expr1, Terminal opNode, Expression expr2) {
    return new InfixExpression(token, expr1, InfixOperator.valueOf(opNode.token.getKind().name()), expr2);
  }

  public static InfixExpression newInstance(Token token, InfixExpression o) {
    return new InfixExpression(token, o.lhs, o.operator, o.rhs);
  }

}
