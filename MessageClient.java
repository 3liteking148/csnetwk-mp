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
	private Stage stage;

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
		Platform.runLater(() -> {
			try {
				stage = new Stage();
				start(stage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private Socket socket;
	public MessageClient(String username, String host, int port)
	{
		this.username = username;

		try
		{
			socket = new Socket(host, port);
			DataInputStream inputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

			// send greetings
			System.out.println("Sending greetings");
			Message initial = new Message(username, username, "hello");
			initial.toDataOutputStream(outputStream);

			System.out.println("OK");

			inputThread = new Thread() {
				public void run() {
					try {
						while(!socket.isClosed()) {
							Message message = Message.fromDataInputStream(inputStream);
							System.out.println(message.username() + ": " + message.content());

							// run on JavaFX thread
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									controller.addMessage(message);
								}
							});
						}
					} catch (IOException e) {
							destroy();
					}
				}
			};

			outputThread = new Thread() {
				public void run() {
					while(!socket.isClosed()) {
						try {
							outputStream.write(sendQueue.take());
						} catch (IOException e) {
							destroy();
						} catch (InterruptedException e) {
							
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
		Platform.runLater(() -> {
			try {
				stage.close();
			} catch (Exception e) {
			}
		});

		if(socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}
