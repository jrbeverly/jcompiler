package ca.uwaterloo.joos1wc.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.scanner.Token;

public class ModifierList extends ASTNode {

  private List<Modifier> modifiers;

  public ModifierList(Token token, List<Modifier> modifiers) {
    super(token);
    this.modifiers = modifiers;
  }

  public List<Modifier> getModifiers() {
    return Collections.unmodifiableList(modifiers);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ModifierList newInstance(Token token) {
    return new ModifierList(token, new ArrayList<Modifier>());
  }

  public static ModifierList newInstance(Token token, Modifier node) {
    List<Modifier> modifiers = new ArrayList<Modifier>();
    modifiers.add(node);
    return new ModifierList(token, modifiers);
  }

  public static ModifierList newInstance(Token token, ModifierList o) {
    return new ModifierList(token, o.modifiers);
  }

  public static ModifierList newInstance(Token token, ModifierList o, Modifier node) {
    List<Modifier> modifiers = o.modifiers;
    modifiers.add(node);
    return new ModifierList(token, modifiers);
  }

}
