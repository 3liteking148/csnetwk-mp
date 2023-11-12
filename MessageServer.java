/*
	CHUA, Harvey
	PINPIN, Lord
*/

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageServer
{
	public static void main(String[] args)
	{
		int nPort = 5678;
		ServerSocket serverSocket = null;
		ConcurrentHashMap<String, MessageServerThread> mapper;

		try {
			serverSocket = new ServerSocket(nPort);
			mapper = new ConcurrentHashMap<>();

			while(true) {
				try {
					System.out.println("Waiting for next connection");
					Socket socket = serverSocket.accept();
					MessageServerThread test = new MessageServerThread(socket, mapper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}