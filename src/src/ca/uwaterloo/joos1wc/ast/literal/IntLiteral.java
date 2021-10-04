package ca.uwaterloo.joos1wc.ast.literal;

import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Register;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.scanner.Token;

public class IntLiteral extends Literal {
  public static final int WIDTH = 4;
  public static final int LS_BYTE = 3;
  
  // is this value negated by a prefix operator
  private boolean negative;

  public IntLiteral(Token token, byte[] literalValue) {
    super(token, literalValue);
    negative = false;
  }
  
  @Override
  public AssemblyNode getAssembly() {
    return AssemblyNode.mov(Register.EAX, this.valueOf());
  }
  
  @Override
  public BooleanLiteral castToBoolean() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "int", "boolean"), token);
  }

  @Override
  public CharLiteral castToChar() throws CastException {
    int value = this.valueOf();
    // negatives have to be a little weird for char casting
    if (negative) {
      value = -value;
    }
    byte[] bytes = new byte[] { (byte)((value & 0x00ff) >> 8) , (byte)(value & 0xff) };
    
    return new CharLiteral(this.token, bytes);
  }
  
  @Override
  public IntLiteral castToInt() throws CastException {
    return this;
  }
  
  @Override
  public StringLiteral castToString() throws CastException {
    // note that casting to strings is non-trivial
    String strValue = "" + this.valueOf();
    // cast up to a string
    return new StringLiteral(this.token, strValue.getBytes());
  }
  
  public int valueOf() {
    int value = 0;
    for (int i = LS_BYTE; i >= 0; i--) {
      value |= ((this.literalValue[i] & 0xff) << (LS_BYTE - i) * 8);
    }
    return value;
  }
  
  public Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException {
    int valueL = this.valueOf();
    int valueR = 0;
    if (toCompare instanceof IntLiteral) {
      valueR = ((IntLiteral)toCompare).valueOf();
      
    } else if (toCompare instanceof CharLiteral) {
      valueR = ((CharLiteral)toCompare).valueOf();
      
    } else if (toCompare instanceof StringLiteral) {
      // cast here, since strings win the auto-casting race
      StringLiteral strCast = this.castToString();
      return strCast.compare(toCompare, operator);
      
    } else {
      // toCompare is a boolean or null
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_TYPE_COMPARISON, "IntLiteral", toCompare.getClass().getName()));
    }
    
    int result         = 0;
    boolean isBool     = false;
    boolean boolResult = false;
    
    switch(operator) {
    case MULT:
      result = valueL * valueR;
      break;
    case DIV:
      if (valueR == 0) {
        return null;
      }
      result = valueL / valueR;
      break;
    case MOD:
      result = valueL % valueR;
      break;
    case PLUS:
      result = valueL + valueR;
      break;
    case MINUS:
      result = valueL - valueR;
      break;
    case LT:
      boolResult = valueL < valueR;
      isBool = true;
      break;
    case GT: 
      boolResult = valueL > valueR;
      isBool = true;
      break;
    case LTE:
      boolResult = valueL <= valueR;
      isBool = true;
      break;
    case GTE:
      boolResult = valueL >= valueR;
      isBool = true;
      break;
    case DEQUAL:
      boolResult = valueL == valueR;
      isBool = true;
      break;
    case NEQ:
      boolResult = valueL != valueR;
      isBool = true;
      break;
    case BITAND:
      result = valueL & valueR;
      break;
    case BITOR:
      result = valueL | valueR;
      break;
    default:
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_COMPARISON_OPERATOR, operator.name(), "IntLiteral"));
    }
    
    // if the operation results in a boolean, return a BooleanLiteral
    if (isBool) {
      byte[] newValue = new byte[] {0x00};
      if (boolResult) {
        newValue[0] = 0x01;
      }
      // this sets a null token TODO: maybe change to generating a new token
      return new BooleanLiteral(null, newValue);
    
    } else {
      // otherwise return an IntLiteral
      byte[] newValue = new byte[WIDTH];
      for (int i = LS_BYTE; i >= 0; i--) {
        newValue[i] = (byte)(result & 0xFF);
        result >>>= 8;
      }
      // this sets a null token TODO: maybe change to generating a new token
      return new IntLiteral(null, newValue);
    }
  }

  /**
   * Because this is hacky and weird, we'll distinguish it by using accessors.
   * 
   * @param negative
   */
  public void setNegative(boolean negative) {
    this.negative = negative;
  }

  public boolean isNegative() {
    return this.negative;
  }

  public byte[] getLiteralValue() {
    return literalValue;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static IntLiteral newInstance(Token token, IntLiteral o) {
    return new IntLiteral(token, o.literalValue);
  }

}
