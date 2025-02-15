public class JoinCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        FileSystemClient.errorMessage(FileSystemClient.ERROR_JOIN_ALREADY);
    }
}
