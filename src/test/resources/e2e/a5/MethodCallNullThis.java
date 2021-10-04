public class MethodCallNullThis {

  public MethodCallNullThis() {
  }

  public void f() {
  }

  public static int test() {
    MethodCallNullThis x = null;
    x.f();
    return 123;
  }

}
