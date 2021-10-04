package ca.uwaterloo.joos1wc.automata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.utility.CharacterStream;

/**
 * A deterministic finite automaton.
 */
public class DFAutomaton {

  private final DFState[] states;
  private final DFTransition[] transitions;
  private TerminalTokenKind kind;

  /**
   * Constructs a deterministic finite automaton that accepts a specific TokenKind.
   * 
   * @param states
   *          The states of the automaton.
   * @param transitions
   *          The transitions of the automaton.
   * @param kind
   *          The token kind representing accepted input.
   */
  public DFAutomaton(DFState[] states, DFTransition[] transitions, TerminalTokenKind kind) {
    assert states != null && transitions != null && kind != null;

    this.states = states;
    this.transitions = transitions;
    this.kind = kind;
  }

  /**
   * Gets the starting state of the deterministic finite automaton.
   * 
   * @return The starting state.
   */
  public DFState getStartingState() {
    return states[0];
  }

  /**
   * Gets the states of the deterministic finite automaton.
   * 
   * @return The states of the deterministic finite automaton.
   */
  public DFState[] getStates() {
    return states;
  }

  /**
   * Gets the transitions of the deterministic finite automaton.
   * 
   * @return The transitions of the deterministic finite automaton.
   */
  public DFTransition[] getTransitions() {
    return transitions;
  }

  /**
   * Gets the kind of token that will be matched on munge.
   * 
   * @return The kind of token that this DFA will match on munge.
   */
  public TerminalTokenKind getKind() {
    return kind;
  }

  /**
   * Iterates through the input stream returning as much of the available input as possible that can be consumed.
   * 
   * @param cursor
   *          The input stream.
   * @return A token built from the maximum possible number of characters from the input stream.
   * @throws IOException
   */
  public Token munge(CharacterStream cursor) throws IOException {
    assert cursor != null;

    DFState state = getStartingState();
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    DFState result = null;
    boolean isScanning = true;

    StringBuilder image = new StringBuilder();
    while (isScanning && !cursor.isEndOfInput()) {
      if (state.isTerminal) {
        result = state;
      }

      char nextSym = cursor.next();
      DFState newstate = null;
      for (DFTransition trans : state.transitions) {
        if (trans.execute(nextSym, buffer)) {
          newstate = trans.toState;
          break;
        }
      }

      if (newstate == null) {
        isScanning = false;
      } else {
        state = newstate;
        image.append(nextSym);
      }
    }

    if (!state.isTerminal && result == null) {
      return null; // unable to parse token
    }

    String lexeme = image.toString();
    return new Token(cursor.getFile(), this.kind, lexeme, cursor.getCurrentLine(), cursor.getLinePosition(),
        buffer.toByteArray());
  }

}
