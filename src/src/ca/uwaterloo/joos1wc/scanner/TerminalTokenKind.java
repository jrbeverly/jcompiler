package ca.uwaterloo.joos1wc.scanner;

import java.io.ByteArrayOutputStream;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.automata.DFState;
import ca.uwaterloo.joos1wc.automata.DFTransition;

public enum TerminalTokenKind implements TokenKind {
  WHITESPACE {
    @Override
    public DFAutomaton getDFAutomaton() {
      DFState[]      states      = new DFState[2];
      DFTransition[] transitions = new DFTransition[2];

      states[0] = new DFState(false); // start
      states[1] = new DFState(true);  // end
      
      transitions[0] = new DFTransition(states[0], DFTransition.WHITESP,  states[1]);
      transitions[1] = new DFTransition(states[1], DFTransition.WHITESP,  states[1]);
      
      return new DFAutomaton(states, transitions, this);
    }
  },
  
  COMMENT {
    @Override
    public DFAutomaton getDFAutomaton() {
      DFState[]      states      = new DFState[6];
      DFTransition[] transitions = new DFTransition[9];
      
      states[0] = new DFState(false); // start
      states[1] = new DFState(false); // we've seen a /
      // if we hit 2 then we've got a one line comment
      states[2] = new DFState(true);  // we've seen two /
      // if we hit 3 then we start a block comment
      states[3] = new DFState(false); // we've seen /*
      states[4] = new DFState(false); // we're in a block comment and saw a *
      states[5] = new DFState(true);  // we finished a block comment with a */
      
      transitions[0] = new DFTransition(states[0], '/', states[1]);
      // transitions for one line comments
      transitions[1] = new DFTransition(states[1], '/', states[2]);
      transitions[2] = new DFTransition(states[2], DFTransition.RESTOFLINE, states[2]);
      
      // transitions for starting block comments
      transitions[3] = new DFTransition(states[1], '*', states[3]);
      transitions[4] = new DFTransition(states[3], DFTransition.NOTSTAR, states[3]);
      // transitions for ending block comments
      transitions[5] = new DFTransition(states[3], '*', states[4]);
      transitions[6] = new DFTransition(states[4], '*', states[4]);
      transitions[7] = new DFTransition(states[4], DFTransition.NOTSLASH, states[3]);
      transitions[8] = new DFTransition(states[4], '/', states[5]);
      
      return new DFAutomaton(states, transitions, this);
    }
  },
  
  // Keywords
  ABSTRACT,    CONTINUE,    FOR,           NEW,          SWITCH,
  ASSERT,      DEFAULT,     IF,            PACKAGE,      SYNCHRONIZED,
  BOOLEAN,     DO,          GOTO,          PRIVATE,      THIS,
  BREAK,       DOUBLE,      IMPLEMENTS,    PROTECTED,    THROW,
  BYTE,        ELSE,        IMPORT,        PUBLIC,       THROWS,
  CASE,        ENUM,        INSTANCEOF,    RETURN,       TRANSIENT,
  CATCH,       EXTENDS,     INT,           SHORT,        TRY,
  CHAR,        FINAL,       INTERFACE,     STATIC,       VOID, 
  CLASS,       FINALLY,     LONG,          STRICTFP,     VOLATILE,
  CONST,       FLOAT,       NATIVE,        SUPER,        WHILE,
  
  // Null literal
  NULL,
  
  // Boolean literals
  TRUE, FALSE,

  // Char literals
  CHARLITERAL {
    private static final String ESCAPE_CODES = "btnfr\"'\\";
    private static final String ESCAPE_CODE_VALS = "\b\t\n\f\r\"'\\";
    
    @Override
    public DFAutomaton getDFAutomaton() {
      final DFState[] states = new DFState[9];
      states[0] = new DFState(false);  // start state
      states[1] = new DFState(false);  // saw an opening apostrophe
      states[2] = new DFState(false);  // saw a char
      states[3] = new DFState(false);  // saw a backslash
      states[4] = new DFState(false);  // saw a valid escape sequence
      states[5] = new DFState(false);  // can see two more octal digits
      states[6] = new DFState(false);  // can see one more octal digit
      states[7] = new DFState(false);  // can see no more octal digits
      states[8] = new DFState(true);   // saw a close apostrophe
      
      // A little hacky - we need to store context across multiple transitions for octal escapes, which should really
      // be kept within DFAutomaton, but we need to do all initialization here
      DFTransition.Action octalAction = new DFTransition.Action() {
        byte val = 0;
        @Override
        public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
          if (t.fromState == states[3]) {
            val = 0;
          }
          if (t.toState == states[8]) {
            out.write(val);
          } else {
            val = (byte)((val << 3) + (c - '0'));
          }
        }
      };
      
      DFTransition[] transitions = new DFTransition[] {
        new DFTransition(states[0], '\'',  states[1]),
        new DFTransition(states[1], DFTransition.NOTAPOS, states[2], new DFTransition.Action() {
          @Override
          public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
            out.write(c);
          }
        }),

        // escape sequences
        new DFTransition(states[1], '\\', states[3]),
        new DFTransition(states[3], DFTransition.ESCAPE, states[4], new DFTransition.Action() {
          @Override
          public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
            out.write(ESCAPE_CODE_VALS.charAt(ESCAPE_CODES.indexOf(c)));
          }
        }),

        // octal escapes
        new DFTransition(states[3], DFTransition.ZEROTOTHREE, states[5], octalAction),
        new DFTransition(states[5], DFTransition.OCTAL, states[6], octalAction),
        new DFTransition(states[6], DFTransition.OCTAL, states[7], octalAction),
        new DFTransition(states[3], DFTransition.FOURTOSEVEN, states[6], octalAction),

        // end on a final apostrophe
        new DFTransition(states[2], '\'', states[8]),
        new DFTransition(states[4], '\'', states[8]),
        new DFTransition(states[5], '\'', states[8], octalAction),
        new DFTransition(states[6], '\'', states[8], octalAction),
        new DFTransition(states[7], '\'', states[8], octalAction),
      };

      return new DFAutomaton(states, transitions, this);
    }
  },
  
  // String literals
  STRLITERAL {
    private static final String ESCAPE_CODES = "btnfr\"'\\";
    private static final String ESCAPE_CODE_VALS = "\b\t\n\f\r\"'\\";
    
    @Override
    public DFAutomaton getDFAutomaton() {
      final DFState[] states = new DFState[8];
      states[0] = new DFState(false);  // start state
      states[1] = new DFState(false);  // saw an opening quote
      states[2] = new DFState(false);  // saw a backslash
      states[3] = new DFState(false);  // saw a valid escape sequence
      states[4] = new DFState(false);  // can see two more octal digits
      states[5] = new DFState(false);  // can see one more octal digit
      states[6] = new DFState(false);  // can see no more octal digits
      states[7] = new DFState(true);   // saw a close quote
      
      DFTransition.Action plainCharAction = new DFTransition.Action() {
        @Override
        public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
          out.write(c);
        }
      };
      
      DFTransition.Action octalAction = new DFTransition.Action() {
        byte val = 0;
        @Override
        public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
          if (t.fromState == states[2]) {
            val = 0;
          }
          if (t.toState == states[1] || t.toState == states[2] || t.toState == states[7]) {
            out.write(val);
            if (t.toState == states[1]) {
              out.write(c);
            }
          } else {
            val = (byte)((val << 3) + (c - '0'));
          }
        }
      };

      DFTransition[] transitions = new DFTransition[] {
        new DFTransition(states[0], '\"',  states[1]),
        new DFTransition(states[1], DFTransition.NOTQUOTE, states[1], plainCharAction),

        // escape sequences
        new DFTransition(states[1], '\\', states[2]),
        new DFTransition(states[2], DFTransition.ESCAPE, states[3], new DFTransition.Action() {
          @Override
          public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
            out.write(ESCAPE_CODE_VALS.charAt(ESCAPE_CODES.indexOf(c)));
          }
        }),

        // octal escapes
        new DFTransition(states[2], DFTransition.ZEROTOTHREE, states[4], octalAction),
        new DFTransition(states[4], DFTransition.OCTAL, states[5], octalAction),
        new DFTransition(states[5], DFTransition.OCTAL, states[6], octalAction),
        new DFTransition(states[2], DFTransition.FOURTOSEVEN, states[5], octalAction),

        // end on a final quote
        new DFTransition(states[1], '\"', states[7]),
        new DFTransition(states[3], '\"', states[7]),
        new DFTransition(states[4], '\"', states[7], octalAction),
        new DFTransition(states[5], '\"', states[7], octalAction),
        new DFTransition(states[6], '\"', states[7], octalAction),

        // make sure we can loop around to the start after an escape sequence
        new DFTransition(states[3], DFTransition.NOTQUOTE, states[1], plainCharAction),
        new DFTransition(states[4], DFTransition.NOTQUOTE, states[1], octalAction),
        new DFTransition(states[5], DFTransition.NOTQUOTE, states[1], octalAction),
        new DFTransition(states[6], DFTransition.NOTQUOTE, states[1], octalAction),
        new DFTransition(states[3], '\\', states[2]),
        new DFTransition(states[4], '\\', states[2], octalAction),
        new DFTransition(states[5], '\\', states[2], octalAction),
        new DFTransition(states[6], '\\', states[2], octalAction)
      };

      return new DFAutomaton(states, transitions, this);
    }
  },
  
  // Int literals
  INTLITERAL {
    @Override
    public DFAutomaton getDFAutomaton() {
      final DFState[] states = new DFState[3];
      DFTransition[] transitions = new DFTransition[3];

      states[0] = new DFState(false); // start
      states[1] = new DFState(true);  // first char was a 0
      states[2] = new DFState(true);  // first char was any other number
      
      DFTransition.Action action = new DFTransition.Action() {
        @Override
        public void execute(char c, DFTransition t, ByteArrayOutputStream out) {
          out.write(c - '0');
        }
      };
      
      // only 0 can start with 0, otherwise it's Octal which we don't support
      transitions[0] = new DFTransition(states[0], '0',  states[1], action);
      transitions[1] = new DFTransition(states[0], DFTransition.POSITIVE, states[2], action);
      // after a non-zero start we can match any number
      transitions[2] = new DFTransition(states[2], DFTransition.NUMERIC, states[2], action);
      
      return new DFAutomaton(states, transitions, this);
    }
  },

  // Separators
  LPAREN("("), RPAREN (")"), LBRACE("{"), RBRACE("}"), LBRACKET("["),
  RBRACKET("]"), SEMICOLON(";"), COMMA(","), DOT("."),
  
  // Operators
  EQ("="), GT(">"), LT("<"), BANG("!"), TILDE("~"), QMARK("?"),
  COLON(":"), DEQUAL("=="), LTE("<="), GTE(">="), NEQ("!="),
  LOGAND("&&"), LOGOR("||"), PP("++"), MM("--"), PLUS("+"),
  MINUS("-"), MULT("*"), DIV("/"), BITAND("&"), BITOR("|"),
  BITNOT("^"), MOD("%"), LSHIFT("<<"), RSHIFT(">>"), URSHIFT(">>>"),
  PLUSEQ("+="), MINUSEQ("-="), MULTEQ("*="), DIVEQ("/="),
  BITANDEQ("&="), BITOREQ("|="), BITNOTEQ("^="), MODEQ("%="),
  LSHIFTEQ("<<="), RSHIFTEQ(">>="), URSHIFTEQ(">>>="),
  
  ID {
    @Override
    public DFAutomaton getDFAutomaton() {
      DFState[]      states      = new DFState[2];
      DFTransition[] transitions = new DFTransition[3];

      states[0] = new DFState(false); // start state
      states[1] = new DFState(true);  // found a valid id
      
      // valid starting characters
      transitions[0] = new DFTransition(states[0], DFTransition.LETTER,  states[1]);
      
      // the rest of the id
      transitions[1] = new DFTransition(states[1], DFTransition.LETTER,  states[1]);
      transitions[2] = new DFTransition(states[1], DFTransition.NUMERIC,  states[1]);

      return new DFAutomaton(states, transitions, this);
    }
  }, 
  
  // EOF is special - leave it at the end
  EOF {
    @Override
    public DFAutomaton getDFAutomaton() {
      DFState[] states = new DFState[] { new DFState(false) };
      return new DFAutomaton(states, null, this);
    }
  };
  
  
  /**
   * This is used to keep track of the actual string that we need to match
   * for a keyword or similar type of token.
   */
  private String stringMatch = null;
  
  /**
   * Parameterless constructor sets the word to match to the lowercase 
   * version of the name of the enum.  If a different string it needed, 
   * the parameterized version of the constructor should be used.
   */
  TerminalTokenKind() {
    this.stringMatch = this.name().toLowerCase();
  }
  /**
   * Parameterized constructor sets the word to match to whatever is 
   * passed in.
   * 
   * @param toMatch the string to match for keyword-like tokens
   */
  TerminalTokenKind(String toMatch) {
    this.stringMatch = toMatch;
  }
  
  /**
   * Get the DFAutomaton which matches this TokenKind
   * The default version of getDFAutomoton returns the keyword DFA
   * using whatever String was set by the constructor.
   * 
   * @return A DFAutomaton which will match this TokenKind
   */
  public DFAutomaton getDFAutomaton() {
    return getKeywordDFA(this.stringMatch, this);
  }
  
  /**
   * Returns the keyword version of a DFAutomaton
   * 
   * @return The DFA for a keyword TokenKind
   */
  private static DFAutomaton getKeywordDFA(String keyword, TerminalTokenKind kind) {
    int length = keyword.length();
    DFState[] states = new DFState[length + 1];
    DFTransition[] transitions = new DFTransition[length];
    
    // add a state for each letter
    for (int i = 0; i < length; i++) {
      DFState nstate = new DFState(false);
      states[i] = nstate;
    }

    // add an accepting state
    DFState endState = new DFState(true);
    states[length] = endState;

    // add a transition for each letter
    for (int i = 0; i < length; i++) {
      char value = keyword.charAt(i);
      DFTransition trans = new DFTransition(states[i], value, states[i + 1]);
      transitions[i] = trans;
    }
    
    return new DFAutomaton(states, transitions, kind);
  }

}
