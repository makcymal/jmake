import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FS {
  // Get existing directory, null if the directory doesn't exist
  public static Path validateDirPath(String dirStr) {
    Path dirPath = Paths.get(dirStr).toAbsolutePath().normalize();
    return Files.exists(dirPath) && Files.isDirectory(dirPath) ? dirPath : null;
  }

  public static Path validateFilePath(String fileStr) {
    Path filePath = Paths.get(fileStr).toAbsolutePath().normalize();
    return Files.exists(filePath) && Files.isRegularFile(filePath) ? filePath : null;
  }

  public static Path validateFilePath(Path dirPath, String fileStr) {
    Path filePath = dirPath.resolve(fileStr).toAbsolutePath().normalize();
    return Files.exists(filePath) && Files.isRegularFile(filePath) ? filePath : null;
  }

  // 
  public static Path touchDir(String dirStr) throws IOException {
    Path dirPath = Paths.get(dirStr).toAbsolutePath().normalize();
    if (Files.exists(dirPath)) {
      return Files.isDirectory(dirPath) ? dirPath : null;
    } else {
      return Files.createDirectory(dirPath);
    }
  }
}
