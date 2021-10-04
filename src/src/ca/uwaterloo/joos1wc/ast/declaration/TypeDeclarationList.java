package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Null;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class TypeDeclarationList extends ASTNode {

  private List<TypeDeclaration> typeDeclarations;

  public TypeDeclarationList(Token token, List<TypeDeclaration> typeDeclarations) {
    super(token);
    this.typeDeclarations = typeDeclarations;
  }

  public List<TypeDeclaration> getTypeDeclarations() {
    return Collections.unmodifiableList(typeDeclarations);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static TypeDeclarationList newInstance(Token token, TypeDeclarationList o) {
    return new TypeDeclarationList(token, o.typeDeclarations);
  }

  // TypeDeclarations -> epsilon
  public static TypeDeclarationList newInstance(Token token) {
    return new TypeDeclarationList(token, new ArrayList<TypeDeclaration>());
  }

  // TypeDeclarations -> TypeDeclaration
  public static TypeDeclarationList newInstance(Token token, TypeDeclaration node) {
    List<TypeDeclaration> typeDeclarations = new ArrayList<TypeDeclaration>();
    typeDeclarations.add(node);
    return new TypeDeclarationList(token, typeDeclarations);
  }
  public static TypeDeclarationList newInstance(Token token, Null node) {
    return new TypeDeclarationList(token, new ArrayList<TypeDeclaration>());
  }

  // TypeDeclarations -> TypeDeclarations TypeDeclaration
  public static TypeDeclarationList newInstance(Token token, TypeDeclarationList o, TypeDeclaration node) {
    List<TypeDeclaration> typeDeclarations = o.typeDeclarations;
    typeDeclarations.add(node);
    return new TypeDeclarationList(token, typeDeclarations);
  }
  public static TypeDeclarationList newInstance(Token token, TypeDeclarationList o, Null node) {
    return new TypeDeclarationList(token, o.typeDeclarations);
  }

}
