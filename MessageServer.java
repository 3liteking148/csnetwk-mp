import java.net.*;
import java.io.*;

public class MessageServer
{
	public MessageServer(InetAddress ip, int port)
	{
		Thread messageServerMainThread = new Thread() {
			public void run() {
				ServerSocket serverSocket = null;

				try {
					serverSocket = new ServerSocket(port, 0, ip);

					while(true) {
						try {
							System.out.println("Waiting for next connection");
							Socket socket = serverSocket.accept();

							// start thread
							new MessageServerThread(socket);
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
