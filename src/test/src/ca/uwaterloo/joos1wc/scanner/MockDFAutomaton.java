package ca.uwaterloo.joos1wc.scanner;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.automata.DFState;
import ca.uwaterloo.joos1wc.automata.DFTransition;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.utility.CharacterStream;

/**
 * Mock class useful for testing classes that need DFAutomaton instances
 *
 */
public class MockDFAutomaton extends DFAutomaton {
  public int returnCount = 0;
  public Token[] tokens = new Token[0];
  
  public MockDFAutomaton(DFState[] states, DFTransition[] transitions, TerminalTokenKind kind) {
    super (states, transitions, kind);
  }
  
  public void setReturnTokens(Token[] toReturn) {
    tokens = toReturn;
    returnCount = 0;
  }
  
  @Override
  public Token munge(CharacterStream cursor) {
    if (returnCount < tokens.length) {
      return tokens[returnCount++];
    } else {
      return null;
    }
  }
  
}
