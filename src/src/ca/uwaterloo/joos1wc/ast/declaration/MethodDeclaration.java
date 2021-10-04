package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.Block;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Modifier;
import ca.uwaterloo.joos1wc.ast.ModifierList;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.Modifier.ModifierKeyword;
import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.scanner.Token;

public class MethodDeclaration extends BodyDeclaration {

  public final List<VariableDeclaration> formalParams;
  public final Block body;
  public final boolean isConstructor;

  private TypeDeclaration typeDecl;
  private Signature signature;

  public MethodDeclaration(Token token, List<Modifier> modifiers, Type type, String name,
      List<VariableDeclaration> formalParams, Block body, boolean isConstructor) {
    super(token, modifiers, type, name);
    this.formalParams = formalParams;
    this.body = body;
    this.isConstructor = isConstructor;
  }

  public boolean isFinal() {
    return isModifier(Modifier.ModifierKeyword.FINAL);
  }

  public boolean isAbstract() {
    return isModifier(Modifier.ModifierKeyword.ABSTRACT);
  }

  public boolean isStatic() {
    return isModifier(Modifier.ModifierKeyword.STATIC);
  }

  public boolean isNative() {
    return isModifier(Modifier.ModifierKeyword.NATIVE);
  }

  public boolean isProtected() {
    return isModifier(Modifier.ModifierKeyword.PROTECTED);
  }

  public boolean isPublic() {
    return isModifier(Modifier.ModifierKeyword.PUBLIC);
  }

  public boolean isModifier(Modifier.ModifierKeyword keyword) {
    if (this.modifiers != null) {
      for (Modifier modifier : this.modifiers) {
        if (modifier.keyword == keyword) {
          return true;
        }
      }
    }

    return false;
  }

  // TODO better name
  public boolean isReallyAbstract() {
    return body == null && !isNative();
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public EntityType getEntityType() {
    return EntityType.METHOD;
  }

  public void setTypeDeclaration(TypeDeclaration typeDecl) {
    this.typeDecl = typeDecl;
  }

  public TypeDeclaration getTypeDeclaration() {
    return typeDecl;
  }

  public String format() {
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append("( ");
    for (VariableDeclaration type : formalParams) {
      sb.append(type.type.getFormalType().toString());
    }
    sb.append(" )");
    return sb.toString();
  }

  public Signature getSignature() {
    if (signature == null) {
      List<Type.FormalType> parameterTypes = new ArrayList<Type.FormalType>(formalParams.size());
      for (VariableDeclaration decl : formalParams) {
        parameterTypes.add(decl.type.getFormalType());
      }
      signature = new Signature(isConstructor, name, parameterTypes);
    }
    return signature;
  }

  @Override
  public String getGlobalName() {
    if (isNative()) {
      return String.format("NATIVE%s.%s", typeDecl.getCanonicalName(), name);
    }

    StringBuilder sb = new StringBuilder();
    sb.append(isConstructor ? "N" : "M");
    sb.append(typeDecl.getCanonicalName()).append(".");
    sb.append(name);
    for (FormalType paramType : getSignature().parameterTypes) {
      sb.append("#").append(paramType.getGlobalName());
    }
    return sb.toString();
  }

  public static MethodDeclaration newInstance(Token token, MethodDeclaration o) {
    return new MethodDeclaration(token, o.modifiers, o.type, o.name, o.formalParams, o.body, o.isConstructor);
  }

  // MethodDeclaration -> MethodHeader MethodBody
  public static MethodDeclaration newInstance(Token token, MethodDeclaration headerNode, Block body) {
    return new MethodDeclaration(token, headerNode.modifiers, headerNode.type, headerNode.name,
        headerNode.formalParams, body, false);
  }

  // MethodHeader -> Modifiers(opt) Type MethodDeclarator
  public static MethodDeclaration newInstance(Token token, ModifierList modifiers, Type type,
      MethodDeclaration declaratorNode) {
    return new MethodDeclaration(token, modifiers.getModifiers(), type, declaratorNode.name,
        declaratorNode.formalParams, null, false);
  }

  // MethodHeader -> Modifiers(opt) VOID MethodDeclarator
  public static MethodDeclaration newInstance(Token token, ModifierList modifiers, Terminal n0,
      MethodDeclaration declaratorNode) {
    return new MethodDeclaration(token, modifiers.getModifiers(), null, declaratorNode.name,
        declaratorNode.formalParams, null, false);
  }

  // MethodDeclarator -> ID ( FormalParameterList(opt) )
  // ConstructorDeclarator -> ID ( FormalParameterList(opt) )
  public static MethodDeclaration newInstance(Token token, Terminal idNode, Terminal n0,
      VariableDeclarationList formalParams, Terminal n1) {
    return new MethodDeclaration(token, null, null, idNode.token.getImage(), formalParams.getVariableDeclarations(),
        null, false);
  }

  // ConstructorDeclaration -> Modifiers(opt) ConstructorDeclarator ConstructorBody
  public static MethodDeclaration newInstance(Token token, ModifierList modifiers, MethodDeclaration declarator,
      Block body) {
    return new MethodDeclaration(token, modifiers.getModifiers(), null, declarator.name, declarator.formalParams, body,
        true);
  }

  // AbstractMethodDeclaration -> MethodHeader ;
  public static MethodDeclaration newInstance(Token token, MethodDeclaration o, Terminal n0) {
    return newInstance(token, o);
  }

  public static class Signature {
    private Boolean isConstructor;
    private String name;
    private List<FormalType> parameterTypes;
    private int hashCode;

    public Signature(boolean isConstructor, String name, List<Type.FormalType> parameterTypes) {
      this.isConstructor = isConstructor;
      this.name = name;
      this.parameterTypes = parameterTypes;

      hashCode = 31 * this.isConstructor.hashCode();
      hashCode = 31 * hashCode + this.name.hashCode();
      for (Object type : parameterTypes) {
        hashCode = 31 * hashCode + type.hashCode();
      }
    }

    public String name() {
      return this.name;
    }

    public List<Type.FormalType> getTypes() {
      return parameterTypes;
    }

    public int size() {
      return this.parameterTypes.size();
    }

    public boolean matches(Signature o) {
      if (!name.equals(o.name) || size() != o.size()) {
        return false;
      }
      for (int i = 0; i < size(); i++) {
        FormalType t1 = parameterTypes.get(i), t2 = o.parameterTypes.get(i);
        if (!t1.isEquivalentTo(t2)) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Signature))
        return false;
      Signature os = (Signature) o;
      return isConstructor == os.isConstructor && name.equals(os.name) && parameterTypes.equals(os.parameterTypes);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }
  }

}
