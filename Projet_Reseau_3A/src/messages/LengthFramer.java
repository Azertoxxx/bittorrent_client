package messages;

import seeder.MessageModel;

import java.io.*;

public class LengthFramer implements Framer {
    private final DataInputStream in; // wrapper for data I/O

    public LengthFramer(InputStream in) throws IOException {
        this.in = new DataInputStream(in);
    }

    public void frameMsg(byte[] message, OutputStream out) throws IOException {
        // write message
        out.write(message);
        out.flush();
    }

    public byte[] nextMsgHandshake() throws IOException {
        int length = 68;
        byte[] msg = new byte[length];
        try{
            in.read(msg,0,length);
        }catch(EOFException e){
            return null;
        }
        return msg;
    }

    public byte[] nextMsg() throws IOException {
        int length;
        try {
            length = in.readInt(); // read 4 bytes
        } catch (EOFException e) { // no (or 1 byte) message
            return null;
        }
        // 0 <= length <= 65535
        byte[] msg = new byte[length];
        in.read(msg,0,length); // if exception, it's a framing error.
        return msg;
    }

    public MessageModel nextMsg2() throws IOException {
        int length;
        try {
            length = in.readInt(); // read 4 bytes
        } catch (EOFException e) { // no (or 1 byte) message
            return null;
        }
        // 0 <= length <= 65535
        byte[] msg = new byte[length];
        in.read(msg,0,length); // if exception, it's a framing error.
        MessageModel msgModel=new MessageModel(length,msg);
        return msgModel;
    }

}
