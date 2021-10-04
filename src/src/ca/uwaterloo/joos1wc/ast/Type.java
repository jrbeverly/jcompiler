package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class Type extends ASTNode {

  public Type(Token token) {
    super(token);
  }

  public abstract FormalType getFormalType();

  public static Type newInstance(Token token, Type o) {
    if (o instanceof ArrayType)
      return ArrayType.newInstance(token, (ArrayType) o);
    if (o instanceof PrimitiveType)
      return PrimitiveType.newInstance(token, (PrimitiveType) o);
    if (o instanceof SimpleType)
      return SimpleType.newInstance(token, (SimpleType) o);
    return null;
  }

  public static class FormalType {
    public final PrimitiveType.Code code;
    public final TypeDeclaration decl;
    public final Boolean isArray;
    public final boolean isNull;
    public final boolean isVoid;

    public static final FormalType NULL = new FormalType(false);
    public static final FormalType VOID = new FormalType(true);

    // Primitive type
    public FormalType(boolean isArray, PrimitiveType.Code code) {
      this.isArray = isArray;
      this.code = code;
      this.decl = null;
      this.isNull = false;
      this.isVoid = false;
    }

    // Reference type
    public FormalType(boolean isArray, TypeDeclaration decl) {
      this.isArray = isArray;
      this.code = null;
      this.decl = decl;
      this.isNull = false;
      this.isVoid = false;
    }

    public FormalType(boolean isVoidOrNull) {
      this.isArray = false;
      this.isNull = !isVoidOrNull;
      this.isVoid = isVoidOrNull;
      this.code = null;
      this.decl = null;
    }

    public boolean isReference() {
      return this.isNull || this.decl != null || this.isArray;
    }

    public boolean isPrimitive() {
      return this.code != null && !this.isArray;
    }

    public static boolean canTypeAssign(FormalType varType, FormalType assignType) {
      // both types are void
      if (varType.isVoid && assignType.isVoid)
        return true;
      if (varType.isVoid ^ assignType.isVoid) {
        return false;
      }

      // they must be both arrays or not
      if (varType.isArray != assignType.isArray && !assignType.isNull)
        return false;

      // compare if both arrays, if primitive type, can only assign between if they match
      if (varType.isArray && assignType.isArray && varType.code != null && assignType.code != null)
        return varType.code == assignType.code;

      // if both primitive, check against each other
      if (varType.isPrimitive() && assignType.isPrimitive()) {
        return PrimitiveType.canTypeAssign(varType.code, assignType.code);
      }

      // if same type match them
      if (varType.isEquivalentTo(assignType)) {
        return true;
      }

      // assigning null, assignType must be ref
      if (assignType.isNull) {
        return varType.isReference();
      }

      // cannot assign a null variable
      if (varType.isNull) {
        return false;
      }

      // cannot assign reference type to a primitive
      if (varType.isPrimitive() && assignType.isReference())
        return false;

      // cannot assign primitive type to a reference
      if (assignType.isPrimitive() && varType.isReference())
        return false;

      // can the type I am assigning be cast down to the variable type
      return assignType.decl.isAssignable(varType.decl);
    }

    /*
     * Equal up to boxing/unboxing
     */
    public boolean isEquivalentTo(FormalType o) {
      // Definite matches/mismatches
      if (isArray != o.isArray || (isPrimitive() && o.isPrimitive() && code != o.code)
          || (isReference() && o.isReference() && decl != o.decl)) {
        return false;
      }
      FormalType primitive, reference;
      if (isPrimitive() && o.isReference()) {
        primitive = this;
        reference = o;
      } else if (isReference() && o.isPrimitive()) {
        primitive = o;
        reference = this;
      } else {
        // Matching reference or primitive types
        return true;
      }
      return primitive.code.getBoxedDeclaration() == reference.decl;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof FormalType))
        return false;
      FormalType ot = (FormalType) o;
      return isArray == ot.isArray && code == ot.code && decl == ot.decl;
    }

    public String format() {
      StringBuilder sb = new StringBuilder();
      if (code != null)
        sb.append(code.name());
      if (decl != null)
        sb.append(decl.name);
      if (isVoid)
        sb.append("void");
      if (isNull)
        sb.append("null");
      if (isArray)
        sb.append("[]");
      return sb.toString();
    }

    public String getGlobalName() {
      StringBuilder sb = new StringBuilder();
      if (isArray) {
        sb.append("A");
      } else if (decl != null) {
        sb.append("C");
      } else {
        sb.append("P");
      }
      if (decl != null) {
        sb.append(decl.getCanonicalName());
      } else {
        sb.append(code.toString().toLowerCase());
      }
      return sb.toString();
    }

    @Override
    public int hashCode() {
      if (isNull) // fix
        return 1;

      return 31 * (31 * isArray.hashCode() + (code == null ? 1 : code.hashCode()))
          + (decl == null ? 1 : decl.hashCode());
    }
  }

}
