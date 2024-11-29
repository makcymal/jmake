public class JMake {
  public static void main(String args[]) {
    try {
      var rc = new RuntimeConfig(args);
      rc.echo();
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(-1);
    }
  }
}
