package ca.uwaterloo.joos1wc.ast;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.ast.expression.CastExpression;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.ParenthesizedExpression;
import ca.uwaterloo.joos1wc.ast.expression.PrefixExpression;
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.CharLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;

public class ExpressionTest {
  private ParenthesizedExpression parens;
  private PrefixExpression negative;
  private IntLiteral ten;
  private CharLiteral a;
  private CastExpression cast;
  private InfixExpression mult;
  
  private IntLiteral zero;
  private InfixExpression eq;
  private PrefixExpression bang;
  private ParenthesizedExpression parens2;
  
  private Type intType = new PrimitiveType(null, PrimitiveType.Code.INT);
  
  @Before
  public void setup() {
    // (-10 * (int)'a')
    ten = new IntLiteral(null, new byte[] { 0x00, 0x00, 0x00, 0x0a });
    a = new CharLiteral(null, new byte[] { 'a' });
    cast = new CastExpression(null, intType, a);
    negative = new PrefixExpression(null, PrefixExpression.PrefixOperator.MINUS, ten);
    mult = new InfixExpression(null, cast, InfixExpression.InfixOperator.MULT, negative);
    parens = new ParenthesizedExpression(null, mult);
    
    // !(0 == (-10 * 'a'))
    zero = new IntLiteral(null, new byte[] { 0x00, 0x00, 0x00, 0x00 });
    eq = new InfixExpression(null, parens, InfixExpression.InfixOperator.DEQUAL, zero);
    parens2 = new ParenthesizedExpression(null, eq);
    bang = new PrefixExpression(null, PrefixExpression.PrefixOperator.BANG, parens2);
  }
  
  @Test
  public void constantValueInt() throws ConstantEvaluationException, CastException {
    Literal result = parens.constantValue();
    Assert.assertNotNull("shouldn't be null", result);
    Assert.assertTrue("should be an IntLiteral", result instanceof IntLiteral);
    IntLiteral intResult = (IntLiteral)result;
    Assert.assertEquals("wrong computed value", -970, intResult.valueOf());
  }
  
  @Test
  public void constantValueBool() throws ConstantEvaluationException, CastException {
    Literal result = bang.constantValue();
    Assert.assertNotNull("shouldn't be null", result);
    Assert.assertTrue("should be a BooleanLiteral", result instanceof BooleanLiteral);
    BooleanLiteral boolResult = (BooleanLiteral)result;
    Assert.assertTrue("wrong computed value", boolResult.valueOf());
  }
}
