import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class FileSystemClientSession {
    private Socket socket;
    private BufferedReader consoleInput;
    private AtomicReference<MessageClient> messageClient = new AtomicReference<>();
    private boolean registered = false;
    private DataInputStream input;
    private DataOutputStream output;

    private String currentUsername = "";
	private String currentFile = "";

    public void sendBlob(byte[] data, int length) throws IOException {
		output.writeInt(length);

		if(length > 0) {
			output.write(data, 0, length);
		}
	}

    private Thread serverListener = new Thread(() -> {
        try {
            while (!socket.isClosed()) {
                int type = input.readInt();
                if (type == FileSystemServer.TEXT) {
                    String message = input.readUTF();
                    FileSystemClient.println(message);
                } else if (type == FileSystemServer.BINARY) {
                    Path filePath = Paths.get("client_files", currentFile);

                    int fragmentSize = input.readInt();
                    if(fragmentSize > 0) {
                        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath.toFile(), true /* append every receive TODO: fix */);) {
                            BufferedOutputStream bufferedFileOutputStream = new BufferedOutputStream(fileOutputStream);
                            byte[] buffer = new byte[8192 + 1];
                            int bytesRead;


                            bytesRead = input.read(buffer, 0, fragmentSize);
                            bufferedFileOutputStream.write(buffer, 0, bytesRead);

                            bufferedFileOutputStream.close();
                            fileOutputStream.close();
                        } catch (Exception e){
                            System.out.print(e);
                        }
                    } else {
                        FileSystemClient.println("File received from Server: " + currentFile);
                        currentFile = "";
                    }

                } else {
                    registered = true;
                    messageClient.set(new MessageClient(currentUsername, socket.getInetAddress().getHostName(), socket.getPort() + 1));
                    messageClient.get().startGUI();
                }
            }
        } catch (IOException e) {
            // die
            FileSystemClient.errorMessage(FileSystemClient.ERROR_LOST_CONNECTION);
            return;
        }
    });

    public FileSystemClientSession(BufferedReader consoleInput, Socket socket) throws IOException {
        this.consoleInput = consoleInput;
        this.socket = socket;
        this.input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.output = new DataOutputStream(socket.getOutputStream()));
    
        CommandInvoker invoker = new CommandInvoker();
        invoker.registerCommand("/join", new JoinCommand());
        invoker.registerCommand("/register", new RegisterCommand());
        invoker.registerCommand("/store", new StoreCommand());
        invoker.registerCommand("/leave", new LeaveCommand());
        invoker.registerCommand("/dir", new DirCommand());
        invoker.registerCommand("/get", new GetCommand());
        invoker.registerCommand("/?", new HelpCommand());
    
        serverListener.start();
    }
    
    public void run() {
        String commandInput;
    
        do {
            try {
                commandInput = FileSystemClient.readLine();
                invoker.executeCommand(commandInput, this);
            } catch (IOException e) {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_LOST_CONNECTION);
                break;
            }
        } while (!commandInput.equals("/leave") && socket.isConnected());
    
        destroy();
    }
    


    public FileSystemClientSession(BufferedReader consoleInput, Socket socket) throws IOException {
        this.consoleInput = consoleInput;
        this.socket = socket;

        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());

        serverListener.start();
    }

    public void destroy() {
        if(messageClient.get() != null) {
            messageClient.get().destroy();
        }

        // bye
        try {
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
