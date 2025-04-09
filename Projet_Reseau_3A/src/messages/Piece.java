package messages;

import java.io.*;
import java.nio.ByteBuffer;

import static messages.MessageUtils.toWireBegin;

public class Piece extends Message{
   //private static final byte[] len = new byte[4];
    private static final byte[] lenBlock = new byte[4];
    //private static final byte id = 7;
    private byte[] index = new byte[4];
    private byte[] begin = new byte[4];
    private byte[] block;
    //block;
    public Piece(){
        id=7;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 9;
        this.block = null;
        this.index = null;  // ex : 5 (11 au total)
        this.begin = null;  // ex : 0
    }
    public Piece(byte[] i, byte[] b, byte[] dataPiece ){
        id=7;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 9;

        this.block =  dataPiece;
        int lenB = block.length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(lenB);
        byte[] lenBitfield = bb.array();
        for(int j=0; j<4; j++){
            len[j] += lenBitfield[j];
        }

        this.index = i;  // ex : 5 (11 au total)
        this.begin = b;  // ex : 0
        //       this.length = l; // ex : 16k = 0x4000

/*        this.dataPiece = new byte[l];
        // remplissage dataPiece = data découpée en bloc

        final int start = this.index * this.length + this.begin;

        for(int j=0;j<l; j++){
            // exemple
            // data : byte[] total de la piece
            // [1 1 1 1 2 2 2 2 3 3 3 3 4 4 4 4 5 5 5 5 6 6 6 6 7 7 7 7]
            // index = 2 ** length = 4 ** begin = 2
            // start = 2*4+2=10
            // -> dataPiece = [3 3 4 4]
            this.dataPiece[j] = data[start+j];
        }*/

    }



    public static byte[] getLenBlock() {
        return lenBlock;
    }



    public byte[] getIndex() {
        return index;
    }

    public byte[] getBegin() {
        return begin;
    }
    @Override
    public byte[] toWire(Message piece) throws IOException {
        Piece p = (Piece)piece; // cast
        ByteArrayOutputStream outputStream = toWireBegin(p);
        outputStream.write(this.getIndex());
        outputStream.write(this.getBegin());
        // outputStream.write(piece.getBlock());
        byte[] data = outputStream.toByteArray();
        return data;
    }
    @Override
    public Piece fromWire(byte[] message) throws IOException {
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
        byte[] data = in.readAllBytes();
        //System.out.println("PieceText FromWire received :");
        //System.out.println(""+len+id+index+begin+data);
        return new Piece(index, begin, data);
    }
    public byte[] getBlock() {
        return block;
    }
}
