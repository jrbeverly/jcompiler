package ca.uwaterloo.joos1wc.scanner;

public class NonTerminalTokenKind implements TokenKind {
  private final String name;
  private final int ordinal;

  // start the ordinals for non-terminals at the end of the ordinals for terminals
  private static int ordinalCount = TerminalTokenKind.EOF.ordinal();
  
  public NonTerminalTokenKind(String name) {
    this.name = name;
    this.ordinal = generateOrdinal();
  }
  
  private static synchronized int generateOrdinal() {
    return ++ordinalCount;
  }

  @Override
  public int ordinal() {
    return this.ordinal;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
