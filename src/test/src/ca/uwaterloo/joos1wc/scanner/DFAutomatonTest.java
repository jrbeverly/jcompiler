package ca.uwaterloo.joos1wc.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public class DFAutomatonTest {
  private MockInputCursor passCursor;
  private MockInputCursor failCursor;
  private final String passInput = "extends a pass";
  private final String failInput = "extended failure";
  // this introduces a dependency, but there's almost no avoiding this one
  private final TerminalTokenKind kind = TerminalTokenKind.EXTENDS;
  private final String lexeme = kind.name().toLowerCase();
  private DFAutomaton dfa;
  
  @Before
  public void setup() throws IOException {
    passCursor = new MockInputCursor(new ByteArrayInputStream(passInput.getBytes()));
    failCursor = new MockInputCursor(new ByteArrayInputStream(failInput.getBytes()));
    
    // re-instantiate for each test just in case there is some kind of state preserved (there shouldn't be)
    dfa = kind.getDFAutomaton();
  }
  
  @Test
  public void inputEnds() throws IOException {
    passCursor.setLength("extends".length());
    Token t = dfa.munge(passCursor);
    
    Assert.assertNotNull("Token shouldn't be null", t);
    Assert.assertEquals("Returned lexeme isn't correct", lexeme, t.getImage());
  }
  
  @Test
  public void pass() throws IOException {
    Token t = dfa.munge(passCursor);
    
    Assert.assertNotNull("Token shouldn't be null", t);
    Assert.assertEquals("Returned lexeme isn't correct", lexeme, t.getImage());
  }
  
  @Test
  public void failPartial() throws IOException {
    Token t = dfa.munge(failCursor);
    
    Assert.assertNull("Token should be null", t);
  }
}
