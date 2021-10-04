package ca.uwaterloo.joos1wc.ast;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.joos1wc.ast.declaration.BodyDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.BodyDeclarationList;
import ca.uwaterloo.joos1wc.ast.declaration.FieldDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.ImportDeclarationList;
import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.PackageDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclarationList;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.VariableDeclarationList;
import ca.uwaterloo.joos1wc.ast.expression.ArrayAccess;
import ca.uwaterloo.joos1wc.ast.expression.ArrayCreation;
import ca.uwaterloo.joos1wc.ast.expression.Assignment;
import ca.uwaterloo.joos1wc.ast.expression.CastExpression;
import ca.uwaterloo.joos1wc.ast.expression.ClassInstanceCreation;
import ca.uwaterloo.joos1wc.ast.expression.FieldAccess;
import ca.uwaterloo.joos1wc.ast.expression.InfixExpression;
import ca.uwaterloo.joos1wc.ast.expression.InstanceofExpression;
import ca.uwaterloo.joos1wc.ast.expression.MethodInvocation;
import ca.uwaterloo.joos1wc.ast.expression.PrefixExpression;
import ca.uwaterloo.joos1wc.ast.expression.ThisExpression;
import ca.uwaterloo.joos1wc.ast.expression.VariableDeclarationExpression;
import ca.uwaterloo.joos1wc.ast.statement.EmptyStatement;
import ca.uwaterloo.joos1wc.ast.statement.ExpressionStatement;
import ca.uwaterloo.joos1wc.ast.statement.ForStatement;
import ca.uwaterloo.joos1wc.ast.statement.IfThenStatement;
import ca.uwaterloo.joos1wc.ast.statement.ReturnStatement;
import ca.uwaterloo.joos1wc.ast.statement.Statement;
import ca.uwaterloo.joos1wc.ast.statement.VariableDeclarationStatement;
import ca.uwaterloo.joos1wc.ast.statement.WhileStatement;
import ca.uwaterloo.joos1wc.codegen.AssemblyNode;
import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

@SuppressWarnings("rawtypes")
public abstract class ASTNode extends TreeNode {
  
  // either address is expected or value
  public boolean addressExpected = false;
  public AssemblyNode asm;

  public ASTNode(Token token) {
    super(token);
  }
  
  /*
   * Production rules for ASTNode construction
   * These rules should follow the joo1w.cfgx grammar file and mapping CFG rules to a ASTNode production
   */
  private static final Map<String, ASTProduction[]> AST_PRODUCTION_MAP =
      new HashMap<String, ASTProduction[]>();

  static {
    // 3: lexical structure
    AST_PRODUCTION_MAP.put("Literal", new ASTProduction[] {
        new ASTProduction(Literal.class, new Class[] { Token.class, Terminal.class }) });
    
    // 4: types, values, and variables
    AST_PRODUCTION_MAP.put("Type", new ASTProduction[] {
        new ASTProduction(Type.class, new Class[] { Token.class, Type.class }) });
    AST_PRODUCTION_MAP.put("PrimitiveType", new ASTProduction[] {
        new ASTProduction(PrimitiveType.class, new Class[] { Token.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("ReferenceType", new ASTProduction[] {
        new ASTProduction(Type.class, new Class[] { Token.class, Type.class }) });
    AST_PRODUCTION_MAP.put("ClassOrInterfaceType", new ASTProduction[] {
        new ASTProduction(SimpleType.class, new Class[] { Token.class, Name.class }) });
    AST_PRODUCTION_MAP.put("ClassType", new ASTProduction[] {
        new ASTProduction(SimpleType.class, new Class[] { Token.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("InterfaceType", new ASTProduction[] {
        new ASTProduction(SimpleType.class, new Class[] { Token.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("ArrayType", new ASTProduction[] {
        new ASTProduction(ArrayType.class, new Class[] { Token.class, PrimitiveType.class, Terminal.class, Terminal.class }),
        new ASTProduction(ArrayType.class, new Class[] { Token.class, Name.class, Terminal.class, Terminal.class }) });
    
    // 6: names
    AST_PRODUCTION_MAP.put("Name", new ASTProduction[] {
        new ASTProduction(Name.class, new Class[] { Token.class, Name.class }) });
    AST_PRODUCTION_MAP.put("SimpleName", new ASTProduction[] {
        new ASTProduction(SimpleName.class, new Class[] { Token.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("QualifiedName", new ASTProduction[] {
        new ASTProduction(QualifiedName.class, new Class[] { Token.class, Name.class, Terminal.class, SimpleName.class }) });
    
    // 7: packages
    AST_PRODUCTION_MAP.put("CompilationUnit", new ASTProduction[] {
        new ASTProduction(CompilationUnit.class, new Class[] { Token.class, PackageDeclaration.class,
          ImportDeclarationList.class, TypeDeclarationList.class }),
        new ASTProduction(CompilationUnit.class, new Class[] { Token.class, Null.class,
          ImportDeclarationList.class, TypeDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("ImportDeclarations", new ASTProduction[] {
        new ASTProduction(ImportDeclarationList.class, new Class[] { Token.class, ImportDeclaration.class }),
        new ASTProduction(ImportDeclarationList.class, new Class[] { Token.class, ImportDeclarationList.class,
          ImportDeclaration.class }) });
    AST_PRODUCTION_MAP.put("ImportDeclarations(opt)", new ASTProduction[] {
        new ASTProduction(ImportDeclarationList.class, new Class[] { Token.class }),
        new ASTProduction(ImportDeclarationList.class, new Class[] { Token.class, ImportDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("TypeDeclarations", new ASTProduction[] {
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class, TypeDeclaration.class }),
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class, TypeDeclarationList.class,
          TypeDeclaration.class }),
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class, Null.class }),
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class, TypeDeclarationList.class,
          Null.class }) });
    AST_PRODUCTION_MAP.put("TypeDeclarations(opt)", new ASTProduction[] {
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class }),
        new ASTProduction(TypeDeclarationList.class, new Class[] { Token.class, TypeDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("PackageDeclaration", new ASTProduction[] {
        new ASTProduction(PackageDeclaration.class, new Class[] { Token.class, Terminal.class, Name.class,
          Terminal.class }) });
    AST_PRODUCTION_MAP.put("PackageDeclaration(opt)", new ASTProduction[] {
        new ASTProduction(PackageDeclaration.class, new Class[] { Token.class, PackageDeclaration.class }),
        new ASTProduction(Null.class, new Class[] { Token.class }) });
    AST_PRODUCTION_MAP.put("ImportDeclaration", new ASTProduction[] {
        new ASTProduction(ImportDeclaration.class, new Class[] { Token.class, ImportDeclaration.class }) });
    AST_PRODUCTION_MAP.put("SingleTypeImportDeclaration", new ASTProduction[] {
        new ASTProduction(ImportDeclaration.class, new Class[] { Token.class, Terminal.class, Name.class,
          Terminal.class }) });
    AST_PRODUCTION_MAP.put("TypeImportOnDemandDeclaration", new ASTProduction[] {
        new ASTProduction(ImportDeclaration.class, new Class[] { Token.class, Terminal.class, Name.class,
          Terminal.class, Terminal.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("TypeDeclaration", new ASTProduction[] {
        new ASTProduction(TypeDeclaration.class, new Class[] { Token.class, TypeDeclaration.class }),
        new ASTProduction(Null.class, new Class[] { Token.class, Terminal.class }) });
    
    // modifiers (only used in LALR(1) grammar)
    AST_PRODUCTION_MAP.put("Modifier", new ASTProduction[] {
        new ASTProduction(Modifier.class, new Class[] { Token.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("Modifiers", new ASTProduction[] {
        new ASTProduction(ModifierList.class, new Class[] { Token.class, Modifier.class }),
        new ASTProduction(ModifierList.class, new Class[] { Token.class, ModifierList.class, Modifier.class }) });
    AST_PRODUCTION_MAP.put("Modifiers(opt)", new ASTProduction[] {
        new ASTProduction(ModifierList.class, new Class[] { Token.class }),
        new ASTProduction(ModifierList.class, new Class[] { Token.class, ModifierList.class }) });
    
    // 8.1: class declaration
    AST_PRODUCTION_MAP.put("ClassDeclaration", new ASTProduction[] {
        new ASTProduction(TypeDeclaration.class, new Class[] { Token.class, ModifierList.class, Terminal.class,
          Terminal.class, SimpleType.class, SimpleTypeList.class, BodyDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("Super", new ASTProduction[] {
        new ASTProduction(SimpleType.class, new Class[] { Token.class, Terminal.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("Super(opt)", new ASTProduction[] {
        new ASTProduction(SimpleType.class, new Class[] { Token.class }),
        new ASTProduction(SimpleType.class, new Class[] { Token.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("Interfaces", new ASTProduction[] {
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, Terminal.class, SimpleTypeList.class }) });
    AST_PRODUCTION_MAP.put("Interfaces(opt)", new ASTProduction[] {
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class }),
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, SimpleTypeList.class }) });
    AST_PRODUCTION_MAP.put("InterfaceTypeList", new ASTProduction[] {
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, SimpleType.class }),
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, SimpleTypeList.class, Terminal.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("ClassBody", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, Terminal.class,
          BodyDeclarationList.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("ClassBodyDeclarations", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclaration.class }),
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclarationList.class,
          BodyDeclaration.class }) });
    AST_PRODUCTION_MAP.put("ClassBodyDeclarations(opt)", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class }),
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("ClassBodyDeclaration", new ASTProduction[] {
        new ASTProduction(BodyDeclaration.class, new Class[] { Token.class, BodyDeclaration.class }) });
    AST_PRODUCTION_MAP.put("ClassMemberDeclaration", new ASTProduction[] {
        new ASTProduction(BodyDeclaration.class, new Class[] { Token.class, BodyDeclaration.class }) });
    
    // 8.3: field declarations
    AST_PRODUCTION_MAP.put("FieldDeclaration", new ASTProduction[] {
        new ASTProduction(FieldDeclaration.class, new Class[] { Token.class, ModifierList.class, Type.class,
          Terminal.class, Terminal.class }),
        new ASTProduction(FieldDeclaration.class, new Class[] { Token.class, ModifierList.class, Type.class,
          Terminal.class, Terminal.class, Expression.class, Terminal.class }) });
    
    // 8.4: method declarations
    AST_PRODUCTION_MAP.put("MethodDeclaration", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, MethodDeclaration.class, Block.class }) });
    AST_PRODUCTION_MAP.put("AbstractMethodDeclaration", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, MethodDeclaration.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("MethodHeader", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, ModifierList.class, Type.class,
          MethodDeclaration.class }),
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, ModifierList.class, Terminal.class,
          MethodDeclaration.class }) });
    AST_PRODUCTION_MAP.put("MethodDeclarator", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, Terminal.class, Terminal.class,
          VariableDeclarationList.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("FormalParameterList", new ASTProduction[] {
        new ASTProduction(VariableDeclarationList.class, new Class[] { Token.class, VariableDeclaration.class }),
        new ASTProduction(VariableDeclarationList.class, new Class[] { Token.class, VariableDeclarationList.class,
          Terminal.class, VariableDeclaration.class }) });
    AST_PRODUCTION_MAP.put("FormalParameterList(opt)", new ASTProduction[] {
        new ASTProduction(VariableDeclarationList.class, new Class[] { Token.class }),
        new ASTProduction(VariableDeclarationList.class, new Class[] { Token.class, VariableDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("FormalParameter", new ASTProduction[] {
        new ASTProduction(VariableDeclaration.class, new Class[] { Token.class, Type.class, Terminal.class }) });
    
    // 8.6: constructor declarations
    AST_PRODUCTION_MAP.put("ConstructorDeclaration", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, ModifierList.class,
          MethodDeclaration.class, Block.class }) });
    AST_PRODUCTION_MAP.put("ConstructorDeclarator", new ASTProduction[] {
        new ASTProduction(MethodDeclaration.class, new Class[] { Token.class, Terminal.class, Terminal.class,
          VariableDeclarationList.class, Terminal.class }) });
    
    // 9.1: interface declarations
    AST_PRODUCTION_MAP.put("InterfaceDeclaration", new ASTProduction[] {
        new ASTProduction(TypeDeclaration.class, new Class[] { Token.class, ModifierList.class, Terminal.class, Terminal.class,
          SimpleTypeList.class, BodyDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("ExtendsInterfaces", new ASTProduction[] {
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, Terminal.class, SimpleType.class }),
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, SimpleTypeList.class, Terminal.class, SimpleType.class }) });
    AST_PRODUCTION_MAP.put("ExtendsInterfaces(opt)", new ASTProduction[] {
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class }),
        new ASTProduction(SimpleTypeList.class, new Class[] { Token.class, SimpleTypeList.class }) });
    AST_PRODUCTION_MAP.put("InterfaceBody", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, Terminal.class,
          BodyDeclarationList.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("InterfaceMemberDeclarations", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclaration.class }),
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclarationList.class,
          BodyDeclaration.class }) });
    AST_PRODUCTION_MAP.put("InterfaceMemberDeclarations(opt)", new ASTProduction[] {
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class }),
        new ASTProduction(BodyDeclarationList.class, new Class[] { Token.class, BodyDeclarationList.class }) });
    AST_PRODUCTION_MAP.put("InterfaceMemberDeclaration", new ASTProduction[] {
        new ASTProduction(BodyDeclaration.class, new Class[] { Token.class, BodyDeclaration.class }) });
    
    // 14: statements
    AST_PRODUCTION_MAP.put("Block", new ASTProduction[] {
        new ASTProduction(Block.class, new Class[] { Token.class, Statement.class }),
        new ASTProduction(Block.class, new Class[] { Token.class, Block.class, Statement.class }),
        new ASTProduction(Block.class, new Class[] { Token.class, Terminal.class, Block.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("BlockStatements", new ASTProduction[] {
        new ASTProduction(Block.class, new Class[] { Token.class, Statement.class }),
        new ASTProduction(Block.class, new Class[] { Token.class, Block.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("BlockStatements(opt)", new ASTProduction[] {
        new ASTProduction(Block.class, new Class[] { Token.class }),
        new ASTProduction(Block.class, new Class[] { Token.class, Block.class }) });
    AST_PRODUCTION_MAP.put("BlockStatement", new ASTProduction[] {
        new ASTProduction(Statement.class, new Class[] { Token.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("LocalVariableDeclarationStatement", new ASTProduction[] {
        new ASTProduction(VariableDeclarationStatement.class, new Class[] { Token.class, VariableDeclaration.class,
          Terminal.class }) });
    AST_PRODUCTION_MAP.put("LocalVariableDeclaration", new ASTProduction[] {
        new ASTProduction(VariableDeclaration.class, new Class[] { Token.class, Type.class, Terminal.class,
          Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("Statement", new ASTProduction[] {
        new ASTProduction(Statement.class, new Class[] { Token.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("StatementNoShortIf", new ASTProduction[] {
        new ASTProduction(Statement.class, new Class[] { Token.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("StatementWithoutTrailingSubstatement", new ASTProduction[] {
        new ASTProduction(Statement.class, new Class[] { Token.class, Statement.class }),
        new ASTProduction(EmptyStatement.class, new Class[] { Token.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("ExpressionStatement", new ASTProduction[] {
        new ASTProduction(ExpressionStatement.class, new Class[] { Token.class, Expression.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("StatementExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("IfThenStatement", new ASTProduction[] {
        new ASTProduction(IfThenStatement.class, new Class[] { Token.class, Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("IfThenElseStatement", new ASTProduction[] {
        new ASTProduction(IfThenStatement.class, new Class[] { Token.class, Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Statement.class, Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("IfThenElseStatementNoShortIf", new ASTProduction[] {
        new ASTProduction(IfThenStatement.class, new Class[] { Token.class, Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Statement.class, Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("WhileStatement", new ASTProduction[] {
        new ASTProduction(WhileStatement.class, new Class[] { Token.class, Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("WhileStatementNoShortIf", new ASTProduction[] {
        new ASTProduction(WhileStatement.class, new Class[] { Token.class, Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("ForStatement", new ASTProduction[] {
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Expression.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Expression.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Null.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Null.class, Terminal.class, Null.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Null.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Null.class, Terminal.class, Null.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Expression.class, Terminal.class, Null.class, Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("ForStatementNoShortIf", new ASTProduction[] {
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Expression.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Expression.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Null.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Null.class,
          Terminal.class, Null.class, Terminal.class, Null.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Null.class, Terminal.class, Expression.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Null.class, Terminal.class, Null.class, Terminal.class, Statement.class }),
        new ASTProduction(ForStatement.class, new Class[] { Token.class,  Terminal.class, Terminal.class, Expression.class,
          Terminal.class, Expression.class, Terminal.class, Null.class, Terminal.class, Statement.class }) });
    AST_PRODUCTION_MAP.put("ForInit", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(VariableDeclarationExpression.class, new Class[] { Token.class, VariableDeclaration.class }) });
    AST_PRODUCTION_MAP.put("ForInit(opt)", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(Null.class, new Class[] { Token.class }) });
    AST_PRODUCTION_MAP.put("ForUpdate", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("ForUpdate(opt)", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(Null.class, new Class[] { Token.class }) });
    AST_PRODUCTION_MAP.put("StatementExpressionList", new ASTProduction[] {
        new ASTProduction(ExpressionList.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(ExpressionList.class, new Class[] { Token.class, ExpressionList.class, Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("ReturnStatement", new ASTProduction[] {
        new ASTProduction(ReturnStatement.class, new Class[] { Token.class, Terminal.class, Expression.class, Terminal.class }),
        new ASTProduction(ReturnStatement.class, new Class[] { Token.class, Terminal.class, Null.class, Terminal.class }) });
    
    // 15: expressions
    AST_PRODUCTION_MAP.put("Primary", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("PrimaryNoNewArray", new ASTProduction[] {
        new ASTProduction(ThisExpression.class, new Class[] { Token.class, Terminal.class }),
        new ASTProduction(Expression.class, new Class[] { Token.class, Terminal.class, Expression.class, Terminal.class }),
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("ClassInstanceCreationExpression", new ASTProduction[] {
        new ASTProduction(ClassInstanceCreation.class, new Class[] { Token.class, Terminal.class, SimpleType.class, Terminal.class,
          ExpressionList.class, Terminal.class}) });
    AST_PRODUCTION_MAP.put("ArgumentList(opt)", new ASTProduction[] {
        new ASTProduction(ExpressionList.class, new Class[] { Token.class }),
        new ASTProduction(ExpressionList.class, new Class[] { Token.class, ExpressionList.class }) });
    AST_PRODUCTION_MAP.put("ArgumentList", new ASTProduction[] {
        new ASTProduction(ExpressionList.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(ExpressionList.class, new Class[] { Token.class, ExpressionList.class, Terminal.class,
          Expression.class }) });
    AST_PRODUCTION_MAP.put("ArrayCreationExpression", new ASTProduction[] {
        new ASTProduction(ArrayCreation.class, new Class[] { Token.class, Terminal.class, PrimitiveType.class, Expression.class }),
        new ASTProduction(ArrayCreation.class, new Class[] { Token.class, Terminal.class, SimpleType.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("DimExpr", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Terminal.class, Expression.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("FieldAccess", new ASTProduction[] {
        new ASTProduction(FieldAccess.class, new Class[] { Token.class, Expression.class, Terminal.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("MethodInvocation", new ASTProduction[] {
        new ASTProduction(MethodInvocation.class, new Class[] { Token.class, Name.class, Terminal.class,
          ExpressionList.class, Terminal.class }),
        new ASTProduction(MethodInvocation.class, new Class[] { Token.class, Expression.class, Terminal.class,
          Terminal.class, Terminal.class, ExpressionList.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("ArrayAccess", new ASTProduction[] {
        new ASTProduction(ArrayAccess.class, new Class[] { Token.class, Expression.class, Terminal.class,
          Expression.class, Terminal.class }) });
    AST_PRODUCTION_MAP.put("UnaryExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(PrefixExpression.class, new Class[] { Token.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("UnaryExpressionNotPlusMinus", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(Name.class, new Class[] { Token.class, Name.class }),
        new ASTProduction(PrefixExpression.class, new Class[] { Token.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("CastExpression", new ASTProduction[] {
        new ASTProduction(CastExpression.class, new Class[] { Token.class, Terminal.class, PrimitiveType.class, Terminal.class,
          Terminal.class, Terminal.class, Expression.class }),
        new ASTProduction(CastExpression.class, new Class[] { Token.class, Terminal.class, PrimitiveType.class,
          Terminal.class, Expression.class }),
        new ASTProduction(CastExpression.class, new Class[] { Token.class, Terminal.class, Expression.class, Terminal.class, Expression.class }),
        new ASTProduction(CastExpression.class, new Class[] { Token.class, Terminal.class, Name.class, Terminal.class, Terminal.class,
          Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("MultiplicativeExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("AdditiveExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("RelationalExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InstanceofExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Type.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("EqualityExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("AndExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("InclusiveOrExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("ConditionalAndExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("ConditionalOrExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(InfixExpression.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("AssignmentExpression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("Assignment", new ASTProduction[] {
        new ASTProduction(Assignment.class, new Class[] { Token.class, Expression.class, Terminal.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("LeftHandSide", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("Expression", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }) });
    AST_PRODUCTION_MAP.put("Expression(opt)", new ASTProduction[] {
        new ASTProduction(Expression.class, new Class[] { Token.class, Expression.class }),
        new ASTProduction(Null.class, new Class[] { Token.class }) });
  }

  /*
   * Factory method to create the appropriate ASTNode for a given token
   */
  public static ASTNode newNonTerminalNode(Token token, List<TreeNode> children) {
    if (!(token.getKind() instanceof NonTerminalTokenKind)) {
      return null;
    }
    
    NonTerminalTokenKind tk = (NonTerminalTokenKind) token.getKind();
    ASTProduction[] productions = AST_PRODUCTION_MAP.get(tk.name());
    if (productions == null) return null;
    
    for (ASTProduction production : productions) {
      if (production.accepts(children)) {
        Object[] args = new Object[children.size()+1];
        args[0] = token;
        for (int i = 0; i < children.size(); i++)
          args[i+1] = children.get(i);
        
        try {
          return (ASTNode) production.lhsClass.getMethod("newInstance", production.rhsClasses).invoke(null, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
            | SecurityException e) {
          System.err.println(production.lhsClass);
          e.printStackTrace();
          throw new RuntimeException("Error while generating AST");
        }
      }
    }
    
    return null;
  }

  public static ASTNode newTerminalNode(Token token) {
    if (!(token.getKind() instanceof TerminalTokenKind)) {
      return null;
    }

    TerminalTokenKind tk = (TerminalTokenKind) token.getKind();

    switch (tk) {
    default:
      return null;
    }
  }
  
  private static class ASTProduction {
    final Class<? extends ASTNode> lhsClass;
    final Class[] rhsClasses;
    
    public ASTProduction(Class<? extends ASTNode> lhsClass, Class[] rhsClasses) {
      this.lhsClass = lhsClass;
      this.rhsClasses = rhsClasses;
    }
    
    public boolean accepts(List<TreeNode> rhsObjs) {
      if (rhsClasses.length != rhsObjs.size() + 1) return false;
      for (int i = 1; i < rhsClasses.length; i++) {
        if (!rhsClasses[i].isInstance(rhsObjs.get(i-1))) return false;
      }
      return true;
    }
  }

}
