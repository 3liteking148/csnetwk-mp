public class LeaveCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        session.setRegistered(false);
        FileSystemClient.println("You have left the session.");
    }
}
