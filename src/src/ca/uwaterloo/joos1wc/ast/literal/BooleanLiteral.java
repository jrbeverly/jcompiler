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

public class BooleanLiteral extends Literal {
  private static final String FALSE = "false";
  private static final String TRUE = "true";

  public BooleanLiteral(Token token, byte[] literalValue) {
    super(token, literalValue);
  }
  
  @Override
  public AssemblyNode getAssembly() {
    return AssemblyNode.mov(Register.EAX, this.literalValue[0]);
  }
  
  @Override
  public BooleanLiteral castToBoolean() throws CastException {
    return this;
  }

  @Override
  public CharLiteral castToChar() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "boolean", "char"), token);
  }
  
  @Override
  public IntLiteral castToInt() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "boolean", "int"), token);
  }
  
  @Override
  public StringLiteral castToString() throws CastException {
    // figure out what the string representation of the boolean is
    String tf = FALSE;
    if (this.valueOf()) {
      tf = TRUE;
    }
    // cast up to a string
    return new StringLiteral(this.token, tf.getBytes());
  }
  
  public boolean valueOf() {
    return this.literalValue[0] == 1;
  }
  
  public Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException {
    boolean valueL = this.valueOf();
    boolean valueR = false;
    
    if (toCompare instanceof BooleanLiteral) {
      valueR = ((BooleanLiteral)toCompare).valueOf();
      
    } else if (toCompare instanceof StringLiteral) {
      // cast up to a string
      StringLiteral strCast = this.castToString();
      // then return that, since we need to return a string anyway
      return strCast.compare(toCompare, operator);
      
    } else {
      // toCompare is an int, char or null
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_TYPE_COMPARISON, "BooleanLiteral", toCompare.getClass().getName()));
    }
    
    boolean result = false;
    
    switch(operator) {
    case DEQUAL:
      result = valueL == valueR;
      break;
    case NEQ:
      result = valueL != valueR;
      break;
    case LOGAND:
      result = valueL && valueR;
      break;
    case LOGOR:
      result = valueL || valueR;
      break;
    case BITAND:
      result = valueL & valueR;
      break;
    case BITOR:
      result = valueL | valueR;
      break;
    default:
      // shouldn't happen
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_COMPARISON_OPERATOR, operator.name(), "BooleanLiteral"));
    }
    
    byte[] newValue = new byte[] {0x00};
    if (result) {
      newValue[0] = 0x01;
    }
    // this sets a null token TODO: maybe change to generating a new token
    return new BooleanLiteral(null, newValue);
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static BooleanLiteral newInstance(Token token, BooleanLiteral o) {
    return new BooleanLiteral(token, o.literalValue);
  }

}
