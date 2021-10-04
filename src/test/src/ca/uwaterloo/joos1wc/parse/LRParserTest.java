package ca.uwaterloo.joos1wc.parse;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.diagnostics.LRException;
import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class LRParserTest {
  // example grammar given in class for LR0 
  // (but with follows so it works here, I know it's not LR0)
  Production[] rules;
  LRParseState[] states;
  LRParser parser;
  
  TokenKind s, e, plus, t, id;
  
  Deque<TreeNode> symbolStack;
  Deque<LRParseState> stateStack;
  
  @Before
  public void setup() {
    s = new NonTerminalTokenKind("S");
    e = new NonTerminalTokenKind("E");
    plus = new NonTerminalTokenKind("+");
    t = new NonTerminalTokenKind("T");
    id = new NonTerminalTokenKind("id");
        
    rules = new Production[] {
      // S -> E $
      new Production(s, Arrays.asList(e, TerminalTokenKind.EOF)),
      // E -> E + T
      new Production(e, Arrays.asList(e, plus, t)),
      // E -> T
      new Production(e, Arrays.asList(t)),
      // T -> id
      new Production(t, Arrays.asList(id))
    };
    
    states = new LRParseState[] {
      new LRParseState(0),
      new LRParseState(1),
      new LRParseState(2),
      new LRParseState(3),
      new LRParseState(4),
      new LRParseState(5),
      new LRParseState(6)
    };
    // state 0 shifts E, T, id
    states[0].addShift(e, states[1]);
    states[0].addShift(t, states[3]);
    states[0].addShift(id, states[4]);
    // state 1 shifts $ and +
    states[1].addShift(TerminalTokenKind.EOF, states[2]);
    states[1].addShift(plus, states[5]);
    // state 2 reduces... well it reduces
    // state 3 reduces with a follow set of $, +
    states[3].addReduce(plus, rules[2]);
    states[3].addReduce(TerminalTokenKind.EOF, rules[2]);
    // state 4 reduces with a follow set of $, +
    states[4].addReduce(plus, rules[3]);
    states[4].addReduce(TerminalTokenKind.EOF, rules[3]);
    // state 5 shifts T and id
    states[5].addShift(t, states[6]);
    states[5].addShift(id, states[4]);
    // state 6 reduces $
    states[6].addReduce(TerminalTokenKind.EOF, rules[1]);
    
    parser = new LRParser(states[0]);
    
    symbolStack = new ArrayDeque<TreeNode>();
    stateStack = new ArrayDeque<LRParseState>();
  }
  
  @Test
  public void reduceSingleToken() throws LRException {
    Token tToken = new Token(null, t, "", 0, 0);
    symbolStack.push(TreeNode.newTerminalNode(tToken));
    stateStack.push(states[0]);
    stateStack.push(states[3]);
    Token next = new Token(null, TerminalTokenKind.EOF, "", 0, 0);
    LRParser.reduceToken(symbolStack, stateStack, next);
    
    Assert.assertEquals("Reduce resulted in wrong state", states[1], stateStack.pop());
    Assert.assertEquals("Reduce pushed the wrong state onto the stack", states[0], stateStack.pop());
    Assert.assertEquals("Wrong symbol on the top of the symbol stack", e, symbolStack.pop().token.getKind());
  }
  
  @Test
  public void reduceMultipleTokens() throws LRException {
    Token tToken = new Token(null, id, "", 0, 0);
    symbolStack.push(TreeNode.newTerminalNode(tToken));
    stateStack.push(states[0]);
    stateStack.push(states[4]);
    Token next = new Token(null, TerminalTokenKind.EOF, "", 0, 0);
    LRParser.reduceToken(symbolStack, stateStack, next);
    
    Assert.assertEquals("Reduce resulted in wrong state", states[1], stateStack.pop());
    Assert.assertEquals("Reduce pushed the wrong state onto the stack", states[0], stateStack.pop());
    Assert.assertEquals("Wrong symbol on the top of the symbol stack", e, symbolStack.pop().token.getKind());
  }
  
  @Test
  public void dontReduceToken() {
    Token tToken = new Token(null, id, "", 0, 0);
    symbolStack.push(TreeNode.newTerminalNode(tToken));
    stateStack.push(states[0]);
    stateStack.push(states[4]);
    Token next = new Token(null, t, "", 0, 0);
    boolean exception = false;
    
    try {
      LRParser.reduceToken(symbolStack, stateStack, next);
    } catch (LRException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown a parse exception due to ending in non-shift state", exception);
    
    Assert.assertEquals("DFState stack shouldn't have changed", states[4], stateStack.pop());
    Assert.assertEquals("DFState stack shouldn't have changed", states[0], stateStack.pop());
    Assert.assertEquals("Symbol stack shouldn't have changed", tToken, symbolStack.pop().token);
  }
  
  @Test
  public void shiftSingleToken() throws LRException {
    Token tToken = new Token(null, e, "", 0, 0);
    symbolStack.push(TreeNode.newTerminalNode(tToken));
    stateStack.push(states[0]);
    stateStack.push(states[1]);
    Token next = new Token(null, plus, "", 0, 0);
    LRParser.shiftToken(symbolStack, stateStack, next);
    
    Assert.assertEquals("Shift resulted in wrong state", states[5], stateStack.pop());
    Assert.assertEquals("Shift left the wrong state onto the stack", states[1], stateStack.pop());
    Assert.assertEquals("Shift left the wrong state onto the stack", states[0], stateStack.pop());
    Assert.assertEquals("Wrong symbol on the top of the symbol stack", plus, symbolStack.pop().token.getKind());
    Assert.assertEquals("Wrong symbol in the symbol stack", e, symbolStack.pop().token.getKind());
  }
  
  @Test
  public void dontShiftToken() {
    Token tToken = new Token(null, e, "", 0, 0);
    symbolStack.push(TreeNode.newTerminalNode(tToken));
    stateStack.push(states[0]);
    stateStack.push(states[1]);
    Token next = new Token(null, t, "", 0, 0);
    boolean exception = false;
    
    try {
      LRParser.shiftToken(symbolStack, stateStack, next);
    } catch (LRException pe) {
      exception = true;
    }
    Assert.assertTrue("ParseException should have been thrown attempting to shift invalid token", exception);
    
    Assert.assertEquals("Shift left the wrong state onto the stack", states[1], stateStack.pop());
    Assert.assertEquals("Shift left the wrong state onto the stack", states[0], stateStack.pop());
    Assert.assertEquals("Wrong symbol in the symbol stack", e, symbolStack.pop().token.getKind());
  }
  
  @Test
  public void popStacksNoPop() {
    Production p = new Production(s, Collections.<TokenKind>emptyList());
    File f = new File("/tmp");
    int line = 10;
    int pos = 100;
    TreeNode t = TreeNode.newTerminalNode(new Token(f, e, "aThing", line, pos));
    TreeNode bottom = TreeNode.newTerminalNode(new Token(f, plus, "bottom", line - 5, pos - 5));
    // note that the situation doesn't have to be valid
    symbolStack.push(bottom);
    symbolStack.push(t);
    stateStack.push(states[0]);
    stateStack.push(states[1]);
    
    TreeNode result = LRParser.popStacks(symbolStack, stateStack, p);
    Assert.assertEquals("Symbol stack shouldn't have been modified", t, symbolStack.pop());
    Assert.assertEquals("Symbol stack shouldn't have been modified", bottom, symbolStack.pop());
    Assert.assertEquals("DFState stack shouldn't have been modified", states[1], stateStack.pop());
    Assert.assertEquals("DFState stack shouldn't have been modified", states[0], stateStack.pop());

    Assert.assertEquals("LHS should have been returned", s, result.token.getKind());
    Assert.assertNull("File in token is wrong", result.token.getFile());
    Assert.assertEquals("Lexeme in token is wrong", "", result.token.getImage());
    Assert.assertEquals("Line in token is wrong", -1, result.token.getLineNumber());
    Assert.assertEquals("Position in token is wrong", -1, result.token.getPosition());
  }
  
  @Test
  public void popStacksPopOne() {
    Production p = new Production(s, Arrays.asList(e));
    File f = new File("/tmp");
    int line = 10;
    int pos = 100;
    TreeNode t = TreeNode.newTerminalNode(new Token(f, e, "aThing", line, pos));
    TreeNode bottom = TreeNode.newTerminalNode(new Token(f, plus, "bottom", line - 5, pos - 5));
    // note that the situation doesn't have to be valid
    symbolStack.push(bottom);
    symbolStack.push(t);
    stateStack.push(states[0]);
    stateStack.push(states[1]);
    
    TreeNode result = LRParser.popStacks(symbolStack, stateStack, p);
    Assert.assertEquals("Symbol stack should have been modified", bottom, symbolStack.pop());
    Assert.assertEquals("DFState stack should have been modified", states[0], stateStack.pop());

    Assert.assertEquals("LHS should have been returned", s, result.token.getKind());
    Assert.assertEquals("File in token is wrong", f, result.token.getFile());
    Assert.assertEquals("Lexeme in token is wrong", "aThing", result.token.getImage());
    Assert.assertEquals("Line in token is wrong", line, result.token.getLineNumber());
    Assert.assertEquals("Position in token is wrong", pos, result.token.getPosition());
  }
  
  @Test
  public void popStacksPopSeveral() {
    Production p = new Production(s, Arrays.asList(e, plus));
    File f = new File("/tmp");
    int line = 10;
    int pos = 100;
    TreeNode t = TreeNode.newTerminalNode(new Token(f, e, "aThing", line, pos));
    TreeNode tplus = TreeNode.newTerminalNode(new Token(f, plus, "plusT", line - 5, pos - 5));
    TreeNode tbottom = TreeNode.newTerminalNode(new Token(f, id, "id", line + 10, pos + 10));
    // note that the situation doesn't have to be valid
    symbolStack.push(tbottom);
    symbolStack.push(tplus);
    symbolStack.push(t);
    stateStack.push(states[0]);
    stateStack.push(states[1]);
    stateStack.push(states[2]);
    
    TreeNode result = LRParser.popStacks(symbolStack, stateStack, p);
    Assert.assertEquals("Symbol stack should have been modified", tbottom, symbolStack.pop());
    Assert.assertEquals("DFState stack should have been modified", states[0], stateStack.pop());

    Assert.assertEquals("LHS should have been returned", s, result.token.getKind());
    Assert.assertEquals("File in token is wrong", f, result.token.getFile());
    // let's figure out how to get whitespace back in here...
    Assert.assertEquals("Lexeme in token is wrong", "plusT aThing", result.token.getImage());
    Assert.assertEquals("Line in token is wrong", line - 5, result.token.getLineNumber());
    Assert.assertEquals("Position in token is wrong", pos - 5, result.token.getPosition());
  }
  
  @Test
  public void parse() throws LRException {
    File f = new File("/tmp");
    int line = 10;
    int pos = 100;
    Token one = new Token(f, id, "var1", line, pos);
    Token two = new Token(f, plus, "+", line, pos+4);
    Token three = new Token(f, id, "var2", line, pos+5);
    Token four = new Token(f, TerminalTokenKind.EOF, "$", line, pos+9);
    List<Token> tokens = Arrays.asList(one, two, three, four);
    
    TreeNode result = parser.parse(tokens);
    Assert.assertNotNull("Parse should have succeeded", result);
  }
}
