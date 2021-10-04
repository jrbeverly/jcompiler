package ca.uwaterloo.joos1wc.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.uwaterloo.joos1wc.Joos1Wc;
import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.diagnostics.Exceptions;
import ca.uwaterloo.joos1wc.diagnostics.LexException;
import ca.uwaterloo.joos1wc.utility.CharacterStream;

/**
 * Responsible for the lexing step of compiling Joos1w source.
 */
public class Lexer {
  private final List<DFAutomaton> dfaList;
  private final Set<TokenKind> ignore;

  public Lexer(List<DFAutomaton> dfaList, Set<TokenKind> ignore) {
    this.dfaList = dfaList;
    this.ignore = ignore;
  }

  /**
   * Lex a Joos1w source file into tokens. Calls getTokensFromDFAs as its worker method.
   * 
   * @param sourceFile
   *          The file containing Joos1w source to lex
   * @return A list of Tokens found in the source, in the order encountered
   * @throws FileNotFoundException
   * @throws IOException
   */
  public List<Token> lex(File sourceFile) throws FileNotFoundException, IOException, LexException {
    assert sourceFile != null;

    // make an CharacterStream from the source file to keep track of reading
    CharacterStream cursor = new CharacterStream(sourceFile);

    try {
      // call the worker method to get the List of Tokens
      return getTokensFromDFAs(cursor);

    } finally {
      // guarantee that we close the stream when we're done
      cursor.close();
    }
  }

  /**
   * Worker method for lexing. Streams the input to the lexers and builds up a list of Tokens using maximal munge.
   * 
   * @param cursor
   *          The CharacterStream to use for keeping track of our place in the input
   * @return A list of the Tokens contained in the source file
   * @throws IOException
   */
  protected List<Token> getTokensFromDFAs(CharacterStream cursor) throws IOException, LexException {
    // the list to return
    List<Token> tokens = new ArrayList<Token>();

    // we assume that cursor starts at the beginning of the file
    while (!cursor.isEndOfInput()) {
      // keep track of the longest token we've seen
      Token longestToken = null;

      // iterate over the list of DFAutomota
      for (DFAutomaton dfa : this.dfaList) {
        // getLongerToken get a token from the dfa and returns the longer of the two
        longestToken = getLongerToken(dfa, cursor, longestToken);
      }

      // if we found any tokens, then we found a longest one
      if (longestToken != null) {
        // advance the cursor to the position after the token we found
        cursor.advance(longestToken.length());
        // add the token to the list we'll return if it isn't in the ignore set
        if (!this.ignore.contains(longestToken.getKind())) {
          tokens.add(longestToken);
        }

      } else {
        // for debugging purposes, first print the list we have
        if (Joos1Wc.DEBUG) {
          Token.printTokenList(tokens);
        }
        // if we didn't manage to find any more tokens, then throw an exception
        String buffer = cursor.getBufferedString();
        int currLine = cursor.getCurrentLine();
        int linePos = cursor.getLinePosition();
        throw new LexException(String.format(Exceptions.ILLEGAL_TOKEN, currLine, linePos, buffer), buffer, currLine,
            linePos);
      }
    }

    // Add the EOF token
    tokens.add(new Token(cursor.getFile(), TerminalTokenKind.EOF, "$", -1, -1));
    return tokens;
  }

  /**
   * Tries to get a token from the passed DFA and input, then returns the longer of the acquired and passed tokens. If
   * the DFA doesn't return a token the the passed token is returned. If the passed token is null then the acquired
   * token is always returned. If both are null then null is returned.
   * 
   * @param dfa
   *          DFAutomaton to call munge on to try to get a new token
   * @param cursor
   *          The CharacterStream that refers to the source file
   * @param longestToken
   *          The current longest token to compare to
   * @return The longer of the passed and munged tokens
   * @throws IOException
   */
  protected static Token getLongerToken(DFAutomaton dfa, CharacterStream cursor, Token longestToken) throws IOException {
    // reset the cursor to the same start position for each DFA
    cursor.resetCurrentPosition();
    // use maximal munge to find a token from the DFA
    Token result = dfa.munge(cursor);

    if (result != null) {
      // if a token is found, and it's longer, store it
      // note that ties go to the first one found
      if (longestToken == null || result.length() > longestToken.length()) {
        longestToken = result;
      }
    }

    return longestToken;
  }
}
