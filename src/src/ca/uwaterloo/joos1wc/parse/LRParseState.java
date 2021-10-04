package ca.uwaterloo.joos1wc.parse;

import java.util.HashMap;
import java.util.Map;

import ca.uwaterloo.joos1wc.scanner.TokenKind;

public class LRParseState {
  private final int id;
  private final Map<TokenKind, Production> reductions;
  private final Map<TokenKind, LRParseState> shifts;
  
  public LRParseState(int id) {
    this.id = id;
    reductions = new HashMap<TokenKind, Production>();
    shifts = new HashMap<TokenKind, LRParseState>();
  }
  
  public void addReduce(TokenKind token, Production rule) {
    reductions.put(token, rule);
  }
  
  public void addShift(TokenKind token, LRParseState state) {
    shifts.put(token, state);
  }
  
  public Production reduce(TokenKind follow) {
    return reductions.get(follow);
  }
  
  public LRParseState shift(TokenKind next) {
    return shifts.get(next);
  }
  
  public boolean isShiftState() {
    return shifts.size() > 0;
  }
  
  // Needed?  Either no or we should add an EOF token at the end of lexing.
  public boolean shiftsEOF() {
    return true;
  }
  
  public int id() {
    return id;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(id).append("\n");
    for (Map.Entry<TokenKind, Production> entry : reductions.entrySet()) {
      sb.append("  ").append(entry.getKey().name()).append(" reduce ").append(entry.getValue().getLHS().name()).append("\n");
    }
    for (Map.Entry<TokenKind, LRParseState> entry : shifts.entrySet()) {
      sb.append("  ").append(entry.getKey().name()).append(" shift ").append(entry.getValue().id()).append("\n");
    }
    return sb.toString();
  }
}
