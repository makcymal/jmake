import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class RuntimeConfig {

  enum Option {
    DIRECTORY, // <dir>
    MAKEFILE, // <makefile>
    GITBRANCH, // <branch>
    JOBSNUM, // <N>
    ASSUMEOLD, // <file1,file2...>
    ASSUMENEW, // [file1,file2...]
    SHELL, // <shell_executable>
    ENVIRON, // <env1=val1,....>
    LOGFILE, // [logfile]
    MAKEALL,
    QUERYTARGETS,
    ECHORECIPES,
    NOWARNINGS,
    DEBUG,
    NONE;

    static final int COUNT_WITH_VALUE = 9;

    boolean withValue() {
      return this.ordinal() < COUNT_WITH_VALUE;
    }
  }

  public ArrayList<String> targetList = new ArrayList<>();

  // Change working directory
  // Default value: current directory
  // Usage: jmake -d src
  public Path directory = Defaults.DIRECTORY;

  // Read file as Makefile
  // Default value: directory/Makefile
  // Usage: jmake -f Makefile
  public Path makefile = Defaults.MAKEFILE;

  // When in git repo, choose working branch.
  // No changes, commits or checkouts are
  // Default value: no branch
  // Usage: jmake -g trunk
  public String gitBranch = "";

  // Run N jobs at once (but as much as possible)
  // Default value: 1 job
  // Usage: jmake -j 4
  public int jobsNum = 1;

  // Assume files old and don't remake them
  // Default value: no files
  // Usage: jmake -o ./main.c,./helper.c
  public TreeSet<Path> assumeOld = new TreeSet<>();

  // Assume files new and remake them,
  // remake all that was touched when no files given
  // Default value: no files
  // Usage: jmake -n ./main.c,./helper.c
  public TreeSet<Path> assumeNew = new TreeSet<>();

  // Assume all touched files new and remake them
  // Default value: false
  // Usage: jmake -n
  public boolean assumeNewAll = false;

  // Change shell executable
  // Default value: /bin/bash
  // Usage: jmake -s /bin/fish
  public String shell = Defaults.SHELL;

  // Set environment variables right after changing shell,
  // directory and choosing branch
  // Default value: no variables
  // Usage: jmake -e PATH=$HOME/bin:$PATH,BUILD=./build
  public String environ = "";

  // Write outputs to file,
  // when no value is given, write to newly created jmake.log
  // Default value: null (writing to System.out)
  // Usage jmake -l build.log
  public Path logfile = null;

  // Make all targets
  // Default value: false
  // Usage: jmake -a
  public boolean makeAll = false;

  // Print which targets will be (re)made and exit
  // Default value: false
  // Usage: jmake -q
  public boolean queryTargets = false;

  // Echo recipes
  // Default value: false
  // Usage: jmake -s
  public boolean echoRecipes = false;

  // Print warnings
  // Default value: true
  // Usage: jmake -w
  public boolean noWarnings = false;

  // Whether JMake should print debug messages
  // Default value: false
  // Usage: jmake -d
  public boolean debug = false;

  Logger log = Logger.INSTANCE;

  RuntimeConfig(String[] args) {
    var optionValues = new ArrayList<ArrayList<String>>(Option.COUNT_WITH_VALUE);
    for (int i = 0; i < Option.COUNT_WITH_VALUE; ++i) {
      optionValues.add(new ArrayList<>());
    }
    var optionSeen = new boolean[Option.COUNT_WITH_VALUE];
    Arrays.fill(optionSeen, false);

    parseArgs(args, optionValues, optionSeen);

    setOptions(optionValues, optionSeen);

    log.setLogLevels(debug, echoRecipes, !noWarnings);
    if (logfile != null) {
      log.setLogfile(logfile);
    }

    log.addDebug(this.toString());
    log.flush();
  }

  void parseArgs(String[] args, ArrayList<ArrayList<String>> optionValues, boolean[] optionSeen) {
    for (int i = 0; i < args.length; ++i) {
      if (args[i].startsWith("-")) {
        var option = parseOption(args[i]);

        if (option.withValue()) {
          optionSeen[option.ordinal()] = true;
          if (i == args.length - 1) {
            break;
          }
          optionValues.get(option.ordinal()).add(args[i + 1]);
          ++i;
        }
      } else {
        targetList.add(args[i]);
      }
    }
  }

  Option parseOption(String opt) {
    switch (opt) {
      case "-d":
      case "--directory":
        return Option.DIRECTORY;

      case "-f":
      case "--makefile":
        return Option.MAKEFILE;

      case "-g":
      case "--git-branch":
        return Option.GITBRANCH;

      case "-j":
      case "--jobs-num":
        return Option.JOBSNUM;

      case "-s":
      case "--shell":
        return Option.SHELL;

      case "-e":
      case "--environ":
        return Option.ENVIRON;

      case "-o":
      case "--asssume-old":
        return Option.ASSUMEOLD;

      case "-n":
      case "--asssume-new":
        return Option.ASSUMENEW;

      case "-a":
      case "--make-all":
        makeAll = true;
        return Option.MAKEALL;

      case "-l":
      case "--logfile":
        logfile = Defaults.LOGFILE;
        return Option.LOGFILE;

      case "-q":
      case "--query-targets":
        queryTargets = true;
        return Option.QUERYTARGETS;

      case "-r":
      case "--echo-recipes":
        echoRecipes = true;
        return Option.ECHORECIPES;

      case "-w":
      case "--no-warnings":
        noWarnings = true;
        return Option.NOWARNINGS;

      case "-h":
      case "--help":
        printHelp();
        System.exit(0);

      case "-v":
      case "--version":
        printVersion();
        System.exit(0);

      case "-D":
        debug = true;
        return Option.NONE;

      default:
        log.addWarning("Unrecognized option: ").addWarning(opt);
        return Option.NONE;
    }
  }

  void setOptions(ArrayList<ArrayList<String>> optionValues, boolean[] optionSeen) {
    if (optionSeen[Option.LOGFILE.ordinal()]) {
      setLogfile(optionValues.get(Option.LOGFILE.ordinal()));
    }
    if (optionSeen[Option.SHELL.ordinal()]) {
      setShell(optionValues.get(Option.SHELL.ordinal()));
    }
    if (optionSeen[Option.ENVIRON.ordinal()]) {
      setEnviron(optionValues.get(Option.ENVIRON.ordinal()));
    }
    if (optionSeen[Option.DIRECTORY.ordinal()]) {
      setDirectory(optionValues.get(Option.DIRECTORY.ordinal()));
    }
    if (optionSeen[Option.MAKEFILE.ordinal()]) {
      setMakefile(optionValues.get(Option.MAKEFILE.ordinal()));
    }
    if (optionSeen[Option.GITBRANCH.ordinal()]) {
      setGitBranch(optionValues.get(Option.GITBRANCH.ordinal()));
    }
    if (optionSeen[Option.JOBSNUM.ordinal()]) {
      setJobsNum(optionValues.get(Option.JOBSNUM.ordinal()));
    }
    if (optionSeen[Option.ASSUMEOLD.ordinal()]) {
      setAssumeOld(optionValues.get(Option.ASSUMEOLD.ordinal()));
    }
    if (optionSeen[Option.ASSUMENEW.ordinal()]) {
      setAssumeNew(optionValues.get(Option.ASSUMENEW.ordinal()));
    }
  }

  void setLogfile(ArrayList<String> logfileList) {
    removeOptionsFromValues(logfileList);
    if (logfileList.size() == 0) {
      logfile = Defaults.LOGFILE;
      return;
    } else if (logfileList.size() > 1) {
      log.addWarning(
          "More than one -l --logfile option were given, the first valid will be chosen");
    }

    boolean foundValid = false;

    for (String currLogfileStr : logfileList) {
      var currLogfilePath = FS.validateFilePath(directory, currLogfileStr);
      if (currLogfilePath != null) {
        logfile = currLogfilePath;
        foundValid = true;
        break;
      }
    }

    if (!foundValid) {
      log.addWarning("No valid logfile was given, using default: Makefile");
    }
  }

  void setShell(ArrayList<String> shellList) {
    removeOptionsFromValues(shellList);
    if (shellList.size() == 0) {
      log.addWarning("No value for -s --shell was given, using default: ")
          .addWarning(Defaults.SHELL);
      return;
    } else if (shellList.size() > 1) {
      log.addWarning("More than one -s --shell option were given, the first valid will be chosen");
    }
  }

  void setEnviron(ArrayList<String> environList) {
    removeOptionsFromValues(environList);
    if (environList.size() == 0) {
      log.addWarning(
          "No value for -e --environ was given hence no environment variable will be set");
      return;
    }
  }

  void setDirectory(ArrayList<String> directoryList) {
    removeOptionsFromValues(directoryList);
    if (directoryList.size() == 0) {
      log.addWarning("No value for -d --directory was given, using default: ")
          .addWarning(Defaults.DIRECTORY.toString());
      return;
    } else if (directoryList.size() > 1) {
      log.addWarning(
          "More than one -d --directory option were given, the first valid will be chosen");
    }

    boolean foundValid = false;

    for (String currDirStr : directoryList) {
      var currDirPath = FS.validateDirPath(currDirStr);
      if (currDirStr != null) {
        directory = currDirPath;
        foundValid = true;
        break;
      } else {
        log.addDebug("Skip directory: ").addDebug(currDirStr);
      }
    }

    if (!foundValid) {
      log.addWarning("No valid directory was given, using default: ")
          .addWarning(directory.toString());
    }
  }

  void setMakefile(ArrayList<String> makefileList) {
    removeOptionsFromValues(makefileList);
    if (makefileList.size() == 0) {
      log.addWarning("No value for -f --makefile was given, using default: ")
          .addWarning(Defaults.MAKEFILE.toString());
      return;
    } else if (makefileList.size() > 1) {
      log.addWarning(
          "More than one -f --makefile option were given, the first valid will be chosen");
    }

    boolean foundValid = false;

    for (String currMakefileStr : makefileList) {
      var currMakefilePath = FS.validateFilePath(directory, currMakefileStr);
      if (currMakefilePath != null) {
        makefile = currMakefilePath;
        foundValid = true;
        break;
      }
    }

    if (!foundValid) {
      log.addWarning("No valid makefile was given, using default: Makefile");
    }
  }

  void setGitBranch(ArrayList<String> gitBranchList) {
    removeOptionsFromValues(gitBranchList);
    if (gitBranchList.size() == 0) {
      log.addWarning("No value for -g --git-branch was given, using current branch");
      return;
    } else if (gitBranchList.size() > 1) {
      log.addWarning(
          "More than one -g --git-branch option were given, the first valid will be chosen");
    }
  }

  void setJobsNum(ArrayList<String> jobsNumList) {
    removeOptionsFromValues(jobsNumList);
    if (jobsNumList.size() == 0) {
      log.addWarning("No value for -j --jobs-num was given, using default: 1");
      return;
    } else if (jobsNumList.size() > 1) {
      log.addWarning(
          "More than one -j --jobs-num option were given, the first valid will be chosen");
    }

    boolean foundValid = false;

    for (String currJobsNumStr : jobsNumList) {
      try {
        int currJobsNum = Integer.parseInt(currJobsNumStr);
        if (currJobsNum < 1) {
          continue;
        }

        jobsNum = currJobsNum;
        foundValid = true;
        break;
      } catch (NumberFormatException e) {
        continue;
      }
    }

    if (!foundValid) {
      log.addWarning("No valid jobs number was given, using default: 1");
    }
  }

  void setAssumeOld(ArrayList<String> assumeOldList) {
    removeOptionsFromValues(assumeOldList);
    if (assumeOldList.size() == 0) {
      log.addWarning("No value for -o --assume-old was given hence no file will be assumed old");
      return;
    }

    for (String assumeOldStr : assumeOldList) {
      for (String fileStr : assumeOldStr.split(",")) {
        Path filePath = FS.validateFilePath(directory, fileStr);
        if (filePath != null) {
          assumeOld.add(filePath);
        }
      }
    }

    if (assumeOld.isEmpty()) {
      log.addWarning("No valid file to assume as new was given");
    }
  }

  void setAssumeNew(ArrayList<String> assumeNewList) {
    removeOptionsFromValues(assumeNewList);
    if (assumeNewList.size() == 0) {
      assumeNewAll = true;
      return;
    }

    for (String assumeNewStr : assumeNewList) {
      for (String fileStr : assumeNewStr.split(",")) {
        Path filePath = FS.validateFilePath(directory, fileStr);
        if (filePath != null) {
          assumeNew.add(filePath);
        }
      }
    }

    if (assumeNew.isEmpty()) {
      log.addWarning("No valid file to assume as new was given");
    }
  }

  void removeOptionsFromValues(ArrayList<String> values) {
    int insertIdx = 0;
    for (int i = 0; i < values.size(); ++i) {
      if (!values.get(i).startsWith("-")) {
        values.set(insertIdx++, values.get(i));
      }
    }
  }

  void printHelp() {
    String msg =
        String.format(
            """
            JMake build system, version %s
            Usage: jmake [option [value]] [targets]

            Options:
              -d --directory    <dir>
                Change working directory
              -f --makefile     <makefile>
                Read file as Makefile
              -g --git-branch   <branch>
                When in git repo, choose working branch.
                No changes, commits or checkouts are done
              -j --jobs-num     <N>
                Run N jobs at once (but as much as possible)
              -o --assume-old   <file1,file2...>
                Assume files old and don't remake them
              -n --assume-new   [file1,file2...]
                Assume files new and remake them,
                remake all that was touched when no files given
              -s --shell        <shell_executable>
                Change shell executable
              -e --environ      <env1=val1,env2=val2...>
                Set environment variables right after changing shell
              -l --logfile      [logfile]
                Write outputs to file, when no value is given,
                write to newly created jmake.log
              -a --make-all
                Make all targets
              -q --query-targets
                Print which targets will be (re)made and exit
              -r --echo-recipes
                Echo recipes
              -w --no-warnings
                Don't print warnings
              -h --help
                Print this message and exit
              -v --version
                Print version information and exit
            """,
            Defaults.VERSION);
    System.out.println(msg);
  }

  void printVersion() {
    String msg =
        String.format("JMake build system, version %s, created by makcymal", Defaults.VERSION);
    System.out.println(msg);
  }

  public String toString() {
    String str =
        String.format(
            """
            Targets: %s
            Working directory: %s
            Reading from: %s
            Git branch: %s
            Jobs running at once: %d
            Shell executable: %s
            Environment variables: %s
            Assume old: %s
            Assume new: %s
            Make all targets: %b
            Query targets: %b
            Echo recipes: %b
            No warnings: %b
            Debug: %b
            """,
            targetList,
            directory,
            makefile,
            gitBranch,
            jobsNum,
            shell,
            environ,
            assumeOld,
            assumeNew,
            makeAll,
            queryTargets,
            echoRecipes,
            noWarnings,
            debug);
    return str;
  }
}
