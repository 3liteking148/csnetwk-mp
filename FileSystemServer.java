/*
	CHUA, Harvey
	PINPIN, Lord
*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSystemServer
{
	static Map<ClientHandler, String> clientMap = new ConcurrentHashMap<ClientHandler, String>();
	static final String SERVER_FILES_DIRECTORY = "server_files";

	private static void sendToAllExceptCurrent(String message, ClientHandler cur) {

		for(ClientHandler client : clientMap.keySet()) {
			if (cur != client){
				try {
					client.sendMessage(message);
				} catch (Exception e) {
				}
			}
		}
	}

	public static void main(String[] args)
	{
		int nPort = Integer.parseInt(args[0]);
		System.out.println("Server: Listening on port " + nPort + "...");
		System.out.println();

		// run message server
		MessageServer messageServer = new MessageServer(nPort + 1);
		System.out.println("Message Server: Listening on port " + nPort + 1 + "...");
		System.out.println();

		ServerSocket serverSocket;
		ExecutorService executor = Executors.newFixedThreadPool(10);

		createServerFilesDirectory();

		try
		{
			serverSocket = new ServerSocket(nPort);
			System.out.println("Server started. Waiting for clients...");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket);

				ClientHandler clientHandler = new ClientHandler(clientSocket);
				clientMap.put(clientHandler, "");
				executor.execute(clientHandler);
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
		finally
		{
			System.out.println("Server: Connection is terminated");
		}
	}


	private static void createServerFilesDirectory() {
		Path serverFilesPath = Paths.get(SERVER_FILES_DIRECTORY);

		if (!Files.exists(serverFilesPath)) {
			try {
				Files.createDirectories(serverFilesPath);
			} catch (IOException e) {
				System.err.println("Error creating 'server_files' directory: " + e.getMessage());
			}
		}
	}

	public static final int TEXT = 0;
	public static final int BINARY = 1;
	public static final int REGISTER = 2;
	static class ClientHandler implements Runnable {
		private Socket clientSocket;
		private String clientNickname;
		private DataOutputStream output;

		public String getClientNickname() {
			return clientNickname;
		}
		// one message/binary data at a time
		public synchronized void sendMessage(String message) throws IOException {
			output.writeInt(TEXT);
			output.writeUTF(message);
		}
		public synchronized void sendBlob(byte[] data, int length) throws IOException {
			output.writeInt(BINARY);
			output.writeInt(length);

			if(length > 0) {
				output.write(data, 0, length);
			}
		}

		public synchronized void sendRegister() {
			try {
				output.writeInt(REGISTER);
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {
				DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
				output = new DataOutputStream(clientSocket.getOutputStream());

				sendMessage("Welcome to the Server!");

				String command[];
				do {
					command = input.readUTF().split(" ");
					switch(command[0]){
						case "/connectivitytest":
							break;
						case "/register":
							handleRegistration(command[1]);
							break;
						case "/dir":
							handleDirectoryList();
							break;
						case "/store":
							handleFileUpload(command[1], input);
							break;
						case "/get":
							handleFileDownload(command[1], input);
							break;
					}
				} while (!command[0].equals("/leave"));

				if(clientNickname != null){
					System.out.println(clientNickname + ": Leaving server...");
				} else {
					System.out.println("Unregistered user: Leaving server...");
				}
				sendMessage("Connection closed. Thank you!");
				handleLeave();
				clientSocket.close();
			} catch (IOException e) {
				// e.printStackTrace();
				handleLeave();
			}
		}

		// only one thread can write to disk for now to prevent multiple clients from writing into the disk
		private void handleFileUpload(String filename, DataInputStream input) throws IOException {
			synchronized(FileSystemServer.class) {
				Path filePath = Paths.get(SERVER_FILES_DIRECTORY, filename);

				while(true) {
					int fragmentSize = (int) input.readInt();
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

						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						Date date = new Date();
						System.out.println(clientNickname + "<" + formatter.format(date) + ">" + ": " + filename + " was uploaded.");
						sendToAllExceptCurrent(clientNickname + "<" + formatter.format(date) + ">" + ": " + filename + " was uploaded.", this);
						break;
					}
				}

			}


		}

		private void handleFileDownload(String filename, DataInputStream input) throws IOException {
			Path filePath = Paths.get(SERVER_FILES_DIRECTORY, filename);
			if (Files.exists(filePath)) {
				// Open a FileInputStream to read the file
				try (FileInputStream fileInputStream = new FileInputStream(filePath.toFile())) {
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
				}
			} else {
				sendMessage("Error: File not found in server.");;
			}
		}

		private void handleDirectoryList() throws IOException {
			try (Stream<Path> folderContents = Files.walk(Paths.get(SERVER_FILES_DIRECTORY))) {
				String filesList = folderContents
						.filter(Files::isRegularFile)
						.map(x -> x.getFileName().toString())
						.collect(Collectors.joining("\n"));

				if (filesList.isEmpty()) {
					filesList = "No files found.";
				}

				sendMessage("Server Directory");
				sendMessage(filesList);
			} catch (IOException e) {
				// Handle IOException appropriately (e.g., log or print the exception)
			}
		}

		// only one thread can register at a time
		private static final Object registrationMutex = new Object();
		private void handleRegistration(String nickname) throws IOException {
			synchronized(registrationMutex) {
				if (!clientMap.containsValue(nickname)) {
					clientMap.put(this, nickname);
					this.clientNickname = nickname;
					sendMessage("Registration successful. Welcome " + nickname + "!");
					sendToAllExceptCurrent(nickname + " has registered. Welcome them!", this);
					sendRegister();
				} else {
					sendMessage("Registration failed. Handle or alias already exists.");
				}
			}

		}

		private void handleLeave() {
			synchronized(registrationMutex) {
				if (clientNickname != null) {
					clientMap.remove(this);
					sendToAllExceptCurrent("Client left: " + clientNickname, this);
					System.out.println("Client left: " + clientNickname);
				} else {
					System.out.println("Unregistered user left.");
				}
			}
		}
	}
}

