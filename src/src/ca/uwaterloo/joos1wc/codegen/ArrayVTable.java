package ca.uwaterloo.joos1wc.codegen;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;

public class ArrayVTable {
  private static final String ARRAY_VTABLE_LABEL = "NATIVE_ARRAY_VTABLE";
  private static final String ARRAY_NAME = "[]";
  private static final ArrayVTable singleton = new ArrayVTable();
  public static final int ARRAY_LENGTH_OFFSET = -4;

  private final VTable objectVTable = TypeDeclaration.getObjectType().getVTable();

  private ArrayVTable() {
  }

  public static ArrayVTable getVTable() {
    return singleton;
  }

  public String getLabel() {
    return ARRAY_VTABLE_LABEL;
  }

  public int getOffset(MethodDeclaration.Signature signature) {
    return objectVTable.getOffset(signature);
  }

  public int getOffset(String fieldName) {
    if (FieldDeclaration.ARRAY_LENGTH.equals(fieldName)) {
      return ARRAY_LENGTH_OFFSET;
    } else {
      return VTable.MISSING_FIELD_SEMAPHORE;
    }
  }

  public void addVTable(AssemblyNode node) {
    // add the class name
    String nameLabel = ARRAY_VTABLE_LABEL + VTable.CLASSNAME_SUFFIX;
    node.addChild(AssemblyNode.label(nameLabel));
    node.addChild(AssemblyNode.dd("" + ARRAY_NAME.length()));
    node.addChild(AssemblyNode.dbChar(ARRAY_NAME));

    String hierarchyTableLabel = ARRAY_VTABLE_LABEL + VTable.HIERARCHY_TABLE_SUFFIX;
    node.addChild(AssemblyNode.label(hierarchyTableLabel));
    Joos1Wc.TYPE_HIERARCHY_TABLE.addHierarchyTableSlice(node, TypeDeclaration.getObjectType());

    // Output class label and addresses of name, type hierarchy table
    node.addChild(AssemblyNode.label(ARRAY_VTABLE_LABEL));
    node.addChild(AssemblyNode.dd(nameLabel));
    node.addChild(AssemblyNode.dd(hierarchyTableLabel));
    node.addChild(AssemblyNode.DD, "no iface methods"); // add a null pointer for interface methods

    // then all the methods in order
    for (MethodDeclaration element : this.objectVTable.getMethods()) {
      node.addChild(AssemblyNode.dd(element.getGlobalName()));
      node.requires(element.getGlobalName());
    }
    node.addChild(AssemblyNode.comment("END vtable " + ARRAY_VTABLE_LABEL));

  }

}
