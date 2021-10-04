package ca.uwaterloo.joos1wc.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.uwaterloo.joos1wc.scanner.Token;

public class ExpressionList extends ASTNode {

  private List<Expression> expressions;

  public ExpressionList(Token token, List<Expression> expressions) {
    super(token);
    this.expressions = expressions;
  }

  public List<Expression> getExpressions() {
    return Collections.unmodifiableList(expressions);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ExpressionList newInstance(Token token, ExpressionList o) {
    return new ExpressionList(token, o.expressions);
  }

  // ExpressionList ->
  public static ExpressionList newInstance(Token token) {
    return new ExpressionList(token, new ArrayList<Expression>());
  }

  // ExpressionList -> Expression
  public static ExpressionList newInstance(Token token, Expression node) {
    List<Expression> expressions = new ArrayList<Expression>();
    expressions.add(node);
    return new ExpressionList(token, expressions);
  }

  // ExpressionList -> ExpressionList COMMA Expression
  public static ExpressionList newInstance(Token token, ExpressionList o, Terminal n0, Expression node) {
    List<Expression> expressions = o.expressions;
    expressions.add(node);
    return new ExpressionList(token, expressions);
  }

}
