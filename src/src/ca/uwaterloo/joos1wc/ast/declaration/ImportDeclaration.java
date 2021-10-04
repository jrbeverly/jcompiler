package ca.uwaterloo.joos1wc.ast.declaration;

import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ImportDeclaration extends ASTNode implements INamedEntityNode {

  public final Name name;
  public final boolean isOnDemand;

  // For type linking - consider refactoring later
  public PackageTable onDemandPackage;
  public TypeDeclaration typeDecl;

  public ImportDeclaration(Token token, Name name, boolean isOnDemand) {
    super(token);
    this.name = name;
    this.isOnDemand = isOnDemand;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public String getName() {
    // TODO fix
    return name.identifier;
  }

  @Override
  public EntityType getEntityType() {
    return isOnDemand ? EntityType.PACKAGE : EntityType.TYPE;
  }

  @Override
  public Type getType() {
    return null;
  }

  // ImportDeclaration -> SingleTypeImportDeclaration
  // ImportDeclaration -> TypeImportOnDemandDeclaration
  public static ImportDeclaration newInstance(Token token, ImportDeclaration o) {
    return new ImportDeclaration(token, o.name, o.isOnDemand);
  }

  // SingleTypeImportDeclaration -> IMPORT Name ;
  public static ImportDeclaration newInstance(Token token, Terminal n0, Name name, Terminal n1) {
    return new ImportDeclaration(token, name, false);
  }

  // TypeImportOnDemandDeclaration -> IMPORT Name . * ;
  public static ImportDeclaration newInstance(Token token, Terminal n0, Name name, Terminal n1, Terminal n2, Terminal n3) {
    return new ImportDeclaration(token, name, true);
  }

}
