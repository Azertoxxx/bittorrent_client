package messages;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class Bitfield extends Message {
    private final byte[] bitfieldArray;
    public Bitfield(){
        this.bitfieldArray = null;
    }
    public Bitfield(byte[] bitfieldArray) {
        id = 5;
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 1;

        //recuperation of the length of the bitfield and add it to the len
        this.bitfieldArray =  bitfieldArray;
        int lenB = bitfieldArray.length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(lenB);
        byte[] lenBitfield = bb.array();
        for(int i=0; i<4; i++){
            len[i] += lenBitfield[i];
        }

    }

    public Bitfield(int nbPiece) {
        len[0] = 0;
        len[1] = 0;
        len[2] = 0;
        len[3] = 1;

        //recuperation of the length of the bitfield and add it to the len
        this.bitfieldArray =  this.getBitfieldArray(nbPiece);
        int lenB = bitfieldArray.length;
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(lenB);
        byte[] lenBitfield = bb.array();
        for(int i=0; i<4; i++){
            len[i] += lenBitfield[i];
        }

    }

    public byte[] getBitfield() {
        return bitfieldArray;
    }
    @Override
    public byte[] toWire(Message bitfield) throws IOException{
        Bitfield b = (Bitfield)bitfield; // cast
        ByteArrayOutputStream outputStream;
        outputStream = MessageUtils.toWireBegin(b);
        outputStream.write((b.getBitfield()));
        byte[] data = outputStream.toByteArray();
        return data;
    }

    @Override
    public Bitfield fromWire(byte[] message) throws IOException {
        ByteArrayInputStream bs = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bs);


        byte[] len = MessageUtils.readLenFromWire(message);

        byte id = in.readByte();

        byte[] bitfieldArray = in.readAllBytes();

        return new Bitfield(bitfieldArray);
    }

    public byte[] getBitfieldArray(int nbPiece){
        int remainingBit=8-(nbPiece%8);
        int totalBit=nbPiece+remainingBit;
        BitSet bitSet=new BitSet();
        bitSet.set(remainingBit,totalBit);
        byte[] byteField=bitSet.toByteArray();
        ArrayUtils.reverse(byteField);
        return byteField;
    }


}
