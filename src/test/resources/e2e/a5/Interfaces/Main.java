public class Main {
  public Main() {}
  
  public static int test() {
    A a = new A();
    int x = a.foo();
    x = x + a.bar();
    if (a instanceof J) {
      J j = (J)a;
      x = x + j.bar();
    }
    if (a instanceof B) {
      B b = (B)a;
      x = x + b.bar();
    }
    if (a instanceof K) {
      K k = (K)a;
      x = x + k.bar();
    }
    x = x + a.raz();
    
    return x;
  }
}