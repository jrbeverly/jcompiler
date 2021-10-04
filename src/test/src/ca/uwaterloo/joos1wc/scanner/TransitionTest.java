package ca.uwaterloo.joos1wc.scanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFState;
import ca.uwaterloo.joos1wc.automata.DFTransition;

public class TransitionTest {
  private DFState one;
  private DFState two;
  private DFState three;

  @Before
  public void setup() {
    one = new DFState(false);
    two = new DFState(false);
    three = new DFState(true);
  }

  @Test
  public void executeRestOfLine() {
    DFTransition t = new DFTransition(one, DFTransition.RESTOFLINE, two);
    String rejects = "\r\n";
    escapeRejects(t, rejects);
  }

  @Test
  public void executeLetter() {
    DFTransition t = new DFTransition(one, DFTransition.LETTER, two);
    String accepts = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_$";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeNumeric() {
    DFTransition t = new DFTransition(one, DFTransition.NUMERIC, two);
    String accepts = "0123456789";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executePositive() {
    DFTransition t = new DFTransition(one, DFTransition.POSITIVE, two);
    String accepts = "123456789";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeEscape() {
    DFTransition t = new DFTransition(one, DFTransition.ESCAPE, two);
    String accepts = "btnfr\"\'\\";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeWhitespace() {
    DFTransition t = new DFTransition(one, DFTransition.WHITESP, two);
    String accepts = " \t\n\r";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeNotquote() {
    DFTransition t = new DFTransition(one, DFTransition.NOTQUOTE, two);
    String rejects = "\r\n\"\\";
    escapeRejects(t, rejects);
  }

  @Test
  public void executeNotApos() {
    DFTransition t = new DFTransition(one, DFTransition.NOTAPOS, two);
    String rejects = "\r\n\'\\";
    escapeRejects(t, rejects);
  }

  @Test
  public void executeNotStar() {
    DFTransition t = new DFTransition(one, DFTransition.NOTSTAR, two);
    String rejects = "*";
    escapeRejects(t, rejects);
  }

  @Test
  public void executeNotSlash() {
    DFTransition t = new DFTransition(one, DFTransition.NOTSLASH, two);
    String rejects = "/*";
    escapeRejects(t, rejects);
  }

  @Test
  public void executeZeroToThree() {
    DFTransition t = new DFTransition(one, DFTransition.ZEROTOTHREE, two);
    String accepts = "0123";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeFourToSeven() {
    DFTransition t = new DFTransition(one, DFTransition.FOURTOSEVEN, two);
    String accepts = "4567";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeOctal() {
    DFTransition t = new DFTransition(one, DFTransition.OCTAL, two);
    String accepts = "01234567";
    escapeAccepts(t, accepts);
  }

  @Test
  public void executeMatch() {
    char f = 'f';
    DFTransition t = new DFTransition(one, f, two);

    for (int i = 0; i < 128; i++) {
      char cast = (char) i;
      if (cast == f) {
        Assert.assertTrue("execute returned false on " + i, t.execute(cast, null));
      } else {
        Assert.assertFalse("execute returned true on " + i, t.execute(cast, null));
      }
    }
  }

  @Test
  public void setStateTransitions() {
    DFTransition t = new DFTransition(one, '1', two);
    DFTransition u = new DFTransition(one, '2', three);
    DFTransition v = new DFTransition(two, '3', three);

    Assert.assertEquals("wrong number of transitions for one", 2, t.fromState.getCount());
    Assert.assertEquals("wrong number of transitions for two", 1, v.fromState.getCount());
    Assert.assertEquals("wrong number of transitions for three", 0, u.toState.getCount());
  }

  /**
   * Helpers for writing magic escape tests
   */
  private void escapeAccepts(DFTransition t, String accepts) {
    for (int i = 0; i < 128; i++) {
      char cast = (char) i;
      if (accepts.indexOf(i) > -1) {
        Assert.assertTrue("execute returned false on " + i, t.execute(cast, null));
      } else {
        Assert.assertFalse("execute returned true on " + i, t.execute(cast, null));
      }
    }
  }

  private void escapeRejects(DFTransition t, String rejects) {
    for (int i = 0; i < 128; i++) {
      char cast = (char) i;
      if (rejects.indexOf(i) > -1) {
        Assert.assertFalse("execute returned true on " + i, t.execute(cast, null));
      } else {
        Assert.assertTrue("execute returned false on " + i, t.execute(cast, null));
      }
    }
  }
}
