package ca.uwaterloo.joos1wc.ast.expression;

import java.util.List;

import ca.uwaterloo.joos1wc.ast.Expression;
import ca.uwaterloo.joos1wc.ast.ExpressionList;
import ca.uwaterloo.joos1wc.ast.Literal;
import ca.uwaterloo.joos1wc.ast.Name;
import ca.uwaterloo.joos1wc.ast.QualifiedName;
import ca.uwaterloo.joos1wc.ast.Terminal;
import ca.uwaterloo.joos1wc.ast.Visitor;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.scanner.Token;

public class MethodInvocation extends Expression {

  public final Expression expr;
  public final Name qualifier;
  public final String methodName;
  public final List<Expression> args;

  public MethodDeclaration methodDecl;

  public MethodInvocation(Token token, Expression expr, Name qualifier, String methodName, List<Expression> args) {
    super(token);
    this.expr = expr;
    this.qualifier = qualifier;
    this.methodName = methodName;
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

  public static MethodInvocation newInstance(Token token, MethodInvocation o) {
    return new MethodInvocation(token, o.expr, o.qualifier, o.methodName, o.args);
  }

  // MethodInvocation -> Name ( ExpressionList )
  public static MethodInvocation newInstance(Token token, Name name, Terminal n0, ExpressionList args, Terminal n1) {
    Name qualifier = null;
    String methodName = name.identifier;
    if (name instanceof QualifiedName) {
      qualifier = ((QualifiedName) name).qualifier;
    }
    return new MethodInvocation(token, null, qualifier, methodName, args.getExpressions());
  }

  // MethodInvocation -> Expression . ID ( ExpressionList )
  public static MethodInvocation newInstance(Token token, Expression expr, Terminal n0, Terminal nameNode, Terminal n1,
      ExpressionList args, Terminal n2) {
    return new MethodInvocation(token, expr, null, nameNode.token.getImage(), args.getExpressions());
  }
}
