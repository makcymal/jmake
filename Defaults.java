import java.nio.file.Path;
import java.nio.file.Paths;

public class Defaults {
  public static final String VERSION = "0.1.0";
  public static final Path DIRECTORY = Paths.get("").toAbsolutePath();
  public static final Path MAKEFILE = DIRECTORY.resolve("Makefile");
  public static final Path LOGFILE = DIRECTORY.resolve("jmake.log");
  public static final String SHELL = "/bin/bash";
}
