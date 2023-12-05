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
        if(!socket.isClosed()) {
            sendQueue.add(data.clone());
        }
    }

    // hack
    private MessageServerThread getCurrentUser() {
        return this;
    }

    private Thread inputThread, outputThread;
    public MessageServerThread(Socket socket, ConcurrentHashMap<String, MessageServerThread> mapper) throws IOException {
        this.socket = socket;
        this.mapper = mapper;

        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        inputThread = new Thread() {
            public void run() {
                try {
                    System.out.println("Waiting for client's configuration");
                    Message initial = Message.fromDataInputStream(inputStream);

                    // should be safe since it's a one-time occurance
                    username = initial.username();
                    mapper.put(username, getCurrentUser());

                    while(!socket.isClosed()) {
                        Message message = Message.fromDataInputStream(inputStream);
                        System.out.println(username + ": received " + message.content());

                        // send to client
                        if(!message.roomname().equals("")) {
                            // unicast
                            MessageServerThread target = mapper.get(message.roomname());
                            if(target != null) {
                                byte[] toSend = (new Message(message.username(), message.username(), message.content())).toByteArray();
                                target.write(toSend);
                            } else {
                                byte[] toSend = (new Message(message.roomname(), "System", "User does not exist or is no longer connected to server.")).toByteArray();
                                write(toSend);
                            }

                        } else {
                            // multicast
                            byte[] toSend = message.toByteArray();
                            for(MessageServerThread m : mapper.values() ) {
                                if(!m.getUsername().equals(username)) { // except self
                                    m.write(toSend);
                                }
                            }
                        }
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

    public void destroy() {
        
    }
}
