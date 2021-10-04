package ca.uwaterloo.joos1wc.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;

public class InterfaceSelectorTable {
  private List<MethodDeclaration.Signature> methodList;
  private HashMap<MethodDeclaration.Signature, Integer> methodMap;
  private static final InterfaceSelectorTable singleton = new InterfaceSelectorTable();

  private InterfaceSelectorTable() {
    methodList = new ArrayList<MethodDeclaration.Signature>();
    methodMap = new HashMap<MethodDeclaration.Signature, Integer>();
  }
  
  public static InterfaceSelectorTable getSelectorTable() {
    return singleton;
  }
  
  public int getOffset(MethodDeclaration.Signature signature) {
    assert methodMap.containsKey(signature);
    return methodMap.get(signature) * CodeGenUtils.DWORD;
  }
  
  public void addMethods(Set<MethodDeclaration.Signature> signatures, Collection<TypeDeclaration> interfaces, 
      TypeDeclaration superClass) {
    for (MethodDeclaration.Signature signature : signatures) {
      // if it's already in there, don't bother
      if (methodMap.containsKey(signature)) {
        continue;
      }

      for (TypeDeclaration iface : interfaces) {
        if (iface.getDeclaredMethod(signature) != null) {
          // it's part of an interface, so add it
          methodMap.put(signature, methodList.size());
          methodList.add(signature);
          break;
        } else {
          Collection<TypeDeclaration> supers = iface.getSuperifaceDecls();
          if (supers != null) {
            this.addMethods(signatures, supers, superClass);
          }
        }
      }
    }
    
    if (superClass != null) {
      this.addMethods(signatures, superClass.getIfaceDecls(), superClass.getSuperclassDecl());
      this.addMethods(superClass.getDeclaredMethods().keySet(), interfaces, superClass.getSuperclassDecl());
    }
  }
  
  public void addSelectorTable(AssemblyNode asm, List<MethodDeclaration> methodsToCheck) {
    // quick make an intersection set
    Map<MethodDeclaration.Signature, MethodDeclaration> intersection = new HashMap<MethodDeclaration.Signature, MethodDeclaration>();
    for (MethodDeclaration method : methodsToCheck) {
      if (methodMap.containsKey(method.getSignature())) {
        intersection.put(method.getSignature(), method);
      }
    }
    // if there is nothing here, just bail out
    if (intersection.isEmpty()) {
      return;
    }
    
    for (MethodDeclaration.Signature signature : methodList) {
      if (intersection.containsKey(signature)) {
        MethodDeclaration method = intersection.get(signature);
        asm.addChild(AssemblyNode.dd(method.getGlobalName()));
        asm.requires(method.getGlobalName());
      } else {
        // have to put something here
        asm.addChild(AssemblyNode.DD, signature.name());
      }
    }
  }

}
