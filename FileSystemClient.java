/*
	CHUA, Harvey
	PINPIN, Lord
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FileSystemClient
{
	public static void errorMessage(String error){
		switch(error){
			case "join_fail":
				System.out.println("Error: Connection to the Server has failed! Please check IP Address and Port Number.");
			case "no_server":
				System.out.println("Error: No connection to existing server to execute command with.");
			case "not_registered":
				System.out.println("Error: No alias has been registered with server.");
		}
	}

	public static void main(String[] args) {
		try {
			BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
			BufferedReader input = null;
			PrintWriter output = null;

			Boolean joined = false;
			Boolean registered = false;

			// Rest of the client code
			String text;
			do {
				System.out.print("Enter a command: ");
				text = consoleInput.readLine();
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
							try {
								Socket socket = new Socket(command[1], Integer.parseInt(command[2]));

								input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
								output = new PrintWriter(socket.getOutputStream(), true);
								joined = true;

							} catch (IOException e) {
								errorMessage("join_fail");
							}
						} else {
							errorMessage("already_joined");
						}
						break;
					case "/register":
						break;
					case "/leave":
						break;
					case "/dir":
						break;
					case "/get":
						break;
					case "/store":
						break;
					default:
						System.out.println("Error: Command not found.");
						break;
				}

			} while (!joined);

			Thread serverListener = new Thread(() -> {
				try {
					String message;
					while ((message = input.readLine()) != null) {
						System.out.println("Server: " + message);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			serverListener.start();

			Thread userInput = new Thread(() -> {
				String commandInput;
				try {
					System.out.print("Enter a command: ");
					commandInput = consoleInput.readLine();
					String[] command = commandInput.split(" ");

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

							break;
						case "/register":
							output.println(command);
							break;
						case "/leave":
							break;
						case "/dir":
							break;
						case "/get":
							break;
						case "/store":
							break;
						default:
							System.out.println("Error: Command not found.");
							break;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			userInput.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}