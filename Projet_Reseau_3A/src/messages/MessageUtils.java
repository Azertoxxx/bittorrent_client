package messages;

import java.io.*;

public class MessageUtils {
    public static ByteArrayOutputStream toWireBegin(Message msg) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write((msg.getLen()));
        outputStream.write((msg.getId()));
        return outputStream;
    }

    public static byte[] readLenFromWire(byte[] message) throws IOException {
        byte[] len;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(message.length);
        len = bos.toByteArray();

        return len;
    }
}
