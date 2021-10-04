package ca.uwaterloo.joos1wc.ast.literal;

import java.io.UnsupportedEncodingException;

import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.scanner.Token;

public class StringLiteral extends Literal {
  public static final String charset = "ASCII";

  public StringLiteral(Token token, byte[] literalValue) {
    super(token, literalValue);
  }
  
  @Override
  public AssemblyNode getAssembly() {
    // for now just return a comment node
    return AssemblyNode.comment("StringLiteral: " + this.valueOf());
  }
  
  @Override
  public BooleanLiteral castToBoolean() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "String", "boolean"), token);
  }

  @Override
  public CharLiteral castToChar() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "String", "char"), token);
  }
  
  @Override
  public IntLiteral castToInt() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "String", "int"), token);
  }
  
  @Override
  public StringLiteral castToString() throws CastException {
    return this;
  }
  
  public String valueOf() {
    if (this.literalValue != null) {
      try {
        return new String(this.literalValue, charset);
      } catch (UnsupportedEncodingException uee) {
        // throw it upstream (should never happen)
        throw new RuntimeException(uee);
      }
    }
    return null;
  }
  
  public Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException {
    String valueL = this.valueOf();
    String valueR = null;
    
    // all these comparisons are actually only legal if 
    // the operator is PLUS, so check for that first
    if (operator == InfixOperator.PLUS) { 
      if (toCompare instanceof BooleanLiteral) {
        valueR = "" + ((BooleanLiteral)toCompare).valueOf();
      
      } else if (toCompare instanceof IntLiteral) {
        valueR = "" + ((IntLiteral)toCompare).valueOf();
      
      } else if (toCompare instanceof CharLiteral) {
        valueR = "" + ((CharLiteral)toCompare).valueOf();
      
      } else if (toCompare instanceof StringLiteral) {
        valueR = ((StringLiteral)toCompare).valueOf();
      
      } else {
        // this is a weird one.  If the operator is PLUS, handle it like a string.
        valueR = NullLiteral.NULL;
      }
    } else {
      if (toCompare instanceof NullLiteral) {
        // If it isn't PLUS, then the only thing valid == or != and for that order 
        // is unimportant.  So flip the order and let NullLiteral do the work.
        return toCompare.compare(this, operator);
        
      } else if (toCompare instanceof StringLiteral) {
        // there's not a great way to get around doing this twice
        valueR = ((StringLiteral)toCompare).valueOf();
      
      }else {
        // Nulls are the only thing for which we can use operators other than +
        throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_COMPARISON_OPERATOR, operator.name(), "StringLiteral"));
      }
    }
    
    StringBuilder result = new StringBuilder(valueL);
    boolean boolResult = false;
    boolean isBool = false;
    
    switch(operator) {
    case PLUS:
      result = result.append(valueR);
      break;
    case DEQUAL:
      // since these are just literals at the moment, we know they're not equal
      boolResult = false;
      isBool = true;
      break;
    case NEQ:
      boolResult = true;
      isBool = true;
      break;
    default:
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_COMPARISON_OPERATOR, operator.name(), "StringLiteral"));
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
      // otherwise return a StringLiteral
      // this sets a null token TODO: maybe change to generating a new token
      return new StringLiteral(null, result.toString().getBytes());
    }
  }


  public static TypeDeclaration getStringType(PackageTable packages) {
    PackageTable jv = packages.getSubpackage("java");
    PackageTable lang = jv.getSubpackage("lang");
    return lang.getType("String");
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static StringLiteral newInstance(Token token, StringLiteral o) {
    return new StringLiteral(token, o.literalValue);
  }

}
