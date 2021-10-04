package ca.uwaterloo.joos1wc.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

public class HierarchyChecker {
  private final Diagnostics diagnostic;

  public HierarchyChecker(Diagnostics diagnostic) {
    this.diagnostic = diagnostic;

    runCheck(Joos1Wc.DEFAULT_PKG, new Checker() {
      @Override
      public void check(TypeDeclaration decl) {
        mapDeclaredMethods(decl);
      }
    });

    runCheck(Joos1Wc.DEFAULT_PKG, new Checker() {
      @Override
      public void check(TypeDeclaration decl) {
        checkCycles(decl);
      }
    });

    // Cycles mess with everything else, so short-circuit the checks if we find one
    if (!diagnostic.isEmpty()) {
      return;
    }

    runCheck(Joos1Wc.DEFAULT_PKG, new Checker() {
      @Override
      public void check(TypeDeclaration decl) {
        mapInheritedMethods(decl);
      }
    });

    runCheck(Joos1Wc.DEFAULT_PKG, new Checker() {
      @Override
      public void check(TypeDeclaration decl) {
        checkDefaultConstructor(decl);
      }
    });
  }

  private void runCheck(PackageTable pkg, Checker checker) {
    for (PackageTable subpackage : pkg.getSubpackages()) {
      runCheck(subpackage, checker);
    }
    for (TypeDeclaration typeDecl : pkg.getTypeDecls()) {
      checker.check(typeDecl);
    }
  }

  private void checkDefaultConstructor(TypeDeclaration typeDecl) {
    if (typeDecl.isClass && typeDecl.hasDefaultConstructor()) {
      TypeDeclaration superclass = typeDecl.getSuperclassDecl();
      if (superclass != null && !superclass.hasDefaultConstructor()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.NO_DEFAULT_CONSTRUCTOR, superclass.name,
            typeDecl.name)));
      }
    }
  }

  private void mapDeclaredMethods(TypeDeclaration typeDecl) {
    Map<MethodDeclaration.Signature, MethodDeclaration> declaredMethods = new HashMap<>();
    for (List<MethodDeclaration> methodList : typeDecl.getMethods()) {
      for (MethodDeclaration method : methodList) {
        if (method.isConstructor && !method.name.equals(typeDecl.name)) {
          diagnostic.add(new SemanticException(String.format(Exceptions.CONSTRUCTOR_MISMATCH, typeDecl.name)));
        }
        if (declaredMethods.containsKey(method.getSignature())) {
          if (method.isConstructor) {
            diagnostic.add(new SemanticException(String.format(Exceptions.CONSTRUCTOR_DUPLICATE, method.name)));
          } else {
            diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_DUPLICATE, method.name)));
          }
        } else {
          declaredMethods.put(method.getSignature(), method);
        }
      }
    }
    typeDecl.setDeclaredMethods(declaredMethods);
  }

  private void mapInheritedMethods(TypeDeclaration typeDecl) {
    Map<MethodDeclaration.Signature, MethodDeclaration> inheritedMethods = typeDecl.getInheritedMethods();

    if (inheritedMethods == null) {
      inheritedMethods = new HashMap<>();

      // Interface inherit from Object (JLS 9.2)
      if (!typeDecl.isClass && typeDecl.getSuperifaceDecls().size() == 0) {
        // TODO refactor check with other java.lang.Object references
        TypeDeclaration object = TypeDeclaration.getObjectType();
        inheritedMethods.putAll(object.getDeclaredMethods());
      }

      Collection<TypeDeclaration> ifaces = typeDecl.isClass ? typeDecl.getIfaceDecls() : typeDecl.getSuperifaceDecls();
      for (TypeDeclaration iface : ifaces) {
        mapInheritedMethods(iface);
        checkAndInherit(inheritedMethods, iface.getInheritedMethods());
      }

      // Check superclass last - overwrite with implementations
      if (typeDecl.isClass) {
        TypeDeclaration superclass = typeDecl.getSuperclassDecl();
        if (superclass != null) {
          mapInheritedMethods(superclass);
          checkAndInherit(inheritedMethods, superclass.getInheritedMethods());
        }
      }

      // Check own declared methods
      for (Map.Entry<MethodDeclaration.Signature, MethodDeclaration> entry : typeDecl.getDeclaredMethods().entrySet()) {
        MethodDeclaration declared = entry.getValue();
        MethodDeclaration overriden = inheritedMethods.get(entry.getKey());
        if (overriden != null && !overriden.isConstructor) {
          checkInheritedMethod(declared, overriden, true);
        }
        inheritedMethods.put(declared.getSignature(), declared);
      }

      // Check if concrete class has abstract methods
      if (typeDecl.isClass && !typeDecl.isAbstract()) {
        for (MethodDeclaration method : inheritedMethods.values()) {
          if (method.isReallyAbstract()) {
            MethodDeclaration declared = typeDecl.getDeclaredMethod(method.getSignature());
            if (declared == null || declared.isReallyAbstract()) {
              diagnostic.add(new SemanticException(String.format(Exceptions.ABSTRACT_NOT_IMPLEMENTED, method.name)));
            }
          }
        }
        for (MethodDeclaration method : typeDecl.getDeclaredMethods().values()) {
          if (method.isReallyAbstract()) {
            diagnostic.add(new SemanticException(String.format(Exceptions.ABSTRACT_NOT_IMPLEMENTED, method.name)));
          }
        }
      }

      typeDecl.setInheritedMethods(inheritedMethods);
    }
  }

  private void checkAndInherit(Map<MethodDeclaration.Signature, MethodDeclaration> current,
      Map<MethodDeclaration.Signature, MethodDeclaration> inherited) {
    for (Map.Entry<MethodDeclaration.Signature, MethodDeclaration> entry : inherited.entrySet()) {
      MethodDeclaration declared = entry.getValue();
      MethodDeclaration overriden = current.get(entry.getKey());
      if (overriden != null && declared != overriden) {
        checkInheritedMethod(declared, overriden, false);
      }
      // Only override if declared method is public (i.e. remember the most accessible version seen)
      if (overriden == null || !declared.isProtected()) {
        current.put(entry.getKey(), declared);
      }
    }
  }

  private void checkInheritedMethod(MethodDeclaration declared, MethodDeclaration overriden,
      boolean checkAbstractOverrides) {
    // Final?
    if (overriden.isFinal()) {
      diagnostic.add(new SemanticException(String.format(Exceptions.FINAL_OVERRIDEN, overriden.name)));
    }
    // TODO what about checkAbstractOverrides here?
    // Static/non-static?
    if (declared.isStatic() && !overriden.isStatic()) {
      diagnostic
          .add(new SemanticException(String.format(Exceptions.INSTANCE_METHOD_OVERRIDEN_BY_STATIC, declared.name)));
    } else if (!declared.isStatic() && overriden.isStatic()) {
      diagnostic
          .add(new SemanticException(String.format(Exceptions.STATIC_METHOD_OVERRIDEN_BY_INSTANCE, declared.name)));
    }
    // Public -> protected?
    // Only check when overriding with a class's own declared methods, or with a concrete superclass method
    if (declared.isProtected() && overriden.isPublic() && (checkAbstractOverrides || !declared.isAbstract())) {
      diagnostic.add(new SemanticException(String.format(Exceptions.CANNOT_REDUCE_VISIBILITY, declared.name)));
    }
    // Different return type?
    Type t1 = declared.getType(), t2 = overriden.getType();
    if (t1 != null || t2 != null) {
      if (t1 == null || t2 == null || !t1.getFormalType().equals(t2.getFormalType())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.CANNOT_CHANGE_RETURN_TYPE, declared.name)));
      }
    }
  }

  private void checkCycles(TypeDeclaration node) {
    // check for cycles in the hierarchy
    try {
      if (node.isClass) {
        // check for class extension cycles
        checkForCycles(node, new Adjacency() {
          @Override
          @SuppressWarnings("unchecked")
          public Collection<TypeDeclaration> adjacentNodes(TypeDeclaration node) {
            if (node.superclass == null) {
              return (Collection<TypeDeclaration>) Collections.EMPTY_SET;
            }
            return Collections.singleton(node.getSuperclassDecl());
          }
        });
      } else {
        // check for interface implements cycles
        checkForCycles(node, new Adjacency() {
          @Override
          public Collection<TypeDeclaration> adjacentNodes(TypeDeclaration node) {
            return node.getSuperifaceDecls();
          }
        });
      }

    } catch (SemanticException se) {
      diagnostic.add(se);
    }
  }

  /**
   * Interface that lets us make a generic cycle checking algorithm
   */
  private static interface Adjacency {
    public Collection<TypeDeclaration> adjacentNodes(TypeDeclaration node);
  }

  /**
   * Check for cycles in some kind of graph
   * 
   * @param node
   *          a node in the graph
   * @param tree
   *          an adjacency relationship for the graph
   * @throws SemanticException
   *           if a cycle is found
   */
  private void checkForCycles(TypeDeclaration node, Adjacency graph) throws SemanticException {
    checkForCyclesWorker(node, graph, new HashSet<TypeDeclaration>());
  }

  /**
   * Worker method for checking for cycles in some kind of graph
   * 
   * @param node
   *          the current state in the depth-first search of the traversal
   * @param graph
   *          an adjacency relationship for the graph
   * @param visited
   *          a set of visited nodes in the graph
   * @throws SemanticException
   *           if a cycle is found
   */
  private void checkForCyclesWorker(TypeDeclaration node, Adjacency graph, Set<TypeDeclaration> visited)
      throws SemanticException {

    // if we've seen the node before, and it matches the start node, then we have a cycle
    if (visited.contains(node)) {
      throw new SemanticException(String.format(Exceptions.CYCLE_IN_HIERARCHY, node.name));
    }

    visited.add(node);
    for (TypeDeclaration neighbour : graph.adjacentNodes(node)) {
      checkForCyclesWorker(neighbour, graph, visited);
    }
    visited.remove(node);
  }

  private interface Checker {
    public void check(TypeDeclaration decl);
  }

}
