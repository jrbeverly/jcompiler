package ca.uwaterloo.joos1wc.ast.expression;

import java.util.List;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.ExpressionList;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.SimpleType;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class ClassInstanceCreation extends Expression {

  public final SimpleType classType;
  public final List<Expression> args;
  public MethodDeclaration constructor;

  public ClassInstanceCreation(Token token, SimpleType classType, List<Expression> args) {
    super(token);
    this.classType = classType;
    this.args = args;
  }
  
  @Override
  public Literal constantValue() {
    return null;
  }

  @Override
  public void accept(Visitor v) {
    v.visit(this);
  }

  public static ClassInstanceCreation newInstance(Token token, ClassInstanceCreation o) {
    return new ClassInstanceCreation(token, o.classType, o.args);
  }

  // ClassInstanceCreation -> NEW Name LPAREN ExpressionList RPAREN
  public static ClassInstanceCreation newInstance(Token token, Terminal n0, SimpleType classType, Terminal n1,
      ExpressionList args, Terminal n2) {
    return new ClassInstanceCreation(token, classType, args.getExpressions());
  }
}
