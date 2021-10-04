package ca.uwaterloo.joos1wc.sem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.uwaterloo.joos1wc.analysis.SymbolTable;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode;
import ca.uwaterloo.joos1wc.ast.INamedEntityNode.EntityType;
import ca.uwaterloo.joos1wc.ast.MockNamedEntityNode;

public class SymbolTableTest {
  private SymbolTable root, branch0, branch1, leaf0, leaf1;
  private String foo, bar, foobar;
  private INamedEntityNode fooNode, barNode, foobarNode;
  
  @Before
  public void setup() {
    foo = "foo";
    bar = "bar";
    foobar = "foobar";
    
    fooNode = new MockNamedEntityNode(foo, EntityType.PACKAGE);
    barNode = new MockNamedEntityNode(bar, EntityType.TYPE);
    foobarNode = new MockNamedEntityNode(foobar, EntityType.METHOD);
    
    root = new SymbolTable();
    branch0 = new SymbolTable(root, bar, barNode);
    branch1 = new SymbolTable(branch0, null, null);
    leaf0 = new SymbolTable(branch1, foo, fooNode);
    leaf1 = new SymbolTable(branch1, foobar, foobarNode);
  }

  @Test
  public void getFound() {
    INamedEntityNode resFoo = leaf0.get(foo);
    Assert.assertNotNull("Should have found symbol " + foo, resFoo);
    Assert.assertEquals("Wrong symbol returned", fooNode, resFoo);
    
    INamedEntityNode resBar0 = leaf0.get(bar);
    Assert.assertNotNull("Should have found symbol " + bar, resBar0);
    Assert.assertEquals("Wrong symbol returned", barNode, resBar0);
    
    INamedEntityNode resBar1 = leaf1.get(bar);
    Assert.assertNotNull("Should have found symbol " + bar, resBar1);
    Assert.assertEquals("Wrong symbol returned", barNode, resBar1);
    
    INamedEntityNode resBar2 = branch1.get(bar);
    Assert.assertNotNull("Should have found symbol " + bar, resBar2);
    Assert.assertEquals("Wrong symbol returned", barNode, resBar2);
    
    INamedEntityNode resBar3 = branch0.get(bar);
    Assert.assertNotNull("Should have found symbol " + bar, resBar3);
    Assert.assertEquals("Wrong symbol returned", barNode, resBar3);
  }
  
  @Test
  public void getMissing() {
    INamedEntityNode res0 = leaf0.get(foobar);
    Assert.assertNull("Should not have found symbol " + foobar, res0);
    INamedEntityNode res1 = branch1.get(foobar);
    Assert.assertNull("Should not have found symbol " + foobar, res1);
    INamedEntityNode res2 = root.get(foobar);
    Assert.assertNull("Should not have found symbol " + foobar, res2);
    
    INamedEntityNode resFoo0 = leaf1.get(foo);
    Assert.assertNull("Should not have found symbol " + foo, resFoo0);
    INamedEntityNode resFoo1 = branch1.get(foo);
    Assert.assertNull("Should not have found symbol " + foo, resFoo1);
    INamedEntityNode resFoo2 = branch0.get(foo);
    Assert.assertNull("Should not have found symbol " + foo, resFoo2);
  }

}
