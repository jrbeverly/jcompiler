package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.List;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.Modifier;
import ca.uwaterloo.joos1wc.ast.ModifierList;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Type;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.scanner.Token;

public class FieldDeclaration extends BodyDeclaration {

  // Stub for <array>.length field
  public static final String ARRAY_LENGTH = "length";
  public static final FieldDeclaration LENGTH_FIELD = new FieldDeclaration(null, null, null, ARRAY_LENGTH, null);

  public final Expression initExpr;

  private TypeDeclaration typeDecl;

  public FieldDeclaration(Token token, List<Modifier> modifiers, Type type, String name, Expression initExpr) {
    super(token, modifiers, type, name);
    this.initExpr = initExpr;
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
    return EntityType.FIELD;
  }

  public void setTypeDeclaration(TypeDeclaration typeDecl) {
    this.typeDecl = typeDecl;
  }

  public TypeDeclaration getTypeDeclaration() {
    return typeDecl;
  }

  @Override
  public String getGlobalName() {
    return String.format("F%s.%s", typeDecl.getCanonicalName(), name);
  }

  public static FieldDeclaration newInstance(Token token, FieldDeclaration o) {
    return new FieldDeclaration(token, o.modifiers, o.type, o.name, o.initExpr);
  }

  // FieldDeclaration -> Modifiers(opt) Type ID ;
  public static FieldDeclaration newInstance(Token token, ModifierList modifiers, Type type, Terminal idNode,
      Terminal n0) {
    return new FieldDeclaration(token, modifiers.getModifiers(), type, idNode.token.getImage(), null);
  }

  // FieldDeclaration -> Modifiers(opt) Type ID = Expression ;
  public static FieldDeclaration newInstance(Token token, ModifierList modifiers, Type type, Terminal idNode,
      Terminal n0, Expression initExpr, Terminal n1) {
    return new FieldDeclaration(token, modifiers.getModifiers(), type, idNode.token.getImage(), initExpr);
  }

}
