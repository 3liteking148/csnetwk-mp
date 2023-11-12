/*
	CHUA, Harvey
	PINPIN, Lord
*/

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageClient extends Application
{
	private static String username;
	private static GUIController controller = null;
	private static BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();

	@Override
	public void start(Stage stage) throws Exception {
		this.controller = new GUIController(sendQueue, username);

		FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("./Messaging.fxml"));
		rootLoader.setController(this.controller);
		Parent root = rootLoader.load();

		stage.setScene(new Scene(root));
		stage.show();

		controller.addMessage(new Message("", "Use this to chat globally"));
	}


	public static void main(String[] args)
	{
		username = args[0];



		try
		{
			System.out.println("0.0.1");
			Socket socket = new Socket("127.0.0.1", 5678);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

			// send greetings
			System.out.println("Sending greetings");
			Message initial = new Message(args[0], "hello");
			initial.toDataOutputStream(outputStream);

			System.out.println("OK");

			Thread inputThread = new Thread() {
				public void run() {

					while(socket.isConnected()) {
						try {
							Message message = Message.fromDataInputStream(inputStream);
							System.out.println(message.username() + ": " + message.content());

							// run on JavaFX thread
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.addMessage(message);
								}
							});
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			};

			Thread outputThread = new Thread() {
				public void run() {
					Scanner scanner = new Scanner(System.in);
					while(socket.isConnected()) {
						try {
							outputStream.write(sendQueue.take());
						} catch (IOException e) {
							throw new RuntimeException(e);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				}
			};

			inputThread.start();
			outputThread.start();

			// run JavaFX
			launch(args);

			inputThread.join();
			outputThread.join();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			System.out.println("Client: Connection is terminated");
		}
	}


}