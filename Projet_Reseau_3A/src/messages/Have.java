package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class Have extends Message{
    //private static final byte[] len = new byte[4];
    // private static final byte id = 4;
    private byte[] pieceIndex;

    public Have(byte[] pieceIndex){
        id=4;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 5;

        this.pieceIndex = pieceIndex;
    }


    public byte[] getPieceIndex() {
        return pieceIndex;
    }
    @Override
    public byte[] toWire(Message have) throws IOException {

         Have h = (Have)have; // cast
        ByteArrayOutputStream outputStream = toWireBegin(h);

        outputStream.write(h.getPieceIndex());
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public Have fromWire(byte[] message) throws IOException {
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
        byte[] pieceIndex = in.readAllBytes();
        System.out.println("Have FromWire received :");
        System.out.println(""+len+id);

        return new Have(pieceIndex);
    }
}
