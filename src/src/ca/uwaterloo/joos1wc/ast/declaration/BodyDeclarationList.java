package ca.uwaterloo.joos1wc.ast.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.ASTNode;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class BodyDeclarationList extends ASTNode {

  private List<BodyDeclaration> bodyDeclarations;

  public BodyDeclarationList(Token token, List<BodyDeclaration> bodyDeclarations) {
    super(token);
    this.bodyDeclarations = bodyDeclarations;
  }

  public List<BodyDeclaration> getBodyDeclarations() {
    return Collections.unmodifiableList(bodyDeclarations);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  // ClassBodyDeclarations(opt) -> ClassBodyDeclarations
  public static BodyDeclarationList newInstance(Token token, BodyDeclarationList o) {
    return new BodyDeclarationList(token, o.bodyDeclarations);
  }

  // ClassBodyDeclarations(opt) -> epsilon
  public static BodyDeclarationList newInstance(Token token) {
    return new BodyDeclarationList(token, new ArrayList<BodyDeclaration>());
  }

  // ClassBodyDeclarations -> ClassBodyDeclaration
  public static BodyDeclarationList newInstance(Token token, BodyDeclaration node) {
    List<BodyDeclaration> bodyDeclarations = new ArrayList<BodyDeclaration>();
    bodyDeclarations.add(node);
    return new BodyDeclarationList(token, bodyDeclarations);
  }

  // ClassBodyDeclarations -> ClassBodyDeclarations ClassBodyDeclaration
  public static BodyDeclarationList newInstance(Token token, BodyDeclarationList o, BodyDeclaration node) {
    List<BodyDeclaration> bodyDeclarations = o.bodyDeclarations;
    bodyDeclarations.add(node);
    return new BodyDeclarationList(token, bodyDeclarations);
  }

  // ClassBody -> { ClassBodyDeclarations(opt) }
  // InterfaceBody -> { InterfaceBodyDeclarations(opt) }
  public static BodyDeclarationList newInstance(Token token, Terminal n0, BodyDeclarationList o, Terminal n1) {
    return new BodyDeclarationList(token, o.bodyDeclarations);
  }

}
