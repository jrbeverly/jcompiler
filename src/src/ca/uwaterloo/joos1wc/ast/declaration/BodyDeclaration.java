package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.List;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Modifier;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Modifier.ModifierKeyword;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class BodyDeclaration extends ASTNode implements INamedEntityNode {

  public final List<Modifier> modifiers;
  public final Type type;
  public String name;

  public BodyDeclaration(Token token, List<Modifier> modifiers, Type type, String name) {
    super(token);
    this.modifiers = modifiers;
    this.type = type;
    this.name = name;
  }
  
  public abstract String getGlobalName();

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

  @Override
  public Type getType() {
    return type;
  }

  public static BodyDeclaration newInstance(Token token, BodyDeclaration o) {
    if (o instanceof FieldDeclaration)
      return FieldDeclaration.newInstance(token, (FieldDeclaration) o);
    if (o instanceof MethodDeclaration)
      return MethodDeclaration.newInstance(token, (MethodDeclaration) o);
    return null;
  }

}
