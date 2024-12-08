import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;

public enum Logger {
  INSTANCE;

  boolean debugOn = false;
  boolean targetOn = true;
  boolean recipeOn = true;
  boolean warningOn = true;
  boolean errorOn = true;

  PrintStream out = System.out;

  StringBuilder debugMsg = new StringBuilder();
  StringBuilder targetMsg = new StringBuilder();
  StringBuilder recipeMsg = new StringBuilder();
  StringBuilder warningMsg = new StringBuilder();
  StringBuilder errorMsg = new StringBuilder();

  public void setLogLevels(boolean debugOn, boolean recipeOn, boolean warningOn) {
    this.debugOn = debugOn;
    this.recipeOn = recipeOn;
    this.warningOn = warningOn;
  }

  public void setLogfile(Path filePath) {
    try {
      out = new PrintStream(new File(filePath.toString()));
    } catch (Exception e) {

    }
  }

  public Logger addDebug(String msg) {
    debugMsg.append(msg);
    return this;
  }

  public Logger addTarget(String msg) {
    targetMsg.append(msg);
    return this;
  }

  public Logger addRecipe(String msg) {
    recipeMsg.append(msg);
    return this;
  }

  public Logger addWarning(String msg) {
    warningMsg.append(msg);
    return this;
  }

  public Logger addError(String msg) {
    errorMsg.append(msg);
    return this;
  }

  public void flush() {
    if (debugOn && !debugMsg.isEmpty()) {
      out.println("--DEBUG--");
      out.println(debugMsg);
    }
    if (targetOn && !targetMsg.isEmpty()) {
      out.println(targetMsg);
    }
    if (recipeOn && !recipeMsg.isEmpty()) {
      out.println(recipeMsg);
    }
    if (warningOn && !warningMsg.isEmpty()) {
      out.println("--WARNING--");
      out.println(warningMsg);
    }
    if (errorOn && !errorMsg.isEmpty()) {
      out.println("--ERROR--");
      out.println(errorMsg);
    }
    debugMsg = new StringBuilder();
    targetMsg = new StringBuilder();
    recipeMsg = new StringBuilder();
    warningMsg = new StringBuilder();
    errorMsg = new StringBuilder();
  }
}
