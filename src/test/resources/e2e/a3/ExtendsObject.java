public class ExtendsObject {
  public ExtendsObject() {
    String s = this.toString();
    System.out.println(s.equals((Object)toString()));
  }
}
