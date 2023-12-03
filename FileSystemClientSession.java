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
            while (true) {
                int type = input.readInt();
                if (type == FileSystemServer.TEXT) {
                    String message = input.readUTF();
                    System.out.println(message);
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
                        System.out.println("File received from Server: " + currentFile);
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
            System.out.println("Closed");
            return;
        }
    });

    public void run() {
        String commandInput = "";

        do{
            try {
                commandInput = consoleInput.readLine();
                String[] command = commandInput.split(" ");
                
                // test connection
                output.writeUTF("/connectivitytest");
                output.flush();

                // AFTER JOINING
                switch(command[0]) {
                    case "/?":
                        if(command.length == 1){
                            System.out.println("Description Input                          | Syntax Sample                 | Input Script\n" +
                                    "\n" +
                                    "Connect to the server application          | /join <server_ip_add> <port>  | /join 192.168.1.1 12345\n" +
                                    "Disconnect to the server application       | /leave                        | /leave\n" +
                                    "Register a unique handle or alias          | /register <handle>            | /register User1\n" +
                                    "Send file to server /store <filename>      | /store <filename>             | /store Hello.txt\n" +
                                    "Request directory file list from a server  | /dir                          | /dir\n" +
                                    "Fetch a file from a server /get <filename> | /get <filename>               | /get Hello.txt\n" +
                                    "Request command help to output all Input   | /?                            | /?\n");
                        } else {
                            FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                        }
                        break;
                    case "/join":
                        FileSystemClient.errorMessage(FileSystemClient.ERROR_JOIN_ALREADY);
                        break;
                    case "/register":
                        if(command.length == 2){
                            if(!registered){
                                output.writeUTF(commandInput);
                                currentUsername = command[1];
                            } else {
                                FileSystemClient.errorMessage(FileSystemClient.ERROR_ALREADY_REGISTERED);
                            }
                        } else {
                            FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                        }
                        break;
                    case "/leave":
                        registered = false;
                        break;
                    case "/dir":
                        if(registered){
                            if(command.length == 1){
                                output.writeUTF(commandInput);
                            } else {
                                FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                            }
                        } else {
                            FileSystemClient.errorMessage(FileSystemClient.ERROR_NOT_REGISTERED);
                        }
                        break;
                    case "/get":
                        if(registered){
                            if(command.length == 2){
                                currentFile = command[1];
                                Path filePath = Paths.get("client_files", currentFile);
                                if (Files.exists(filePath)) {
                                    try {
                                        // Delete the file
                                        Files.delete(filePath);
                                        System.out.println("File deleted successfully.");
                                    } catch (IOException e) {
                                        System.err.println("Failed to delete the file: " + e.getMessage());
                                    }
                                }
                                output.writeUTF(commandInput);

                            } else {
                                FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                            }
                        } else {
                            FileSystemClient.errorMessage(FileSystemClient.ERROR_NOT_REGISTERED);
                        }
                        break;
                    case "/store":

                        if(registered){
                            if(command.length == 2){
                                String filename = command[1];
                                Path filePath = Paths.get(FileSystemClient.CLIENT_FILES_DIRECTORY, filename);

                                if (Files.exists(filePath)) {
                                    try {
                                        output.writeUTF(commandInput);

                                        // Open a FileInputStream to read the file
                                        FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                                        BufferedInputStream bufferedFileInputStream = new BufferedInputStream(fileInputStream);
                                        // Read and send the file content to the server
                                        byte[] buffer = new byte[8192 + 1];
                                        int bytesRead;

                                        while ((bytesRead = bufferedFileInputStream.read(buffer)) > 0) {
                                            System.out.println(bytesRead);
                                            sendBlob(buffer, bytesRead);
                                        }

                                        // indicate EOF
                                        sendBlob(buffer, 0);

                                        // Flush the output stream to ensure all data is sent
                                        output.flush();
                                        fileInputStream.close();
                                        bufferedFileInputStream.close();
                                        System.out.println("File uploaded successfully.");


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    FileSystemClient.errorMessage(FileSystemClient.ERROR_NO_FILE_CLIENT);
                                }
                            } else {
                                FileSystemClient.errorMessage(FileSystemClient.ERROR_INVALID_PARAMS);
                            }
                        } else {
                            FileSystemClient.errorMessage(FileSystemClient.ERROR_NOT_REGISTERED);
                        }
                        break;
                    default:
                        System.out.println("Error: Command not found.");
                        break;
                }
            } catch (IOException e) {
                FileSystemClient.errorMessage(FileSystemClient.ERROR_LOST_CONNECTION);
                break;
            }
        } while(!commandInput.equals("/leave"));

        // leave
        destroy();
    }


    public FileSystemClientSession(BufferedReader consoleInput, Socket socket) throws IOException {
        this.consoleInput = consoleInput;
        this.socket = socket;

        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        output = new DataOutputStream(socket.getOutputStream());

        serverListener.start();
    }

    private void destroy() {
        serverListener.interrupt();
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
