package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class SimpleType extends Type {

  public final Name name;

  public SimpleType(Token token, Name name) {
    super(token);
    this.name = name;
  }

  public FormalType getFormalType() {
    assert (name.declNode != null && name.declNode instanceof TypeDeclaration);
    return ((TypeDeclaration) name.declNode).formalType;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static SimpleType newInstance(Token token, SimpleType o) {
    return new SimpleType(token, o.name);
  }

  // SimpleType -> epsilon
  public static SimpleType newInstance(Token token) {
    return new SimpleType(token, null);
  }

  // SimpleType -> Name
  public static SimpleType newInstance(Token token, Name name) {
    return new SimpleType(token, name);
  }

  // SimpleType -> EXTENDS SimpleType
  public static SimpleType newInstance(Token token, Terminal n0, SimpleType type) {
    return new SimpleType(token, type.name);
  }

}
