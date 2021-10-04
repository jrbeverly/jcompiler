public class NonStaticAccessOfStaticField {
    public static int x;
    public NonStaticAccessOfStaticField() {
    }
      public int m() {
            NonStaticAccessOfStaticField a = new NonStaticAccessOfStaticField();
                return a.x;
                  }
}
