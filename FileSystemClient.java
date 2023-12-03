/*
	CHUA, Harvey
	PINPIN, Lord
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class FileSystemClient extends Application
{
	public static final int TEXT = 0;
	public static final int BINARY = 1;
	public static final int ERROR_JOIN_FAIL = 0;
	public static final int ERROR_JOIN_ALREADY = 1;
	public static final int ERROR_NO_SERVER = 2;
	public static final int ERROR_NOT_REGISTERED = 3;
	public static final int ERROR_NO_COMMAND = 4;
	public static final int ERROR_INVALID_PARAMS = 5;
	public static final int ERROR_NO_FILE_CLIENT = 6;
	public static final int ERROR_NO_FILE_SERVER = 7;
	public static final int ERROR_ALREADY_REGISTERED = 8;
	public static final String CLIENT_FILES_DIRECTORY = "client_files";
	public static Boolean joined = false;

	@Override
	public void start(Stage stage) {
	}

	public static void errorMessage(int error){
		switch(error){
			case ERROR_JOIN_FAIL:
				System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
				break;
			case ERROR_JOIN_ALREADY:
				System.out.println("Error: Already joined.");
				break;
			case ERROR_NO_SERVER:
				System.out.println("Error: No connection to existing server to execute command with.");
				break;
			case ERROR_NOT_REGISTERED:
				System.out.println("Error: No alias has been registered with server.");
				break;
			case ERROR_NO_COMMAND:
				System.out.println("Error: Command not found.");
				break;
			case ERROR_INVALID_PARAMS:
				System.out.println("Error: Command parameters do not match or is not allowed.");
				break;
			case ERROR_NO_FILE_CLIENT:
				System.out.println("Error: File not found.");
				break;
			case ERROR_NO_FILE_SERVER:
				System.out.println("Error: File not found in server.");
				break;
			case ERROR_ALREADY_REGISTERED:
				System.out.println("Error: Already registered!");
				break;

		}
	}

	private static void createClientFilesDirectory() {
		Path clientFilesPath = Paths.get(CLIENT_FILES_DIRECTORY);

		if (!Files.exists(clientFilesPath)) {
			try {
				Files.createDirectories(clientFilesPath);
			} catch (IOException e) {
				System.err.println("Error creating 'client_files' directory: " + e.getMessage());
			}
		}
	}


	public static void main(String[] args) {
		// important: do not close JavaFX after exit
		Platform.setImplicitExit(false);

		System.out.println("You have opened the file and message system application. Here are the commands:");
					System.out.println("Description Input                          | Syntax Sample                 | Input Script\n" +
									"\n" +
									"Connect to the server application          | /join <server_ip_add> <port>  | /join 192.168.1.1 12345\n" +
									"Disconnect to the server application       | /leave                        | /leave\n" +
									"Register a unique handle or alias          | /register <handle>            | /register User1\n" +
									"Send file to server /store <filename>      | /store <filename>             | /store Hello.txt\n" +
									"Request directory file list from a server  | /dir                          | /dir\n" +
									"Fetch a file from a server /get <filename> | /get <filename>               | /get Hello.txt\n" +
									"Request command help to output all Input   | /?                            | /?\n");
		while(true){

			try {

				BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
				Socket socket = null;


				createClientFilesDirectory();

				while (!joined) {
					String text = consoleInput.readLine();
					String[] command = text.split(" ");

					// JOIN FIRST
					switch(command[0]) {
						case "/?":
							System.out.println("Description Input                          | Syntax Sample                 | Input Script\n" +
									"\n" +
									"Connect to the server application          | /join <server_ip_add> <port>  | /join 192.168.1.1 12345\n" +
									"Disconnect to the server application       | /leave                        | /leave\n" +
									"Register a unique handle or alias          | /register <handle>            | /register User1\n" +
									"Send file to server /store <filename>      | /store <filename>             | /store Hello.txt\n" +
									"Request directory file list from a server  | /dir                          | /dir\n" +
									"Fetch a file from a server /get <filename> | /get <filename>               | /get Hello.txt\n" +
									"Request command help to output all Input   | /?                            | /?\n");
							break;
						case "/join":
							if (!joined) {
								if(command.length == 3){
									try {
										socket = new Socket(command[1], Integer.parseInt(command[2]));
										joined = false;

										System.out.println("Server: Connection to the File Exchange Server is successful!");
										FileSystemClientSession session = new FileSystemClientSession(consoleInput, socket);
										session.run();

										// after end of session


									} catch (IOException e) {
										joined = false;
										errorMessage(ERROR_JOIN_FAIL);
									}
								}
								else{
									errorMessage(ERROR_INVALID_PARAMS);
								}
							} else {
								errorMessage(ERROR_JOIN_ALREADY);
							}
							break;
						case "/register":
						case "/leave":
						case "/dir":
						case "/get":
						case "/store":
							errorMessage(ERROR_NO_SERVER);
							break;
						default:
							errorMessage(ERROR_NO_SERVER);
							break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
