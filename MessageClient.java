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
	private String username;
	private GUIController controller = null;
	private BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();
	private Thread inputThread, outputThread;
	@Override
	public void start(Stage stage) throws Exception {
		stage.setTitle(username);

		this.controller = new GUIController(sendQueue, username);

		FXMLLoader rootLoader = new FXMLLoader(getClass().getResource("./Messaging.fxml"));
		rootLoader.setController(this.controller);
		Parent root = rootLoader.load();

		stage.setScene(new Scene(root));
		stage.show();

		controller.addMessage(new Message(Message.GLOBAL_ROOM, "System", "Use this to chat globally"));
	}

	public void startGUI() {
		Platform.startup(() -> {
			try {
				start(new Stage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public MessageClient(String username, String host, int port)
	{
		this.username = username;

		try
		{
			Socket socket = new Socket(host, port);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

			// send greetings
			System.out.println("Sending greetings");
			Message initial = new Message(username, username, "hello");
			initial.toDataOutputStream(outputStream);

			System.out.println("OK");

			inputThread = new Thread() {
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

			outputThread = new Thread() {
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void destroy() {
		if(inputThread != null) {
			inputThread.interrupt();
		}

		if(outputThread != null) {
			outputThread.interrupt();
		}

		Platform.runLater(() -> {
			try {
				stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}


}
