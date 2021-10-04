package ca.uwaterloo.joos1wc.codegen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;

public class TypeHierarchyTable {

  private Map<TypeDeclaration, Integer> typeDeclIndex = new LinkedHashMap<>();

  public TypeHierarchyTable(List<TreeNode> sourceTrees) {
    for (TreeNode root : sourceTrees) {
      for (TypeDeclaration typeDecl : ((CompilationUnit) root).typeDecls) {
        typeDeclIndex.put(typeDecl, typeDeclIndex.size());
      }
    }
  }

  public void addHierarchyTableSlice(AssemblyNode asm, TypeDeclaration typeDecl) {
    Set<TypeDeclaration> supertypes = typeDecl.getSupertypes();
    for (TypeDeclaration supertype : typeDeclIndex.keySet()) {
      asm.addChild(AssemblyNode.db(supertypes.contains(supertype) ? "0x01" : "0x00"), supertype.getCanonicalName());
    }
  }

  public int getIndex(TypeDeclaration typeDecl) {
    return typeDeclIndex.get(typeDecl);
  }

}
