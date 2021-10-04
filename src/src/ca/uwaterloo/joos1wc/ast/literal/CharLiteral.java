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

public class CharLiteral extends Literal {
  public static int MIN_CHAR = -(int)Math.pow(2, 15);
  public static int MAX_CHAR = (int)Math.pow(2, 15) - 1;

  public CharLiteral(Token token, byte[] literalValue) {
    super(token, literalValue);
    // extend the value to 16 bits if not done already
    if (literalValue.length == 1) {
      this.literalValue = new byte[] { 0x00, this.literalValue[0] };
    }
  }
  
  @Override
  public AssemblyNode getAssembly() {
    return AssemblyNode.mov(Register.EAX, (int)this.valueOf());
  }
  
  @Override
  public BooleanLiteral castToBoolean() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "char", "boolean"), token);
  }

  @Override
  public CharLiteral castToChar() throws CastException {
    return this;
  }
  
  @Override
  public IntLiteral castToInt() throws CastException {
    return new IntLiteral(this.token, new byte[] { 0x00, 0x00, this.literalValue[0], this.literalValue[1] });
  }
  
  @Override
  public StringLiteral castToString() throws CastException {
    return new StringLiteral(this.token, new byte[] { this.literalValue[1] });
  }
  
  public char valueOf() {
    return (char) (((this.literalValue[0] & 0xff) << 8) | (this.literalValue[1] & 0xff));
  }
  
  public Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException {
    // just treat them like ints and then cast back down
    IntLiteral intCast = this.castToInt();
    
    if (toCompare instanceof IntLiteral) {
      // result is cast up, since comparison with an int, so just return the result
      return intCast.compare(toCompare, operator);
      
    } else if (toCompare instanceof CharLiteral) {
      // use the logic in IntLiteral
      return intCast.compare(toCompare, operator);
      
    } else if (toCompare instanceof StringLiteral) {
      // cast to a string, then use that
      StringLiteral strCast = this.castToString();
      // result is cast to a string, so just return the result
      return strCast.compare(toCompare, operator);
      
    } else {
      // toCompare is a boolean or null
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_TYPE_COMPARISON, "CharLiteral", toCompare.getClass().getName()));
    }
    
  }


  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static CharLiteral newInstance(Token token, CharLiteral o) {
    return new CharLiteral(token, o.literalValue);
  }

}
