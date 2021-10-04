package ca.uwaterloo.joos1wc.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.diagnostics.ParseException;
import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class LRGrammarTest {
  LRGrammar grammar;
  Map<String, TokenKind> tokenMap;
  Scanner scanner;
  TokenKind foo;
  TokenKind bar;
  List<LRParseState> states;
  List<Production> rules;
  Production rule0;
  Production rule1;
  
  @Before
  public void setup() {
    grammar = new LRGrammar();
    tokenMap = grammar.getTokenMap();
    
    foo = new NonTerminalTokenKind("Foo");
    bar = new NonTerminalTokenKind("Bar");
    
    states = new ArrayList<LRParseState>(2);
    states.add(new LRParseState(0));
    states.add(new LRParseState(1));

    List<TokenKind> list0 = new ArrayList<TokenKind>(1);
    list0.add(TerminalTokenKind.PLUS);
    rule0 = new Production(foo, list0);
    
    List<TokenKind> list1 = new ArrayList<TokenKind>(0);
    rule1 = new Production(bar, list1);
    
    rules = new ArrayList<Production>(2);
    rules.add(rule0);
    rules.add(rule1);
  }
  
  @After
  public void teardown() {
    if (scanner != null) {
      scanner.close();
    }
  }

  @Test
  public void processValidTerminals() throws IOException {
    scanner = new Scanner("2\nPLUS\nABSTRACT");
    try {
      grammar.processTerminals(scanner);
    } catch (ParseException pe) {
      Assert.fail("No ParseException should have been thrown with valid terminals");
    }
  }
  
  @Test
  public void processInvalidTerminals() throws IOException {
    scanner = new Scanner("4\nPLUS\nABSTRACT\nWHITESPACE\nFLOATLITERAL");
    boolean exception = false;
    try {
      grammar.processTerminals(scanner);
    } catch (ParseException pe) {
      exception = true;
    }
    
    Assert.assertTrue("ParseException should have been thrown with invalid terminals", exception);
  }
  
  @Test
  public void processValidNonterminals() throws IOException {
    scanner = new Scanner("3\nFoo\nBar\nFooBar(0)");
    grammar.processNonTerminals(scanner);
    
    int[] ords = new int[3];

    String name = "Foo";
    TokenKind token = tokenMap.get(name);
    Assert.assertNotNull("Couldn't get TokenKind for " + name, token);
    Assert.assertEquals("Name is wrong for TokenKind", name, token.name());
    ords[0] = token.ordinal();
    
    name = "Bar";
    token = tokenMap.get(name);
    Assert.assertNotNull("Couldn't get TokenKind for " + name, token);
    Assert.assertEquals("Name is wrong for TokenKind", name, token.name());
    ords[1] = token.ordinal();
    
    name = "FooBar(0)";
    token = tokenMap.get(name);
    Assert.assertNotNull("Couldn't get TokenKind for " + name, token);
    Assert.assertEquals("Name is wrong for TokenKind", name, token.name());
    ords[2] = token.ordinal();
    
    Assert.assertEquals("Ordinals should be sequential", ords[0] + 1, ords[1]);
    Assert.assertEquals("Ordinals should be sequential", ords[1] + 1, ords[2]);
  }
  
  @Test
  public void processInvalidNonterminals() throws IOException {
    scanner = new Scanner("3\nFoo\nFoo\nFoo\nBar");
    grammar.processNonTerminals(scanner);
    
    String name = "Foo";
    TokenKind token = tokenMap.get(name);
    Assert.assertNotNull("Couldn't get TokenKind for " + name, token);
    Assert.assertEquals("Name is wrong for TokenKind", name, token.name());
    
    TokenKind tokenBar = tokenMap.get("Bar");
    Assert.assertNull("Shouldn't be able to get token for Bar", tokenBar);
  }
  
  @Test
  public void processValidRules() throws IOException, ParseException {
    tokenMap.put("Foo", foo);
    tokenMap.put("Bar", bar);
    scanner = new Scanner("3\nFoo\nFoo Bar\nBar ABSTRACT Bar PLUS");
    
    List<Production> rules = grammar.processRules(scanner);
    Assert.assertEquals("Wrong number of rules", 3, rules.size());
    
    Production rule0 = rules.get(0);
    Production rule1 = rules.get(1);
    Production rule2 = rules.get(2);
    
    Assert.assertEquals("Wrong LHS 0", foo, rule0.getLHS());
    Assert.assertEquals("Wrong LHS 1", foo, rule1.getLHS());
    Assert.assertEquals("Wrong LHS 2", bar, rule2.getLHS());
    
    Assert.assertEquals("Wrong length RHS 0", 0, rule0.getRHS().size());
    Assert.assertEquals("Wrong length RHS 1", 1, rule1.getRHS().size());
    Assert.assertEquals("Wrong length RHS 2", 3, rule2.getRHS().size());
    
    Assert.assertEquals("Wrong nonterm RHS", bar, rule1.getRHS().get(0));
    
    Assert.assertEquals("Wrong term RHS", TerminalTokenKind.ABSTRACT, rule2.getRHS().get(0));
    Assert.assertEquals("Wrong nonterm RHS", bar, rule2.getRHS().get(1));
    Assert.assertEquals("Wrong term RHS", TerminalTokenKind.PLUS, rule2.getRHS().get(2));
  }
  
  @Test
  public void processInvalidRules() throws IOException {
    scanner = new Scanner("3\nFoo\nFoo Bar\nBar ABSTRACT Bar PLUS");
    boolean exception = false;
    
    try {
      grammar.processRules(scanner);
    } catch (ParseException pe) {
      exception = true;
    }
    
    Assert.assertTrue("Should have thrown a ParseException with unknown tokens", exception);
  }
  
  @Test
  public void processStates() throws IOException {
    scanner = new Scanner("10");
    List<LRParseState> states = grammar.processStates(scanner);
    
    Assert.assertEquals("Wrong number of states", 10, states.size());
    for (int i = 0; i < 10; i++) {
      Assert.assertEquals("DFState has the wrong id", i, states.get(i).id());
    }
    
    scanner.close();
    scanner = new Scanner("1");
    List<LRParseState> states2 = grammar.processStates(scanner);
    
    Assert.assertEquals("Wrong number of states", 1, states2.size());
    Assert.assertEquals("DFState has the wrong id", 0, states2.get(0).id());
  }
  
  @Test
  public void processValidShiftTransitions() throws IOException, ParseException {
    tokenMap.put("Foo", foo);
    tokenMap.put("Bar", bar);
    scanner = new Scanner("3\n0 ABSTRACT shift 1\n1 Foo shift 0\n1 PLUS shift 1");
    
    grammar.processTransitions(scanner, states, rules);

    LRParseState s0abstract = states.get(0).shift(TerminalTokenKind.ABSTRACT);
    LRParseState s0plus = states.get(0).shift(TerminalTokenKind.PLUS);
    LRParseState s0foo = states.get(0).shift(foo);
    LRParseState s0bar = states.get(0).shift(bar);
    Assert.assertEquals("DFState 0 should shift ABSTRACT to state 1", states.get(1), s0abstract);
    Assert.assertNull("DFState 0 should not shift PLUS", s0plus);
    Assert.assertNull("DFState 0 should not shift Foo", s0foo);
    Assert.assertNull("DFState 0 should not shift Bar", s0bar);
    
    LRParseState s1abstract = states.get(1).shift(TerminalTokenKind.ABSTRACT);
    LRParseState s1plus = states.get(1).shift(TerminalTokenKind.PLUS);
    LRParseState s1foo = states.get(1).shift(foo);
    LRParseState s1bar = states.get(1).shift(bar);
    Assert.assertNull("DFState 1 should not shift ABSTRACT", s1abstract);
    Assert.assertEquals("DFState 1 should shift PLUS to state 1", states.get(1), s1plus);
    Assert.assertEquals("DFState 1 should shift Foo to state 0", states.get(0), s1foo);
    Assert.assertNull("DFState 1 should not shift Bar", s1bar);
  }
  
  @Test
  public void processInvalidShiftTransitions() throws IOException {
    scanner = new Scanner("3\n0 ABSTRACT shift 1\n1 Foo shift 0\n1 PLUS shift 1");
    boolean exception = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal tokens", exception);
    
    scanner.close();
    scanner = new Scanner("2\n0 ABSTRACT shift 2\n0 PLUS shift 1");
    boolean exception2 = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception2 = true;
    }
    Assert.assertTrue("Should have thrown an exception with missing shift state", exception2);
    
    scanner.close();
    scanner = new Scanner("2\n0 ABSTRACT shift 1\n3 PLUS shift 1");
    boolean exception3 = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception3 = true;
    }
    Assert.assertTrue("Should have thrown an exception with missing origin state", exception3);
  }
  
  @Test
  public void processValidReduceTransitions() throws IOException, ParseException {
    tokenMap.put("Foo", foo);
    tokenMap.put("Bar", bar);
    scanner = new Scanner("3\n0 ABSTRACT reduce 1\n1 Bar reduce 0\n1 PLUS reduce 1");
    
    grammar.processTransitions(scanner, states, rules);

    Production s0abstract = states.get(0).reduce(TerminalTokenKind.ABSTRACT);
    Production s0plus = states.get(0).reduce(TerminalTokenKind.PLUS);
    Production s0foo = states.get(0).reduce(foo);
    Production s0bar = states.get(0).reduce(bar);
    Assert.assertEquals("DFState 0 should reduce ABSTRACT to rule 1", rules.get(1), s0abstract);
    Assert.assertNull("DFState 0 should not reduce PLUS", s0plus);
    Assert.assertNull("DFState 0 should not reduce Foo", s0foo);
    Assert.assertNull("DFState 0 should not reduce Bar", s0bar);
    
    Production s1abstract = states.get(1).reduce(TerminalTokenKind.ABSTRACT);
    Production s1plus = states.get(1).reduce(TerminalTokenKind.PLUS);
    Production s1foo = states.get(1).reduce(foo);
    Production s1bar = states.get(1).reduce(bar);
    Assert.assertNull("DFState 1 should not reduce ABSTRACT", s1abstract);
    Assert.assertEquals("DFState 1 should reduce PLUS to rule 1", rules.get(1), s1plus);
    Assert.assertNull("DFState 1 should not reduce Foo", s1foo);
    Assert.assertEquals("DFState 1 should reduce Bar to rule 0", rules.get(0), s1bar);
  }
  
  @Test
  public void processInvalidReduceTranstions() throws IOException, ParseException {
    scanner = new Scanner("3\n0 ABSTRACT reduce 1\n1 Bar reduce 0\n1 PLUS reduce 1");
    boolean exception = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal tokens", exception);
    
    scanner.close();
    scanner = new Scanner("2\n0 ABSTRACT reduce 2\n0 PLUS reduce 1");
    boolean exception2 = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception2 = true;
    }
    Assert.assertTrue("Should have thrown an exception with missing production rule", exception2);
    
    scanner.close();
    scanner = new Scanner("2\n0 ABSTRACT reduce 1\n3 PLUS reduce 1");
    boolean exception3 = false;
    
    try {
      grammar.processTransitions(scanner, states, rules);
    } catch(ParseException pe) {
      exception3 = true;
    }
    Assert.assertTrue("Should have thrown an exception with missing origin state", exception3);
  }
  
  @Test
  public void getShortRuleFromString() throws ParseException {
    String inFoo = "Foo";
    boolean exception = false;
    
    try {
      grammar.getRuleFromString(inFoo);
    } catch (ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal", exception);
    
    tokenMap.put("Foo", foo);
    tokenMap.put("Bar", bar);
    
    Production pFoo = grammar.getRuleFromString(inFoo);
    Assert.assertEquals("Wrong LHS for Foo rule", foo, pFoo.getLHS());
    Assert.assertEquals("RHS should be empty for Foo rule", 0, pFoo.getRHS().size());
  }
  
  @Test
  public void getLongRuleFromString() throws ParseException {
    tokenMap.put("Foo", foo);
    String inFoo = "Foo ABSTRACT PLUS ABSTRACT PLUS Bar";
    boolean exception = false;
    
    try {
      grammar.getRuleFromString(inFoo);
    } catch (ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal", exception);
    
    tokenMap.put("Bar", bar);
    
    Production pFoo = grammar.getRuleFromString(inFoo);
    Assert.assertEquals("Wrong LHS for Foo rule", foo, pFoo.getLHS());
    List<TokenKind> rhs = pFoo.getRHS();
    Assert.assertEquals("Wrong size of RHS for Foo rule", 5, rhs.size());
    Assert.assertEquals("Wrong value in RHS", TerminalTokenKind.ABSTRACT, rhs.get(0));
    Assert.assertEquals("Wrong value in RHS", TerminalTokenKind.PLUS, rhs.get(1));
    Assert.assertEquals("Wrong value in RHS", TerminalTokenKind.ABSTRACT, rhs.get(2));
    Assert.assertEquals("Wrong value in RHS", TerminalTokenKind.PLUS, rhs.get(3));
    Assert.assertEquals("Wrong value in RHS", bar, rhs.get(4));
  }
  
  @Test
  public void addShiftTransitionFromString() throws ParseException {
    String inFoo = "1 Foo shift 0";
    boolean exception = false;
    
    try {
      grammar.addTransitionFromString(inFoo, states, rules);
    } catch (ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal", exception);
    
    tokenMap.put("Foo", foo);
    
    grammar.addTransitionFromString(inFoo, states, rules);
    LRParseState s0 = states.get(0).shift(foo);
    LRParseState s1 = states.get(1).shift(foo);
    
    Assert.assertNull("DFState 0 should not shift Foo", s0);
    Assert.assertEquals("DFState 1 should shift Foo to state 0", states.get(0), s1);
  }
  
  @Test
  public void addReduceTransitionFromString() throws ParseException {
    String inBar = "1 Bar reduce 0";
    boolean exception = false;
    
    try {
      grammar.addTransitionFromString(inBar, states, rules);
    } catch (ParseException pe) {
      exception = true;
    }
    Assert.assertTrue("Should have thrown an exception with unknown non-terminal", exception);
    
    tokenMap.put("Bar", bar);
    
    grammar.addTransitionFromString(inBar, states, rules);
    Production r0 = states.get(0).reduce(bar);
    Production r1 = states.get(1).reduce(bar);
    
    Assert.assertNull("DFState 0 should not reduce Bar", r0);
    Assert.assertEquals("DFState 1 should reduce Bar to production rule 0", rules.get(0), r1);
  }
}
