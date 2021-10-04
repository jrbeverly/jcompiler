public class VariableDeclarationInFor {
  public VariableDeclarationInFor() {
    for (int i = 0; i < 10; i=i+1) System.out.println(i);
    int i = 0;
    for (i = 0; i < 10; i=i+1) System.out.println(i);
  }
}
