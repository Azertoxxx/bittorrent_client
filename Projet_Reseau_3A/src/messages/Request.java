package messages;

import java.io.*;

import static messages.MessageUtils.toWireBegin;

public class Request extends Message{
    //private static final byte[] len = new byte[4];
    //private static final byte id = 6;
    private byte[] index= new byte[4];  // integer specifying the zero-based piece index
    private byte[] begin= new byte[4];  // integer specifying zero-based byte offset within the piece
    private byte[] length= new byte[4]; // integer specifying the requested length.  ( default : 0x4000 = (16384)_10)

    public Request(byte[] l, byte[] i, byte[] b){
//    	Message: Len:13, Request, Piece (Idx:0x6,Begin:0x0,Len:0x4000)
//      Message Length: 13 (0x0000000d)
//      Message Type: Request (6)
//      Piece index: 0x00000006
//      Begin offset of piece: 0x00000000
//      Piece Length: 0x00004000
        id=6;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 13;//attention toutefois au Narrowing Primitive Conversion : https://stackoverflow.com/a/1935721

        this.length = l; // 2^14=16KB= 0x4000 -  pour l'instant
        this.index = i; // init à 0
        this.begin = b; // init à 0
    }

    public Request(){
        id=6;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 13;
    }

    public byte[] getIndex() {
        return index;
    }

    public byte[] getBegin() {
        return begin;
    }

    public byte[] getLenght() {
        return length;
    }
    @Override
    public byte[] toWire(Message request) throws IOException {
        Request r = (Request)request; // cast
        ByteArrayOutputStream outputStream = toWireBegin(r);
        outputStream.write(this.getIndex());
        outputStream.write(this.getBegin());
        outputStream.write(this.getLenght());
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public Request fromWire(byte[] message) throws IOException {
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
        byte[] index = in.readNBytes(4);
        byte[] begin = in.readNBytes(4);
        byte[] length = in.readNBytes(4);
        // System.out.println("Request FromWire received :");
        //  System.out.println(""+len+id+index+begin);

        return new Request(length, index, begin);
    }
}
