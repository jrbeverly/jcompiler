package ca.uwaterloo.joos1wc.ast.expression;

import java.text.ParseException;

import ca.uwaterloo.joos1wc.ast.ArrayType;
import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.Token;

public class CastExpression extends Expression {

  public final Type type;
  public final Expression expr;

  public CastExpression(Token token, Type type, Expression expr) {
    super(token);
    this.type = type;
    this.expr = expr;
  }

  public FormalType getFormalType() {
    return type.getFormalType();
  }

  @Override
  public Literal constantValue() throws ConstantEvaluationException, CastException {
    Literal literal = expr.constantValue();
    // if the constant value of expr is unknown, then this is too
    if (literal == null) {
      return null;
    }

    // but if it isn't, then we have some work to do
    FormalType formalType = this.type.getFormalType();
    if (formalType != null && formalType.code != null) {
      switch (formalType.code) {
      case BOOLEAN:
        return literal.castToBoolean();
      case INT:
        return literal.castToInt();
      case CHAR:
        return literal.castToChar();
      default:
        // for now, just return null
        // TODO: figure out what should actually happen here
        return null;
      }
    }
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static CastExpression newInstance(Token token, CastExpression o) {
    return new CastExpression(token, o.type, o.expr);
  }

  // See 19.1.5 at http://titanium.cs.berkeley.edu/doc/java-langspec-1.0/19.doc.html

  // CastExpression -> LPAREN PrimitiveType LBRACKET RBRACKET RPAREN Expression
  public static CastExpression newInstance(Token token, Terminal n0, PrimitiveType type, Terminal n2, Terminal n3,
      Terminal n4, Expression expr) {
    return new CastExpression(token, new ArrayType(type.token, type), expr);
  }

  // CastExpression -> LPAREN PrimitiveType RPAREN Expression
  public static CastExpression newInstance(Token token, Terminal n0, PrimitiveType type, Terminal n2, Expression expr) {
    return new CastExpression(token, type, expr);
  }

  // CastExpression -> LPAREN Expression RPAREN Expression
  public static CastExpression newInstance(Token token, Terminal n0, Expression type, Terminal n1, Expression expr)
      throws ParseException {
    if (!(type instanceof Name)) {
      throw new ParseException("Expected Name when casting and got expression", 0);
    }
    return new CastExpression(token, new SimpleType(type.token, (Name) type), expr);
  }

  // CastExpression -> LPAREN Name LBRACKET RBRACKET RPAREN Expression
  public static CastExpression newInstance(Token token, Terminal n0, Name type, Terminal n2, Terminal n3, Terminal n4,
      Expression expr) {
    return new CastExpression(token, new ArrayType(type.token, new SimpleType(type.token, (Name) type)), expr);
  }

}
