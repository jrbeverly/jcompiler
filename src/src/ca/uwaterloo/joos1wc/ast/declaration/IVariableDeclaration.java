package ca.uwaterloo.joos1wc.ast.declaration;

public interface IVariableDeclaration {

  // Should refactor the VariableDeclaration node, but for some reason it breaks some tests.

  public void setStackOffset(int stackOffset);

  public int getStackOffset();

}
