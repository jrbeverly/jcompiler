package ca.uwaterloo.joos1wc.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclarationList;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclarationList;
import ca.uwaterloo.joos1wc.scanner.Token;

public class CompilationUnit extends ASTNode {

  public final PackageDeclaration packageDecl;
  public final List<ImportDeclaration> importDecls;
  public final List<TypeDeclaration> typeDecls; // This doesn't have to be a list?

  // For convenience in semantic analysis
  private final Map<String, TypeDeclaration> typesInNamespace = new HashMap<>();

  public CompilationUnit(Token token, PackageDeclaration packageDecl, List<ImportDeclaration> importDecls,
      List<TypeDeclaration> typeDecls) {
    super(token);
    this.packageDecl = packageDecl;
    this.importDecls = importDecls;
    this.typeDecls = typeDecls;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public PackageTable getLocalPackage() {
    return packageDecl == null ? Joos1Wc.DEFAULT_PKG : packageDecl.packageTable;
  }

  public TypeDeclaration getTypeInNamespace(String name) {
    return typesInNamespace.get(name);
  }

  public void addTypeToNamespace(String name, TypeDeclaration decl) {
    typesInNamespace.put(name, decl);
  }

  public static CompilationUnit newInstance(Token token, PackageDeclaration packageDecl,
      ImportDeclarationList importDecls, TypeDeclarationList typeDecls) {
    return new CompilationUnit(token, packageDecl, importDecls.getImportDeclarations(), typeDecls.getTypeDeclarations());
  }

  public static CompilationUnit newInstance(Token token, Null n0, ImportDeclarationList importDecls,
      TypeDeclarationList typeDecls) {
    return new CompilationUnit(token, null, importDecls.getImportDeclarations(), typeDecls.getTypeDeclarations());
  }

}
