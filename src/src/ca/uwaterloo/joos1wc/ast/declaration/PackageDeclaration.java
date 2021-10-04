package ca.uwaterloo.joos1wc.ast.declaration;

import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class PackageDeclaration extends ASTNode {

  public final Name name;

  public PackageTable packageTable;

  public PackageDeclaration(Token token, Name name) {
    super(token);
    this.name = name;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  // PackageDeclaration(opt) -> PackageDeclaration
  public static PackageDeclaration newInstance(Token token, PackageDeclaration o) {
    return new PackageDeclaration(token, o.name);
  }

  // PackageDeclaration -> PACKAGE Name ;
  public static PackageDeclaration newInstance(Token token, Terminal n0, Name name, Terminal n1) {
    return new PackageDeclaration(token, name);
  }

}
