package ca.uwaterloo.joos1wc.automata;

import java.io.ByteArrayOutputStream;

/**
 * A transition between two states.
 */
public class DFTransition {

  public static interface Action {
    public void execute(char c, DFTransition t, ByteArrayOutputStream out);
  }

  /*
   * START OF MAGIC CHARACTERS: These reduce the number of needed transitions in the DFAs.
   */
  /** Defines a transition accepting characters until line break is encountered. */
  public static final char RESTOFLINE = 128;
  /** Defines a transition accepting upper and lowercase letters including special characters (_$). */
  public static final char LETTER = 129;
  /** Defines a transition accepting positive integers. */
  public static final char POSITIVE = 130;
  /** Defines a transition accepting integers. */
  public static final char NUMERIC = 131;
  /** Defines a transition accepting escape characters. */
  public static final char ESCAPE = 132;
  /** Defines a transition accepting whitespace. */
  public static final char WHITESP = 133;
  /** Defines a transition accepting characters with the exception of quotation marks, backslash or line breaks. */
  public static final char NOTQUOTE = 134;
  /** Defines a transition accepting characters with the exception of apostrophes, backslash or line breaks. */
  public static final char NOTAPOS = 135;
  /** Defines a transition accepting characters with the exception of asterisks. */
  public static final char NOTSTAR = 136;
  /** Defines a transition accepting characters with the exception of slash or asterisks. */
  public static final char NOTSLASH = 137;
  /** Defines a transition accepting integers zero to three. */
  public static final char ZEROTOTHREE = 138;
  /** Defines a transition accepting integers four to seven. */
  public static final char FOURTOSEVEN = 139;
  /** Defines a transition accepting octal integers. */
  public static final char OCTAL = 140;

  /* END OF MAGIC CHARACTERS */

  /** The initial state. */
  public final DFState fromState;

  /** The transition variable for each value. */
  public final char transition;

  /** The final state. */
  public final DFState toState;

  private Action action = null;

  /**
   * Constructs a transition between two states.
   * 
   * @param stateA
   *          The starting state of the automaton.
   * @param transition
   *          The transition statement.
   * @param stateB
   *          The ending state of the automaton.
   */
  public DFTransition(final DFState stateA, final char transition, final DFState stateB) {
    assert stateA != null && stateB != null;

    this.fromState = stateA;
    this.transition = transition;
    this.toState = stateB;

    this.fromState.addTransition(this);
  }

  public DFTransition(final DFState stateA, final char transition, final DFState stateB, Action action) {
    this(stateA, transition, stateB);
    this.action = action;
  }

  /**
   * Attempt to execute the transition on the specified character.
   * 
   * @param input
   *          An input character.
   * @return True if the transition is successful; false otherwise.
   */
  public boolean execute(char input, ByteArrayOutputStream out) {
    if (!shouldExecute(input)) {
      return false;
    }
    if (action != null) {
      action.execute(input, this, out);
    }
    return true;
  }

  /**
   * Check if the transition applies to the specified character.
   * 
   * @param input
   *          An input character.
   * @return True if the transition applies to the character; false otherwise.
   */
  private boolean shouldExecute(char input) {
    // Consider usage of Character.isDigit / isLetter below
    switch (this.transition) {

    case RESTOFLINE:
      return !(input == '\n' || input == '\r');
    case LETTER:
      // upper case then lower case
      return (input >= 'A' && input <= 'Z') || (input >= 'a' && input <= 'z') || input == '_' || input == '$';
    case POSITIVE:
      // only 1-9, not 0
      return input >= '1' && input <= '9';
    case NUMERIC:
      // only 1-9, not 0
      return input >= '0' && input <= '9';
    case ESCAPE:
      return input == 'b' || input == 't' || input == 'n' || input == 'f' || input == 'r' || input == '\"'
          || input == '\'' || input == '\\';
    case WHITESP:
      return input == ' ' || input == '\t' || input == '\n' || input == '\r';
    case NOTQUOTE:
      return input != '\"' && input != '\\' && input != '\n' && input != '\r';
    case NOTAPOS:
      return input != '\'' && input != '\\' && input != '\n' && input != '\r';
    case NOTSTAR:
      return input != '*';
    case NOTSLASH:
      return input != '/' && input != '*';
    case ZEROTOTHREE:
      return input >= '0' && input <= '3';
    case FOURTOSEVEN:
      return input >= '4' && input <= '7';
    case OCTAL:
      return input >= '0' && input <= '7';

    default:
      return this.transition == input;
    }
  }

}