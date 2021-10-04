package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.CharLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.ast.literal.NullLiteral;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class Literal extends Expression {

  protected byte[] literalValue;

  public Literal(Token token, byte[] literalValue) {
    super(token);
    this.literalValue = literalValue;
  }
  
  @Override
  public Literal constantValue() {
    return this;
  }
  
  public abstract Literal compare(Literal toCompare, InfixOperator operator) throws ConstantEvaluationException, CastException;
  public abstract BooleanLiteral castToBoolean() throws CastException;
  public abstract CharLiteral castToChar() throws CastException;
  public abstract IntLiteral castToInt() throws CastException;
  public abstract StringLiteral castToString() throws CastException;
  
  public abstract AssemblyNode getAssembly();

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static TypeDeclaration getObjectType(PackageTable packages) {
    PackageTable jv = packages.getSubpackage("java");
    PackageTable lang = jv.getSubpackage("lang");
    return lang.getType("Object");
  }

  public static Literal newInstance(Token token, Terminal node) {
    byte[] v = node.token.getLiteralValue();
    switch ((TerminalTokenKind) node.token.getKind()) {
    case INTLITERAL:
      return new IntLiteral(token, v);
    case TRUE:
      return new BooleanLiteral(token, new byte[] { 0x01 });
    case FALSE:
      return new BooleanLiteral(token, new byte[] { 0x00 });
    case CHARLITERAL:
      return new CharLiteral(token, v);
    case STRLITERAL:
      return new StringLiteral(token, v);
    case NULL:
      return new NullLiteral(token);
    default:
      return null;
    }
  }

  public static Literal newInstance(Token token, Literal o) {
    if (o instanceof IntLiteral)
      return IntLiteral.newInstance(token, (IntLiteral) o);
    if (o instanceof BooleanLiteral)
      return BooleanLiteral.newInstance(token, (BooleanLiteral) o);
    if (o instanceof CharLiteral)
      return CharLiteral.newInstance(token, (CharLiteral) o);
    if (o instanceof StringLiteral)
      return StringLiteral.newInstance(token, (StringLiteral) o);
    if (o instanceof NullLiteral)
      return NullLiteral.newInstance(token, (NullLiteral) o);
    return null;
  }

}
