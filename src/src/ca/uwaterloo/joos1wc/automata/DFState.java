package ca.uwaterloo.joos1wc.automata;

import java.util.ArrayList;
import java.util.List;

/**
 * The state of a deterministic finite automaton.
 */
public class DFState {
  /** used to keep the ids unique between all States */
  private static int nextId = 1;

  /** The identifier of the state. */
  public final int id;

  /** Determines if the state is terminal. */
  public final Boolean isTerminal;

  /** A list of states that are accessible from this state. */
  public final List<DFTransition> transitions;

  /**
   * Constructs a state.
   * 
   * @param terminal
   *          Determines if the state is terminal.
   */
  public DFState(final Boolean terminal) {
    this.id = getNextId();
    this.isTerminal = terminal;
    this.transitions = new ArrayList<DFTransition>();
  }

  /**
   * Gets the next usable DFState id Thread-safe
   * 
   * @return the next usable id
   */
  private static synchronized int getNextId() {
    return nextId++;
  }

  /**
   * Adds a DFTransition to the list of transitions that are possible from this state.
   * 
   * @param transition
   *          a transition which is possible from this state
   */
  public void addTransition(DFTransition transition) {
    transitions.add(transition);
  }

  /**
   * Returns the number of transitions in this state.
   * 
   * @return the number of transitions in this state.
   */
  public int getCount() {
    return transitions.size();
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj
   *          The reference state with which to compare.
   * @return True if this object is the same as the obj argument; false otherwise.
   */
  public boolean equals(DFState obj) {
    return obj.id == id;
  }

  /***
   * Returns a string representation of the object.
   * 
   * @return A string representation of the object.
   */
  public String toString() {
    return String.format("<DFState %d %b>", id, isTerminal);
  }
}
