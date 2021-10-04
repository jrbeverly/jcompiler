package ca.uwaterloo.joos1wc.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.PrimitiveType;
import ca.uwaterloo.joos1wc.ast.PrimitiveType.Code;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.SimpleName;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration.Signature;
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
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.CharLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.ast.literal.NullLiteral;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.ast.statement.IfThenStatement;
import ca.uwaterloo.joos1wc.ast.statement.ReturnStatement;
import ca.uwaterloo.joos1wc.ast.statement.VariableDeclarationStatement;
import ca.uwaterloo.joos1wc.ast.statement.WhileStatement;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.SemanticException;

public class TypeChecker {
  private final Diagnostics diagnostic;

  PackageTable localPackage;
  CompilationUnit localUnit;
  FormalType strType;
  FormalType objType;
  FormalType icloneable;
  FormalType iserializable;

  public TypeChecker(Diagnostics diagnostic) {
    // If we don't see a package declaration, we are in the default package
    this.localPackage = Joos1Wc.DEFAULT_PKG;
    this.diagnostic = diagnostic;

    strType = StringLiteral.getStringType(Joos1Wc.DEFAULT_PKG).formalType;
    objType = Literal.getObjectType(Joos1Wc.DEFAULT_PKG).formalType;

    // array interfaces
    icloneable = Joos1Wc.DEFAULT_PKG.getSubpackage("java").getSubpackage("lang").getType("Cloneable").formalType;
    iserializable = Joos1Wc.DEFAULT_PKG.getSubpackage("java").getSubpackage("io").getType("Serializable").formalType;

    runCheck(Joos1Wc.DEFAULT_PKG);
  }

  private void runCheck(PackageTable pkg) {
    for (PackageTable subpackage : pkg.getSubpackages()) {
      runCheck(subpackage);
    }
    for (TypeDeclaration typeDecl : pkg.getTypeDecls()) {
      for (List<MethodDeclaration> mdecls : typeDecl.getMethods()) {
        for (MethodDeclaration mdecl : mdecls) {
          TypeCheckerVisitor visitor = new TypeCheckerVisitor(typeDecl, mdecl);
          mdecl.accept(visitor);
        }
        for (FieldDeclaration fdecl : typeDecl.getFields()) {
          TypeCheckerVisitor visitor = new TypeCheckerVisitor(typeDecl, null);
          fdecl.accept(visitor);
        }
      }
    }
  }

  class TypeCheckerVisitor extends RecursiveVisitor {

    TypeDeclaration localType;
    MethodDeclaration localMethod;

    public TypeCheckerVisitor(TypeDeclaration type, MethodDeclaration mdecl) {
      localType = type;
      localMethod = mdecl;
    }

    @Override
    public void visit(PackageDeclaration node) {
      localPackage = node.packageTable;
    }

    @Override
    public void visit(ImportDeclaration node) {
    }

    @Override
    public void preVisit(CompilationUnit node) {
      localUnit = node;
    }

    @Override
    public void preVisit(TypeDeclaration node) {
      localType = node;
    }

    @Override
    public void preVisit(MethodDeclaration node) {
      localMethod = node;
    }

    @Override
    public void preVisit(ArrayCreation node) {
      node.expressionType = node.type.getFormalType();
    }

    @Override
    public void postVisit(ArrayAccess node) {
      FormalType type = node.array.expressionType;
      if (type.code != null)
        node.expressionType = new FormalType(false, type.code);
      else
        node.expressionType = new FormalType(false, type.decl);

      FormalType inType = node.index.expressionType;
      if (!(inType.isPrimitive() && inType.code.isNumeric())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.EXPECTED_TYPE,
            PrimitiveType.Code.INT.formalType.format(), node.token.getImage(), inType.format())));
      }
    }

    @Override
    public void postVisit(ArrayCreation node) {
      FormalType inType = node.dimExpr.expressionType;
      if (!(inType.isPrimitive() && inType.code.isNumeric())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.EXPECTED_TYPE,
            PrimitiveType.Code.INT.formalType.format(), node.token.getImage(), inType.format())));
      }
    }

    @Override
    public void preVisit(InstanceofExpression node) {
      node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;
    }

    @Override
    public void preVisit(ThisExpression node) {
      node.expressionType = localType.formalType;
    }

    @Override
    public void preVisit(VariableDeclarationExpression node) {
      node.expressionType = node.type.getFormalType();
    }

    @Override
    public void postVisit(Assignment node) {
      node.expressionType = node.lhs.expressionType;
      FormalType lhsType = node.lhs.expressionType;
      FormalType rhsType = node.rhs.expressionType;

      if (lhsType == null || rhsType == null) {
        assert diagnostic.getCount() > 0;
        return;
      }

      if (node.lhs.isFinal) {
        diagnostic.add(new SemanticException(Exceptions.FINAL_FIELD_ASSIGNMENT));
      } else if (!canAssignTo(lhsType, rhsType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.VARIABLE_ASSIGNMENT_MISMATCH,
            node.lhs.token.getImage(), lhsType.format(), rhsType.format())));
      }
    }

    @Override
    public void preVisit(ClassInstanceCreation node) {
      node.expressionType = node.classType.getFormalType();
    }

    @Override
    public void preVisit(StringLiteral node) {
      node.expressionType = strType;
    }

    @Override
    public void preVisit(IntLiteral node) {
      node.expressionType = PrimitiveType.Code.INT.formalType;
    }

    @Override
    public void preVisit(CharLiteral node) {
      node.expressionType = PrimitiveType.Code.CHAR.formalType;
    }

    @Override
    public void preVisit(BooleanLiteral node) {
      node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;
    }

    @Override
    public void preVisit(NullLiteral node) {
      node.expressionType = FormalType.NULL;
    }

    @Override
    public void preVisit(CastExpression node) {
      node.expressionType = node.getFormalType();
    }

    @Override
    public void preVisit(SimpleName node) {
      assert node.declNode != null || node.packageTable != null;
      if (node.declNode != null) {
        node.expressionType = getDeclType(node.declNode);
      }
    }

    @Override
    public void postVisit(QualifiedName node) {
      assert node.declNode != null || node.packageTable != null;
      if (node.declNode != null) {
        node.expressionType = getDeclType(node.declNode);
        node.isFinal = node.declNode == FieldDeclaration.LENGTH_FIELD;
      }
    }

    @Override
    public void postVisit(CastExpression node) {
      FormalType castTo = node.expressionType;
      FormalType exprCast = node.expr.expressionType;
      if (objType.equals(castTo) || objType.equals(exprCast)) {
        return;
      }

      if (castTo.isArray != exprCast.isArray) {
        diagnostic
            .add(new SemanticException(String.format(Exceptions.CANNOT_CAST, exprCast.format(), castTo.format())));
        return;

      }

      if (castTo.isPrimitive() && exprCast.isPrimitive()) {
        if (!PrimitiveType.canCast(castTo.code, exprCast.code)) {
          diagnostic.add(new SemanticException(
              String.format(Exceptions.CANNOT_CAST, exprCast.format(), castTo.format())));

        }
      }
    }

    private FormalType getDeclType(INamedEntityNode node) {
      if (node instanceof TypeDeclaration) {
        return ((TypeDeclaration) node).formalType;
      } else if (node == FieldDeclaration.LENGTH_FIELD) {
        return Code.INT.formalType;
      } else {
        return node.getType().getFormalType();
      }
    }

    /*
     * Finally link field access
     */
    @Override
    public void postVisit(FieldAccess node) {
      if (node.expr.expressionType.isArray && node.name.equals(FieldDeclaration.ARRAY_LENGTH)) {
        // <array>.length
        node.expressionType = Code.INT.formalType;
        node.isFinal = true;
        node.declNode = FieldDeclaration.LENGTH_FIELD;
      } else {
        // Other field?
        try {
          // Assume LHS is a variable, doesn't matter for resolution
          node.declNode = LinkerUtils.resolveField(localType, node.expr.expressionType.decl, node.name,
              EntityType.VARIABLE, false);
          if (node.declNode == null) {
            diagnostic.add(new SemanticException(String.format(Exceptions.NO_RESOLUTION, node.name)));
            return;
          }
          node.expressionType = node.declNode.type.getFormalType();
        } catch (SemanticException e) {
          diagnostic.add(e);
        }
      }

      if (node.expr instanceof ParenthesizedExpression) {
        ParenthesizedExpression ptype = (ParenthesizedExpression) node.expr;
        if (ptype.expr instanceof SimpleName) {
          SimpleName sname = (SimpleName) ptype.expr;
          if (sname.identifier.equals(sname.declNode.getName())) {
            diagnostic.add(new SemanticException(String.format(Exceptions.TYPE_IN_PAREN, node.token.getImage())));
            return;
          }
        }
      }
    }

    @Override
    public void postVisit(ParenthesizedExpression node) {
      node.expressionType = node.expr.expressionType;
    }

    @Override
    public void postVisit(InfixExpression node) {
      FormalType lhsType = node.lhs.expressionType;
      FormalType rhsType = node.rhs.expressionType;
      boolean bad_operator = false;

      if (lhsType == null || rhsType == null) {
        node.expressionType = FormalType.NULL;
        return;
      }

      if (strType.equals(lhsType) || strType.equals(rhsType)) {
        switch (node.operator) {
        case NEQ:
        case DEQUAL:
          node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;

          TypeDeclaration tdecl = PrimitiveType.Code.INT.getBoxedDeclaration();
          if (lhsType.decl == tdecl || rhsType.decl == tdecl) {
            diagnostic.add(new SemanticException(String.format(Exceptions.EQUALITY_MISMATCH, lhsType.format(),
                rhsType.format())));
          }
          break;
        case PLUS:
          if (lhsType.isVoid || rhsType.isVoid) {
            diagnostic.add(new SemanticException(String.format(Exceptions.BAD_OPERATOR_USAGE, node.operator,
                lhsType.format(), rhsType.format())));
          }
          node.expressionType = strType;
          return;
        default:
          bad_operator = true;
          break;
        }
      }

      if (lhsType.isPrimitive() && rhsType.isPrimitive()) {
        PrimitiveType.Code lhscode = lhsType.code;
        PrimitiveType.Code rhscode = rhsType.code;

        switch (node.operator) {
        case GT:
        case GTE:
        case LT:
        case LTE:
        case NEQ:
        case DEQUAL:
        case LOGAND:
        case LOGOR:
          node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;
          break;
        case BITAND:
        case BITOR:
          if (lhscode.isBoolean() && rhscode.isBoolean()) {
            node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;
            break;
          }
        case DIV:
        case MINUS:
        case MOD:
        case MULT:
        case PLUS:
        default:
          node.expressionType = PrimitiveType.Code.INT.formalType;
          break;
        }

        if (!PrimitiveType.canPerform(lhscode, node.operator) || !PrimitiveType.canPerform(rhscode, node.operator)) {
          bad_operator = true;
        }
      } else if (lhsType.isReference() && rhsType.isPrimitive()) {
        bad_operator = true;
      } else if (rhsType.isReference() && lhsType.isPrimitive()) {
        bad_operator = true;
      } else {
        switch (node.operator) {
        case NEQ:
        case DEQUAL:
          if (lhsType.isVoid || rhsType.isVoid) {
            diagnostic.add(new SemanticException(String.format(Exceptions.EQUALITY_NOT_VOID)));
          }
          node.expressionType = PrimitiveType.Code.BOOLEAN.formalType;
          break;
        default:
          bad_operator = true;
          break;
        }
      }

      if (bad_operator) {
        diagnostic.add(new SemanticException(String.format(Exceptions.BAD_OPERATOR_USAGE, node.operator,
            lhsType.format(), rhsType.format())));
      }
    }

    public void postVisit(ThisExpression node) {
      if (localMethod != null && localMethod.isStatic()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.THIS_STATIC_METHOD, node.token.getImage())));
      }
    }

    @Override
    public void postVisit(InstanceofExpression node) {
      FormalType lhsType = node.expr.expressionType;
      FormalType rhsType = node.referenceType.getFormalType();

      // if primitive (even if array) throw exception
      if (lhsType.isPrimitive()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.INSTANCEOF_PRIMITIVE, lhsType.code.toString())));
      } else if (rhsType.isPrimitive()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.INSTANCEOF_PRIMITIVE, rhsType.code.toString())));
      }

      // everyone is ref type now

      // everyone can be instanceOf Object
      // also works for isNull
      if (lhsType.isNull || objType.equals(rhsType)) {
        return;
      }

      // Catch incompatible array types
      if (rhsType.code != lhsType.code && !objType.equals(lhsType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.INSTANCEOF_INCOMPATIBLE_TYPES)));
        return;
      }

      if (rhsType.decl != null && lhsType.decl != null) {
        TypeDeclaration rdecl = rhsType.decl;
        TypeDeclaration ldecl = lhsType.decl;
        if (rdecl.isClass && ldecl.isClass && !rdecl.isSubclassOf(ldecl) && !ldecl.isSubclassOf(rdecl)) {
          diagnostic.add(new SemanticException(String.format(Exceptions.CANNOT_INSTANCEOF, lhsType.format(),
              rhsType.format())));
        }
      }

    }

    @Override
    public void postVisit(PrefixExpression node) {
      FormalType type = node.expr.expressionType;
      node.expressionType = type;

      switch (node.operator) {
      case BANG:
        if (type.code != PrimitiveType.Code.BOOLEAN) {
          diagnostic.add(new SemanticException(String.format(Exceptions.BANG_NOT_BOOLEAN, node.token.getImage())));
        }
        break;
      case MINUS:
        if (!type.code.isNumeric()) {
          diagnostic.add(new SemanticException(String.format(Exceptions.NEGATE_NOT_NUMERIC, node.token.getImage())));
        }
        break;
      default:
        break;
      }
    }

    @Override
    public void postVisit(ReturnStatement node) {
      assert localMethod != null;

      // expecting void, there is expression
      if (localMethod.type == null && node.expr != null) {
        diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_RETURN_VOID, localMethod.name)));
        return;
      } else if (localMethod.type != null && node.expr == null) {
        diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_RETURN_EMPTY, localMethod.name)));
        return;
      }

      FormalType varType = localMethod.type == null ? FormalType.NULL : localMethod.type.getFormalType();
      FormalType returnType = node.expr == null ? FormalType.NULL : node.expr.expressionType;

      if (!canAssignTo(varType, returnType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.RETURN_MISTYPE, localMethod.name,
            varType.format(), returnType.format())));
      }
    }

    @Override
    public void postVisit(VariableDeclarationStatement node) {
      if (node.initExpr == null)
        return;

      FormalType initType = node.initExpr.expressionType;
      FormalType varType = node.type.getFormalType();

      if (!canAssignTo(varType, initType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.VARIABLE_ASSIGNMENT_MISMATCH, node.name,
            varType.format(), initType.format())));
      }
    }

    @Override
    public void postVisit(FieldDeclaration node) {
      if (node.initExpr == null)
        return;

      FormalType initType = node.initExpr.expressionType;
      FormalType varType = node.type.getFormalType();

      if (!canAssignTo(varType, initType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.VARIABLE_ASSIGNMENT_MISMATCH, node.name,
            varType.format(), initType.format())));
      }
    }

    @Override
    public void postVisit(VariableDeclaration node) {
      if (node.initExpr == null)
        return;

      FormalType initType = node.initExpr.expressionType;
      FormalType varType = node.type.getFormalType();

      if (!canAssignTo(varType, initType)) {
        diagnostic.add(new SemanticException(String.format(Exceptions.VARIABLE_ASSIGNMENT_MISMATCH, node.name,
            varType.format(), initType.format())));
      }
    }

    private boolean canAssignTo(FormalType varType, FormalType initType) {
      if (varType == objType)
        return true;

      if (initType.isArray && varType.decl != null
          && (varType.decl == icloneable.decl || varType.decl == iserializable.decl))
        return true;

      return FormalType.canTypeAssign(varType, initType);
    }

    @Override
    public void postVisit(IfThenStatement node) {
      FormalType condType = node.expr.expressionType;
      if (!(condType.isPrimitive() && condType.code.isBoolean())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.IF_ASSIGNABLE, condType.format(),
            node.expr.token.getImage())));
      }
    }

    @Override
    public void postVisit(WhileStatement node) {
      FormalType condType = node.expr.expressionType;
      if (!(condType.isPrimitive() && condType.code.isBoolean())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.WHILE_ASSIGNABLE, condType.format(),
            node.expr.token.getImage())));
      }
    }

    @Override
    public void postVisit(ClassInstanceCreation node) {
      FormalType classType = node.classType.getFormalType();
      TypeDeclaration tdecl = classType.decl;
      if (tdecl == null)
        return;

      if (tdecl.isInterface) {
        diagnostic.add(new SemanticException(String.format(Exceptions.INTERFACE_CONSTRUCT, tdecl.name)));
        return;
      }

      if (tdecl.isAbstract()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.ABSTRACT_CONSTRUCT, tdecl.name)));
        return;
      }

      TypeDeclaration cdecl = tdecl;
      boolean exists = false;
      List<Type.FormalType> types = new ArrayList<Type.FormalType>(node.args.size());
      for (int i = 0; i < node.args.size(); i++) {
        FormalType ftype = node.args.get(i).expressionType;
        types.add(ftype);
      }

      while (cdecl != null) {
        Signature sign = new Signature(true, cdecl.name, types);
        MethodDeclaration decl = tdecl.getDeclaredMethod(sign);
        cdecl = cdecl.getSuperclassDecl();

        if (decl != null) {
          if (decl.isProtected() && tdecl.getPackage() != localType.getPackage()) {
            diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_PROTECTED_ACCESS, tdecl.name)));
          }
          exists = true;
          node.constructor = decl;
          break;
        }
      }

      // does not exist
      if (!exists) {
        diagnostic.add(new SemanticException(String.format(Exceptions.CONSTRUCTOR_NOT_FOUND, tdecl.name)));
      }
    }

    @Override
    public void postVisit(MethodInvocation node) {
      // handle primitive expr method invocation
      if ((node.expr != null && node.expr.expressionType.isPrimitive())
          || (node.qualifier != null && node.qualifier.expressionType.isPrimitive())) {
        diagnostic.add(new SemanticException(String.format(Exceptions.PRIMITIVE_METHOD_CALL, node.token.getImage())));
        return;
      }

      FormalType targetType;
      EntityType lhsEntityType = null;
      if (node.expr != null) {
        targetType = node.expr.expressionType;
        lhsEntityType = EntityType.VARIABLE; // close enough (minor hack)
      } else if (node.qualifier != null) {
        targetType = node.qualifier.expressionType;
        lhsEntityType = node.qualifier.declNode.getEntityType();
      } else {
        targetType = localType.formalType;
      }
      TypeDeclaration target = targetType.decl;

      List<Type.FormalType> paramTypes = new ArrayList<Type.FormalType>(node.args.size());
      for (int i = 0; i < node.args.size(); i++) {
        paramTypes.add(node.args.get(i).expressionType);
      }

      if (target == null) {
        diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_NOT_FOUND, node.methodName,
            getTypes(paramTypes), localType.name)));
        return;
      }

      Signature sig = new Signature(false, node.methodName, paramTypes);
      MethodDeclaration method = target.getInheritedMethod(sig);
      if (method == null) {
        // Could not find exact match - look for other applicable methods
        boolean found = false;
        for (Map.Entry<Signature, MethodDeclaration> entry : target.getInheritedMethods().entrySet()) {
          if (entry.getKey().matches(sig)) {
            // Must be ambiguous, since we do not allow closest match overloading
            if (found) {
              diagnostic.add(new SemanticException(String.format(Exceptions.AMBIGUOUS_METHOD_INVOCATION,
                  node.methodName)));
              return;
            }
            found = true;
            method = entry.getValue();
          }
        }

        if (!found) {
          diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_NOT_FOUND, node.methodName,
              getTypes(paramTypes), localType.name)));
          return;
        }
      }

      assert method != null;

      // Check static/non-static invocation
      boolean isInStaticScope = localMethod != null && localMethod.isStatic();
      if (((lhsEntityType == null && isInStaticScope) // implicit type in static scope
          || lhsEntityType == EntityType.TYPE) // explicit type
          && !method.isStatic()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.STATIC_REF_CONTEXT, node.methodName,
            localMethod.name)));
      } else if ((lhsEntityType == null // implicit type
          || lhsEntityType == EntityType.FIELD || lhsEntityType == EntityType.VARIABLE) // explicit this
          && method.isStatic()) {
        diagnostic.add(new SemanticException(String.format(Exceptions.STATIC_METHOD_ACCESS, node.methodName)));
      }

      // Check access restrictions
      if (target.isClass) {
        // Determine which class actually declares the called method
        TypeDeclaration receiver = target;
        while (receiver != null && !receiver.declaresMethod(method.getSignature())) {
          receiver = receiver.getSuperclassDecl();
        }
        // TODO may not actually find the receiver

        if (method.isProtected() && target.getPackage() != localType.getPackage()) {
          if (!localType.isSubclassOf(receiver)
              || (lhsEntityType != EntityType.TYPE && !target.isSubclassOf(localType))) {
            diagnostic.add(new SemanticException(String.format(Exceptions.METHOD_PROTECTED_ACCESS, node.methodName)));
          }
        }
      }

      node.methodDecl = method;
      node.expressionType = (method.type == null) ? FormalType.VOID : method.type.getFormalType();
    }
  }

  private static String getTypes(List<FormalType> exprs) {
    StringBuilder builder = new StringBuilder();
    builder.append("{ ");
    for (FormalType ftype : exprs) {
      builder.append(ftype.format() + " ");
    }
    builder.append("}");
    return builder.toString();
  }

}
