package ca.uwaterloo.joos1wc.scanner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFAutomaton;
import ca.uwaterloo.joos1wc.scanner.TerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;
import ca.uwaterloo.joos1wc.utility.CharacterStream;

/**
 * This class contains functional tests that combine the DFA class and the definitions in TerminalTokenKind.
 * This is meant to demonstrate that the DFAs work as expected for different tokens.
 *
 */
public class DFAutomatonFunctionalTest {
  @Test
  public void testWhitespaceMatch() {
    TerminalTokenKind t = TerminalTokenKind.WHITESPACE;
    shouldMatch(t, "    ");
    shouldMatch(t, "\t\t\t");
    shouldMatch(t, "\n");
    shouldMatch(t, "\r");
    shouldMatch(t, "\n\r");
    shouldMatch(t, " \n   \r\t");
    shouldMatch(t, "  \t   \t   ");
    shouldMatch(t, "     \n");
    shouldNotMatch(t, "\b ");
    shouldNotMatch(t, "a\t");
    shouldNotMatch(t, "(\n");
    shouldNotMatch(t, "\\n");
    shouldNotMatch(t, "\\t");
    shouldNotMatch(t, "\\r");
    shouldNotMatch(t, "\\ ");
  }
  
  @Test
  public void testCommentMatch() {
    TerminalTokenKind t = TerminalTokenKind.COMMENT;
    shouldMatch(t, "// this is a comment");
    shouldMatch(t, "/* this is a block comment */");
    shouldMatch(t, "// comment with escapes \\t \\b \\u0034 etc.");
    shouldMatch(t, "/* block comment with escapes \\t \\b \\u0034 etc.*/");
    shouldMatch(t, "/****** block comment with lots of extra stars *********/");
    shouldMatch(t, "/**  some more \n  *  more\n\r  * and yeah\n  */");
    shouldMatch(t, "//");
    shouldMatch(t, "/**/");
    shouldMatch(t, "// /*   still a comment");
    shouldMatch(t, "/*    //    */");
    shouldNotMatch(t, "/ /");
    shouldNotMatch(t, "/*/");
    shouldNotMatch(t, "/*    never ends!");
    shouldNotMatch(t, "/\n/");
  }
  
  @Test
  public void testCharLiteralMatch() {
    TerminalTokenKind t = TerminalTokenKind.CHARLITERAL;
    shouldMatchWithValue(t, "' '", " ".getBytes());
    shouldMatchWithValue(t, "'\\n'", "\n".getBytes());
    shouldMatchWithValue(t, "'0'", "0".getBytes());
    shouldMatchWithValue(t, "'a'", "a".getBytes());
    shouldMatchWithValue(t, "'\\''", "'".getBytes());
    shouldMatchWithValue(t, "'\\\\'", "\\".getBytes());
    shouldMatchWithValue(t, "'\t'", "\t".getBytes());
    shouldMatchWithValue(t, "'\\012'", "\012".getBytes());
    shouldMatchWithValue(t, "'\\123'", "\123".getBytes());
    shouldMatchWithValue(t, "'\\67'", "\67".getBytes());
    shouldNotMatch(t, "''");
    shouldNotMatch(t, "'\n'");
    shouldNotMatch(t, "'");
    shouldNotMatch(t, "'  '");
    shouldNotMatch(t, "'\\n\\r'");
    shouldNotMatch(t, "'\\'");
    shouldNotMatch(t, "'\\78'");
    shouldNotMatch(t, "'\\456'");
  }
  
  @Test
  public void testStrLiteralMatch() throws UnsupportedEncodingException {
    TerminalTokenKind t = TerminalTokenKind.STRLITERAL;
    shouldMatchWithValue(t, "\"\"", "".getBytes());
    shouldMatchWithValue(t, "\"    \"", "    ".getBytes());
    shouldMatchWithValue(t, "\"123\"", "123".getBytes());
    shouldMatchWithValue(t, "\"''\"", "''".getBytes());
    shouldMatchWithValue(t, "\"I'm a string\"", "I'm a string".getBytes());
    shouldMatchWithValue(t, "\"// I'm a string with a comment in it\"", "// I'm a string with a comment in it".getBytes());
    shouldMatchWithValue(t, "\"escapes \\n\\r  \\b \\\\ \"", "escapes \n\r  \b \\ ".getBytes());
    shouldMatchWithValue(t, "\"fun chars... +123.123 /* */ ' ' \\\"\\\'\"", "fun chars... +123.123 /* */ ' ' \"'".getBytes());
    shouldMatchWithValue(t, "\"\\\\\\\"\\\\\\\'\\\\\\\\\"", "\\\"\\'\\\\".getBytes());
    shouldMatchWithValue(t, "\"'\"", "'".getBytes());
    shouldMatchWithValue(t, "\"octal escape 1 \\012\\123\"", "octal escape 1 \012\123".getBytes());
    shouldMatchWithValue(t, "\"octal escape 2 \\234\\345\"", "octal escape 2 \234\345".getBytes("ISO-8859-1"));
    shouldMatchWithValue(t, "\"octal escape 3 \\45\\67\"", "octal escape 3 \45\67".getBytes());
    shouldMatchWithValue(t, "\"octal escape 4 \\012345\"", "octal escape 4 \012345".getBytes());
    shouldNotMatch(t, "\"asdf\nasdf\"");
    shouldNotMatch(t, "\"fdsa\rfdsa\"");
    shouldNotMatch(t, "\"\\u0034\"");
    shouldNotMatch(t, "\"\\\\\'");
    shouldNotMatch(t, "\"");
    shouldNotMatch(t, "\"x");
    shouldNotMatch(t, "\"\\89\"");
  }
  
  @Test
  public void testIntLiteralMatch() {
    TerminalTokenKind t = TerminalTokenKind.INTLITERAL;
    shouldMatchWithValue(t, "42", new byte[] {0x00, 0x00, 0x00, (byte)0x2A});
    shouldMatchWithValue(t, "1234", new byte[] {0x00, 0x00, 0x04, (byte)0xD2});
    shouldMatchWithValue(t, "0", new byte[] {0x00, 0x00, 0x00, 0x00});
    shouldMatchWithValue(t, "10", new byte[] {0x00, 0x00, 0x00, 0x0A});
    shouldMatchWithValue(t, "2147483647", new byte[] {0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF});
    shouldMatchWithValue(t, "2147483648", new byte[] {(byte)0x80, 0x00, 0x00, 0x00});
    shouldNotMatch(t, "_0"); // ahem
    shouldMatchWithException(t, "2147483649");
    shouldMatchWithException(t, "1304124398714");
  }
  
  @Test
  public void testIdMatch() {
    TerminalTokenKind t = TerminalTokenKind.ID;
    shouldMatch(t, "asdf");
    shouldMatch(t, "abstract");
    shouldMatch(t, "_id");
    shouldMatch(t, "$php");
    shouldMatch(t, "_$_$_$_$_$_$_$");
    shouldMatch(t, "moar_catz");
    shouldMatch(t, "foo$bar");
    shouldMatch(t, "number1");
    shouldMatch(t, "and0too");
    shouldMatch(t, "all_together_$now_9876_ftw");
    shouldNotMatch(t, "4justkidding");
    shouldNotMatch(t, "-thisisbad");
    shouldNotMatch(t, ".again");
  }
  
  @Test
  public void testStringMatch() {
    TerminalTokenKind t = TerminalTokenKind.ABSTRACT;
    
    shouldMatch(t, "abstract");
    shouldNotMatch(t, "abstra\nct");
    shouldNotMatch(t, "Abstract");
    shouldNotMatch(t, " abstract");
  }

  /**
   * Helper methods for checking that the correct strings are matched.
   */
  private void shouldNotMatch(TerminalTokenKind kind, String match) {
    DFAutomaton dfa = kind.getDFAutomaton();
    try {
      CharacterStream input = new MockInputCursor(new ByteArrayInputStream(match.getBytes()));

      Assert.assertNull(kind.name() + " munge should return null for " + match, dfa.munge(input));
      
    } catch (IOException ioe) {
      Assert.fail("Caught IOException during " + kind.name() + " munge: " + ioe.getMessage());
    }
  }
  
  private void shouldMatch(TerminalTokenKind kind, String match) {
    shouldMatchWithValue(kind, match, null);
  }
  
  private void shouldMatchWithValue(TerminalTokenKind kind, String match, byte[] value) {
    DFAutomaton dfa = kind.getDFAutomaton();
    try {
      CharacterStream input = new MockInputCursor(new ByteArrayInputStream(match.getBytes()));

      Token token = dfa.munge(input);
      Assert.assertNotNull(kind.name() + " munge should return a token for " + match, token);
      Assert.assertEquals(kind.name() + " lexeme is wrong", match, token.getImage());
      if (value != null) {
        Assert.assertArrayEquals(value, token.getLiteralValue());
      }
      
    } catch (IOException ioe) {
      Assert.fail("Caught IOException during " + kind.name() + " munge: " + ioe.getMessage());
    }
  }
  
  private void shouldMatchWithException(TerminalTokenKind kind, String match) {
    DFAutomaton dfa = kind.getDFAutomaton();
    try {
      CharacterStream input = new MockInputCursor(new ByteArrayInputStream(match.getBytes()));

      dfa.munge(input);
      Assert.fail("Should have thrown exception");
      
    } catch (IOException ioe) {
      Assert.fail("Caught IOException during " + kind.name() + " munge: " + ioe.getMessage());
    } catch (RuntimeException e) {}
  }
}
