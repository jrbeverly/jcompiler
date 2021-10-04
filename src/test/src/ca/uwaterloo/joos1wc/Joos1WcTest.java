package ca.uwaterloo.joos1wc;

import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.CLASS;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.EOF;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.ID;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.LBRACE;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.LPAREN;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.PUBLIC;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.RBRACE;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.RPAREN;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.STATIC;
import static ca.uwaterloo.joos1wc.scanner.TerminalTokenKind.VOID;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class Joos1WcTest {
  private static final String file0 = "test/resources/unit/J1_finalclass2.java";
  private static final String file1 = "test/resources/unit/J1_minuschar.java";
  
  private static int line = 0;
  private static int posn = 0;
  
  private String[] files;
  private List<List<Token>> tokens;
  
  @Before
  public void setup() {
    files = new String[] {file0, file1};
    
    tokens = new ArrayList<List<Token>>();
    List<Token> list0 = Arrays.asList(
        makeToken(PUBLIC), makeToken(CLASS), makeToken(ID, "TestJoos"), makeToken(LBRACE),
        makeToken(PUBLIC), makeToken(ID, "TestJoos"), makeToken(LPAREN), makeToken(RPAREN), makeToken(LBRACE), makeToken(RBRACE),
        makeToken(RBRACE), makeToken(EOF)
      );
    tokens.add(list0);
    List<Token> list1 = Arrays.asList(
        makeToken(PUBLIC), makeToken(CLASS), makeToken(ID, "TestJoos1W"), makeToken(LBRACE),
        makeToken(PUBLIC), makeToken(ID, "TestJoos1W"), makeToken(LPAREN), makeToken(RPAREN), makeToken(LBRACE), makeToken(RBRACE),
        makeToken(PUBLIC), makeToken(STATIC), makeToken(VOID), makeToken(ID, "test"), makeToken(LPAREN), makeToken(RPAREN), makeToken(LBRACE), makeToken(RBRACE),
        makeToken(RBRACE), makeToken(EOF)
      );
    tokens.add(list1);
  }
  
  private Token makeToken(TokenKind kind) {
    return makeToken(kind, null);
  }
  private Token makeToken(TokenKind kind, String lexeme) {
    File file = new File("/tmp");
    String name = lexeme;
    if (name == null) {
      name = kind.name();
    }
    
    return new Token(file, kind, kind.name(), line++, posn++);
  }

  @Test
  public void tokenizeMultipleFiles() throws Exception {
    List<List<Token>> returnedTokens = Joos1Wc.tokenize(files);
    Assert.assertNotNull("Tokenize should return something", returnedTokens);
    Assert.assertEquals("Wrong number of token lists", files.length, returnedTokens.size());
    
    List<Token> tokens0 = returnedTokens.get(0);
    Assert.assertEquals("Wrong number of tokens in first file", 24, tokens0.size());
    List<Token> tokens1 = returnedTokens.get(1);
    Assert.assertEquals("Wrong number of tokens in second file",  25, tokens1.size());
  }
  
  @Test
  public void parseMultipleTokenLists() throws Exception {
    List<TreeNode> returnedTrees = Joos1Wc.parse(tokens);
    Assert.assertNotNull("Parse should return something", returnedTrees);
    Assert.assertEquals("Wrong number of trees", tokens.size(), returnedTrees.size());
    // kind of hard to test past that, at the moment
    // maybe make a counting visitor that just counts the nodes?
  }
  
}