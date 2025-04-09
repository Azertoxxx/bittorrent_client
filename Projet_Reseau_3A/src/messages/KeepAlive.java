package messages;

import java.io.*;

public class KeepAlive extends Message{

    //private static final byte[] len = new byte[4];

    public KeepAlive(){
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 0;
    }

    @Override
    public byte[] toWire(Message keepAlive) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write((this.getLen()));
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public KeepAlive fromWire(byte[] message) throws IOException {
        // TG - 30/10/20
        ByteArrayInputStream bs = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bs);

        /*byte[] len = new byte[4];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(message.length);
        len = bos.toByteArray();*/
        byte[] len = MessageUtils.readLenFromWire(message);
        System.out.println("KeepAlive FromWire received :");
        System.out.println(""+len);

        return new KeepAlive();
    }
}
