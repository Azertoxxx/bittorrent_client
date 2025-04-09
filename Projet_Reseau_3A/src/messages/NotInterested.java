package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class NotInterested extends Message{
    //private static final byte[] len = new byte[4];
    //private static final byte id = 3;

    public NotInterested(){
        id=3;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 1;
    }


    @Override
    public byte[] toWire(Message notInterested) throws IOException {
        NotInterested n = (NotInterested)notInterested; // cast
        ByteArrayOutputStream outputStream = toWireBegin(n);
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public NotInterested fromWire(byte[] message) throws IOException {
        // TG - 30/10/20
        ByteArrayInputStream bs = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bs);

        /*byte[] len = new byte[4];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        dos.writeInt(message.length);
        len = bos.toByteArray();*/
        byte[] len = MessageUtils.readLenFromWire(message);

        byte id = in.readByte();
        System.out.println("NotInterested FromWire received :");
        System.out.println(""+len+id);

        return new NotInterested();
    }

}
