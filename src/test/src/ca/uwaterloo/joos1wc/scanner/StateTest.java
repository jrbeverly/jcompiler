package ca.uwaterloo.joos1wc.scanner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.automata.DFState;
import ca.uwaterloo.joos1wc.automata.DFTransition;

public class StateTest {
  private DFState s;
  private DFState t;
  
  @Before
  public void setup() {
    s = new DFState(false);
    t = new DFState(true);
  }
  
  @Test
  public void uniqueIds() {
    Assert.assertNotEquals("ids should be unique", s.id, t.id);
    Assert.assertEquals("ids should be sequential", s.id + 1, t.id);
    Assert.assertTrue("ids should be greater than 0", (s.id > 0 && t.id > 0));
  }
  
  @Test
  public void transitionCount() {
    Assert.assertEquals("transition count for empty state is wrong", 0, s.getCount());
    DFTransition trans = new DFTransition(s, '1', t);
    Assert.assertEquals("fromState is wrong", s, trans.fromState);
    Assert.assertEquals("transition count is wrong after adding transition", 1, s.getCount());
  }
  
  @Test
  public void equals() {
    Assert.assertTrue("state should equal itself", s.equals(s));
    Assert.assertFalse("two states shouldn't be equal (s)", s.equals(t));
    Assert.assertFalse("two states shouldn't be equal (t)", t.equals(s));
  }
}
