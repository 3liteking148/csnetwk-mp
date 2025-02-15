import java.io.IOException;

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
