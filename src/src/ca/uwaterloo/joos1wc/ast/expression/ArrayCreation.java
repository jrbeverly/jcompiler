package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.ArrayType;
import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ArrayCreation extends Expression {

  public final ArrayType type;
  public final Expression dimExpr;

  public ArrayCreation(Token token, ArrayType type, Expression dimExpr) {
    super(token);
    this.type = type;
    this.dimExpr = dimExpr;
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ArrayCreation newInstance(Token token, ArrayCreation o) {
    return new ArrayCreation(token, o.type, o.dimExpr);
  }

  // ArrayCreation -> NEW PrimitiveType Expression
  public static ArrayCreation newInstance(Token token, Terminal n0, PrimitiveType type, Expression dimExpr) {
    return new ArrayCreation(token, new ArrayType(type.token, type), dimExpr);
  }

  // ArrayCreation -> NEW Name Expression
  public static ArrayCreation newInstance(Token token, Terminal n0, SimpleType typeName, Expression dimExpr) {
    return new ArrayCreation(token, new ArrayType(typeName.token, typeName), dimExpr);
  }
}
