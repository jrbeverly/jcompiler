package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.ast.Type.FormalType;
import ca.uwaterloo.joos1wc.ast.expression.ArrayAccess;
import ca.uwaterloo.joos1wc.ast.expression.ArrayCreation;
import ca.uwaterloo.joos1wc.ast.expression.Assignment;
import ca.uwaterloo.joos1wc.ast.expression.CastExpression;
import ca.uwaterloo.joos1wc.ast.expression.ClassInstanceCreation;
import ca.uwaterloo.joos1wc.ast.expression.FieldAccess;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.InstanceofExpression;
import ca.uwaterloo.joos1wc.ast.expression.MethodInvocation;
import ca.uwaterloo.joos1wc.ast.expression.ParenthesizedExpression;
import ca.uwaterloo.joos1wc.ast.expression.PrefixExpression;
import ca.uwaterloo.joos1wc.ast.expression.ThisExpression;
import ca.uwaterloo.joos1wc.ast.expression.VariableDeclarationExpression;
import ca.uwaterloo.joos1wc.diagnostics.CastException;
import ca.uwaterloo.joos1wc.diagnostics.ConstantEvaluationException;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public abstract class Expression extends ASTNode {

  // Should really be private with getters/setters
  public FormalType expressionType;
  public boolean isFinal = false;

  public Expression(Token token) {
    super(token);
  }

  // Should find a better way to do this abstract copy constructor - could use reflection?
  public static Expression newInstance(Token token, Expression o) {
    if (o instanceof ArrayAccess)
      return ArrayAccess.newInstance(token, (ArrayAccess) o);
    if (o instanceof ArrayCreation)
      return ArrayCreation.newInstance(token, (ArrayCreation) o);
    if (o instanceof Assignment)
      return Assignment.newInstance(token, (Assignment) o);
    if (o instanceof CastExpression)
      return CastExpression.newInstance(token, (CastExpression) o);
    if (o instanceof ClassInstanceCreation)
      return ClassInstanceCreation.newInstance(token, (ClassInstanceCreation) o);
    if (o instanceof FieldAccess)
      return FieldAccess.newInstance(token, (FieldAccess) o);
    if (o instanceof InfixExpression)
      return InfixExpression.newInstance(token, (InfixExpression) o);
    if (o instanceof InstanceofExpression)
      return InstanceofExpression.newInstance(token, (InstanceofExpression) o);
    if (o instanceof Literal)
      return Literal.newInstance(token, (Literal) o);
    if (o instanceof MethodInvocation)
      return MethodInvocation.newInstance(token, (MethodInvocation) o);
    if (o instanceof Name)
      return Name.newInstance(token, (Name) o);
    if (o instanceof ParenthesizedExpression)
      return ParenthesizedExpression.newInstance(token, (ParenthesizedExpression) o);
    if (o instanceof PrefixExpression)
      return PrefixExpression.newInstance(token, (PrefixExpression) o);
    if (o instanceof ThisExpression)
      return ThisExpression.newInstance(token, (ThisExpression) o);
    if (o instanceof VariableDeclarationExpression)
      return VariableDeclarationExpression.newInstance(token, (VariableDeclarationExpression) o);
    return null;
  }
  
  public abstract Literal constantValue() throws ConstantEvaluationException, CastException;

  // Expression -> LPAREN Expression RPAREN
  // Expression -> LBRACKET Expression RBRACKET
  public static Expression newInstance(Token token, Terminal n0, Expression o, Terminal n1) {
    if (n0.token.getKind() == TerminalTokenKind.LPAREN)
      return ParenthesizedExpression.newInstance(token, n0, o, n1);
    return newInstance(token, o);
  }

}
