import java.net.*;
import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageServer
{
	public MessageServer(int port)
	{
		Thread messageServerMainThread = new Thread() {
			public void run() {
				ServerSocket serverSocket = null;
				ConcurrentHashMap<String, MessageServerThread> mapper;

				try {
					serverSocket = new ServerSocket(port);
					mapper = new ConcurrentHashMap<>();

					while(true) {
						try {
							System.out.println("Waiting for next connection");
							Socket socket = serverSocket.accept();

							// start thread
							new MessageServerThread(socket, mapper);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		messageServerMainThread.start();
	}
}
