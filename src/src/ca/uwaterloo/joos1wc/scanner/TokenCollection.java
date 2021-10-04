package ca.uwaterloo.joos1wc.scanner;

import java.io.File;
import java.util.List;

public class TokenCollection {

  private File _file;
  private List<Token> _tokens;

  public TokenCollection() {

  }

  public boolean add(Token token) {
    return true;
  }

  // Get ListOf(Token)

  public int size() {
    return _tokens.size();
  }
}
