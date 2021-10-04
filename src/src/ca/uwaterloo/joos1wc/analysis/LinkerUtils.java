package ca.uwaterloo.joos1wc.analysis;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.SimpleName;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

public class LinkerUtils {

  /*
   * Utility class to facilitate code-sharing between type- and name-linking passes. This may not be strictly necessary,
   * but is the easiest way to refactor the code after I realized why we need separate passes for the two stages.
   */

  private LinkerUtils() {
  }

  static TypeDeclaration resolveType(CompilationUnit unit, SimpleName node) throws SemanticException {
    TypeDeclaration decl;

    // Type? (this, or single import)
    decl = unit.getTypeInNamespace(node.identifier);
    if (decl != null) {
      return decl;
    }

    // Type? (in local package)
    decl = unit.getLocalPackage().getType(node.identifier);
    if (decl != null) {
      return decl;
    }

    // Type? (on demand/automatic import)
    for (ImportDeclaration importDecl : unit.importDecls) {
      if (importDecl.isOnDemand) {
        TypeDeclaration imported = importDecl.onDemandPackage.getType(node.identifier);
        if (decl == null) {
          decl = imported;
        } else if (imported != null && imported != decl) {
          throw new SemanticException(String.format(Exceptions.AMBIGUOUS_ON_DEMAND_IMPORT, node.identifier));
        }
      }
    }
    PackageTable java = Joos1Wc.DEFAULT_PKG.getSubpackage("java");
    if (java != null) {
      PackageTable lang = java.getSubpackage("lang");
      if (lang != null) {
        TypeDeclaration imported = lang.getType(node.identifier);
        if (decl == null) {
          decl = imported;
        } else if (imported != null && imported != decl) {
          throw new SemanticException(String.format(Exceptions.AMBIGUOUS_ON_DEMAND_IMPORT, node.identifier));
        }
      }
    }

    return decl;
  }

  static PackageTable resolvePackage(SimpleName node) {
    return Joos1Wc.DEFAULT_PKG.getSubpackage(node.identifier);
  }

  static TypeDeclaration resolveType(QualifiedName node) {
    if (node.qualifier.packageTable != null) {
      return node.qualifier.packageTable.getType(node.identifier);
    }
    return null;
  }

  static PackageTable resolvePackage(QualifiedName node) {
    if (node.qualifier.packageTable != null) {
      return node.qualifier.packageTable.getSubpackage(node.identifier);
    }
    return null;
  }

  static FieldDeclaration resolveField(final TypeDeclaration thisDecl, final TypeDeclaration targetType, String name,
      EntityType lhsEntityType, boolean isInStaticScope) throws SemanticException {
    FieldDeclaration ret = null;
    TypeDeclaration type = targetType;
    while (ret == null && type != null) {
      ret = type.getField(name);
      if (ret == null) {
        type = type.superclass == null ? null : (TypeDeclaration) type.superclass.name.declNode;
      }
    }
    // If resolved:
    if (ret != null) {
      // Check access restrictions
      if (ret.isProtected() && thisDecl.getPackage() != type.getPackage()
          && (!thisDecl.isSubclassOf(type) || (lhsEntityType != EntityType.TYPE && !targetType.isSubclassOf(thisDecl)))) {
        throw new SemanticException(String.format(Exceptions.FIELD_PROTECTED_ACCESS, name));
      }
      // Check static/non-static access
      if (((lhsEntityType == null && !isInStaticScope) // implicit this in non-static scope
          || lhsEntityType == EntityType.FIELD || lhsEntityType == EntityType.VARIABLE) // explicit this
          && ret.isStatic()) {
        throw new SemanticException(String.format(Exceptions.NONSTATIC_STATIC_FIELD_ACCESS, name));
      } else if (((lhsEntityType == null && isInStaticScope) // implicit type, static scope
          || lhsEntityType == EntityType.TYPE) // explicit type
          && !ret.isStatic()) {
        throw new SemanticException(String.format(Exceptions.STATIC_NONSTATIC_FIELD_ACCESS, name));
      }
    }
    return ret;
  }

}
