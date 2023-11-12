/*
	CHUA, Harvey
	PINPIN, Lord
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSystemServer
{
	public static void main(String[] args)
	{
		int nPort = Integer.parseInt(args[0]);
		System.out.println("Server: Listening on port " + args[0] + "...");
		System.out.println();
		ServerSocket serverSocket;
		Socket serverEndpoint1, serverEndpoint2;
		ExecutorService executor = Executors.newFixedThreadPool(10);

		try
		{
			serverSocket = new ServerSocket(nPort);
			System.out.println("Server started. Waiting for clients...");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket);

				ClientHandler clientHandler = new ClientHandler(clientSocket);
				executor.execute(clientHandler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Server: Connection is terminated");
		}
	}
}


class ClientHandler implements Runnable {
	private Socket clientSocket;

	public ClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

			output.println("Welcome to the Server!");

			String command;
			do {
				command = input.readLine();

				// Handle certain commands
			} while (!command.equals("/leave"));

			System.out.println("Leaving server...");
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}