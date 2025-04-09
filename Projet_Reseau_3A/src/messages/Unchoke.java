package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class Unchoke extends Message{
    //private static final byte[] len = new byte[4];
    //private static final byte id = 1;

    public Unchoke(){
        id=1;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 1;
    }

    @Override
    public byte[] toWire(Message unchoke) throws IOException {
        Unchoke u = (Unchoke)unchoke; // cast
        ByteArrayOutputStream outputStream = toWireBegin(u);
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public Unchoke fromWire(byte[] message) throws IOException {
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

        return new Unchoke();

    }
}
