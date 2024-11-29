import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;

enum CliOption {
  DIRECTORY, MAKEFILE, GITBRANCH, JOBS, ALWAYSMAKE, MAKEALL, OPTIONCOUNT, NONE;
}

public class RuntimeConfig {
  public ArrayList<String> targetList = new ArrayList<>();

  // Specify working directory
  // Usage: jmake -C dir
  public Path directory = Paths.get("").toAbsolutePath();

  // Read file as Makefile
  // Usage: jmake -f Makefile
  public Path makefile = Paths.get("").toAbsolutePath().resolve("Makefile");

  // Specify what git branch to use
  // Usage: jmake -g trunk
  public String gitbranch = "";

  // Use multiple jobs for independent targets
  // Usage: jmake -j 4
  public int jobs = 1;

  // Whether jmake should unconditionally alwaysmake targets
  // Usage: jmake -B
  public boolean alwaysmake = false;

  // Whether jmake should make all targets
  // Usage: jmake -a
  public boolean makeall = false;

  RuntimeConfig(String args[]) {
    var prevOption = CliOption.NONE;
    var optionVals = new String[CliOption.OPTIONCOUNT.ordinal()];

    for (int i = 0; i < args.length; ++i) {

      if (args[i].startsWith("-")) {
        if (prevOption != CliOption.NONE) {
          throw new IllegalArgumentException(
              String.format("Option %s doesn't have value", args[i - 1]));
        }
        prevOption = parseOption(args[i]);

      } else {
        optionVals[prevOption.ordinal()] = args[i];
        prevOption = CliOption.NONE;
      }
    }

    setOptions(optionVals);
  }

  public void echo() {
    String echoMsg = String.format(
        """
            Working directory: %s
            Reading from: %s
            Specified git branch: %s
            Jobs running at once: %d
            Unconditionally make targets: %b
            Make all targets: %b
            """,
        directory, makefile, gitbranch, jobs, alwaysmake, makeall);
    System.out.println(echoMsg);
  }

  CliOption parseOption(String arg) {
    switch (arg) {
      case "-C":
      case "--directory":
        return CliOption.DIRECTORY;
      case "-f":
      case "--file":
      case "--makefile":
        return CliOption.MAKEFILE;
      case "-g":
      case "--git-branch":
        return CliOption.GITBRANCH;
      case "-j":
      case "--jobs":
        return CliOption.JOBS;
      case "-B":
      case "--always-make":
        return CliOption.ALWAYSMAKE;
      case "-a":
      case "--make-all":
        return CliOption.MAKEALL;
      case "-h":
      case "--help":
        printHelp();
        System.exit(0);
      default:
        throw new IllegalArgumentException("Unrecognized prevOption: " + arg);
    }
  }

  void setOptions(String optionVals[]) {
    // directory
    String dirname = optionVals[CliOption.DIRECTORY.ordinal()];
    Path directory = Paths.get(dirname).toAbsolutePath();
    if (!Files.exists(directory) || !Files.isDirectory(directory)) {
      throw new InputMismatchException("Directory doesn't exist: " + directory);
    }

    // makefile
    String makefilename = optionVals[CliOption.MAKEFILE.ordinal()];
    Path makefile = directory.resolve(makefilename);
    if (!Files.exists(makefile) || !Files.isRegularFile(makefile)) {
      throw new InputMismatchException("Makefile doesn't exist: " + makefile);
    }
    
    // git branch
    
  }

  void printHelp() {
    String helpMsg = """
        JMake build system
        Usage: jmake [prevOption value] [targets]
        Options:
          -C --directory        Specify working directory
          -f --file --makefile  Read file as Makefile
          -g --git-branch       Specify what git branch to use
          -j --jobs             Run N jobs at once (but as much as possible)
          -B --always-make      Unconditionall remake targets
          -a --make-all         Make all targets
          -h --help             Print this message and exit
        """;
    System.out.print(helpMsg);
  }
}
