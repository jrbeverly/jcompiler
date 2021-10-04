package ca.uwaterloo.joos1wc.ast;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.CharLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.ast.literal.NullLiteral;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;


public class LiteralTest {
  private BooleanLiteral boolTrue;
  private BooleanLiteral boolFalse;
  private CharLiteral charA;
  private CharLiteral charB;
  private CharLiteral char0;
  private CharLiteral charnl;
  private IntLiteral int0;
  private IntLiteral int97;
  private IntLiteral int256;
  private StringLiteral strFoo;
  private StringLiteral strBar;
  private NullLiteral null0;
  
  @Before
  public void setup() {
    // note that we're not checking anything to do with tokens here
    
    boolTrue = new BooleanLiteral(null, new byte[] { 0x01 });
    boolFalse = new BooleanLiteral(null, new byte[] { 0x00 });

    charA = new CharLiteral(null, new byte[] { (byte)'a' });
    charB = new CharLiteral(null, new byte[] { (byte)'b' });
    char0 = new CharLiteral(null, new byte[] { (byte)'0' });
    charnl = new CharLiteral(null, new byte[] { (byte)'\n' });
    
    int0 = new IntLiteral(null, getBytes(0));
    int97 = new IntLiteral(null, getBytes(97));
    int256 = new IntLiteral(null, getBytes(256));
    
    strFoo = new StringLiteral(null, "foo".getBytes());
    strBar = new StringLiteral(null, "bar".getBytes());
    
    null0 = new NullLiteral(null);
  }
  
  private byte[] getBytes(int from) {
    byte[] newValue = new byte[4];
    for (int i = 3; i >= 0; i--) {
      newValue[i] = (byte)(from & 0xFF);
      from >>>= 8;
    }
    return newValue;
  }
  
  @Test
  public void compareBooltoBool() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(boolTrue.compare(boolTrue, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be equal", testBool(boolTrue.compare(boolTrue, InfixOperator.NEQ)));
    Assert.assertTrue("and should be true", testBool(boolTrue.compare(boolTrue, InfixOperator.LOGAND)));
    Assert.assertFalse("or should be false", testBool(boolFalse.compare(boolFalse, InfixOperator.LOGOR)));


    Assert.assertTrue("should be equal", testBool(boolTrue.compare(boolFalse, InfixOperator.NEQ)));
    Assert.assertFalse("should not be equal", testBool(boolTrue.compare(boolFalse, InfixOperator.DEQUAL)));
    Assert.assertFalse("and should be false", testBool(boolFalse.compare(boolTrue, InfixOperator.LOGAND)));
    Assert.assertTrue("or should be true", testBool(boolFalse.compare(boolTrue, InfixOperator.LOGOR)));
  }
  
  @Test
  public void compareBooltoStr() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should cast to string", "truefoo", testStr(boolTrue.compare(strFoo, InfixOperator.PLUS)));
    Assert.assertEquals("should cast to string", "falsebar", testStr(boolFalse.compare(strBar, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareBoolIllegal() {
    testIllegalLiterals(boolTrue, Arrays.asList(int0, char0, null0));
    
    testIllegalOperators(boolTrue, Arrays.asList(InfixOperator.PLUS, InfixOperator.GT, InfixOperator.MOD));
  }
  
  @Test
  public void compareCharToChar() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(charA.compare(charA, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be equal", testBool(charA.compare(charB, InfixOperator.DEQUAL)));
    Assert.assertEquals("add is wrong", 107, testInt(charA.compare(charnl, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(charA.compare(charA, InfixOperator.NEQ)));
    Assert.assertTrue("should be less than", testBool(charA.compare(charB, InfixOperator.LT)));
    Assert.assertEquals("and is wrong", 96, testInt(charA.compare(charB, InfixOperator.BITAND)));
  }
  
  @Test
  public void compareCharToInt() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(charA.compare(int97, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be equal", testBool(char0.compare(int0, InfixOperator.DEQUAL)));
    Assert.assertEquals("add is wrong", 97, testInt(charA.compare(int0, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(charA.compare(int97, InfixOperator.NEQ)));
    Assert.assertTrue("should be less than", testBool(char0.compare(int256, InfixOperator.LT)));
    Assert.assertEquals("or is wrong", 98, testInt(charB.compare(int0, InfixOperator.BITOR)));
  }
  
  @Test
  public void compareCharToStr() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should cast to string", "afoo", testStr(charA.compare(strFoo, InfixOperator.PLUS)));
    Assert.assertEquals("should cast to string", "bbar", testStr(charB.compare(strBar, InfixOperator.PLUS)));
    Assert.assertEquals("should cast to string", "0foo", testStr(char0.compare(strFoo, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareCharIllegal() {
    testIllegalLiterals(charA, Arrays.asList(boolTrue, null0));
    
    testIllegalOperators(charA, Arrays.asList(InfixOperator.LOGAND, InfixOperator.LOGOR));
  }
  
  @Test
  public void compareIntToInt() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(int256.compare(int256, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be equal", testBool(int256.compare(int97, InfixOperator.DEQUAL)));
    Assert.assertEquals("add is wrong", 353, testInt(int256.compare(int97, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(int97.compare(int97, InfixOperator.NEQ)));
    Assert.assertTrue("should be less than", testBool(int97.compare(int256, InfixOperator.LT)));
    Assert.assertEquals("div is wrong", 2, testInt(int256.compare(int97, InfixOperator.DIV)));
  }
  
  @Test
  public void compareIntToChar() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(int97.compare(charA, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be equal", testBool(int0.compare(char0, InfixOperator.DEQUAL)));
    Assert.assertEquals("add is wrong", 145, testInt(int97.compare(char0, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(int97.compare(charA, InfixOperator.NEQ)));
    Assert.assertTrue("should be greater than", testBool(int256.compare(charB, InfixOperator.GT)));
    Assert.assertEquals("or is wrong", 98, testInt(int0.compare(charB, InfixOperator.BITOR)));
  }
  
  @Test
  public void compareIntToStr() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should cast to string", "97foo", testStr(int97.compare(strFoo, InfixOperator.PLUS)));
    Assert.assertEquals("should cast to string", "256bar", testStr(int256.compare(strBar, InfixOperator.PLUS)));
    Assert.assertEquals("should cast to string", "0foo", testStr(int0.compare(strFoo, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareIntIllegal() {
    testIllegalLiterals(charA, Arrays.asList(boolTrue, null0));
    
    testIllegalOperators(charA, Arrays.asList(InfixOperator.LOGAND, InfixOperator.LOGOR));
  }
  
  @Test
  public void compareStrToStr() throws ConstantEvaluationException, CastException {
    Assert.assertFalse("should not be equal", testBool(strFoo.compare(strFoo, InfixOperator.DEQUAL)));
    Assert.assertTrue("should not be euqal", testBool(strFoo.compare(strFoo, InfixOperator.NEQ)));
    Assert.assertEquals("should append", "foobar", testStr(strFoo.compare(strBar, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareStrToBool() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should append", "footrue", testStr(strFoo.compare(boolTrue, InfixOperator.PLUS)));
    Assert.assertEquals("should append", "barfalse", testStr(strBar.compare(boolFalse, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareStrToChar() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should append", "fooa", testStr(strFoo.compare(charA, InfixOperator.PLUS)));
    Assert.assertEquals("should append", "barb", testStr(strBar.compare(charB, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareStrToInt() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should append", "foo97", testStr(strFoo.compare(int97, InfixOperator.PLUS)));
    Assert.assertEquals("should append", "bar256", testStr(strBar.compare(int256, InfixOperator.PLUS)));
  }
  
  @Test
  public void compareStrToNull() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should append", "foonull", testStr(strFoo.compare(null0, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(strFoo.compare(null0, InfixOperator.DEQUAL)));
    Assert.assertTrue("should not be euqal", testBool(strFoo.compare(null0, InfixOperator.NEQ)));
  }
  
  @Test
  public void compareStrIllegal() {
    // note that we're using == for the operator here, so actually illegal for most types
    testIllegalLiterals(strFoo, Arrays.asList(boolTrue, char0, int0));
    // most operators are illegal, actually
    testIllegalOperators(strFoo, Arrays.asList(InfixOperator.GTE, InfixOperator.MULT, InfixOperator.BITAND, InfixOperator.LOGOR));
  }
  
  @Test
  public void compareNullToNull() throws ConstantEvaluationException, CastException {
    Assert.assertTrue("should be equal", testBool(null0.compare(null0, InfixOperator.DEQUAL)));
    Assert.assertFalse("should not be euqal", testBool(null0.compare(null0, InfixOperator.NEQ)));
  }
  
  @Test
  public void compareNullToStr() throws ConstantEvaluationException, CastException {
    Assert.assertEquals("should append", "nullfoo", testStr(null0.compare(strFoo, InfixOperator.PLUS)));
    Assert.assertFalse("should not be equal", testBool(null0.compare(strFoo, InfixOperator.DEQUAL)));
    Assert.assertTrue("should not be euqal", testBool(null0.compare(strFoo, InfixOperator.NEQ)));
  }
  
  @Test
  public void compareNullIllegal() {
    testIllegalLiterals(null0, Arrays.asList(boolTrue, char0, int0));
    // most operators are illegal, actually
    testIllegalOperators(null0, Arrays.asList(InfixOperator.PLUS, InfixOperator.GTE, InfixOperator.MULT, InfixOperator.BITAND, InfixOperator.LOGOR));
  }
  
  /**
   * Helper methods below here 
   */
  
  private boolean testBool(Literal lit) {
    return ((BooleanLiteral)lit).valueOf();
  }
  
  private int testInt(Literal lit) {
    return ((IntLiteral)lit).valueOf();
  }
  
  private String testStr(Literal lit) {
    return ((StringLiteral)lit).valueOf();
  }
  
  private void testIllegalLiterals(Literal test, List<Literal> lits) {
    for (Literal lit : lits) {
      boolean thrown = false;

      try {
        test.compare(lit, InfixOperator.DEQUAL);
      } catch (Exception e) {
        thrown = true;
      }
      Assert.assertTrue("exception should be thrown on illegal type", thrown);
    }
  }
  
  private void testIllegalOperators(Literal test, List<InfixOperator> ops) {
    for (InfixOperator op : ops) {
      boolean thrown = false;

      try {
        test.compare(test, op);
      } catch (Exception e) {
        thrown = true;
      }
      Assert.assertTrue("exception should be thrown on illegal operator " + op, thrown);
    }
  }
}