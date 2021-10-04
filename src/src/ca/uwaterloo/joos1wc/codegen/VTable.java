package ca.uwaterloo.joos1wc.codegen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Register;

public class VTable {
  public static final int MISSING_FIELD_SEMAPHORE = -1;
  public static final int MISSING_METHOD_SEMAPHORE = -1;
  public static final int HIERARCHY_TABLE_OFFSET = CodeGenUtils.DWORD;

  public static final String CLASSNAME_SUFFIX = "$classname";
  public static final String IFACE_SUFFIX = "$ifacetable";
  public static final String HIERARCHY_TABLE_SUFFIX = "$hierarchy";
  private static final String INITIALIZER_SUFFIX = "$initializer";

  private String label;
  private String name;
  private final TypeDeclaration typeDecl;
  private final List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
  private final Map<MethodDeclaration.Signature, Integer> methodOffsets = new HashMap<MethodDeclaration.Signature, Integer>();
  private final List<FieldDeclaration> fields = new ArrayList<FieldDeclaration>();
  private final Map<String, Integer> fieldOffsets = new HashMap<String, Integer>();

  public VTable(TypeDeclaration typeDecl) {
    assert typeDecl.isClass;
    // add methods to the global SelectorTable
    InterfaceSelectorTable.getSelectorTable().addMethods(typeDecl.getDeclaredMethods().keySet(), typeDecl.getIfaceDecls(),
        typeDecl.getSuperclassDecl());
    
    TypeDeclaration superclass = typeDecl.getSuperclassDecl();
    if (superclass != null) {
      init(typeDecl.getGlobalName(), typeDecl.name, typeDecl.getFields(), 
          typeDecl.getDeclaredMethods(), typeDecl.getInheritedMethods(), superclass.getVTable());
      
    } else {
      // this should only be for Object
      assert "Cjava.lang.Object".equals(typeDecl.getGlobalName());
      
      init(typeDecl.getGlobalName(), typeDecl.name, typeDecl.getFields(), typeDecl.getDeclaredMethods().values());
    }
    this.typeDecl = typeDecl;
  }

  private void init(String label, String name, Collection<FieldDeclaration> fields,
      Collection<MethodDeclaration> methods) {
    this.label = label;
    this.name = name;
    // leave space for name label, hierarchy table and interface method table
    int offset = 3 * CodeGenUtils.DWORD;
    for (MethodDeclaration decl : methods) {
      if (!decl.isStatic()) {
        this.methods.add(decl);
        this.methodOffsets.put(decl.getSignature(), offset);
      }
      offset += CodeGenUtils.DWORD;
    }
    offset = CodeGenUtils.DWORD;
    for (FieldDeclaration field : fields) {
      if (!field.isStatic()) {
        this.fields.add(field);
        this.fieldOffsets.put(field.name, offset);
        offset += CodeGenUtils.DWORD;
      }
    }
  }

  private void init(String label, String name, Collection<FieldDeclaration> fields, 
      Map<MethodDeclaration.Signature, MethodDeclaration> declaredMethods,
      Map<MethodDeclaration.Signature, MethodDeclaration> inheritedMethods, VTable table) {
    this.label = label;
    this.name = name;
    // leave space for name label, hierarchy table and interface method table
    int offset = 3 * CodeGenUtils.DWORD;

    // temporary data structure to keep track of what methods still need
    // added to the VTable after handling inheritance
    Set<MethodDeclaration.Signature> handled = new HashSet<MethodDeclaration.Signature>();

    // iterate through the VTable so we preserve its order
    for (MethodDeclaration method : table.getMethods()) {
      // the method is redefined, so use the local definition
      if (declaredMethods.containsKey(method.getSignature())) {
        this.methods.add(declaredMethods.get(method.getSignature()));
        // keep track of when we use methods from the declared map
        handled.add(method.getSignature());
      } else {
        // the method isn't redefined, so use the definition from the VTable
        this.methods.add(method);
      }
      this.methodOffsets.put(method.getSignature(), offset);
      offset += CodeGenUtils.DWORD;
    }

    // iterate over the declared methods and add the ones we haven't yet handled
    for (Map.Entry<MethodDeclaration.Signature, MethodDeclaration> entry : declaredMethods.entrySet()) {
      // handled contains the methods that were overrides
      if (!handled.contains(entry.getKey()) && !entry.getValue().isStatic()) {
        this.methods.add(entry.getValue());
        this.methodOffsets.put(entry.getKey(), offset);
        offset += CodeGenUtils.DWORD;
      }
    }

    // now handle fields
    // copy all the fields from the parent VTable, and set their offsets the same
    offset = CodeGenUtils.DWORD;
    for (FieldDeclaration field : table.getFields()) {
      this.fields.add(field);
      this.fieldOffsets.put(field.name, offset);
      offset += CodeGenUtils.DWORD;
    }

    // now add any new fields
    for (FieldDeclaration field : fields) {
      if (!field.isStatic()) {
        this.fields.add(field);
        this.fieldOffsets.put(field.name, offset);
        offset += CodeGenUtils.DWORD;
      }
    }
  }

  public String getLabel() {
    return this.label;
  }

  public String getInitializerLabel() {
    return this.label + INITIALIZER_SUFFIX;
  }

  public List<MethodDeclaration> getMethods() {
    return this.methods;
  }

  public List<FieldDeclaration> getFields() {
    return this.fields;
  }

  public int getOffset(MethodDeclaration.Signature signature) {
    if (this.methodOffsets.containsKey(signature)) {
      return this.methodOffsets.get(signature);
    } else {
      return MISSING_METHOD_SEMAPHORE;
    }
  }

  public int getOffset(String fieldName) {
    if (this.fieldOffsets.containsKey(fieldName)) {
      return this.fieldOffsets.get(fieldName);
    } else {
      return MISSING_FIELD_SEMAPHORE;
    }
  }
  
  public static int getSelectorIndexOffset() {
    return CodeGenUtils.DWORD * 2;
  }

  public void addVTable(AssemblyNode node) {
    // add the class name
    String nameLabel = this.label + CLASSNAME_SUFFIX;
    node.addChild(AssemblyNode.label(nameLabel));
    node.addChild(AssemblyNode.dd("" + this.name.length()));
    node.addChild(AssemblyNode.dbChar(this.name));

    String hierarchyTableLabel = this.label + HIERARCHY_TABLE_SUFFIX;
    node.addChild(AssemblyNode.label(hierarchyTableLabel));
    Joos1Wc.TYPE_HIERARCHY_TABLE.addHierarchyTableSlice(node, typeDecl);
    
    // add the iface table
    String ifaceTableLabel = this.label + IFACE_SUFFIX;
    node.addChild(AssemblyNode.label(ifaceTableLabel));
    InterfaceSelectorTable.getSelectorTable().addSelectorTable(node, this.methods);

    // Output class label and addresses of name, type hierarchy table and interface method table
    node.addChild(AssemblyNode.label(this.label));
    node.addChild(AssemblyNode.dd(nameLabel));
    node.addChild(AssemblyNode.dd(hierarchyTableLabel));
    node.addChild(AssemblyNode.dd(ifaceTableLabel));

    // then all the methods in order
    for (MethodDeclaration element : this.methods) {
      if (!element.isAbstract()) {
        node.addChild(AssemblyNode.dd(element.getGlobalName()));
        node.requires(element.getGlobalName());
      } else {
        node.addChild(AssemblyNode.DD, element.getGlobalName());
      }
    }
    node.addChild(AssemblyNode.comment("END vtable " + label));
  }

  public void addInitializer(AssemblyNode node) {
    // add a function that initializes the fields
    String initializerLabel = this.label + INITIALIZER_SUFFIX;
    node.addChild(AssemblyNode.comment(String.format("START Initializer for %s", this.name)));
    node.addChild(AssemblyNode.label(initializerLabel));
    // add setup stuff for a function
    node.addChild(AssemblyNode.push(Register.EBP));
    node.addChild(AssemblyNode.mov(Register.EBP, Register.ESP));

    // then run any initializers
    for (FieldDeclaration field : this.fields) {
      if (field.initExpr != null) {
        node.addChild(field.initExpr.asm);
        // This function has no parameters, so "this" needs to be at EBP - 2
        // more importantly, it needs to be there for the code for field initializers to work properly
        node.addChild(AssemblyNode.movFromMem(Register.EBX, Register.EBP, -(CodeGenUtils.DWORD)
                * CodeGenVisitor.DEFAULT_THIS_OFFSET), "this");
        node.addChild(AssemblyNode.lea(Register.EBX, Register.EBX, this.getOffset(field.name)), "this." + field.name);
        // store the value
        node.addChild(AssemblyNode.movToMem(Register.EBX, Register.EAX));
      }
    }
    // now return from the function
    node.addChild(AssemblyNode.LEAVE);
    node.addChild(AssemblyNode.RET);
    node.addChild(AssemblyNode.comment(String.format("END Initializer for %s", this.name)));
  }

}
