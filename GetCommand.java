import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GetCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        try {
            if (session.isRegistered() && args.length == 2) {
                String filename = args[1];
                session.setCurrentFile(filename);
                Path filePath = Paths.get("client_files", filename);

                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                    FileSystemClient.println("Existing file deleted before downloading.");
                }

                session.getOutputStream().writeUTF("/get " + filename);
            } else {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
