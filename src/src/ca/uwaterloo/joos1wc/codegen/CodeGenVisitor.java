package ca.uwaterloo.joos1wc.codegen;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.ArrayType;
import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.SimpleName;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.IVariableDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.ast.expression.ArrayAccess;
import ca.uwaterloo.joos1wc.ast.expression.ArrayCreation;
import ca.uwaterloo.joos1wc.ast.expression.Assignment;
import ca.uwaterloo.joos1wc.ast.expression.CastExpression;
import ca.uwaterloo.joos1wc.ast.expression.ClassInstanceCreation;
import ca.uwaterloo.joos1wc.ast.expression.FieldAccess;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.InstanceofExpression;
import ca.uwaterloo.joos1wc.ast.expression.MethodInvocation;
import ca.uwaterloo.joos1wc.ast.expression.ParenthesizedExpression;
import ca.uwaterloo.joos1wc.ast.expression.PrefixExpression;
import ca.uwaterloo.joos1wc.ast.expression.ThisExpression;
import ca.uwaterloo.joos1wc.ast.expression.VariableDeclarationExpression;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression.InfixOperator;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.ast.statement.EmptyStatement;
import ca.uwaterloo.joos1wc.ast.statement.ExpressionStatement;
import ca.uwaterloo.joos1wc.ast.statement.ForStatement;
import ca.uwaterloo.joos1wc.ast.statement.IfThenStatement;
import ca.uwaterloo.joos1wc.ast.statement.ReturnStatement;
import ca.uwaterloo.joos1wc.ast.statement.Statement;
import ca.uwaterloo.joos1wc.ast.statement.VariableDeclarationStatement;
import ca.uwaterloo.joos1wc.ast.statement.WhileStatement;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Condition;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode.Register;

public class CodeGenVisitor extends RecursiveVisitor {

  public static final int DEFAULT_THIS_OFFSET = -2;

  final TypeDeclaration CLASS_STRING = StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG);

  // we need a sane default so that field initializers know where to look
  int thisOffset = DEFAULT_THIS_OFFSET;
  int stackSize = 0;

  @Override
  public void visit(TypeDeclaration node) {
    // Run code gen on parent class first
    if (node.asm != null) {
      return;
    }
    if (node.superclass != null) {
      ((TypeDeclaration) node.superclass.name.declNode).accept(new CodeGenVisitor());
    }
    super.visit(node);
  }

  /**
   * This section handles setting the inherited attribute "addressExpected".
   * 
   * If you're implementing something and you need one of its children to give you an address instead of a value, you
   * need to add a preVisit and set that child's addressExpected attribute to true.
   */

  @Override
  public void preVisit(Assignment node) {
    node.lhs.addressExpected = true;
  }

  /**
   * This section handles actual code generation
   */

  @Override
  public void postVisit(ArrayAccess node) {
    node.asm = new AssemblyNode();
    node.asm.addChild(node.array.asm);
    // address of the array goes on the stack
    node.asm.addChild(AssemblyNode.push(Register.EAX));
    // get the array length for comparison
    int lengthOffset = ArrayVTable.getVTable().getOffset(FieldDeclaration.ARRAY_LENGTH);
    node.asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EAX, lengthOffset), node.array.token.getImage()
        + ".length");
    // push it onto the stack
    node.asm.addChild(AssemblyNode.push(Register.EAX));
    // now get the index
    node.asm.addChild(node.index.asm);
    // pop the length into ebx
    node.asm.addChild(AssemblyNode.pop(Register.EBX));
    // compare
    node.asm.addChild(AssemblyNode.cmp(Register.EAX, Register.EBX));
    // bail if the index is greater or equal
    node.asm.requires(CodeGenUtils.EXCEPTION);
    node.asm.addChild(AssemblyNode.j(Condition.GE, CodeGenUtils.EXCEPTION));
    // if we get here then we're okay - eax still has the index
    // add 1 because the first element, like a normal object, is the vtable
    node.asm.addChild(AssemblyNode.add(Register.EAX, 0x01));
    // multiply the index by 4 for the offset
    node.asm.addChild(AssemblyNode.imul(Register.EAX, CodeGenUtils.DWORD));
    node.asm.addChild(AssemblyNode.pop(Register.EBX));
    // add the index offset and the base address
    node.asm.addChild(AssemblyNode.add(Register.EAX, Register.EBX));
    if (!node.addressExpected) {
      // dereference
      node.asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EAX, 0));
    }
  }

  @Override
  public void postVisit(ArrayCreation node) {
    node.asm = new AssemblyNode();

    CodeGenUtils.allocateArray(node.asm, node.type.type.getFormalType(), node.dimExpr.asm);
  }

  @Override
  public void postVisit(ArrayType node) {
    // TODO remove?
    node.asm = node.type.asm;
  }

  @Override
  public void postVisit(Assignment node) {
    node.asm = new AssemblyNode();
    node.asm.addChild(node.lhs.asm);
    // eax should be the address where we want to store things
    node.asm.addChild(AssemblyNode.push(Register.EAX));
    node.asm.addChild(node.rhs.asm);
    // now eax should be the value we want to store
    node.asm.addChild(AssemblyNode.pop(Register.EBX));
    // store to it
    node.asm.addChild(AssemblyNode.movToMem(Register.EBX, Register.EAX), node.token.getImage());
    // not really sure what should be in eax after this
    // it shouldn't probably matter, but having the value we stored there
    // seems like more or less the right thing
  }

  @Override
  public void postVisit(Block node) {
    node.asm = new AssemblyNode();
    for (Statement stmt : node.statements) {
      node.asm.addChild(stmt.asm);
    }
    // Pop block declared variables off the stack
    if (node.statements.size() > 0) {
      stackSize -= CodeGenUtils.popStack(node.asm, node.statements.get(node.statements.size() - 1).symbols,
          node.symbols);
    }
  }

  @Override
  public void postVisit(CastExpression node) {
    // like a few others, this might be a constant expression
    AssemblyNode constant = CodeGenUtils.getConstantAssembly(node);
    if (constant != null) {
      node.asm = constant;
    } else {
      node.asm = new AssemblyNode();
      node.asm.addChild(node.expr.asm);
      FormalType exprType = node.expr.expressionType;
      FormalType castType = node.type.getFormalType();

      if (castType.isPrimitive()) {
        if (exprType.isPrimitive()) {
          switch (castType.code) {
          case BOOLEAN:
            // This should never happen
            if (exprType.code != PrimitiveType.Code.BOOLEAN) {
              throw new RuntimeException("Cannot cast to boolean.");
            }
            break;
          case BYTE:
            // no type is smaller so we're definitely narrowing
            node.asm.addChild(AssemblyNode.and(Register.EAX, 0xff));
            break;
          case CHAR:
          case SHORT:
            // we might be widening here
            if (exprType.code == PrimitiveType.Code.BYTE) {
              // widen from 8 to 16 bytes
              node.asm.addChild(AssemblyNode.CBW);
            }
            node.asm.addChild(AssemblyNode.and(Register.EAX, 0xffff));
            break;
          case INT:
            // definitely widening
            if (exprType.code == PrimitiveType.Code.BYTE) {
              // first widen from 8 to 16 bytes
              node.asm.addChild(AssemblyNode.CBW);
            }
            // widen from 16 to 32 bytes
            node.asm.addChild(AssemblyNode.CWDE);
            break;
          }
        } else {
          // This was causing J1_1_cast_multiplereferencearray to fail
          // Object a = null;
          // a = (Object)(int[])(Object)(Integer[])a;

          // if we're casting to a primitive, the expression had better be too
          // TODO: this is only necessary because of a bug somewhere that means we're
          // confused about the return type of a string charAt call
          // throw new RuntimeException("Can't cast to primitive from non-primitive: " + exprType.getGlobalName() +
          // " -> "
          // + formalType.getGlobalName());
        }
      } else {
        // Save reference to object
        node.asm.addChild(AssemblyNode.push(Register.EAX));

        // Prepare call to CodeGenUtils helper
        node.asm.addChild(AssemblyNode.mov(Register.EBX, Register.EAX));
        node.asm.addChild(AssemblyNode.mov(Register.EAX, 0));
        String endLabel = AssemblyNode.generateLabel("CastCheck");

        // Null? (automatically succeeds)
        node.asm.addChild(AssemblyNode.cmp(Register.EBX, 0));
        node.asm.addChild(AssemblyNode.set(Condition.E, Register.AL));
        node.asm.addChild(AssemblyNode.j(Condition.E, endLabel), "LHS is null");

        // Call CodeGenUtils helper
        CodeGenUtils.addInstanceofCheck(node.asm, castType, endLabel);
        node.asm.addChild(AssemblyNode.label(endLabel), node.token.getImage());

        // Check result and either throw exception or recover object reference on successful cast
        node.asm.addChild(AssemblyNode.cmp(Register.EAX, 0));
        node.asm.requires(CodeGenUtils.EXCEPTION);
        node.asm.addChild(AssemblyNode.j(Condition.E, CodeGenUtils.EXCEPTION));
        node.asm.addChild(AssemblyNode.pop(Register.EAX));
      }
    }
  }

  @Override
  public void postVisit(ClassInstanceCreation node) {
    // type declaration of the class we're instantiating
    TypeDeclaration type = node.classType.getFormalType().decl;

    node.asm = new AssemblyNode();
    // first allocate memory
    CodeGenUtils.allocateObject(node.asm, type);
    // push the address onto the stack
    node.asm.addChild(AssemblyNode.push(Register.EAX));

    // call the constructor
    Collection<AssemblyNode> nodeargs = CodeGenUtils.getExpressionNodes(node.args);
    CodeGenUtils.callMethod(node.asm, node.constructor, nodeargs);

    // finally pop the address of the instance
    node.asm.addChild(AssemblyNode.pop(Register.EAX));
  }

  @Override
  public void postVisit(EmptyStatement node) {
    node.asm = new AssemblyNode();
  }

  @Override
  public void postVisit(ExpressionStatement node) {
    node.asm = node.expr.asm;
  }

  @Override
  public void postVisit(FieldAccess node) {
    node.asm = new AssemblyNode();
    node.asm.addChild(node.expr.asm);

    CodeGenUtils.addFieldAccess(node.asm, node.declNode, node.addressExpected);
  }

  @Override
  public void postVisit(FieldDeclaration node) {
    node.asm = new AssemblyNode();
    if (node.initExpr != null) {
      node.asm.addChild(node.initExpr.asm);
    }
  }

  @Override
  public void postVisit(ForStatement node) {
    node.asm = new AssemblyNode();
    String startLabel = AssemblyNode.generateLabel("For");
    String endLabel = AssemblyNode.generateLabel("EndFor");

    if (node.forInit != null) {
      node.asm.addChild(node.forInit.asm);
    }
    node.asm.addChild(AssemblyNode.label(startLabel));

    if (node.condExpr != null) {
      node.asm.addChild(node.condExpr.asm);
      node.asm.addChild(AssemblyNode.cmp(Register.EAX, CodeGenUtils.BOOLEAN_TRUE));
      node.asm.addChild(AssemblyNode.j(Condition.NE, endLabel));
    }

    if (node.statement != null)
      node.asm.addChild(node.statement.asm);

    if (node.forUpdate != null) {
      node.asm.addChild(node.forUpdate.asm);
    }
    node.asm.addChild(AssemblyNode.jmp(startLabel));
    node.asm.addChild(AssemblyNode.label(endLabel));

    // Pop declared variables off the stack
    stackSize -= CodeGenUtils.popStack(node.asm, node.statement.symbols, node.symbols);
  }

  @Override
  public void postVisit(IfThenStatement node) {
    node.asm = new AssemblyNode();

    String falseLabel = AssemblyNode.generateLabel("Else");
    String endLabel = AssemblyNode.generateLabel("EndIf");

    // condition expression
    node.asm.addChild(node.expr.asm);

    if (node.trueStatement != null) {
      // check if condition is true
      node.asm.addChild(AssemblyNode.cmp(Register.EAX, CodeGenUtils.BOOLEAN_TRUE));
      node.asm.addChild(AssemblyNode.j(Condition.NE, falseLabel));

      // execute statement
      node.asm.addChild(node.trueStatement.asm);

      // jump to exit
      node.asm.addChild(AssemblyNode.jmp(endLabel));
      node.asm.addChild(AssemblyNode.label(falseLabel));
    }

    if (node.falseStatement != null) {
      // false statement execution
      node.asm.addChild(node.falseStatement.asm);
    }
    node.asm.addChild(AssemblyNode.label(endLabel));
  }

  @Override
  public void postVisit(InfixExpression node) {
    // might be a constant expression, so check for that first
    AssemblyNode constant = CodeGenUtils.getConstantAssembly(node);

    if (constant != null) {
      node.asm = constant;
    } else {
      String endLabel = AssemblyNode.generateLabel("LOG");
      node.asm = new AssemblyNode();
      node.asm.addChild(node.lhs.asm);
      // Short-circuit && and ||
      if (node.operator == InfixOperator.LOGAND) {
        node.asm.addChild(AssemblyNode.cmp(Register.EAX, 0));
        node.asm.addChild(AssemblyNode.j(Condition.E, endLabel));
      } else if (node.operator == InfixOperator.LOGOR) {
        node.asm.addChild(AssemblyNode.cmp(Register.EAX, 0));
        node.asm.addChild(AssemblyNode.j(Condition.NE, endLabel));
      }
      node.asm.addChild(AssemblyNode.push(Register.EAX));
      node.asm.addChild(node.rhs.asm);
      node.asm.addChild(AssemblyNode.mov(Register.EBX, Register.EAX));
      node.asm.addChild(AssemblyNode.pop(Register.EAX));

      AssemblyNode logicalNode = null;

      // lhs is in EAX and rhs is in EBX
      switch (node.operator) {
      case MULT:
        node.asm.addChild(AssemblyNode.imul(Register.EAX, Register.EBX));
        break;
      case DIV:
        // check for EBX == 0 and call __exception if so
        node.asm.requires(CodeGenUtils.EXCEPTION);
        node.asm.addChild(AssemblyNode.cmp(Register.EBX, 0));
        node.asm.addChild(AssemblyNode.j(Condition.E, CodeGenUtils.EXCEPTION));
        node.asm.addChild(AssemblyNode.CDQ);
        node.asm.addChild(AssemblyNode.idiv(Register.EBX));
        break;
      case MOD:
        // check for EBX == 0 and call __exception if so
        node.asm.requires(CodeGenUtils.EXCEPTION);
        node.asm.addChild(AssemblyNode.cmp(Register.EBX, 0));
        node.asm.addChild(AssemblyNode.j(Condition.E, CodeGenUtils.EXCEPTION));
        node.asm.addChild(AssemblyNode.CDQ);
        node.asm.addChild(AssemblyNode.idiv(Register.EBX));
        node.asm.addChild(AssemblyNode.mov(Register.EAX, Register.EDX));
        break;
      case PLUS:
        if (node.expressionType == CLASS_STRING.formalType) {
          CodeGenUtils.performStringConcat(node.asm, CLASS_STRING, node.lhs, node.rhs);
        } else {
          node.asm.addChild(AssemblyNode.add(Register.EAX, Register.EBX));
        }
        break;
      case MINUS:
        node.asm.addChild(AssemblyNode.sub(Register.EAX, Register.EBX));
        break;
      case BITAND:
      case LOGAND:
        node.asm.addChild(AssemblyNode.and(Register.EAX, Register.EBX));
        break;
      case BITOR:
      case LOGOR:
        node.asm.addChild(AssemblyNode.or(Register.EAX, Register.EBX));
        break;
      case LT:
        logicalNode = AssemblyNode.set(Condition.L, Register.AL);
        break;
      case GT:
        logicalNode = AssemblyNode.set(Condition.G, Register.AL);
        break;
      case LTE:
        logicalNode = AssemblyNode.set(Condition.LE, Register.AL);
        break;
      case GTE:
        logicalNode = AssemblyNode.set(Condition.GE, Register.AL);
        break;
      case DEQUAL:
        logicalNode = AssemblyNode.set(Condition.E, Register.AL);
        break;
      case NEQ:
        logicalNode = AssemblyNode.set(Condition.NE, Register.AL);
        break;
      }

      // if it's a logical operation, we need to do some work first
      if (logicalNode != null) {
        // compare
        node.asm.addChild(AssemblyNode.cmp(Register.EAX, Register.EBX));
        // clear the register
        node.asm.addChild(AssemblyNode.mov(Register.EAX, 0));
        // set the flag
        node.asm.addChild(logicalNode);
      }

      // For short-circuit evaluation
      if (node.operator == InfixOperator.LOGAND || node.operator == InfixOperator.LOGOR) {
        node.asm.addChild(AssemblyNode.label(endLabel));
      }
    }
  }

  @Override
  public void postVisit(InstanceofExpression node) {
    node.asm = new AssemblyNode();
    node.asm.addChild(node.expr.asm);

    FormalType rhsType = node.referenceType.getFormalType();
    assert !rhsType.isPrimitive();

    // Prepare call to CodeGenUtils helper
    node.asm.addChild(AssemblyNode.mov(Register.EBX, Register.EAX));
    node.asm.addChild(AssemblyNode.mov(Register.EAX, 0));
    String endLabel = AssemblyNode.generateLabel("EndInstanceof");

    // Null?
    node.asm.addChild(AssemblyNode.cmp(Register.EBX, 0));
    node.asm.addChild(AssemblyNode.j(Condition.E, endLabel), "LHS is null");

    CodeGenUtils.addInstanceofCheck(node.asm, rhsType, endLabel);
    node.asm.addChild(AssemblyNode.label(endLabel), node.token.getImage());
  }

  @Override
  public void postVisit(Literal node) {
    node.asm = node.getAssembly();
  }

  @Override
  public void preVisit(MethodDeclaration node) {
    stackSize = 0;

    // Set the stack offsets for the parameters
    // -1 is eip
    // -2 and on are the method parameters
    int offset = -2;
    ListIterator<VariableDeclaration> it = node.formalParams.listIterator(node.formalParams.size());
    while (it.hasPrevious()) {
      it.previous().setStackOffset(offset--);
    }
    thisOffset = offset;
  }

  @Override
  public void postVisit(MethodDeclaration node) {
    node.asm = new AssemblyNode();
    String name = node.getGlobalName();
    if (!node.isNative()) {
      node.asm.addChild(AssemblyNode.label(name));

      if (node.body != null) {
        // Registers? What registers?
        node.asm.addChild(AssemblyNode.push(Register.EBP));
        node.asm.addChild(AssemblyNode.mov(Register.EBP, Register.ESP));

        if (node.isConstructor) {
          // type declaration of the class we're instantiating
          TypeDeclaration type = node.getTypeDeclaration();

          // get this and push it onto the stack again
          AssemblyNode asm = AssemblyNode.movFromMem(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * thisOffset);
          node.asm.addChild(asm, "this");
          node.asm.addChild(AssemblyNode.push(Register.EAX));

          // if there's a parent class, call its zero-arg constructor
          if (type.superclass != null) {
            MethodDeclaration superConstructor = CodeGenUtils.getZeroArgConstructor(type.getSuperclassDecl());

            // call the constructor
            CodeGenUtils.callMethod(node.asm, superConstructor, 0);
          }

          // VTable of the class
          VTable vtable = type.getVTable();
          // call the initialization procedure for the fields
          node.asm.requires(vtable.getInitializerLabel());
          node.asm.addChild(AssemblyNode.call(vtable.getInitializerLabel()));

          // pop this
          node.asm.addChild(AssemblyNode.pop(Register.EAX));
        }

        node.asm.addChild(node.body.asm);
      } else {
        node.asm.addChild(AssemblyNode.comment("empty method"));
      }
    }
    // if this is void type or a constructor, add an implicit return
    if (node.type == null) {
      node.asm.addChild(AssemblyNode.LEAVE);
      node.asm.addChild(AssemblyNode.RET);
    }
    node.asm.addChild(AssemblyNode.comment("END method " + name));
    thisOffset = DEFAULT_THIS_OFFSET;
  }

  @Override
  public void postVisit(MethodInvocation node) {
    node.asm = new AssemblyNode();
    MethodDeclaration method = node.methodDecl;

    // Push "this" on to stack
    if (!method.isStatic()) {
      if (node.expr != null) {
        node.asm.addChild(node.expr.asm);
        node.asm.addChild(AssemblyNode.comment("this = " + node.expr.token.getImage()));
      } else if (node.qualifier != null) {
        node.asm.addChild(node.qualifier.asm);
        node.asm.addChild(AssemblyNode.comment("this = " + node.qualifier.token.getImage()));
      } else {
        AssemblyNode asm = AssemblyNode.movFromMem(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * thisOffset);
        node.asm.addChild(asm, "this");
      }
      node.asm.addChild(AssemblyNode.push(Register.EAX));
    }

    Collection<AssemblyNode> nodeargs = CodeGenUtils.getExpressionNodes(node.args);
    CodeGenUtils.callMethod(node.asm, method, nodeargs);

    if (!method.isStatic()) {
      // pop this
      node.asm.addChild(AssemblyNode.add(Register.ESP, CodeGenUtils.DWORD), "clear this");
    }
  }

  @Override
  public void postVisit(QualifiedName node) {
    node.asm = node.qualifier.asm;
    if (node.declNode == null || node.declNode.getEntityType() != EntityType.FIELD) {
      return;
    }

    FieldDeclaration decl = (FieldDeclaration) node.declNode;
    CodeGenUtils.addFieldAccess(node.asm, decl, node.addressExpected);
  }

  @Override
  public void postVisit(ParenthesizedExpression node) {
    node.asm = node.expr.asm;
  }

  @Override
  public void postVisit(PrefixExpression node) {
    // optimize a little bit and see if this is a const expression
    // note that this also saves us from figuring out when to negate literals
    AssemblyNode constant = CodeGenUtils.getConstantAssembly(node);

    if (constant != null) {
      node.asm = constant;
    } else {
      node.asm = new AssemblyNode();
      node.asm.addChild(node.expr.asm);

      if (node.operator == PrefixExpression.PrefixOperator.MINUS) {
        node.asm.addChild(AssemblyNode.neg(Register.EAX));
      } else {
        node.asm.addChild(AssemblyNode.xor(Register.EAX, 1));
      }
    }
  }

  @Override
  public void postVisit(PrimitiveType node) {
    // TODO remove?
    node.asm = new AssemblyNode();
  }

  @Override
  public void postVisit(ReturnStatement node) {
    node.asm = new AssemblyNode();
    if (node.expr != null) {
      node.asm.addChild(node.expr.asm);
    }
    node.asm.addChild(AssemblyNode.LEAVE);
    node.asm.addChild(AssemblyNode.RET);
  }

  @Override
  public void postVisit(SimpleName node) {
    node.asm = new AssemblyNode();
    if (node.declNode == null) {
      return;
    }

    switch (node.declNode.getEntityType()) {
    case VARIABLE: {
      IVariableDeclaration decl = (IVariableDeclaration) node.declNode;
      if (node.addressExpected) {
        node.asm.addChild(AssemblyNode.lea(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * decl.getStackOffset()),
            "&" + node.token.getImage());
      } else {
        node.asm.addChild(
            AssemblyNode.movFromMem(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * decl.getStackOffset()),
            node.token.getImage());
      }
      return;
    }
    case FIELD: {
      FieldDeclaration decl = (FieldDeclaration) node.declNode;
      if (!decl.isStatic()) {
        node.asm.addChild(AssemblyNode.movFromMem(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * thisOffset),
            "this");
      }
      CodeGenUtils.addFieldAccess(node.asm, decl, node.addressExpected);
      return;
    }
    default:
      return;
    }
  }

  @Override
  public void postVisit(SimpleType node) {
    // TODO remove?
    node.asm = new AssemblyNode();
  }

  @Override
  public void postVisit(StringLiteral node) {
    node.asm = new AssemblyNode();
    String value = node.valueOf();

    CodeGenUtils.addStringLiteral(node.asm, CLASS_STRING, value);
  }

  @Override
  public void postVisit(ThisExpression node) {
    node.asm = AssemblyNode.movFromMem(Register.EAX, Register.EBP, -(CodeGenUtils.DWORD) * thisOffset);
    node.asm.setComment("This Expression");
  }

  @Override
  public void postVisit(TypeDeclaration node) {
    node.asm = new AssemblyNode();

    if (node.isInterface) {
      return;
    }

    // construct vtable (note that this outputs the class label)
    node.getVTable().addVTable(node.asm);
    // add the array vtable to the object file
    if (node == TypeDeclaration.getObjectType()) {
      ArrayVTable.getVTable().addVTable(node.asm);
    }

    // Declare static fields, methods
    for (FieldDeclaration field : node.getFields()) {
      if (field.isStatic()) {
        node.asm.provides(field.getGlobalName());
      }
    }
    for (List<MethodDeclaration> methods : node.getMethods()) {
      for (MethodDeclaration method : methods) {
        if (method.isStatic() && !method.isNative()) {
          node.asm.provides(method.getGlobalName());
        }
      }
    }

    // Define static fields
    node.asm.addChild(AssemblyNode.SECTION_DATA);
    for (FieldDeclaration field : node.getFields()) {
      if (field.isStatic()) {
        node.asm.addChild(AssemblyNode.label(field.getGlobalName()));
        node.asm.addChild(AssemblyNode.DD);
      }
    }

    node.asm.addChild(AssemblyNode.SECTION_TEXT);

    // instance field initialization
    node.getVTable().addInitializer(node.asm);

    // Static field initialization
    String label = CodeGenUtils.classInitLabel(node);
    node.asm.provides(label);
    node.asm.addChild(AssemblyNode.label(label));
    for (FieldDeclaration field : node.getFields()) {
      if (field.isStatic()) {
        node.asm.addChild(field.asm);
        node.asm.addChild(AssemblyNode.movToMem(field.getGlobalName(), Register.EAX));
      }
    }
    node.asm.addChild(AssemblyNode.RET);

    // Define static methods
    for (List<MethodDeclaration> methods : node.getMethods()) {
      for (MethodDeclaration method : methods) {
        node.asm.addChild(method.asm);
      }
    }
  }

  @Override
  public void postVisit(VariableDeclaration node) {
    node.asm = new AssemblyNode();
  }

  @Override
  public void postVisit(VariableDeclarationExpression node) {
    node.setStackOffset(++stackSize);

    node.asm = new AssemblyNode();
    assert node.initExpr != null;
    node.asm.addChild(node.initExpr.asm);
    node.asm.addChild(AssemblyNode.push(Register.EAX), "var " + node.token.getImage());
  }

  @Override
  public void postVisit(VariableDeclarationStatement node) {
    node.setStackOffset(++stackSize);

    node.asm = new AssemblyNode();
    assert node.initExpr != null;
    node.asm.addChild(node.initExpr.asm);
    node.asm.addChild(AssemblyNode.push(Register.EAX), "var " + node.token.getImage());
  }

  @Override
  public void postVisit(WhileStatement node) {
    node.asm = new AssemblyNode();
    String startLabel = AssemblyNode.generateLabel("While");
    String endLabel = AssemblyNode.generateLabel("EndWhile");

    // start label
    node.asm.addChild(AssemblyNode.label(startLabel));

    // condition label
    node.asm.addChild(node.expr.asm);

    // compare if expression evaluates to true
    node.asm.addChild(AssemblyNode.cmp(Register.EAX, CodeGenUtils.BOOLEAN_TRUE));

    // exit if the while condition is not true
    node.asm.addChild(AssemblyNode.j(Condition.NE, endLabel));
    if (node.statement != null) {
      node.asm.addChild(node.statement.asm);
    }

    // jump to start of condition
    node.asm.addChild(AssemblyNode.jmp(startLabel));
    // end condition
    node.asm.addChild(AssemblyNode.label(endLabel));
  }

}
