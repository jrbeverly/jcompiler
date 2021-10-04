package ca.uwaterloo.joos1wc.ast;

import ca.uwaterloo.joos1wc.scanner.NonTerminalTokenKind;
import ca.uwaterloo.joos1wc.scanner.Token;

public class MockNamedEntityNode extends TreeNode implements INamedEntityNode {

  private String name;
  private EntityType entityType;

  public MockNamedEntityNode(String name, EntityType entityType) {
    super(new Token(null, new NonTerminalTokenKind(name), name, 0, 0));
    this.name = name;
    this.entityType = entityType;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public EntityType getEntityType() {
    return entityType;
  }

  @Override
  public Type getType() {
    return null;
  }

}
