import java.io.IOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public interface Command {
    void execute(String[] args, FileSystemClientSession session);
}


