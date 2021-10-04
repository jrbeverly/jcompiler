package ca.uwaterloo.joos1wc.parse;

import java.util.List;

import ca.uwaterloo.joos1wc.scanner.Token;

/* Parse tree stores the output after the LRParser has run creating a tree structure*/
public class ParseTree {

  public interface Node {
  }

  /* Represents a production rule and its children */
  public class ProductionNode implements Node {
    public String production; //point to production rule
    public List<Node> children;
  }

  /* Represents a token from the object */
  public class TokenNode implements Node {
    public Token token;
  }

  public Node Root;

  public ParseTree(Node root) {

  }
}
