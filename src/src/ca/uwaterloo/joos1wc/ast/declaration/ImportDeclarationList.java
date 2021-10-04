package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ImportDeclarationList extends ASTNode {

  private List<ImportDeclaration> importDeclarations;

  public ImportDeclarationList(Token token, List<ImportDeclaration> importDeclarations) {
    super(token);
    this.importDeclarations = importDeclarations;
  }

  public List<ImportDeclaration> getImportDeclarations() {
    return Collections.unmodifiableList(importDeclarations);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ImportDeclarationList newInstance(Token token, ImportDeclarationList o) {
    return new ImportDeclarationList(token, o.importDeclarations);
  }

  // ImportDeclarations -> epsilon
  public static ImportDeclarationList newInstance(Token token) {
    return new ImportDeclarationList(token, new ArrayList<ImportDeclaration>());
  }

  // ImportDeclarations -> ImportDeclaration
  public static ImportDeclarationList newInstance(Token token, ImportDeclaration node) {
    List<ImportDeclaration> importDeclarations = new ArrayList<ImportDeclaration>();
    importDeclarations.add(node);
    return new ImportDeclarationList(token, importDeclarations);
  }

  // ImportDeclarations -> ImportDeclarations ImportDeclaration
  public static ImportDeclarationList newInstance(Token token, ImportDeclarationList o, ImportDeclaration node) {
    List<ImportDeclaration> importDeclarations = o.importDeclarations;
    importDeclarations.add(node);
    return new ImportDeclarationList(token, importDeclarations);
  }

}
