package ca.uwaterloo.joos1wc.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.scanner.Token;

public class SimpleTypeList extends ASTNode {

  private List<SimpleType> types;

  public SimpleTypeList(Token token, List<SimpleType> types) {
    super(token);
    this.types = types;
  }

  public List<SimpleType> getTypes() {
    return Collections.unmodifiableList(types);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static SimpleTypeList newInstance(Token token, SimpleTypeList o) {
    return new SimpleTypeList(token, o.types);
  }

  // SimpleTypeList -> epsilon
  public static SimpleTypeList newInstance(Token token) {
    return new SimpleTypeList(token, new ArrayList<SimpleType>());
  }

  // SimpleTypeList -> SimpleType
  public static SimpleTypeList newInstance(Token token, SimpleType node) {
    List<SimpleType> types = new ArrayList<SimpleType>();
    types.add(node);
    return new SimpleTypeList(token, types);
  }

  // SimpleTypeList -> IMPLEMENTS SimpleTypeList
  public static SimpleTypeList newInstance(Token token, Terminal n0, SimpleType node) {
    return newInstance(token, node);
  }

  public static SimpleTypeList newInstance(Token token, SimpleTypeList o, SimpleType node) {
    List<SimpleType> types = o.types;
    types.add(node);
    return new SimpleTypeList(token, types);
  }

  // SimpleTypeList -> IMPLEMENTS SimpleTypeList
  public static SimpleTypeList newInstance(Token token, Terminal n0, SimpleTypeList o) {
    return newInstance(token, o);
  }

  // SimpleTypeList -> SimpleTypeList COMMA SimpleType
  public static SimpleTypeList newInstance(Token token, SimpleTypeList o, Terminal n0, SimpleType node) {
    return newInstance(token, o, node);
  }

}
