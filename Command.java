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


public class JoinCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        FileSystemClient.errorMessage(FileSystemClient.ERROR_JOIN_ALREADY);
    }
}

public class RegisterCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        try {
            if (args.length == 2) {
                if (!session.isRegistered()) {
                    session.getOutputStream().writeUTF("/register " + args[1]);
                    session.setCurrentUsername(args[1]);
                } else {
                    FileSystemClient.errorMessage(FileSystemClient.ERROR_ALREADY_REGISTERED);
                }
            } else {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


public class StoreCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        try {
            if (session.isRegistered() && args.length == 2) {
                String filename = args[1];
                Path filePath = Paths.get(FileSystemClient.CLIENT_FILES_DIRECTORY, filename);

                if (Files.exists(filePath)) {
                    session.getOutputStream().writeUTF("/store " + filename);

                    try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                         BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream)) {

                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = bufferedFileInputStream.read(buffer)) > 0) {
                            session.sendBlob(buffer, bytesRead);
                        }
                        session.sendBlob(buffer, 0); // EOF indicator
                        session.getOutputStream().flush();
                        FileSystemClient.println("File uploaded successfully.");
                    }
                } else {
                    FileSystemClient.errorMessage(FileSystemClient.ERROR_NO_FILE_CLIENT);
                }
            } else {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_NOT_REGISTERED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
