package ca.uwaterloo.joos1wc.scanner;

import org.junit.Assert;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.automata.DFState;
import ca.uwaterloo.joos1wc.automata.DFTransition;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;

public class TerminalTokenKindTest {
  @Test
  public void getDFAKeywordAbstract() {
    stringDFATestHelper(TerminalTokenKind.ABSTRACT, "abstract");
    stringDFATestHelper(TerminalTokenKind.CLASS, "class");
  }
  
  @Test
  public void getDFASeparator() {
    stringDFATestHelper(TerminalTokenKind.LPAREN, "(");
    stringDFATestHelper(TerminalTokenKind.SEMICOLON, ";");
  }
  
  @Test
  public void getDFAOperator() {
    stringDFATestHelper(TerminalTokenKind.PLUS, "+");
    stringDFATestHelper(TerminalTokenKind.URSHIFTEQ, ">>>=");
  }
  
  /**
   * Don't do this for every custom DFA, but do it for at least one to make sure it's not
   * the same as the String-matching DFAs.
   */
  @Test
  public void getDFAWhitespace() {
    DFAutomaton dfa = TerminalTokenKind.WHITESPACE.getDFAutomaton();

    DFState[] s = dfa.getStates();
    DFTransition[] t = dfa.getTransitions();
    TerminalTokenKind k = dfa.getKind();
    
    Assert.assertEquals("number of states is wrong for whitespace", 2, s.length);
    Assert.assertEquals("number of transitions is wrong for whitespace", 2, t.length);
    Assert.assertEquals("kind is wrong for whitespace", TerminalTokenKind.WHITESPACE, k);
    
    Assert.assertEquals("wrong number of transitions for state 0", 1, s[0].getCount());
    Assert.assertEquals("wrong number of transitions for state 1", 1, s[1].getCount());
    Assert.assertFalse("state 0 should not be terminating", s[0].isTerminal);
    Assert.assertTrue("state 1 should be terminating", s[1].isTerminal);
  }
  
  private void stringDFATestHelper(TerminalTokenKind kind, String match) {
    String name = kind.name();
    DFAutomaton dfa = kind.getDFAutomaton();

    DFState[] s = dfa.getStates();
    DFTransition[] t = dfa.getTransitions();
    TerminalTokenKind k = dfa.getKind();
    
    Assert.assertEquals("number of states is wrong for " + name, match.length() + 1, s.length);
    Assert.assertEquals("number of transitions is wrong for " + name, match.length(), t.length);
    Assert.assertEquals("kind is wrong for " + name, kind, k);
    
    int i;
    for (i = 0; i < match.length(); i++) {
      Assert.assertFalse("state " + i + " should not be terminating", s[i].isTerminal);
      Assert.assertEquals("state " + i + " should have one exiting transition", 1, s[i].getCount());
    }
    Assert.assertTrue("final state should be terminating", s[i].isTerminal);
    Assert.assertEquals("final state should have no exiting transitions", 0, s[i].getCount());

    
    char [] c = match.toCharArray();
    for (int j = 0; j < c.length; j++) {
      Assert.assertEquals("transition is wrong for " + name, c[j], t[j].transition);
    }
  }
}
