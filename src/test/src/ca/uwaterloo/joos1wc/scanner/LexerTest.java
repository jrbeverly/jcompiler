package ca.uwaterloo.joos1wc.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.diagnostics.JoosException;
import ca.uwaterloo.joos1wc.diagnostics.LexException;
import ca.uwaterloo.joos1wc.scanner.Lexer;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.scanner.TokenKind;
import ca.uwaterloo.joos1wc.utility.CharacterStream;

public class LexerTest {
  Token[][] tokens;
  File file = new File("/tmp/token");
  MockInputCursor cursor;
  List<DFAutomaton> dfas;
  Set<TokenKind> ignore;
  Lexer lexer;
  
  @Before
  public void setup() throws IOException {
    tokens = new Token[2][3];
    
    tokens[0][0] = new Token(file, TerminalTokenKind.NEQ, "!=", 1, 2);
    tokens[0][1] = null;
    tokens[0][2] = null;
    
    tokens[1][0] = new Token(file, TerminalTokenKind.BANG, "!", 1, 2);
    tokens[1][1] = new Token(file, TerminalTokenKind.ABSTRACT, "abstract", 1, 10);
    tokens[1][2] = new Token(file, TerminalTokenKind.WHITESPACE, "     ", 4, 4);
    
    // doesn't matter what we pass in...
    cursor = new MockInputCursor(new ByteArrayInputStream("xxxxxxxxxxxxxxxxxxxx".getBytes()));
    
    MockDFAutomaton dfa0 = new MockDFAutomaton(null, null, null);
    MockDFAutomaton dfa1 = new MockDFAutomaton(null, null, null);
    dfa0.setReturnTokens(tokens[0]);
    dfa1.setReturnTokens(tokens[1]);
    
    dfas = new ArrayList<DFAutomaton>(2);
    dfas.add(dfa0);
    dfas.add(dfa1);
    
    ignore = new HashSet<TokenKind>(0);
    
    lexer = new Lexer(dfas, ignore);
  }
  
  @Test
  public void getLongerTokenNull() throws IOException {
    DFAutomaton dfa = dfas.get(0);
    
    Token response0 = Lexer.getLongerToken(dfa, cursor, null);
    Assert.assertEquals("reset should have been called once", 1, cursor.resetCount);
    Assert.assertEquals("first token should have been returned",  tokens[0][0], response0);
    
    Token response1 = Lexer.getLongerToken(dfa, cursor, null);
    Assert.assertEquals("reset should have been called twice", 2, cursor.resetCount);
    Assert.assertNull("null should have been returned",  response1);
    
    Token response2 = Lexer.getLongerToken(dfa, cursor, response0);
    Assert.assertEquals("reset should have been called thrice", 3, cursor.resetCount);
    Assert.assertEquals("response0 should have been returned",  response0, response2);
  }
  
  @Test
  public void getLongerToken() throws IOException {
    DFAutomaton dfa = dfas.get(1);
    
    Token response0 = Lexer.getLongerToken(dfa, cursor, null);
    Assert.assertEquals("reset should have been called once", 1, cursor.resetCount);
    Assert.assertEquals("first token should have been returned",  tokens[1][0], response0);
    
    Token response1 = Lexer.getLongerToken(dfa, cursor, response0);
    Assert.assertEquals("reset should have been called twice", 2, cursor.resetCount);
    Assert.assertEquals("second token should have been returned", tokens[1][1], response1);
    
    Token response2 = Lexer.getLongerToken(dfa, cursor, response1);
    Assert.assertEquals("reset should have been called thrice", 3, cursor.resetCount);
    Assert.assertEquals("second token should have been returned",  tokens[1][1], response2);
  }
  
  @Test
  public void emptyInput() throws IOException, JoosException {
    CharacterStream empty = new MockInputCursor(new ByteArrayInputStream("".getBytes()));

    List<Token> returned = lexer.getTokensFromDFAs(empty);
    Assert.assertNotNull("returned should be empty but not null", returned);
    Assert.assertEquals("returned should be empty except for EOF", 1, returned.size());
    Assert.assertEquals("EOF is missing", TerminalTokenKind.EOF, returned.get(0).getKind());
    Assert.assertEquals("reset should not have been called", 0, cursor.resetCount);
  }
  
  @Test
  public void parseException() throws IOException  {
    // enough that we can't reach it
    cursor.setLength(100);
    boolean caught = false;
    List<Token> returned = null;
    try {
      returned = lexer.getTokensFromDFAs(cursor);
    } catch (LexException pe) {
      caught = true;
    }
    
    Assert.assertNull("returned should be null", returned);
    Assert.assertTrue("A ParseException should have been thrown", caught);
  }
  
  @Test
  public void getTokens() throws IOException, LexException {
    cursor.setLength(15);
    List<Token> returned = lexer.getTokensFromDFAs(cursor);
    
    Assert.assertNotNull("returned shouldn't be null", returned);
    Assert.assertEquals("returned has the wrong number of elements", 4, returned.size());
    Assert.assertEquals("reset called the wrong number of times", 6, cursor.resetCount);
    
    Assert.assertEquals("first element is wrong", tokens[0][0], returned.get(0));
    Assert.assertEquals("second element is wrong", tokens[1][1], returned.get(1));
    Assert.assertEquals("third element is wrong", tokens[1][2], returned.get(2));
    Assert.assertEquals("final element is wrong", TerminalTokenKind.EOF, returned.get(3).getKind());
    
    Assert.assertEquals("advanced the input the wrong amount", 15, cursor.advanced);
  }
  
  @Test
  public void endOfInput() throws IOException, LexException {
    // should end after 2 tokens
    cursor.setLength(10);
    List<Token> returned = lexer.getTokensFromDFAs(cursor);
    
    Assert.assertEquals("returned has the wrong number of elements", 3, returned.size());
    Assert.assertEquals("reset called the wrong number of times", 4, cursor.resetCount);
    
    Assert.assertEquals("first element is wrong", tokens[0][0], returned.get(0));
    Assert.assertEquals("second element is wrong", tokens[1][1], returned.get(1));
    Assert.assertEquals("final element is wrong", TerminalTokenKind.EOF, returned.get(2).getKind());
    
    Assert.assertEquals("advanced the input the wrong amount", 10, cursor.advanced);
  }
  
  @Test
  public void ignore() throws IOException, LexException {
    ignore.add(TerminalTokenKind.ABSTRACT);
    // basically repeat the getTokens test but this time whitespace shouldn't show up
    cursor.setLength(15);
    List<Token> returned = lexer.getTokensFromDFAs(cursor);

    Assert.assertNotNull("returned shouldn't be null", returned);
    Assert.assertEquals("returned has the wrong number of elements", 3, returned.size());
    Assert.assertEquals("reset called the wrong number of times", 6, cursor.resetCount);
    
    Assert.assertEquals("first element is wrong", tokens[0][0], returned.get(0));
    Assert.assertEquals("second element is wrong", tokens[1][2], returned.get(1));
    Assert.assertEquals("final element is wrong", TerminalTokenKind.EOF, returned.get(2).getKind());
    
    Assert.assertEquals("advanced the input the wrong amount", 15, cursor.advanced);
  }
}