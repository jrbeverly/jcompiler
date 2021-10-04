package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.Token;

public class QualifiedName extends Name {
  public final Name qualifier;

  public QualifiedName(Token token, Name qualifier, String identifier) {
    super(token, identifier);
    this.qualifier = qualifier;
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public String toString() {
    return String.format("QualifiedName (%s.%s)", qualifier.toString(), identifier);
  }

  public static QualifiedName newInstance(Token token, Name qualifier, Terminal n0, SimpleName name) {
    return new QualifiedName(token, qualifier, name.identifier);
  }

  public static QualifiedName newInstance(Token token, QualifiedName o) {
    return new QualifiedName(token, o.qualifier, o.identifier);
  }

}
