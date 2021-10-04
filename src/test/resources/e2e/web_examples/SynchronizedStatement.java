public class SynchronizedStatement {
    public Integer x;
      public void m() {
            synchronized(x) {
                    x = x-1;
                        }
      }
}
