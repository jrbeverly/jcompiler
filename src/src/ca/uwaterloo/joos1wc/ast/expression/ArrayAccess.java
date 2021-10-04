package ca.uwaterloo.joos1wc.ast.expression;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ArrayAccess extends Expression {

  public final Expression array;
  public final Expression index;

  public ArrayAccess(Token token, Expression array, Expression index) {
    super(token);
    this.array = array;
    this.index = index;
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ArrayAccess newInstance(Token token, ArrayAccess o) {
    return new ArrayAccess(token, o.array, o.index);
  }

  // ArrayAccess -> Expression [ Expression ]
  public static ArrayAccess newInstance(Token token, Expression array, Terminal n0, Expression index, Terminal n1) {
    return new ArrayAccess(token, array, index);
  }
}
