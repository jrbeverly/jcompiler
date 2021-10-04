package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class ArrayType extends Type {

  public final Type type;

  public ArrayType(Token token, Type type) {
    super(token);
    this.type = type;
  }

  public FormalType getFormalType() {
    FormalType ft = type.getFormalType();
    return ft.code == null ? new FormalType(true, ft.decl) : new FormalType(true, ft.code);
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ArrayType newInstance(Token token, ArrayType o) {
    return new ArrayType(token, o.type);
  }

  // ArrayType -> PrimitiveType [ ]
  public static ArrayType newInstance(Token token, PrimitiveType type, Terminal n0, Terminal n1) {
    return new ArrayType(token, type);
  }

  // ArrayType -> Name [ ]
  public static ArrayType newInstance(Token token, Name name, Terminal n0, Terminal n1) {
    return new ArrayType(token, new SimpleType(name.token, name));
  }

}
