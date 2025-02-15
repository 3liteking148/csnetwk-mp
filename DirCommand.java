import java.io.IOException;

public class DirCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        try {
            if (session.isRegistered()) {
                if (args.length == 1) {
                    session.getOutputStream().writeUTF("/dir");
                } else {
                    FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                }
            } else {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_NOT_REGISTERED);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
