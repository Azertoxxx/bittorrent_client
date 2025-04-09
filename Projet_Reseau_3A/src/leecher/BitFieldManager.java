package leecher;

import messages.Bitfield;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

public class BitFieldManager {
    private List<Integer> listPiece;
    private BitSet bitField;
    private int nbPiece;

    public BitFieldManager(Bitfield bitfield,int nbPiece) {
        byte[] bfByte=bitfield.getBitfield();
        this.nbPiece=nbPiece;
        listPiece=convertArrayToList(bfByte);


    }
    public List convertArrayToList(byte[] array){
        BitSet bitSet= BitSet.valueOf(array);
        List<Integer> list=new LinkedList<>();
        int indexFin=nbPiece-1;
        int rem=(nbPiece%8==0?0:8-(nbPiece%8));
        int indexBitSet;
        for(int i=0;i<nbPiece;i++){
            indexBitSet=indexFin+rem-i;
            if(bitSet.get(indexBitSet)==true){
                list.add(i);
            }
        }
        return list;
    }

    public List<Integer> getListPiece() {
        return listPiece;
    }

    public void setListPiece(List<Integer> listPiece) {
        this.listPiece = listPiece;
    }

    public BitSet getBitField() {
        return bitField;
    }

    public void setBitField(BitSet bitField) {
        this.bitField = bitField;
    }
}
