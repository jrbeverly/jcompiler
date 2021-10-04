public class VariableDeclarationInWhile {
  public VariableDeclarationInWhile() {
    int i = 0;
    while (i < 10) {
      int x = i * i;
      System.out.println(x);
    }
    int x = i * i;
    System.out.println(x);
  }
}
