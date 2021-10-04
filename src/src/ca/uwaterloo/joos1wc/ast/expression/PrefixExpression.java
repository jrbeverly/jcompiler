package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.Token;

public class PrefixExpression extends Expression {

  public enum PrefixOperator {
    BANG, MINUS
  }

  public final PrefixOperator operator;
  public final Expression expr;

  public PrefixExpression(Token token, PrefixOperator operator, Expression expr) {
    super(token);
    // HACK: this is so we can weed ints that are out of range
    if (expr instanceof IntLiteral) {
      ((IntLiteral) expr).setNegative(operator == PrefixOperator.MINUS);
    }
    this.operator = operator;
    this.expr = expr;
  }
  
  @Override
  public Literal constantValue() throws ConstantEvaluationException, CastException {
    // get the constant value of whatever we're modifying
    Literal exprLiteral = expr.constantValue();
    // if it's not constant, then we aren't either
    if (exprLiteral == null) {
      return null;
    }
    // if it is constant, though, we might need to modify it
    
    // handle BANG
    if (exprLiteral instanceof BooleanLiteral && operator == PrefixOperator.BANG) {
      BooleanLiteral boolLiteral = (BooleanLiteral)exprLiteral;
      byte[] newValue = new byte[] {0x01};
      if (boolLiteral.valueOf()) {
        newValue[0] = 0x00;
      }
      // this sets a null token TODO: probably change to generating a new token
      return new BooleanLiteral(exprLiteral.token, newValue);
    }
    
    // handle MINUS
    if (exprLiteral instanceof IntLiteral && operator == PrefixOperator.MINUS) {
      IntLiteral intLiteral = (IntLiteral)exprLiteral;
      int value = -(intLiteral.valueOf());
      // otherwise return an IntLiteral
      byte[] newValue = new byte[IntLiteral.WIDTH];
      for (int i = IntLiteral.LS_BYTE; i >= 0; i--) {
        newValue[i] = (byte)(value & 0xFF);
        value >>>= 8;
      }
      // this sets a null token TODO: maybe change to generating a new token
      return new IntLiteral(exprLiteral.token, newValue);
    }
    
    return exprLiteral;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static PrefixExpression newInstance(Token token, Terminal opNode, Expression expr) {
    return new PrefixExpression(token, PrefixOperator.valueOf(opNode.token.getKind().name()), expr);
  }

  public static PrefixExpression newInstance(Token token, PrefixExpression o) {
    return new PrefixExpression(token, o.operator, o.expr);
  }

}
