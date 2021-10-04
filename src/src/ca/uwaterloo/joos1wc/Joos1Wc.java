package ca.uwaterloo.joos1wc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.uwaterloo.joos1wc.analysis.HierarchyChecker;
import ca.uwaterloo.joos1wc.analysis.NameLinkerVisitor;
import ca.uwaterloo.joos1wc.analysis.PackageTable;
import ca.uwaterloo.joos1wc.analysis.ReachabilityVisitor;
import ca.uwaterloo.joos1wc.analysis.SymbolVisitor;
import ca.uwaterloo.joos1wc.analysis.TypeChecker;
import ca.uwaterloo.joos1wc.analysis.TypeLinkerVisitor;
import ca.uwaterloo.joos1wc.ast.CompilationUnit;
import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;
import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.codegen.CodeGenUtils;
import ca.uwaterloo.joos1wc.codegen.CodeGenVisitor;
import ca.uwaterloo.joos1wc.codegen.TypeHierarchyTable;
import ca.uwaterloo.joos1wc.diagnostics.Diagnostics;
import ca.uwaterloo.joos1wc.diagnostics.JoosException;
import ca.uwaterloo.joos1wc.diagnostics.LRException;
import ca.uwaterloo.joos1wc.diagnostics.LexException;
import ca.uwaterloo.joos1wc.diagnostics.ParseException;
import ca.uwaterloo.joos1wc.diagnostics.ValidationException;
import ca.uwaterloo.joos1wc.parse.LRGrammar;
import ca.uwaterloo.joos1wc.parse.LRParser;
import ca.uwaterloo.joos1wc.parse.LRParseState;
import ca.uwaterloo.joos1wc.parse.TreePrinter;
import ca.uwaterloo.joos1wc.parse.Weeder;
import ca.uwaterloo.joos1wc.scanner.Lexer;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class Joos1Wc {
  private static final int EXIT_CODE_ERROR = 42;
  private static final int EXIT_CODE_NUM_ARGUMENTS = 2;
  private static final String GRAMMAR_FILE = "tools/joos1w.lalr1";
  public static File CWD = new File(System.getProperty("user.dir")); // To allow E2E test to change working directory
  public static boolean DEBUG = false;

  public static PackageTable DEFAULT_PKG = null;
  public static TypeHierarchyTable TYPE_HIERARCHY_TABLE = null;

  public static void main(String[] args) {
    if (args.length > 0) {
      try {
        run(args);

        // unless we decide to care, let's just catch everything and drop out
        // with a useful message and an error code.
      } catch (NullPointerException exception) {
        exception.printStackTrace();
        System.exit(EXIT_CODE_ERROR);
      } catch (Exception exception) {
        System.err.println(exception.getMessage());
        if (DEBUG) {
          exception.printStackTrace();
        }
        System.exit(EXIT_CODE_ERROR);
      }
    } else {
      System.err.println("At least one command line argument is required.");
      System.exit(EXIT_CODE_NUM_ARGUMENTS);
    }
  }

  static void run(String[] sourceFileNames) throws FileNotFoundException, IOException, JoosException,
      ValidationException {
    Diagnostics diagnostics = new Diagnostics();
    List<List<Token>> allTokens = tokenize(sourceFileNames);

    if (DEBUG) {
      for (List<Token> tokens : allTokens) {
        Token.printTokenList(tokens);
      }
    }

    List<TreeNode> allTrees = parse(allTokens);
    DEFAULT_PKG = new PackageTable();

    for (TreeNode root : allTrees) {
      if (DEBUG) {
        root.accept(new TreePrinter());
      }
      root.accept(new Weeder());

      SymbolVisitor v = new SymbolVisitor(diagnostics);
      root.accept(v);
      checkErrors(diagnostics);
    }

    for (TreeNode root : allTrees) {
      TypeLinkerVisitor v = new TypeLinkerVisitor(diagnostics);
      root.accept(v);
      checkErrors(diagnostics);
    }

    new HierarchyChecker(diagnostics);
    checkErrors(diagnostics);

    for (TreeNode root : allTrees) {
      NameLinkerVisitor v = new NameLinkerVisitor(diagnostics);
      root.accept(v);
      checkErrors(diagnostics);
    }

    new TypeChecker(diagnostics);
    checkErrors(diagnostics);

    for (TreeNode root : allTrees) {
      ReachabilityVisitor v = new ReachabilityVisitor(diagnostics);
      root.accept(v);
      checkErrors(v.getErrors());
    }

    TYPE_HIERARCHY_TABLE = new TypeHierarchyTable(allTrees);
    CodeGenVisitor codeGenVisitor = new CodeGenVisitor();
    for (TreeNode root : allTrees) {
      root.accept(codeGenVisitor);
    }
    CodeGenUtils.addStartBlock(allTrees);

    for (TreeNode root : allTrees) {
      CompilationUnit unit = (CompilationUnit) root;
      for (TypeDeclaration typeDecl : unit.typeDecls) {
        String outputFilename = String.format("output/%s.s", typeDecl.getCanonicalName());
        typeDecl.asm.print(new PrintStream(new FileOutputStream(new File(Joos1Wc.CWD, outputFilename))));
      }
    }
    // CodeGenUtils.writeTypeTable("typeTable");
  }

  static void checkErrors(Diagnostics diag) throws JoosException {
    Collection<JoosException> errors = diag.getErrors();
    if (!errors.isEmpty()) {
      // For now, just throw the first error we see
      throw errors.iterator().next();
    }
  }

  static List<List<Token>> tokenize(String[] sourceFileNames) throws FileNotFoundException, IOException,
      ParseException, LexException {
    // generate a list of DFAutomaton for Lexing
    List<DFAutomaton> dfaList = new ArrayList<DFAutomaton>();
    for (TerminalTokenKind kind : TerminalTokenKind.values()) {
      dfaList.add(kind.getDFAutomaton());
    }
    Set<TokenKind> ignore = new HashSet<TokenKind>(2);
    ignore.add(TerminalTokenKind.WHITESPACE);
    ignore.add(TerminalTokenKind.COMMENT);
    Lexer lexer = new Lexer(dfaList, ignore);
    List<List<Token>> allTokens = new ArrayList<List<Token>>();

    for (String sourceFileName : sourceFileNames) {
      File sourceFile = new File(sourceFileName);
      allTokens.add(lexer.lex(sourceFile));
    }

    return allTokens;
  }

  static List<TreeNode> parse(List<List<Token>> allTokens) throws FileNotFoundException, IOException, ParseException,
      LRException {
    LRGrammar grammar = new LRGrammar();
    LRParseState startState = grammar.fromFile(new FileInputStream(GRAMMAR_FILE));
    LRParser parser = new LRParser(startState);

    List<TreeNode> trees = new ArrayList<TreeNode>(allTokens.size());
    for (List<Token> tokens : allTokens) {
      trees.add(parser.parse(tokens));
    }

    return trees;
  }
}
