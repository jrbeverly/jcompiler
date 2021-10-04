public class NonStaticAccessOfStaticMethod {
  public NonStaticAccessOfStaticMethod() {
  }
    public static int m1() {
          return 42;
            }
      public int m2() {
            NonStaticAccessOfStaticMethod a = new NonStaticAccessOfStaticMethod();
                return a.m1();
                  }
}
