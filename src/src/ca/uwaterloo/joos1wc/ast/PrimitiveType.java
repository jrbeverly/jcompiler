package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.scanner.Token;

public class PrimitiveType extends Type {

  public enum Code {
    BOOLEAN("Boolean"), BYTE("Byte"), SHORT("Short"), INT("Integer"), CHAR("Character");
    public final String boxedName;
    public final FormalType formalType;

    Code(String boxedName) {
      this.boxedName = boxedName;
      formalType = new FormalType(false, this);
    }

    public boolean isBoolean() {
      return this == BOOLEAN;
    }

    public boolean isNumeric() {
      return this == BYTE || this == INT || this == SHORT || this == CHAR;
    }

    public boolean isNumber() {
      return this == BYTE || this == INT || this == SHORT;
    }

    public TypeDeclaration getBoxedDeclaration() {
      return Joos1Wc.DEFAULT_PKG.getSubpackage("java").getSubpackage("lang").getType(boxedName);
    }
  }

  public final Code code;

  public PrimitiveType(Token token, Code code) {
    super(token);
    this.code = code;
  }

  public FormalType getFormalType() {
    return code.formalType;
  }

  public static boolean canTypeAssign(Code varType, Code initType) {
    switch (varType) {
    case BOOLEAN:
      return initType == Code.BOOLEAN;
    case BYTE:
      return initType == Code.BYTE;
    case SHORT:
      return initType == Code.BYTE || initType == Code.SHORT;
    case CHAR:
      return initType == Code.SHORT || initType == Code.CHAR;
    case INT:
      return initType != Code.BOOLEAN;
    default:
      return false;
    }
  }

  public static boolean canCast(Code assigned, Code init) {
    switch (assigned) {
    case BOOLEAN:
      return init == Code.BOOLEAN;
    case BYTE:
    case SHORT:
    case CHAR:
    case INT:
      return init != Code.BOOLEAN;

    default:
      return false;
    }
  }

  public static boolean canPerform(Code code, InfixOperator operator) {
    switch (code) {
    case BOOLEAN:
      switch (operator) {
      case BITAND:
      case BITOR:
      case DEQUAL:
      case GT:
      case GTE:
      case LOGAND:
      case LOGOR:
      case LT:
      case LTE:
      case NEQ:
        return true;
      case MINUS:
      case MOD:
      case MULT:
      case PLUS:
      case DIV:
      default:
        return false;
      }
    case CHAR:
    case INT:
    case SHORT:
    case BYTE:
      switch (operator) {
      case BITAND:
      case BITOR:
      case DEQUAL:
      case GT:
      case GTE:
      case LT:
      case LTE:
      case NEQ:
      case MINUS:
      case MOD:
      case MULT:
      case PLUS:
      case DIV:
        return true;
      case LOGAND:
      case LOGOR:
      default:
        return false;
      }
    }
    return false;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static PrimitiveType newInstance(Token token, PrimitiveType o) {
    return new PrimitiveType(token, o.code);
  }

  // PrimitiveType -> BOOLEAN|BYTE|SHORT|INT|CHAR
  public static PrimitiveType newInstance(Token token, Terminal node) {
    return new PrimitiveType(token, Code.valueOf(node.token.getKind().name()));
  }

}
