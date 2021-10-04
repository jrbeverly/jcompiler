package ca.uwaterloo.joos1wc.parse;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import ca.uwaterloo.joos1wc.ast.TreeNode;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.LRException;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public class LRParser {
  private final LRParseState startState;

  public LRParser(LRParseState startState) {
    this.startState = startState;
  }

  /**
   * Parse through the list of tokens and return a parse tree Implements Shift-reduce
   * 
   * @throws LRException
   */
  public TreeNode parse(List<Token> tokens) throws LRException {
    Deque<TreeNode> symbolStack = new ArrayDeque<TreeNode>();
    Deque<LRParseState> stateStack = new ArrayDeque<LRParseState>();

    // currentState is c in the shift reduce algorithm we were given in class
    stateStack.push(startState);

    for (Token next : tokens) {
      reduceToken(symbolStack, stateStack, next);
      if (next.getKind() == TerminalTokenKind.EOF && stateStack.peek().shiftsEOF())
        break;
      shiftToken(symbolStack, stateStack, next);
    }

    if (symbolStack.size() > 1) {
      StringBuilder sb = new StringBuilder();
      for(TreeNode next : symbolStack)
      {
        sb.append(next.token.getImage());
        sb.append(' ');
      }
      throw new LRException(String.format(Exceptions.ROOT_REDUCTION, sb.toString()));
    }

    return symbolStack.pop();
  }

  public static void reduceToken(Deque<TreeNode> symbolStack, Deque<LRParseState> stateStack, Token next)
      throws LRException {

    LRParseState currentState = stateStack.peek();
    Production reduction;
    // while this is a reduce state where the the follow set contains c
    while ((reduction = currentState.reduce(next.getKind())) != null) {
      // pop the size of the right hand side off both stacks
      // this gets the new reduced token
      TreeNode lhs = popStacks(symbolStack, stateStack, reduction);

      currentState = stateStack.peek();
      symbolStack.push(lhs);
      currentState = currentState.shift(lhs.token.getKind());
      stateStack.push(currentState);
    }

    if (!currentState.isShiftState()) {
      StringBuilder sb = new StringBuilder();
      Iterator<TreeNode> nodes = symbolStack.iterator();
      while (nodes.hasNext()) {
        sb.append(nodes.next().token.getImage());
        sb.append(' ');
      }

      String error = String.format(Exceptions.SHIFT_EXPECTED, next.getImage(), sb.toString(), next.getLineNumber(),
          next.getPosition());

      throw new LRException(error);
    }
  }

  public static void shiftToken(Deque<TreeNode> symbolStack, Deque<LRParseState> stateStack, Token next)
      throws LRException {

    LRParseState currentState = stateStack.peek();
    // this is the point when we're told to fail if shifting on c doesn't work
    // but we'll defer until we actually try to shift and throw an exception
    // at that point
    LRParseState nextState = currentState.shift(next.getKind());
    if (nextState == null) {
      // System.err.format("Error shifting from state %s", currentState);
      // System.err.format("Token: %s (%s)\n", next.getKind().name(), next.getImage());

      String error = String.format(Exceptions.SHIFT_FAILURE, next.getImage(), next.getKind().name(),
          next.getLineNumber(), next.getPosition());
      throw new LRException(error);
    }
    symbolStack.push(TreeNode.newTerminalNode(next));
    stateStack.push(nextState);
  }

  /**
   * Pop the size of the production off both the symbol and state stacks and return the reduced token. This attempts to
   * preserve information about the source contained in the tokens within the returned token.
   * 
   * @param symbolStack
   *          The stack of TreeNodes to pop
   * @param stateStack
   *          The stack of ParseStates to pop
   * @param production
   *          The production rule to reduce from
   * @return A new TreeNode representing the reduction
   */
  public static TreeNode popStacks(Deque<TreeNode> symbolStack, Deque<LRParseState> stateStack, Production production) {
    int size = production.getRHS().size();
    Deque<TreeNode> reverseStack = new ArrayDeque<TreeNode>(size);

    File file = null;
    StringBuilder lexeme = new StringBuilder();
    int line = -1;
    int pos = -1;
    
    for (int i = size - 1; i >= 0; i--) {
      TreeNode node = symbolStack.pop();
      stateStack.pop();

      reverseStack.push(node);

      // Get the file, line number and line position from the token at the lowest position
      // in the stack. This should be the token that showed up first, so its line number and
      // position should be the ones we want for the lot.
      if (i == 0) {
        file = node.token.getFile();
        line = node.token.getLineNumber();
        pos = node.token.getPosition();
      }
    }

    List<TreeNode> children = new ArrayList<TreeNode>(reverseStack.size());
    while (!reverseStack.isEmpty()) {
      TreeNode node = reverseStack.pop();
      children.add(node);
      lexeme.append(node.token.getImage());
      if (!reverseStack.isEmpty()) {
        lexeme.append(" ");
      }
    }

    return TreeNode.newNonTerminalNode(new Token(file, production.getLHS(), lexeme.toString(), line, pos), children);
  }
}
