import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageServerThread {
    private Socket socket;
    private ConcurrentHashMap<String, MessageServerThread> mapper;
    private String username;

    // threadsafe queue
    private BlockingQueue<byte[]> sendQueue = new LinkedBlockingQueue<>();

    public void write(byte[] data) {
        if(socket.isConnected()) {
            sendQueue.add(data.clone());
        }
    }

    // hack
    private MessageServerThread getCurrentUser() {
        return this;
    }
    public MessageServerThread(Socket socket, ConcurrentHashMap<String, MessageServerThread> mapper) throws IOException {
        this.socket = socket;
        this.mapper = mapper;

        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        Thread inputThread = new Thread() {
            public void run() {

                try {
                    System.out.println("Waiting for client's configuration");
                    Message initial = Message.fromDataInputStream(inputStream);

                    // should be safe since it's a one-time occurance
                    username = initial.username();

                    // xx
                    System.out.println("Username: " + username);
                    mapper.put(username, getCurrentUser());


                    while(socket.isConnected()) {
                        try {
                            Message message = Message.fromDataInputStream(inputStream);
                            System.out.println(username + ": received " + message.content());

                            // send to client
                            if(!message.roomname().equals("")) {
                                // unicast
                                byte[] toSend = (new Message(message.username(), message.username(), message.content())).toByteArray();
                                mapper.get(message.roomname()).write(toSend);
                            } else {
                                // multicast
                                byte[] toSend = message.toByteArray();
                                for(MessageServerThread m : mapper.values() ) {
                                    if(!m.getUsername().equals(username)) { // except self
                                        m.write(toSend);
                                    }
                                }
                            }

                        } catch (IOException e) {
                        }
                    }
                } catch (IOException e) {
                }
            }
        };

        Thread outputThread = new Thread() {
            public void run() {
                while(socket.isConnected()) {
                    try {
                        byte[] toSend = sendQueue.take();
                        outputStream.write(toSend);

                    } catch (Exception e) {
                    }
                }
            }
        };

        inputThread.start();
        outputThread.start();
    }

    public String getUsername() {
        return this.username;
    }


}
