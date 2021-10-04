package ca.uwaterloo.joos1wc.ast;

import java.util.ArrayDeque;
import java.util.Deque;

import ca.uwaterloo.joos1wc.ast.declaration.BodyDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
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
import ca.uwaterloo.joos1wc.ast.literal.BooleanLiteral;
import ca.uwaterloo.joos1wc.ast.literal.CharLiteral;
import ca.uwaterloo.joos1wc.ast.literal.IntLiteral;
import ca.uwaterloo.joos1wc.ast.literal.NullLiteral;
import ca.uwaterloo.joos1wc.ast.literal.StringLiteral;
import ca.uwaterloo.joos1wc.ast.statement.EmptyStatement;
import ca.uwaterloo.joos1wc.ast.statement.ExpressionStatement;
import ca.uwaterloo.joos1wc.ast.statement.ForStatement;
import ca.uwaterloo.joos1wc.ast.statement.IfThenStatement;
import ca.uwaterloo.joos1wc.ast.statement.ReturnStatement;
import ca.uwaterloo.joos1wc.ast.statement.Statement;
import ca.uwaterloo.joos1wc.ast.statement.VariableDeclarationStatement;
import ca.uwaterloo.joos1wc.ast.statement.WhileStatement;

public abstract class RecursiveVisitor implements Visitor {
  // the top of the stack is the parent of the node
  protected Deque<TreeNode> stack = new ArrayDeque<TreeNode>();

  @Override
  public void visit(ArrayAccess node) {
    preVisit(node);
    stack.push(node);
    node.array.accept(this);
    node.index.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ArrayCreation node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    node.dimExpr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ArrayType node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(Assignment node) {
    preVisit(node);
    stack.push(node);
    node.lhs.accept(this);
    node.rhs.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(Block node) {
    preVisit(node);
    stack.push(node);
    for (Statement stmt : node.statements) {
      stmt.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(BooleanLiteral node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(CastExpression node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    node.expr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(CharLiteral node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(ClassInstanceCreation node) {
    preVisit(node);
    stack.push(node);
    node.classType.accept(this);
    for (Expression arg : node.args) {
      arg.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(CompilationUnit node) {
    preVisit(node);
    stack.push(node);
    if (node.packageDecl != null) {
      node.packageDecl.accept(this);
    }
    for (ImportDeclaration importDecl : node.importDecls) {
      importDecl.accept(this);
    }
    for (TypeDeclaration typeDecl : node.typeDecls) {
      typeDecl.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(EmptyStatement node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(ExpressionStatement node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(FieldAccess node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(FieldDeclaration node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ForStatement node) {
    preVisit(node);
    stack.push(node);
    if (node.forInit != null) {
      node.forInit.accept(this);
    }
    if (node.condExpr != null) {
      node.condExpr.accept(this);
    }
    if (node.forUpdate != null) {
      node.forUpdate.accept(this);
    }
    node.statement.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(IfThenStatement node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    if (node.trueStatement != null) {
      node.trueStatement.accept(this);
    }
    if (node.falseStatement != null) {
      node.falseStatement.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ImportDeclaration node) {
    preVisit(node);
    stack.push(node);
    node.name.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(InfixExpression node) {
    preVisit(node);
    stack.push(node);
    node.lhs.accept(this);
    node.rhs.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(InstanceofExpression node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    node.referenceType.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(IntLiteral node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(MethodDeclaration node) {
    preVisit(node);
    stack.push(node);
    for (Modifier modifier : node.modifiers) {
      modifier.accept(this);
    }
    if (node.type != null) {
      node.type.accept(this);
    }
    for (VariableDeclaration param : node.formalParams) {
      param.accept(this);
    }
    if (node.body != null) {
      node.body.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(MethodInvocation node) {
    preVisit(node);
    stack.push(node);
    if (node.expr != null) {
      node.expr.accept(this);
    }
    if (node.qualifier != null) {
      node.qualifier.accept(this);
    }
    for (Expression arg : node.args) {
      arg.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(Modifier node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(NonTerminal node) {
    preVisit(node);
    stack.push(node);
    for (TreeNode child : node.children) {
      child.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(NullLiteral node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(PackageDeclaration node) {
    preVisit(node);
    stack.push(node);
    node.name.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ParenthesizedExpression node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(PrefixExpression node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(PrimitiveType node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(QualifiedName node) {
    preVisit(node);
    stack.push(node);
    node.qualifier.accept(this);
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(ReturnStatement node) {
    preVisit(node);
    stack.push(node);
    if (node.expr != null) {
      node.expr.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(SimpleName node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(SimpleType node) {
    preVisit(node);
    stack.push(node);
    if (node.name != null) {
      node.name.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(StringLiteral node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(Terminal node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(ThisExpression node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(TreeNode node) {
    preVisit(node);
    postVisit(node);
  }

  @Override
  public void visit(TypeDeclaration node) {
    preVisit(node);
    stack.push(node);
    for (Modifier modifier : node.modifiers) {
      visit(modifier);
    }
    if (node.isClass) {
      if (node.superclass != null)
        node.superclass.accept(this);
      for (SimpleType child : node.ifaces) {
        child.accept(this);
      }
    } else {
      for (SimpleType child : node.superifaces) {
        child.accept(this);
      }
    }
    for (BodyDeclaration bodyDecl : node.getBody()) {
      bodyDecl.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(VariableDeclaration node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(VariableDeclarationExpression node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(VariableDeclarationStatement node) {
    preVisit(node);
    stack.push(node);
    node.type.accept(this);
    if (node.initExpr != null) {
      node.initExpr.accept(this);
    }
    stack.pop();
    postVisit(node);
  }

  @Override
  public void visit(WhileStatement node) {
    preVisit(node);
    stack.push(node);
    node.expr.accept(this);
    node.statement.accept(this);
    stack.pop();
    postVisit(node);
  }

  protected void preVisit(ArrayAccess node) {
    preVisit((Expression) node);
  }

  protected void preVisit(ArrayCreation node) {
    preVisit((Expression) node);
  }

  protected void preVisit(ArrayType node) {
    preVisit((Type) node);
  }

  protected void preVisit(Assignment node) {
    preVisit((Expression) node);
  }

  protected void preVisit(Block node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(BodyDeclaration node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(BooleanLiteral node) {
    preVisit((Literal) node);
  }

  protected void preVisit(CastExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(CharLiteral node) {
    preVisit((Literal) node);
  }

  protected void preVisit(ClassInstanceCreation node) {
    preVisit((Expression) node);
  }

  protected void preVisit(CompilationUnit node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(EmptyStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(Expression node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(ExpressionStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(FieldAccess node) {
    preVisit((Expression) node);
  }

  protected void preVisit(FieldDeclaration node) {
    preVisit((BodyDeclaration) node);
  }

  protected void preVisit(ForStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(IfThenStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(ImportDeclaration node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(InfixExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(InstanceofExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(IntLiteral node) {
    preVisit((Literal) node);
  }

  protected void preVisit(Literal node) {
    preVisit((Expression) node);
  }

  protected void preVisit(MethodDeclaration node) {
    preVisit((BodyDeclaration) node);
  }

  protected void preVisit(MethodInvocation node) {
    preVisit((Expression) node);
  }

  protected void preVisit(Modifier node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(Name node) {
    preVisit((Expression) node);
  }

  protected void preVisit(NonTerminal node) {
    preVisit((TreeNode) node);
  }

  protected void preVisit(NullLiteral node) {
    preVisit((Literal) node);
  }

  protected void preVisit(PackageDeclaration node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(ParenthesizedExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(PrefixExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(PrimitiveType node) {
    preVisit((Type) node);
  }

  protected void preVisit(QualifiedName node) {
    preVisit((Name) node);
  }

  protected void preVisit(ReturnStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(SimpleName node) {
    preVisit((Name) node);
  }

  protected void preVisit(SimpleType node) {
    preVisit((Type) node);
  }

  protected void preVisit(Statement node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(StringLiteral node) {
    preVisit((Literal) node);
  }

  protected void preVisit(Terminal node) {
    preVisit((TreeNode) node);
  }

  protected void preVisit(ThisExpression node) {
    preVisit((Expression) node);
  }

  protected void preVisit(TreeNode node) {
  }

  protected void preVisit(Type node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(TypeDeclaration node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(VariableDeclaration node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(VariableDeclarationExpression node) {
    preVisit((ASTNode) node);
  }

  protected void preVisit(VariableDeclarationStatement node) {
    preVisit((Statement) node);
  }

  protected void preVisit(WhileStatement node) {
    preVisit((Statement) node);
  }

  protected void postVisit(ArrayAccess node) {
    postVisit((Expression) node);
  }

  protected void postVisit(ArrayCreation node) {
    postVisit((Expression) node);
  }

  protected void postVisit(ArrayType node) {
    postVisit((Type) node);
  }

  protected void postVisit(Assignment node) {
    postVisit((Expression) node);
  }

  protected void postVisit(Block node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(BodyDeclaration node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(BooleanLiteral node) {
    postVisit((Literal) node);
  }

  protected void postVisit(CastExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(CharLiteral node) {
    postVisit((Literal) node);
  }

  protected void postVisit(ClassInstanceCreation node) {
    postVisit((Expression) node);
  }

  protected void postVisit(CompilationUnit node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(EmptyStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(Expression node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(ExpressionStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(FieldAccess node) {
    postVisit((Expression) node);
  }

  protected void postVisit(FieldDeclaration node) {
    postVisit((BodyDeclaration) node);
  }

  protected void postVisit(ForStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(IfThenStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(ImportDeclaration node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(InfixExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(InstanceofExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(IntLiteral node) {
    postVisit((Literal) node);
  }

  protected void postVisit(Literal node) {
    postVisit((Expression) node);
  }

  protected void postVisit(MethodDeclaration node) {
    postVisit((BodyDeclaration) node);
  }

  protected void postVisit(MethodInvocation node) {
    postVisit((Expression) node);
  }

  protected void postVisit(Modifier node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(Name node) {
    postVisit((Expression) node);
  }

  protected void postVisit(NonTerminal node) {
    postVisit((TreeNode) node);
  }

  protected void postVisit(NullLiteral node) {
    postVisit((Literal) node);
  }

  protected void postVisit(PackageDeclaration node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(ParenthesizedExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(PrefixExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(PrimitiveType node) {
    postVisit((Type) node);
  }

  protected void postVisit(QualifiedName node) {
    postVisit((Name) node);
  }

  protected void postVisit(ReturnStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(SimpleName node) {
    postVisit((Name) node);
  }

  protected void postVisit(SimpleType node) {
    postVisit((Type) node);
  }

  protected void postVisit(Statement node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(StringLiteral node) {
    postVisit((Literal) node);
  }

  protected void postVisit(Terminal node) {
    postVisit((TreeNode) node);
  }

  protected void postVisit(ThisExpression node) {
    postVisit((Expression) node);
  }

  protected void postVisit(TreeNode node) {
  }

  protected void postVisit(Type node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(TypeDeclaration node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(VariableDeclaration node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(VariableDeclarationExpression node) {
    postVisit((ASTNode) node);
  }

  protected void postVisit(VariableDeclarationStatement node) {
    postVisit((Statement) node);
  }

  protected void postVisit(WhileStatement node) {
    postVisit((Statement) node);
  }

}
