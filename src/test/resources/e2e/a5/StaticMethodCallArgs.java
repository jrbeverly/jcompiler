public class StaticMethodCallArgs {

  public StaticMethodCallArgs() {}

  public static int test() {
    return StaticMethodCallArgs.test1(21) + StaticMethodCallArgs.test2(9);
  }

  public static int test1(int x) {
    return 2 * x;
  }

  public static int test2(int y) {
    return y * y;
  }

}
