package ca.uwaterloo.joos1wc.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.declaration.BodyDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.VariableDeclarationExpression;
import ca.uwaterloo.joos1wc.ast.statement.ForStatement;
import ca.uwaterloo.joos1wc.ast.statement.VariableDeclarationStatement;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

/**
 * This is a Visitor implementation for adding and the Symbol Tables to each node in the AST.
 */
public class SymbolVisitor extends RecursiveVisitor {

  private final Diagnostics diagnostic;

  /*
   * lastNodeInScope refers to the last visited node in the current scope. It is updated to the current node before
   * recursing and again to the current node after recursing in nodes that encompass a new scope (e.g. Block)
   */
  TreeNode lastNodeInScope = null;

  PackageTable currentPackage;

  public SymbolVisitor(Diagnostics diagnostic) {
    currentPackage = Joos1Wc.DEFAULT_PKG;
    this.diagnostic = diagnostic;
  }

  @Override
  public void visit(PackageDeclaration node) {
    node.accept(new RecursiveVisitor() {
      @Override
      public void postVisit(Name nameNode) {
        String name = nameNode.identifier;
        if (!currentPackage.isDefault() && currentPackage.getType(name) != null) {
          diagnostic.add(new SemanticException(String.format(Exceptions.PACKAGE_NAME_COLLISION, name,
              currentPackage.getFullName())));
        }
        PackageTable pt = currentPackage.getSubpackage(name);
        if (pt == null) {
          pt = currentPackage.addSubpackage(name);
        }
        currentPackage = pt;
      }
    });
    node.packageTable = currentPackage;
  }

  @Override
  public void preVisit(CompilationUnit node) {
    // bottom of the symbol table "stack"
    node.symbols = new SymbolTable();
    lastNodeInScope = node;
  }

  /**
   * The default action is to just inherit the symbol table of the last node in the current scope
   */
  @Override
  public void preVisit(TreeNode node) {
    assert lastNodeInScope != null;
    node.symbols = lastNodeInScope.symbols;
    lastNodeInScope = node;
  }

  @Override
  public void preVisit(TypeDeclaration node) {
    if ((!currentPackage.isDefault() && currentPackage.getSubpackage(node.name) != null)
        || currentPackage.getType(node.name) != null) {
      diagnostic.add(new SemanticException(String.format(Exceptions.PACKAGE_NAME_COLLISION, node.name,
          currentPackage.getFullName())));
    }
    currentPackage.addType(node.name, node);
    node.setPackage(currentPackage);

    // Preserve order of field declaration
    Map<String, FieldDeclaration> fields = new LinkedHashMap<>();
    Map<String, List<MethodDeclaration>> methods = new HashMap<>();
    for (BodyDeclaration bodyDecl : node.getBody()) {
      // Smelly, but isolated
      if (bodyDecl instanceof FieldDeclaration) {
        FieldDeclaration fieldDecl = (FieldDeclaration) bodyDecl;
        fieldDecl.setTypeDeclaration(node);
        if (fields.containsKey(fieldDecl.name)) {
          diagnostic.add(new SemanticException(String.format(Exceptions.DUPLICATE_FIELD, fieldDecl.name)));
        } else {
          fields.put(fieldDecl.name, fieldDecl);
        }
      } else {
        assert (bodyDecl instanceof MethodDeclaration);
        MethodDeclaration methodDecl = (MethodDeclaration) bodyDecl;
        methodDecl.setTypeDeclaration(node);
        List<MethodDeclaration> methodList = methods.get(methodDecl.name);
        if (methodList == null) {
          methodList = new ArrayList<>();
          methods.put(methodDecl.name, methodList);
        }
        methodList.add(methodDecl);
      }
    }
    node.setFields(fields);
    node.setMethods(methods);

    node.symbols = new SymbolTable(lastNodeInScope.symbols, node.name, node);
    lastNodeInScope = node;
  }

  /**
   * Only update lastNodeInScope post-traversal to indicate forward reference (legacy)
   */

  @Override
  public void preVisit(FieldDeclaration node) {
    node.symbols = new SymbolTable(lastNodeInScope.symbols, node.name, node);
  }

  @Override
  public void postVisit(FieldDeclaration node) {
    lastNodeInScope = node;
  }

  /**
   * RecursiveVisitor doesn't provide a straightforward way to insert operations between two children, so we just
   * override visit to start scope between the type and initExpr
   */

  @Override
  public void visit(VariableDeclaration node) {
    stack.push(node);
    node.type.accept(this);
    checkDuplicateLocalVar(node);
    node.symbols = new SymbolTable(lastNodeInScope.symbols, node.name, node);
    lastNodeInScope = node;
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
  }

  @Override
  public void visit(VariableDeclarationExpression node) {
    stack.push(node);
    node.type.accept(this);
    checkDuplicateLocalVar(node);
    node.symbols = new SymbolTable(lastNodeInScope.symbols, node.name, node);
    lastNodeInScope = node;
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
  }

  @Override
  public void visit(VariableDeclarationStatement node) {
    stack.push(node);
    node.type.accept(this);
    checkDuplicateLocalVar(node);
    node.symbols = new SymbolTable(lastNodeInScope.symbols, node.name, node);
    lastNodeInScope = node;
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
  }

  /**
   * Below here are the end-of-scope nodes that need to change what the last node in scope was.
   */
  @Override
  public void postVisit(Block node) {
    lastNodeInScope = node;
  }

  @Override
  public void postVisit(ForStatement node) {
    lastNodeInScope = node;
  }

  @Override
  public void postVisit(MethodDeclaration node) {
    lastNodeInScope = node;
  }

  @Override
  public void postVisit(TypeDeclaration node) {
    lastNodeInScope = node;
  }

  public PackageTable getPackage() {
    return currentPackage;
  }

  private void checkDuplicateLocalVar(INamedEntityNode node) {
    for (SymbolTable.Entry entry : lastNodeInScope.symbols) {
      if (entry.decl.getEntityType() == EntityType.VARIABLE && entry.key.equals(node.getName())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.DUPLICATE_LOCAL_VAR, node.getName())));
        break;
      }
    }
  }

}
