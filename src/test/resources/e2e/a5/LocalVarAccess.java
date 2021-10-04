public class LocalVarAccess {

  public LocalVarAccess() {}

  public static int test() {
    return LocalVarAccess.test1() + LocalVarAccess.test2();
  }

  public static int test1() {
    int x = 21;
    return 2 * x;
  }

  public static int test2() {
    int y = 9;
    return y * y;
  }

}
