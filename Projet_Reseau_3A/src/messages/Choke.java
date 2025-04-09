package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class Choke extends Message{
    //private static final byte[] len = new byte[4];
    // private static final byte id = 0;

    public Choke(){
        id=0;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 1;
    }

    @Override
    public byte[] toWire(Message choke) throws IOException {
        Choke c = (Choke)choke; // cast
        ByteArrayOutputStream outputStream = toWireBegin(c);
        byte[] data = outputStream.toByteArray();
        return data;
    }

    @Override
    public Choke fromWire(byte[] message) throws IOException {
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
        System.out.println("Choke FromWire received :");
        System.out.println(""+len+id);

        return new Choke();
    }
}
