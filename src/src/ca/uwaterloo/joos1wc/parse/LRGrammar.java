package ca.uwaterloo.joos1wc.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.InvalidGrammarException;
import ca.uwaterloo.joos1wc.diagnostics.ParseException;
import ca.uwaterloo.joos1wc.diagnostics.TokenNotFoundException;
import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.TokenKind;

/**
 * Essentially a factory class for the data structure of ParseStates and Productions that the LRParser uses.
 *
 */
public class LRGrammar {
  private static final String REDUCE = "reduce";
  private final Map<String, TokenKind> tokenMap;

  public LRGrammar() {
    tokenMap = new HashMap<String, TokenKind>();
    for (TokenKind token : TerminalTokenKind.values()) {
      tokenMap.put(token.name(), token);
    }
  }

  public Map<String, TokenKind> getTokenMap() {
    return tokenMap;
  }

  // simple read from file method
  public LRParseState fromFile(InputStream input) throws IOException, ParseException, InvalidGrammarException {
    Scanner scanner = new Scanner(input);
    List<LRParseState> states = null;
    List<Production> rules = null;

    try {
      // handle terminals
      processTerminals(scanner);

      // handle non-terminals
      processNonTerminals(scanner);

      // next is the name of the start symbol, which we don't care about
      scanner.nextLine();

      // then the production rules
      rules = processRules(scanner);

      // then generate the states
      states = processStates(scanner);

      // finally the transitions
      processTransitions(scanner, states, rules);

    } finally {
      scanner.close();
    }

    // return the start state
    return states.get(0);
  }

  /**
   * Parse out the terminals from an lr1 file. Validates that they all already exist in the tokenMap.
   * 
   * @param scanner
   *          A Scanner object where the next line is the start of the terminals section
   * @param tokenMap
   *          A map of known tokens
   * @throws IOException
   * @throws ParseException
   */
  public void processTerminals(Scanner scanner) throws IOException, ParseException {
    assert scanner != null && scanner.hasNextLine() && tokenMap != null;

    String terminalCountLine = scanner.nextLine();
    int terminalCount = Integer.parseInt(terminalCountLine);
    while (terminalCount-- > 0) {
      String terminal = scanner.nextLine();
      // just verify that they're available
      if (tokenMap.get(terminal) == null) {
        throw new TokenNotFoundException(String.format(Exceptions.TERMINAL_NOT_FOUND, terminal), terminal);
      }
    }
  }

  /**
   * Parse out the non-terminals from an lr1 file. Adds them to the passed tokenMap
   * 
   * @param scanner
   *          A Scanner object where the next line is the start of the non-terminals section
   * @param tokenMap
   *          A map of tokens to which to add the non-terminals
   * @throws IOException
   * @throws ParseException
   */
  public void processNonTerminals(Scanner scanner) throws IOException {
    assert scanner != null && scanner.hasNextLine() && tokenMap != null;

    String nonTerminalCountLine = scanner.nextLine();
    int nonTerminalCount = Integer.parseInt(nonTerminalCountLine);
    while (nonTerminalCount-- > 0) {
      String nonTerminal = scanner.nextLine();
      // just add them to the map
      tokenMap.put(nonTerminal, new NonTerminalTokenKind(nonTerminal));
    }
  }

  /**
   * Parse out the production rules from an lr1 file and returns them in a list.
   * 
   * @param scanner
   *          A Scanner object where the next line is the start of the rules section
   * @param tokenMap
   *          A map of known tokens
   * @return a list of Production rules parse from the scanner
   * @throws IOException
   * @throws ParseException
   */
  public List<Production> processRules(Scanner scanner) throws IOException, ParseException {
    assert scanner != null && scanner.hasNextLine() && tokenMap != null;

    String ruleCountLine = scanner.nextLine();
    int ruleCount = Integer.parseInt(ruleCountLine);
    List<Production> rules = new ArrayList<Production>(ruleCount);
    while (ruleCount-- > 0) {
      String ruleString = scanner.nextLine();
      // handle the rules
      rules.add(getRuleFromString(ruleString));
    }
    return rules;
  }

  /**
   * Creates the states specified in an lr1 file and returns them in a list. The created states are empty
   * 
   * @param scanner
   *          A Scanner object where the next line is the start of the states section
   * @return a list of ParseStates, parsed from the scanner
   * @throws IOException
   * @throws ParseException
   */
  public List<LRParseState> processStates(Scanner scanner) throws IOException {
    assert scanner != null && scanner.hasNextLine();

    String stateCountLine = scanner.nextLine();
    int stateCount = Integer.parseInt(stateCountLine);
    List<LRParseState> states = new ArrayList<LRParseState>(stateCount);
    for (int i = 0; i < stateCount; i++) {
      // just create empty states
      states.add(new LRParseState(i));
    }
    return states;
  }

  /**
   * Parse transitions from an lr1 file and add them to the passed states.
   * 
   * @param scanner
   *          A Scanner object where the next line is the start of the transitions section
   * @param states
   *          A list of states specified in the lr1 file
   * @param rules
   *          A list of production rules specified in the lr1 file
   * @param tokenMap
   *          A map of known tokens
   * @throws IOException
   * @throws InvalidGrammarException
   * @throws TokenNotFoundException
   */
  public void processTransitions(Scanner scanner, List<LRParseState> states, List<Production> rules) throws IOException,
      TokenNotFoundException, InvalidGrammarException {
    assert scanner != null && scanner.hasNextLine() && tokenMap != null && rules != null && states != null;

    String transitionCountLine = scanner.nextLine();
    int transitionCount = Integer.parseInt(transitionCountLine);
    while (transitionCount-- > 0) {
      // add all the transitions to the states
      String transitionString = scanner.nextLine();
      addTransitionFromString(transitionString, states, rules);
    }
  }

  /**
   * Get a Production rule from the string in the lr1 file. This exists because I am not sure String.split is okay,
   * since it uses a regex.
   * 
   * @param ruleString
   *          The line from the lr1 file that describes a rule
   * @return A Production object that the rule describes
   * @throws TokenNotFoundException
   */
  public Production getRuleFromString(String ruleString) throws TokenNotFoundException {
    char[] chars = ruleString.toCharArray();

    List<TokenKind> rhs = new ArrayList<TokenKind>();
    TokenKind lhs = null;

    StringBuilder word = new StringBuilder();
    int wordCount = 0;

    for (int i = 0; i <= chars.length; i++) {
      if (i < chars.length && chars[i] != ' ') {
        word.append(chars[i]);

      } else {
        if (wordCount == 0) {
          lhs = tokenMap.get(word.toString());
          if (lhs == null) {
            throw new TokenNotFoundException(String.format(Exceptions.TOKEN_NOT_FOUND, word.toString()),
                word.toString());
          }
        } else {
          TokenKind rht = tokenMap.get(word.toString());
          if (rht == null) {
            throw new TokenNotFoundException(String.format(Exceptions.TOKEN_NOT_FOUND, word.toString()),
                word.toString());
          } else {
            rhs.add(rht);
          }
        }

        word = new StringBuilder();
        wordCount++;
      }
    }

    return new Production(lhs, rhs);
  }

  /**
   * Add a transition to a state based on a line in the lr1 file. This exists because I'm not sure String.split is okay,
   * since it uses a regex.
   * 
   * @param transitionString
   *          The line from the lr1 file describing a transition
   * @param states
   *          The list of states described in the lr1 file
   * @param rules
   *          The list of rules described in the lr1 file
   * @throws InvalidGrammarException
   * @throws TokenNotFoundException
   */
  public void addTransitionFromString(String transitionString, List<LRParseState> states, List<Production> rules)
      throws InvalidGrammarException, TokenNotFoundException {
    char[] chars = transitionString.toCharArray();

    int originState = -1;
    TokenKind token = null;
    boolean reduce = false;
    int last = -1;

    StringBuilder word = new StringBuilder();
    int wordCount = 0;

    for (int i = 0; i < chars.length; i++) {
      if (chars[i] != ' ') {
        word.append(chars[i]);

      } else {
        if (wordCount == 0) {
          originState = Integer.parseInt(word.toString());

        } else if (wordCount == 1) {
          token = tokenMap.get(word.toString());
          if (token == null) {
            throw new TokenNotFoundException(String.format(Exceptions.TOKEN_NOT_FOUND, word.toString()),
                word.toString());
          }
        } else if (wordCount == 2) {
          String action = word.toString();
          if (REDUCE.equals(action)) {
            reduce = true;
          } else {
            reduce = false;
          }
        }

        word = new StringBuilder();
        wordCount++;
      }
    }
    last = Integer.parseInt(word.toString());

    if (originState >= states.size() || originState < 0) {
      throw new InvalidGrammarException(String.format(Exceptions.OUT_OF_BOUNDS_STATE, originState, states.size()),
          originState);
    }

    if (reduce) {
      // bounds checking
      if (last >= rules.size() || last < 0) {
        throw new InvalidGrammarException(String.format(Exceptions.OUT_OF_BOUNDS_REDUCE, last, rules.size()), last);
      }
      states.get(originState).addReduce(token, rules.get(last));

    } else {
      // bounds checking
      if (last >= states.size() || last < 0) {
        throw new InvalidGrammarException(String.format(Exceptions.OUT_OF_BOUNDS_SHIFT, last, states.size()), last);
      }
      states.get(originState).addShift(token, states.get(last));
    }
  }

}
