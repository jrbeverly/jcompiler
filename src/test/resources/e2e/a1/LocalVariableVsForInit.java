public class LocalVariableVsForInit {
  public LocalVariableVsForInit() {
  }
  public static void main(String[] args) {
    int x = 1;
    for (int i = 0; i < 10; i = i + 1) {
      x = x * 2;
    }
  }
}
