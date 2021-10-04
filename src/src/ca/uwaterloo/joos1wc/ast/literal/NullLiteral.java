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

public class NullLiteral extends Literal {
  public static final String NULL = "null";

  public NullLiteral(Token token) {
    super(token, null);
  }

  @Override
  public AssemblyNode getAssembly() {
    AssemblyNode ret = new AssemblyNode();
    ret.addChild(AssemblyNode.mov(Register.EAX, 0), "NULL");
    return ret;
  }

  @Override
  public BooleanLiteral castToBoolean() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "null", "boolean"), token);
  }

  @Override
  public CharLiteral castToChar() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "null", "char"), token);
  }

  @Override
  public IntLiteral castToInt() throws CastException {
    throw new CastException(String.format(Exceptions.CANNOT_CAST, "null", "int"), token);
  }

  @Override
  public StringLiteral castToString() throws CastException {
    return new StringLiteral(this.token, NULL.getBytes());
  }

  public Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException {
    boolean isNull = false;
    byte[] newValue = new byte[] { 0x00 };

    // just simplify the logic slightly by doing the instanceof check here
    if (toCompare instanceof NullLiteral) {
      isNull = true;
    } else if (toCompare instanceof StringLiteral) {
      // weird one. If the operator is plus, cast to a string. Otherwise continue.
      if (operator == InfixOperator.PLUS) {
        StringLiteral strCast = this.castToString();
        return strCast.compare(toCompare, operator);
      }
    } else {
      // toCompare is an int, char or boolean
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_TYPE_COMPARISON, "NullLiteral", toCompare
          .getClass().getName()));
    }

    switch (operator) {
    case DEQUAL:
      if (isNull)
        newValue[0] = 0x01;
      break;
    case NEQ:
      if (!isNull)
        newValue[0] = 0x01;
      break;
    default:
      throw new ConstantEvaluationException(String.format(Exceptions.ILLEGAL_COMPARISON_OPERATOR, operator.name(),
          "NullLiteral"));
    }

    // this sets a null token TODO: maybe change to generating a new token
    return new BooleanLiteral(null, newValue);
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static NullLiteral newInstance(Token token, NullLiteral o) {
    return new NullLiteral(token);
  }

}
