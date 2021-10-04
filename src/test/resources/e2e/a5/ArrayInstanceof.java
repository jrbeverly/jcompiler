public class ArrayInstanceof {

  public ArrayInstanceof() {}

  public static int test() {
    Object x = new int[0];
    if (!(x instanceof int[])) {
      return 0;
    }
    if (x instanceof byte[]) {
      return 1;
    }
    x = new byte[0];
    if (x instanceof int[]) {
      return 2;
    }
    x = new ArrayInstanceof[0];
    if (!(x instanceof Object[])) {
      return 3;
    }
    if (!(x instanceof ArrayInstanceof[])) {
      return 4;
    }
    x = new Object[0];
    if (!(x instanceof Object[])) {
      return 5;
    }
    if (x instanceof ArrayInstanceof[]) {
      return 6;
    }
    return 123;
  }

}
