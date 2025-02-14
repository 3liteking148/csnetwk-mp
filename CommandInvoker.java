import java.util.HashMap;
import java.util.Map;

public class CommandInvoker {
    private Map<String, Command> commands = new HashMap<>();

    public void registerCommand(String commandName, Command command) {
        commands.put(commandName, command);
    }

    public void executeCommand(String commandInput, FileSystemClientSession session) {
        String[] args = commandInput.split(" ");
        Command command = commands.get(args[0]);

        if (command != null) {
            command.execute(args, session);
        } else {
            FileSystemClient.println("Error: Command not found.");
        }
    }
}
