package ca.uwaterloo.joos1wc.scanner;

import java.io.File;
import java.util.List;

/**
 * The token is an abstract symbol representing a kind of lexical unit.
 * */
public class Token {

  private final String lexeme;
  private final TokenKind tkind;
  private final File sourcefile;
  private final int linenumber;
  private final int position;
  private byte[] literalValue = null;

  /**
   * Constructs a new token for the specified text and kind.
   * 
   * @param file
   *          The filename of this Token.
   * @param kind
   *          A value that describes the kind of this token.
   * @param text
   *          The string image of the token.
   * @param line
   *          The line number of the character of this Token.
   * @param posn
   *          The column number of the first character of this Token.
   * */
  public Token(File file, TokenKind kind, String text, int line, int posn) {
    tkind = kind;
    lexeme = text;
    sourcefile = file;
    linenumber = line;
    position = posn;
  }

  public Token(File file, TokenKind kind, String text, int line, int posn, byte[] literalValue) {
    this(file, kind, text, line, posn);

    // Do some post-processing for integer literals
    if (kind == TerminalTokenKind.INTLITERAL) {
      long value = 0;
      for (byte x : literalValue) {
        value = value * 10 + x;
        if (value > 0x80000000l) {
          throw new RuntimeException("Integer value out of range");
        }
      }
      this.literalValue = new byte[4];
      for (int i = 3; i >= 0; i--) {
        this.literalValue[i] = (byte) (value & 0xFF);
        value >>>= 8;
      }
    } else {
      this.literalValue = literalValue;
    }
  }

  /**
   * A value that describes the kind of this token.
   * 
   * @return A value that describes the token.
   * */
  public TokenKind getKind() {
    return tkind;
  }

  /**
   * The string image of the token.
   * 
   * @return The string image of the token.
   * */
  public String getImage() {
    return lexeme;
  }

  /**
   * Returns the length of this string. The length is equal to the number of ASCII-7 code units in the string.
   * 
   * @return The length of the sequence of characters represented by this object.
   */
  public int length() {
    return lexeme.length();
  }

  /**
   * The line number of the character of this Token.
   * 
   * @return The line number of this Token.
   * */
  public int getLineNumber() {
    return linenumber;
  }

  /**
   * The column number of the first character of this Token.
   * 
   * @return The column number of this Token.
   * */
  public int getPosition() {
    return position;
  }

  /**
   * The file of this Token.
   * 
   * @return The file of this Token.
   * */
  public File getFile() {
    return sourcefile;
  }

  public byte[] getLiteralValue() {
    return literalValue;
  }

  @Override
  public boolean equals(Object toCompare) {
    if (!(toCompare instanceof Token))
      return false;
    if (toCompare == this)
      return true;
    return tkind == ((Token) toCompare).getKind();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tkind == null) ? 0 : tkind.ordinal());
    return result;
  }

  @Override
  public String toString() {
    if (tkind == TerminalTokenKind.WHITESPACE) {
      return "<WS>";
    }
    return String.format("<%s %s>", tkind, "\"" + lexeme + "\"");
  }

  /**
   * Prints a sequence of tokens.
   * 
   * @param tokens
   *          A sequence of tokens.
   */
  public static void printTokenList(List<Token> tokens) {
    assert tokens != null;

    int lastLine = 0;
    int nextLine = 0;

    for (Token token : tokens) {
      nextLine = token.getLineNumber();
      while (lastLine < nextLine) {
        System.out.println();
        lastLine++;
      }

      System.out.print(token.toString() + " ");
    }
    System.out.println();
  }
}
