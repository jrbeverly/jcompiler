package ca.uwaterloo.joos1wc.ast;

public interface INamedEntityNode {

  public enum EntityType {
    METHOD, PACKAGE, TYPE, VARIABLE, FIELD
  }

  public String getName();

  public EntityType getEntityType();

  public Type getType();

}
