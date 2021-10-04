package ca.uwaterloo.joos1wc.codegen;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.analysis.SymbolTable;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration.Signature;
import ca.uwaterloo.joos1wc.ast.literal.NullLiteral;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Condition;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Register;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;

public class CodeGenUtils {

  public static final int DWORD = 4;

  public static final String EXCEPTION = "__exception";
  public static final String MALLOC = "__malloc";

  public static final int BOOLEAN_TRUE = 0x1;

  public static final int BOOLEAN_FALSE = 0x0;

  private CodeGenUtils() {
  }

  public static Collection<AssemblyNode> getExpressionNodes(List<Expression> args) {
    AssemblyNode[] nodes = new AssemblyNode[args.size()];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = args.get(i).asm;
    }
    return Arrays.asList(nodes);
  }

  /**
   * The address of 'this' should already be on the top of the stack when callMethod is called, unless the method is
   * static.
   */
  public static void callMethod(AssemblyNode asm, MethodDeclaration method, Collection<AssemblyNode> nodes) {
    asm.addChild(AssemblyNode.comment("calling " + method.getGlobalName()));

    // Evaluate and push arguments on to stack
    int args = 0;
    for (AssemblyNode arg : nodes) {
      asm.addChild(arg);
      asm.addChild(AssemblyNode.push(Register.EAX));
      args++;
    }

    callMethod(asm, method, args);

    // Expect return value to be in EAX - clear arguments off stack
    asm.addChild(AssemblyNode.add(Register.ESP, DWORD * nodes.size()), String.format("clear %d args", nodes.size()));
  }

  public static void addNullCheck(AssemblyNode asm, Register reg) {
    asm.addChild(AssemblyNode.cmp(reg, 0));
    asm.requires(EXCEPTION);
    asm.addChild(AssemblyNode.j(Condition.E, EXCEPTION));
  }

  public static void callMethod(AssemblyNode asm, MethodDeclaration method, int numberOfArguments) {
    // Call method
    if (method.isStatic()) {
      String methodName = method.getGlobalName();
      asm.requires(methodName);
      asm.addChild(AssemblyNode.call(methodName));
    } else {
      TypeDeclaration type = method.getTypeDeclaration();

      int vtableOffset = CodeGenUtils.DWORD * numberOfArguments;
      // get the runtime vtable (same as this)
      asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.ESP, vtableOffset), "this");
      addNullCheck(asm, Register.EAX);
      // dereference
      asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EAX), "vtable");

      if (type.isClass) {
        // get the offset from the VTable if it's a class
        VTable vtable = type.getVTable();
        int offset = vtable.getOffset(method.getSignature());
        assert offset != VTable.MISSING_METHOD_SEMAPHORE;

        asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EAX, offset));

      } else {
        // it's an interface and we have to use the selector index table
        asm.addChild(AssemblyNode.comment(String.format("find method %s in interface %s", method.name, type.name)));

        int methodOffset = InterfaceSelectorTable.getSelectorTable().getOffset(method.getSignature());
        int selectorOffset = VTable.getSelectorIndexOffset();
        // get the selector table
        asm.addChild(AssemblyNode.movFromMem(Register.EBX, Register.EAX, selectorOffset), "selector table");
        // get the method
        asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EBX, methodOffset), method.name);
      }
      addNullCheck(asm, Register.EAX);
      asm.addChild(AssemblyNode.call(Register.EAX));
    }
  }

  public static void allocateObject(AssemblyNode asm, TypeDeclaration type) {
    assert asm != null && type != null;
    // get the size of block we need to allocate
    asm.addChild(AssemblyNode.mov(Register.EAX, type.getInstanceSize()));
    // call the runtime procedure __malloc
    asm.requires(CodeGenUtils.MALLOC);
    asm.addChild(AssemblyNode.call(CodeGenUtils.MALLOC));

    // put a reference to the vtable as the first element
    VTable vtable = type.getVTable();
    asm.requires(vtable.getLabel());
    asm.addChild(AssemblyNode.movToMem(Register.EAX, vtable.getLabel()));

    // eax has the address of the object
  }

  /*-
   * Layout of "Type[] array" (offset in DWORDs)
   * -3           -2                          -1       0               1         ...
   * primitive?   VTable(Type)/primitiveIdx   length   VTable(array)   array[0]  ...
   */
  public static void allocateArray(AssemblyNode asm, FormalType type, AssemblyNode sizeExpr) {
    asm.addChild(AssemblyNode.comment("ArrayCreation Start"));

    // Calculate and store array size for later
    asm.addChild(sizeExpr);
    asm.addChild(AssemblyNode.push(Register.EAX), "array size");

    // Calculate in-memory size (4 DWORD overhead)
    asm.addChild(AssemblyNode.add(Register.EAX, 0x04));
    asm.addChild(AssemblyNode.imul(Register.EAX, CodeGenUtils.DWORD));
    // Arrays are special, so we don't use the generic method for creating them
    asm.requires(CodeGenUtils.MALLOC);
    asm.addChild(AssemblyNode.call(CodeGenUtils.MALLOC));

    // Move address to correct offset (see layout above)
    asm.addChild(AssemblyNode.add(Register.EAX, 3 * DWORD));

    // -3: primitive?
    asm.addChild(AssemblyNode.movToMem(Register.EAX, type.isPrimitive() ? 1 : 0, -3 * DWORD), "-3: primitive?");

    // -2: component type vtable reference/index of primitive type
    if (!type.isPrimitive()) {
      String componentVtableLabel = type.decl.getVTable().getLabel();
      asm.requires(componentVtableLabel);
      asm.addChild(AssemblyNode.movToMem(Register.EAX, componentVtableLabel, -2 * DWORD), "-2: component type vtable");
    } else {
      asm.addChild(AssemblyNode.movToMem(Register.EAX, type.code.ordinal(), -2 * DWORD), "-2: " + type.code);
    }

    // -1: length
    asm.addChild(AssemblyNode.pop(Register.EBX), "array size");
    asm.addChild(AssemblyNode.movToMem(Register.EAX, Register.EBX, -1 * DWORD), "-1: length");

    // 0: array vtable reference
    String arrayVtableLabel = ArrayVTable.getVTable().getLabel();
    asm.requires(arrayVtableLabel);
    asm.addChild(AssemblyNode.movToMem(Register.EAX, arrayVtableLabel));

    asm.addChild(AssemblyNode.comment("ArrayCreation End"));
    // eax still has the address of the array
  }

  public static MethodDeclaration getZeroArgConstructor(TypeDeclaration type) {
    List<FormalType> args = Arrays.asList(new FormalType[] {});
    Signature sig = new Signature(true, type.name, args);
    return type.getDeclaredMethod(sig);
  }

  public static MethodDeclaration getStringConstructor(TypeDeclaration type) {
    List<FormalType> args = Arrays.asList(new FormalType[] { new FormalType(true, PrimitiveType.Code.CHAR) });
    Signature sig = new Signature(true, "String", args);
    return type.getDeclaredMethod(sig);
  }

  public static AssemblyNode getConstantAssembly(Expression expr) {
    Literal literal;
    try {
      literal = expr.constantValue();
    } catch (ConstantEvaluationException cee) {
      // for now just rethrow these as runtime exceptions
      throw new RuntimeException(cee);
    } catch (CastException ce) {
      throw new RuntimeException(ce);
    }

    if (literal != null) {
      if (literal instanceof StringLiteral) {
        AssemblyNode asm = new AssemblyNode();
        String value = ((StringLiteral) literal).valueOf();
        addStringLiteral(asm, StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG), value);
        return asm;
      }
      return literal.getAssembly();
    }
    return null;
  }

  public static void addStartBlock(List<TreeNode> sourceTrees) {
    CompilationUnit main = (CompilationUnit) sourceTrees.get(0);
    if (main.typeDecls.size() == 0) {
      return;
    }

    TypeDeclaration mainType = main.typeDecls.get(0);
    mainType.asm.addChild(AssemblyNode.label("_start"));

    // Call all static initialization methods
    for (TreeNode rootNode : sourceTrees) {
      for (TypeDeclaration typeDecl : ((CompilationUnit) rootNode).typeDecls) {
        if (typeDecl.isClass) {
          String label = classInitLabel(typeDecl);
          mainType.asm.requires(label);
          mainType.asm.addChild(AssemblyNode.call(label));
        }
      }
    }

    // Call test()
    MethodDeclaration mainMethod = mainType.getDeclaredMethod(new Signature(false, "test", Collections
        .<FormalType> emptyList()));
    if (mainMethod == null) {
      return;
    }
    mainType.asm.addChild(AssemblyNode.call(mainMethod.getGlobalName()));

    // Exit
    mainType.asm.addChild(AssemblyNode.mov(Register.EBX, Register.EAX));
    mainType.asm.addChild(AssemblyNode.mov(Register.EAX, "1"));
    mainType.asm.addChild(AssemblyNode.INT_EXIT);
  }

  public static int popStack(AssemblyNode parent, SymbolTable from, SymbolTable to) {
    int popped = 0;
    for (SymbolTable.Entry entry : from) {
      if (entry == to.head()) {
        break;
      }
      parent.addChild(AssemblyNode.pop(Register.EBX), "clear " + entry.key);
      popped++;
    }
    return popped;
  }

  public static String classInitLabel(TypeDeclaration node) {
    return String.format("%s$init", node.getGlobalName());
  }

  /**
   * The LHS qualifier should be in $eax when this is called
   */
  public static void addFieldAccess(AssemblyNode asm, FieldDeclaration fieldDecl, boolean addressExpected) {
    if (fieldDecl == FieldDeclaration.LENGTH_FIELD || !fieldDecl.isStatic()) {
      // Instance field access
      int offset;
      if (fieldDecl == FieldDeclaration.LENGTH_FIELD) {
        offset = ArrayVTable.getVTable().getOffset(fieldDecl.name);
      } else {
        TypeDeclaration type = fieldDecl.getTypeDeclaration();
        offset = type.getVTable().getOffset(fieldDecl.name);
      }
      if (addressExpected) {
        asm.addChild(AssemblyNode.add(Register.EAX, offset), "&" + fieldDecl.name);
      } else {
        asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EAX, offset), fieldDecl.name);
      }

    } else {
      // Static field access
      String name = fieldDecl.getGlobalName();
      asm.requires(name);
      if (addressExpected) {
        asm.addChild(AssemblyNode.mov(Register.EAX, name), "&" + fieldDecl.name);
      } else {
        asm.addChild(AssemblyNode.movFromMem(Register.EAX, name, 0), fieldDecl.name);
      }
    }
  }

  public static void addStringLiteral(AssemblyNode asm, TypeDeclaration CLASS_STRING, String value) {
    char[] chars = value.toCharArray();
    int size = chars.length;

    // / CONSTRUCTION OF CHAR ARRAY
    StringBuilder sb = new StringBuilder();
    for (char c : chars) {
      sb.append((int) c).append(" ");
    }
    asm.addChild(AssemblyNode.comment(sb.toString()));

    // we need to set the length of the char array
    AssemblyNode sizeOf = AssemblyNode.mov(Register.EAX, size);
    // allocate an array for the type
    allocateArray(asm, PrimitiveType.Code.CHAR.formalType, sizeOf);

    // set offset
    int offset = 1;

    // populate said array with chars
    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];

      // value of char is now in register
      asm.addChild(AssemblyNode.mov(Register.EBX, (int) ch));

      // set memory value to char value
      asm.addChild(AssemblyNode.movToMem(Register.EAX, Register.EBX, CodeGenUtils.DWORD * offset));

      // advance pointer in array forward
      offset++;
    }

    // push the array address onto the stack
    asm.addChild(AssemblyNode.push(Register.EAX)); // array ref

    // BEGIN STRING CONSTRUCTION
    // first allocate memory
    allocateObject(asm, CLASS_STRING);

    // pop the array pointer
    asm.addChild(AssemblyNode.pop(Register.EBX));
    // now push the string pointer onto the stack (this)
    asm.addChild(AssemblyNode.push(Register.EAX));
    // and the array pointer as the first param
    asm.addChild(AssemblyNode.push(Register.EBX));

    // get constructor
    MethodDeclaration constructor = CodeGenUtils.getStringConstructor(CLASS_STRING);

    // call the constructor (we have already set up the args
    callMethod(asm, constructor, 1);

    // clear char[] arg off the stack
    asm.addChild(AssemblyNode.pop(Register.EBX));

    // finally pop the address of the instance
    asm.addChild(AssemblyNode.pop(Register.EAX));
  }

  /*
   * Expects expr value to be in $eax
   */
  private static void safeToString(AssemblyNode asm, Expression expr) {
    Literal literal = null;
    try {
      literal = expr.constantValue();
    } catch (ConstantEvaluationException cee) {
      // for now just rethrow these as runtime exceptions
      throw new RuntimeException(cee);
    } catch (CastException ce) {
      throw new RuntimeException(ce);
    }
    if (literal != null && literal instanceof NullLiteral) {
      addStringLiteral(asm, StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG), "null");
    } else if (expr.expressionType.isReference()) {
      String toStringLabel = AssemblyNode.generateLabel("ToString");
      String endToStringLabel = AssemblyNode.generateLabel("EndToString");
      asm.addChild(AssemblyNode.cmp(Register.EAX, 0));
      asm.addChild(AssemblyNode.j(Condition.NE, toStringLabel), "not null");
      addStringLiteral(asm, StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG), "null");
      asm.addChild(AssemblyNode.jmp(endToStringLabel), "null");
      asm.addChild(AssemblyNode.label(toStringLabel));
      callToString(asm, StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG), expr);
      asm.addChild(AssemblyNode.label(endToStringLabel));
    } else {
      callToString(asm, StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG), expr);
    }
  }

  public static void performStringConcat(AssemblyNode node, TypeDeclaration CLASS_STRING, Expression lhs, Expression rhs) {
    List<FormalType> args = Arrays.asList(new FormalType[] { CLASS_STRING.formalType });
    MethodDeclaration concat = CLASS_STRING.getDeclaredMethod(new Signature(false, "concat", args));

    node.addChild(AssemblyNode.comment("Start String concatenation"));
    node.requires(concat.getGlobalName());

    // get value of lhs (push rhs first)
    node.addChild(AssemblyNode.push(Register.EBX), "rhs");
    safeToString(node, lhs);

    // pop rhs back off
    node.addChild(AssemblyNode.pop(Register.EBX));
    // get value of rhs (push lhs valueOf or toString result)
    node.addChild(AssemblyNode.push(Register.EAX), "String(lhs)");

    // then put rhs into EAX and get the value again
    node.addChild(AssemblyNode.mov(Register.EAX, Register.EBX), "rhs");
    safeToString(node, rhs);

    // rhs goes in eax and lhs goes in ebx
    node.addChild(AssemblyNode.pop(Register.EBX));

    // concat the values
    node.addChild(AssemblyNode.push(Register.EBX), "String(lhs)");
    node.addChild(AssemblyNode.push(Register.EAX), "String(rhs)");
    node.addChild(AssemblyNode.call(concat.getGlobalName()), "l.concat(r)");
    node.addChild(AssemblyNode.pop(Register.EBX));
    node.addChild(AssemblyNode.pop(Register.EBX));

    node.addChild(AssemblyNode.comment("End String concatenation"));
    // eax has the new string!
  }

  public static void callToString(AssemblyNode node, TypeDeclaration CLASS_STRING, Expression expr) {
    // it is a primitive
    if (expr.expressionType.isPrimitive()) {
      // We get valueOf to convert to string
      List<FormalType> args = Arrays.asList(new FormalType[] { expr.expressionType });
      MethodDeclaration valueOf = CLASS_STRING.getDeclaredMethod(new Signature(false, "valueOf", args));
      String valueOfLabel = valueOf.getGlobalName();

      // need valueOf method
      node.requires(valueOfLabel);

      // call method (don't push this because valueOf is static)
      node.addChild(AssemblyNode.push(Register.EAX), "arg");
      node.addChild(AssemblyNode.call(valueOfLabel), "valueOf(i)");

      // pop stack
      node.addChild(AssemblyNode.pop(Register.EDX));

    } else {
      // we need to get the toString method
      Signature toString = new Signature(false, "toString", Collections.<FormalType> emptyList());

      // since toString is an Object method, it's in the same place for every class
      // juse use the offset from String and the runtime vtable
      VTable vtable = CLASS_STRING.getVTable();

      // get the runtime vtable (eax is the instance, so it's a pointer to the vtable)
      // just dereference it
      node.addChild(AssemblyNode.movFromMem(Register.EBX, Register.EAX), "[vtable]");

      // push this
      node.addChild(AssemblyNode.push(Register.EAX), "this");

      // get method value to call
      node.addChild(AssemblyNode.lea(Register.EBX, Register.EBX, vtable.getOffset(toString)), "&toString");
      // dereference it again
      node.addChild(AssemblyNode.movFromMem(Register.EBX, Register.EBX), "toString");
      node.addChild(AssemblyNode.call(Register.EBX), "toString()");

      // pop this off the stack
      node.addChild(AssemblyNode.pop(Register.EDX));
    }
  }

  /**
   * Assumes that LHS is in $ebx and non-null. Places result in $al. Assumes $eax has been set to 0. Clobbers $ebx and
   * $ecx.
   */
  public static void addInstanceofCheck(AssemblyNode asm, FormalType rhsType, String endLabel) {
    if (!rhsType.isArray) {
      asm.addChild(AssemblyNode.movFromMem(Register.EBX, Register.EBX), "dereference vtable");
      checkHierarchyTable(asm, rhsType.decl, Register.EBX, Register.AL);
    } else {
      // Is LHS actually an array?
      String arrayVtableLabel = ArrayVTable.getVTable().getLabel();
      asm.requires(arrayVtableLabel);
      asm.addChild(AssemblyNode.movFromMem(Register.ECX, Register.EBX), "dereference array vtable");
      asm.addChild(AssemblyNode.cmp(Register.ECX, arrayVtableLabel));
      asm.addChild(AssemblyNode.j(Condition.NE, endLabel), "LHS not array");
      // Check component type
      asm.addChild(AssemblyNode.movFromMem(Register.ECX, Register.EBX, -3 * CodeGenUtils.DWORD));
      asm.addChild(AssemblyNode.cmp(Register.ECX, rhsType.code == null ? 0 : 1),
          "component type primitive/non-primitive");
      asm.addChild(AssemblyNode.j(Condition.NE, endLabel), "primitive/non-primitive mismatch");
      asm.addChild(AssemblyNode.movFromMem(Register.ECX, Register.EBX, -2 * CodeGenUtils.DWORD));
      if (rhsType.code != null) {
        // Primitive: check primitive index
        asm.addChild(AssemblyNode.cmp(Register.ECX, rhsType.code.ordinal()));
        asm.addChild(AssemblyNode.set(Condition.E, Register.AL));
      } else {
        // Reference: check vtable entry
        // Component type vtable address is in $ecx
        checkHierarchyTable(asm, rhsType.decl, Register.ECX, Register.AL);
      }
    }
  }

  /**
   * Clobbers $ebx. Assumes vtable is at address stored at $vtableReg.
   */
  private static void checkHierarchyTable(AssemblyNode asm, TypeDeclaration typeDecl, Register vtableReg,
      Register resultReg) {
    asm.addChild(AssemblyNode.movFromMem(Register.EBX, vtableReg, VTable.HIERARCHY_TABLE_OFFSET),
        "dereference hierarchy table");
    asm.addChild(AssemblyNode.movFromMem(resultReg, Register.EBX, Joos1Wc.TYPE_HIERARCHY_TABLE.getIndex(typeDecl)));
  }

}
