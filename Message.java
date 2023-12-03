import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Message encoding:
 * <p>
 * roomnameLength : int      (if 0, then it is a broadcast/global message)
 * roomname       : byte[]   (if empty, then it is a broadcast/global message)
 * usernameLength : int      (if 0, then it is a broadcast/global message)
 * username       : byte[]   (if empty, then it is a broadcast/global message)
 * contentLength  : int
 * content        : byte[]
 * <p>
 * integers are encoded in big endian
 */

public record Message(String roomname, String username, String content) {
    public static final String GLOBAL_ROOM = "";

    public static Message fromByteArray(byte[] data) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

        return Message.fromDataInputStream(stream);
    }

    public static Message fromDataInputStream(DataInputStream stream) throws IOException {
        int roomnameLength = stream.readInt();
        String roomname = new String(stream.readNBytes(roomnameLength), StandardCharsets.UTF_8);

        int usernameLength = stream.readInt();
        String username = new String(stream.readNBytes(usernameLength), StandardCharsets.UTF_8);

        int contentLength = stream.readInt();
        String content = new String(stream.readNBytes(contentLength), StandardCharsets.UTF_8);

        return new Message(roomname, username, content);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(out);

        this.toDataOutputStream(stream);
        return out.toByteArray();
    }

    public void toDataOutputStream(DataOutputStream stream) throws IOException {
        byte[] roomnameBytes = this.roomname.getBytes(StandardCharsets.UTF_8);
        byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = this.content.getBytes(StandardCharsets.UTF_8);

        stream.writeInt(roomnameBytes.length);
        stream.write(roomnameBytes);
        stream.writeInt(usernameBytes.length);
        stream.write(usernameBytes);
        stream.writeInt(contentBytes.length);
        stream.write(contentBytes);
        stream.flush();
    }

    public boolean isGlobal() {
        return this.username.equals(GLOBAL_ROOM);
    }

    public void display() {
        if (this.isGlobal()) {
            System.out.print("Broadcast: ");
        } else {
            System.out.print(this.username() + ": ");
        }

        System.out.println(this.content());
    }
}
