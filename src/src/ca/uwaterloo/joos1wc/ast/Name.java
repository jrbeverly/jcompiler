package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class Name extends Expression {

  public final String identifier;
  public INamedEntityNode declNode;
  public PackageTable packageTable;

  public Name(Token token, String identifier) {
    super(token);
    this.identifier = identifier;
  }

  public static Name newInstance(Token token, Name name) {
    if (name instanceof SimpleName) return SimpleName.newInstance(token, (SimpleName) name);
    if (name instanceof QualifiedName) return QualifiedName.newInstance(token, (QualifiedName) name);
    return null;
  }

}
