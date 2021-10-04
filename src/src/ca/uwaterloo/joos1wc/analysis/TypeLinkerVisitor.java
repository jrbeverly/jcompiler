package ca.uwaterloo.joos1wc.analysis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.SimpleName;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.JoosException;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

public class TypeLinkerVisitor extends RecursiveVisitor {

  private final Diagnostics diagnostic;

  CompilationUnit unit;
  TypeDeclaration thisDecl;

  boolean inFieldDeclaration = false;

  public TypeLinkerVisitor(Diagnostics diagnostic) {
    this.diagnostic = diagnostic;
  }

  @Override
  public void visit(ImportDeclaration node) {
    ImportVisitor iv = new ImportVisitor(node.isOnDemand);
    node.accept(iv);
    if (iv.error != null) {
      diagnostic.add(iv.error);
      return;
    }

    if (node.isOnDemand) {
      node.onDemandPackage = iv.currentPackage;
    } else {
      node.typeDecl = iv.typeDecl;
      // Check for collisions
      TypeDeclaration decl = unit.getTypeInNamespace(node.getName());
      if (decl == null) {
        unit.addTypeToNamespace(node.getName(), node.typeDecl);
      } else if (decl != node.typeDecl) {
        // Multiple single imports with the same name are allowed only if they refer to the same type
        diagnostic.add(new SemanticException(String.format(Exceptions.IMPORT_COLLISION, node.getName())));
      }
    }
  }

  @Override
  public void visit(SimpleType node) {
    TypeVisitor tv = new TypeVisitor();
    node.accept(tv);
  }

  /*
   * Use the field/method mapping constructed by SymbolVisitor to make sure we resolve fields before methods. (not sure
   * if this actually necessary)
   */
  @Override
  public void visit(TypeDeclaration node) {
    stack.push(node);

    if (node.isClass) {
      if (node.superclass != null) {
        node.superclass.accept(this);
        TypeDeclaration superDecl = (TypeDeclaration) node.superclass.name.declNode;
        node.setSuperclassDecl(superDecl);
        // make sure classes aren't extending interfaces
        if (superDecl.isInterface) {
          diagnostic.add(new SemanticException(String.format(Exceptions.CLASS_EXTENDS_INTERFACE, node.name,
              superDecl.name)));
        }
        if (superDecl.isFinal()) {
          diagnostic
              .add(new SemanticException(String.format(Exceptions.CLASS_EXTENDS_FINAL, node.name, superDecl.name)));
        }
      } else {
        // Inherit from Object
        // TODO refactor this and automatic import java.lang check
        TypeDeclaration objectDecl = TypeDeclaration.getObjectType();
        assert (objectDecl != null);
        if (node != objectDecl) {
          node.setSuperclassDecl(objectDecl);
        }
      }

      Set<TypeDeclaration> uniqueIfaces = new HashSet<TypeDeclaration>(node.ifaces.size());
      for (SimpleType child : node.ifaces) {
        child.accept(this);
        // make sure classes aren't implementing classes
        TypeDeclaration ifaceDecl = (TypeDeclaration) child.name.declNode;
        if (ifaceDecl != null && ifaceDecl.isClass) {
          diagnostic
              .add(new SemanticException(String.format(Exceptions.CLASS_IMPLEMENTS_CLASS, node.name, ifaceDecl.name)));
        }
        if (uniqueIfaces.contains(ifaceDecl)) {
          diagnostic
              .add(new SemanticException(String.format(Exceptions.IMPLEMENTS_REPEATED, node.name, ifaceDecl.name)));
        }
        uniqueIfaces.add(ifaceDecl);
      }
      node.setIfaceDecls(uniqueIfaces);

    } else {
      Set<TypeDeclaration> uniqueIfaces = new HashSet<TypeDeclaration>(node.superifaces.size());
      for (SimpleType child : node.superifaces) {
        child.accept(this);
        // make sure interfaces aren't extending classes
        TypeDeclaration ifaceDecl = (TypeDeclaration) child.name.declNode;
        if (ifaceDecl.isClass) {
          diagnostic.add(new SemanticException(String.format(Exceptions.INTERFACE_EXTENDS_CLASS, node.name,
              ifaceDecl.name)));
        }
        if (uniqueIfaces.contains(ifaceDecl)) {
          diagnostic.add(new SemanticException(String.format(Exceptions.EXTENDS_REPEATED, node.name, ifaceDecl.name)));
        }
        uniqueIfaces.add(ifaceDecl);
      }
      node.setSuperifaceDecls(uniqueIfaces);
    }

    for (FieldDeclaration fieldDecl : node.getFields()) {
      fieldDecl.accept(this);
    }
    for (List<MethodDeclaration> methodDecls : node.getMethods()) {
      for (MethodDeclaration methodDecl : methodDecls) {
        methodDecl.accept(this);
      }
    }

    stack.pop();
  }

  @Override
  public void preVisit(CompilationUnit node) {
    unit = node;
    thisDecl = node.typeDecls.size() > 0 ? node.typeDecls.get(0) : null;
    if (thisDecl != null) {
      unit.addTypeToNamespace(thisDecl.getName(), thisDecl);
    }
  }

  private static class ImportVisitor extends RecursiveVisitor {
    public PackageTable lastPackage, currentPackage;
    public TypeDeclaration typeDecl;
    private boolean isOnDemand;
    public JoosException error;

    public ImportVisitor(boolean isOnDemand) {
      currentPackage = Joos1Wc.DEFAULT_PKG;
      this.isOnDemand = isOnDemand;
    }

    @Override
    public void postVisit(Name node) {
      if (error != null) {
        return;
      }

      if (typeDecl != null) {
        // A prefix resolved to a type - error
        String fullname = lastPackage == null ? "" : lastPackage.getFullName() + ".";
        fullname += typeDecl.name;
        error = new SemanticException(String.format(Exceptions.TYPE_AS_PACKAGE_PREFIX, fullname));
        return;
      }

      // Try to find subpackage
      lastPackage = currentPackage;
      currentPackage = currentPackage.getSubpackage(node.identifier);

      if (currentPackage == null) {
        if (isOnDemand) {
          // Name did not resolve to subpackage - cannot be on-demand import
          String missingPackage = lastPackage == null ? "" : lastPackage.getFullName() + ".";
          missingPackage += node.identifier;
          error = new SemanticException(String.format(Exceptions.PACKAGE_NOT_FOUND, missingPackage));
        } else {
          // Find type in original subpackage
          typeDecl = lastPackage.getType(node.identifier);
          if (typeDecl == null) {
            error = new SemanticException(String.format(Exceptions.TYPE_NOT_FOUND, node.identifier,
                lastPackage == null ? "default package" : "package " + lastPackage.getFullName()));
          }
        }
      }
    }
  }

  private class TypeVisitor extends RecursiveVisitor {
    @Override
    public void postVisit(SimpleName node) {
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

    @Override
    public void postVisit(QualifiedName node) {
      // Qualifier must be a package
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
      }
      diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.identifier)));
    }
  }

}
