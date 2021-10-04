package ca.uwaterloo.joos1wc.analysis;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.ArrayType;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.Assignment;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.SimpleName;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

public class NameLinkerVisitor extends RecursiveVisitor {

  private final Diagnostics diagnostic;

  PackageTable localPackage;
  CompilationUnit unit;
  TypeDeclaration thisDecl;

  boolean inFieldDeclaration = false;
  boolean isInStaticScope = false;

  public NameLinkerVisitor(Diagnostics diagnostic) {
    // If we don't see a package declaration, we are in the default package
    this.localPackage = Joos1Wc.DEFAULT_PKG;
    this.diagnostic = diagnostic;
  }

  @Override
  public void visit(PackageDeclaration node) {
    localPackage = node.packageTable;
  }

  /*
   * Already resolved by TypeLinkerVisitor - do nothing here
   */
  @Override
  public void visit(ImportDeclaration node) {
  }

  @Override
  public void preVisit(FieldDeclaration node) {
    inFieldDeclaration = true;
    isInStaticScope = node.isStatic();
  }

  @Override
  public void postVisit(FieldDeclaration node) {
    inFieldDeclaration = false;
  }

  @Override
  public void preVisit(MethodDeclaration node) {
    isInStaticScope = node.isStatic();
  }

  @Override
  public void preVisit(CompilationUnit node) {
    unit = node;
    thisDecl = node.typeDecls.size() > 0 ? node.typeDecls.get(0) : null;
  }

  /*
   * Only applies to name nodes in the type/body, as the package and import declarations are handled separately.
   */
  @Override
  public void postVisit(SimpleName node) {
    if (node.declNode != null || node.packageTable != null) {
      return;
    }

    // Local variable?
    for (SymbolTable.Entry entry : node.symbols) {
      if (entry.decl.getEntityType() != EntityType.VARIABLE) {
        break;
      }
      if (entry.key.equals(node.identifier)) {
        node.declNode = entry.decl;
        // Error if we are in the initializer of the declaration
        for (TreeNode ancestor : stack) {
          if (ancestor == entry.decl) {
            diagnostic
                .add(new SemanticException(String.format(Exceptions.LOCAL_VARIABLE_IN_OWN_INITIALIZER, entry.key)));
          }
        }
        return;
      }
    }

    // Field?
    try {
      node.declNode = LinkerUtils.resolveField(thisDecl, thisDecl, node.identifier, null, isInStaticScope);
    } catch (SemanticException e) {
      diagnostic.add(e);
    }
    if (node.declNode != null) {
      // Check for forward reference
      if (thisDecl.getField(node.identifier) != null && isForwardReference(node)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.FORWARD_REFERENCE, node.identifier)));
      }
      return;
    }

    // Type?
    try {
      node.declNode = LinkerUtils.resolveType(unit, node);
    } catch (SemanticException e) {
      diagnostic.add(e);
    }
    if (node.declNode != null) {
      return;
    }

    // Package?
    node.packageTable = LinkerUtils.resolvePackage(node);
    if (node.packageTable == null) {
      diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
    }
  }

  /*
   * Only applies to name nodes in the type/body, as the package and import declarations are handled separately.
   */
  @Override
  public void postVisit(QualifiedName node) {
    if (node.qualifier.declNode == null && node.qualifier.packageTable == null) {
      return;
    }
    if (node.declNode != null || node.packageTable != null) {
      return;
    }

    // Qualifier is a package
    if (node.qualifier.packageTable != null) {
      // Type?
      node.declNode = LinkerUtils.resolveType(node);
      if (node.declNode != null) {
        return;
      }
      // Package?
      node.packageTable = LinkerUtils.resolvePackage(node);
      if (node.packageTable != null) {
        return;
      }
      // Neither - error
      diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
      return;
    }

    EntityType lhsEntityType = node.qualifier.declNode.getEntityType();
    switch (lhsEntityType) {
    case TYPE: {
      // If the qualifier is a type, we must be looking at a member (field) of the type
      TypeDeclaration typeDecl = (TypeDeclaration) node.qualifier.declNode;
      try {
        node.declNode = LinkerUtils.resolveField(thisDecl, typeDecl, node.identifier, lhsEntityType, isInStaticScope);
      } catch (SemanticException e) {
        diagnostic.add(e);
      }
      if (node.declNode != null) {
        return;
      }
      diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
      break;
    }

    case FIELD:
    case VARIABLE: {
      // If the qualifier is a field or variable, check its type
      // TODO for now, don't need to worry about inheriting from Object, which has no fields
      Type type = node.qualifier.declNode.getType();
      if (type instanceof ArrayType) {
        // Array - can only do .length
        if (node.identifier.equals(FieldDeclaration.ARRAY_LENGTH)) {
          node.declNode = FieldDeclaration.LENGTH_FIELD;
        } else {
          diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
        }
      } else if (type instanceof PrimitiveType) {
        // Primitive - no members
        diagnostic.add(new SemanticException(String.format(Exceptions.PRIMITIVE_NO_RESOLUTION, node.identifier)));
      } else if (type instanceof SimpleType) {
        // Reference - check fields
        TypeDeclaration typeDecl = (TypeDeclaration) ((SimpleType) type).name.declNode;
        try {
          node.declNode = LinkerUtils.resolveField(thisDecl, typeDecl, node.identifier, lhsEntityType, isInStaticScope);
        } catch (SemanticException e) {
          diagnostic.add(e);
        }
        if (node.declNode != null) {
          return;
        }
        diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
      }
      break;
    }

    default:
      break;
    }
  }

  private boolean isForwardReference(SimpleName node) {
    if (!inFieldDeclaration) {
      return false;
    }
    // Is the reference on the LHS of an assignment? If so, this isn't a forward reference
    // TODO we don't have to worry about field access, but what about array access?
    if (stack.peek() instanceof Assignment) {
      Assignment assignment = (Assignment) stack.peek();
      if (assignment.lhs == node) {
        return false;
      }
    }
    // Is the field found in the symbol table? If so, this isn't a forward reference
    for (SymbolTable.Entry entry : node.symbols) {
      if (entry.key.equals(node.declNode.getName()) && entry.decl.getEntityType() == EntityType.FIELD) {
        return false;
      }
    }
    return true;
  }

}
