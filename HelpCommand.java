public class HelpCommand implements Command {
    @Override
    public void execute(String[] args, FileSystemClientSession session) {
        if (args.length == 1) {
            FileSystemClient.println("Available Commands:\n" +
                    "/join <server_ip> <port>  - Connect to server\n" +
                    "/leave                    - Disconnect from server\n" +
                    "/register <handle>        - Register a username\n" +
                    "/store <filename>        - Send a file to the server\n" +
                    "/dir                     - List files on the server\n" +
                    "/get <filename>          - Fetch a file from the server\n" +
                    "/?                       - Show this help menu");
        } else {
            FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
        }
    }
}
