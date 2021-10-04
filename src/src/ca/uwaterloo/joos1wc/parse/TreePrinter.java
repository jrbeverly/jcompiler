package ca.uwaterloo.joos1wc.parse;

import java.io.PrintStream;

import ca.uwaterloo.joos1wc.ast.RecursiveVisitor;
import ca.uwaterloo.joos1wc.ast.TreeNode;

public class TreePrinter extends RecursiveVisitor {

  private int depth = 0;
  private final PrintStream writer;

  public TreePrinter() {
    this.writer = System.out;
  }

  public TreePrinter(PrintStream writer) {
    this.writer = writer;
  }

  @Override
  protected void preVisit(TreeNode node) {
    for (int i = 0; i < depth; i++) {
      writer.print("| ");
    }
    writer.print(node.toString());
    writer.println();
    depth++;
  }

  @Override
  protected void postVisit(TreeNode node) {
    depth--;
  }

}
