// JOOS1:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JOOS2:PARSER_WEEDER,PARSER_EXCEPTION,TYPE_CHECKING,INVALID_INSTANCEOF
// JAVAC:UNKNOWN
// 
public class Je_1_InstanceOf_NoRelation {
  /* Parser+Weeder => instanceof is not allowed on primitive types */
  public Je_1_InstanceOf_NoRelation() {
  }

  public static int test() {
    String s = "hello";
if (s instanceof Number) return 42;
return 123;
  }
}
