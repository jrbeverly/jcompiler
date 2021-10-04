import java.io.*;
import java.lang.reflect.*;
import java.net.*;

class A5Driver {

  public static void main(String[] args) throws Exception {
    File cwd = new File(System.getProperty("user.dir"));
    File classDir = new File(cwd, args[0]);
    ClassLoader cl = new URLClassLoader(new URL[] { classDir.toURI().toURL() });
    Class<?> c = cl.loadClass(args[1]);
    Method m = c.getMethod("test");
    try {
      int code = (int) m.invoke(null);
      System.exit(code);
    } catch (InvocationTargetException e) {
      System.exit(13);
    }
  }

}
