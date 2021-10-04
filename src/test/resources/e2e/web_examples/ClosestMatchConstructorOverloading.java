public class ClosestMatchConstructorOverloading {
    public ClosestMatchConstructorOverloading() {}
      public ClosestMatchConstructorOverloading(Object x, Object y) {}
        public ClosestMatchConstructorOverloading(Object x, ClosestMatchConstructorOverloading y) {}
          public void m() {
                new ClosestMatchConstructorOverloading(new ClosestMatchConstructorOverloading(), new ClosestMatchConstructorOverloading());
                  }
}
