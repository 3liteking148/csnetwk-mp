import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Message encoding:
 * <p>
 * usernameLength : int      (if 0, then it is a broadcast/global message)
 * username       : byte[]   (if empty, then it is a broadcast/global message)
 * contentLength  : int
 * content        : byte[]
 * <p>
 * integers are encoded in big endian
 */

public record Message(String username, String content) {

    public static Message fromByteArray(byte[] data) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data));

        return Message.fromDataInputStream(stream);
    }

    public static Message fromDataInputStream(DataInputStream stream) throws IOException {
        int usernameLength = stream.readInt();
        String username = new String(stream.readNBytes(usernameLength), StandardCharsets.UTF_8);

        int contentLength = stream.readInt();
        String content = new String(stream.readNBytes(contentLength), StandardCharsets.UTF_8);

        return new Message(username, content);
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream stream = new DataOutputStream(out);

        this.toDataOutputStream(stream);
        return out.toByteArray();
    }

    public void toDataOutputStream(DataOutputStream stream) throws IOException {
        byte[] usernameBytes = this.username.getBytes(StandardCharsets.UTF_8);
        byte[] contentBytes = this.content.getBytes(StandardCharsets.UTF_8);

        stream.writeInt(usernameBytes.length);
        stream.write(usernameBytes);
        stream.writeInt(contentBytes.length);
        stream.write(contentBytes);
        stream.flush();
        System.out.println("Wrote bytes");
    }

    public boolean isGlobal() {
        return this.username.equals("");
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
