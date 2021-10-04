package ca.uwaterloo.joos1wc.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.ast.declaration.MethodDeclaration;
import ca.uwaterloo.joos1wc.ast.declaration.TypeDeclaration;

public class MethodDeclarationSignatureTest {
  TypeDeclaration t0, t1;
  Type.FormalType f0, f1;
  MethodDeclaration.Signature m0, m1, m2, c0, c1, c2;

  @Before
  public void setup() {
    t0 = new TypeDeclaration(null, null, "t0", null, null, null, null, true, false);
    t1 = new TypeDeclaration(null, null, "t1", null, null, null, null, true, false);
    f0 = new Type.FormalType(false, t0);
    f1 = new Type.FormalType(false, t1);
    
    m0 = new MethodDeclaration.Signature(false, "method", Arrays.asList(f0, f1));
    m1 = new MethodDeclaration.Signature(false, "method", Arrays.asList(f0, f1));
    m2 = new MethodDeclaration.Signature(false, "method", Arrays.asList(f0));
    
    c0 = new MethodDeclaration.Signature(true, "t0", Arrays.asList(f0, f1));
    c1 = new MethodDeclaration.Signature(true, "t1", Arrays.asList(f0, f1));
    c2 = new MethodDeclaration.Signature(true, "t0", Arrays.asList(f0));
  }
  
  @Test
  public void equal() {
    Assert.assertTrue("Same should be equal", m0.equals(m0));
    Assert.assertTrue("Equal should be equal", m0.equals(m1));
  }
  
  @Test
  public void notEqual() {
    Assert.assertFalse("Different should not be equal", m0.equals(m2));
    Assert.assertFalse("Different should not be equal", m2.equals(m1));
  }
  
  @Test
  public void setContains() {
    Set<MethodDeclaration.Signature> set = new HashSet<MethodDeclaration.Signature>();
    set.add(m0);
    Assert.assertTrue("Should be contained in set", set.contains(m0));
    Assert.assertTrue("Should be contained in set", set.contains(m1));
    Assert.assertFalse("Should not be contained in set", set.contains(m2));
  }
  
  @Test
  public void constructors() {
    Assert.assertTrue("Should be equal if the same parameters", c0.equals(c1));
    Assert.assertFalse("Should not be equal if different params", c0.equals(c2));
  }
}
