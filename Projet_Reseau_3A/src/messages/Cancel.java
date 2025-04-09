package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class Cancel extends Message{
    //private static final byte[] len = new byte[4];
    //private static final byte id = 8;
    private byte[] index= new byte[4];  // integer specifying the zero-based piece index
    private byte[] begin= new byte[4];  // integer specifying zero-based byte offset within the piece
    private byte[] length= new byte[4]; // integer specifying the requested length.  ( default : 0x4000 = (16384)_10)

    public Cancel(byte[] l, byte[] i, byte[] b){
        id=8;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 13;
        this.length = l; // 2^14=16KB= 0x4000 -  pour l'instant
        this.index = i; // init à 0
        this.begin = b; // init à 0


    }


    public byte[] getIndex() {
        return index;
    }

    public void setIndex(byte[] index) {
        this.index = index;
    }

    public byte[] getBegin() {
        return begin;
    }

    public void setBegin(byte[] begin) {
        this.begin = begin;
    }

    public byte[] getLength() {
        return length;
    }

    public void setLength(byte[] length) {
        this.length = length;
    }

    @Override
    public byte[] toWire(Message cancel) throws IOException {
        Cancel c = (Cancel)cancel; // cast
        ByteArrayOutputStream outputStream = toWireBegin(c);
        outputStream.write(c.getIndex());
        outputStream.write(c.getBegin());
        outputStream.write(c.getLength());
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public Cancel fromWire(byte[] message) throws IOException {
        // TG - 30/10/20
        // *- Longueur à editer -*
        ByteArrayInputStream bs = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bs);
        //Message msg = new Cancel();
        byte[] len = MessageUtils.readLenFromWire(message);
        byte[] id = in.readNBytes(10);
        byte[] index = in.readNBytes(10);
        byte[] begin = in.readNBytes(10);
        byte[] length = in.readNBytes(10);
        System.out.println("Cancel FromWire received :");
        System.out.println(""+len+id+index+begin+length);

        return new Cancel(length,index,begin);
    }
}
