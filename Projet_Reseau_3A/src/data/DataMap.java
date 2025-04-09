package data;

import utils.converter.ConvertFileToArray;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DataMap {
    Map<Integer,byte[] > dataMap;

    public Map<Integer, byte[]> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<Integer, byte[]> dataMap) {
        this.dataMap = dataMap;
    }

    public DataMap(){
        this.dataMap=new HashMap<Integer, byte[] >();
    }

    public void splitFile(File f,int pieceLen,int nbPiece){
        ConvertFileToArray convertFileToArray=new ConvertFileToArray(f);
        byte[] input=convertFileToArray.getDataArray();

        ByteBuffer bb = ByteBuffer.wrap(input);
        int lastPiece=nbPiece-1;
        int offset=0;

        for(int i=0;i<=nbPiece-1;i++){
            if(i==lastPiece){
                pieceLen=input.length%pieceLen; //taille de lastpiece
            }

            byte[] data=new byte[pieceLen];
            System.arraycopy(input, offset, data, 0, pieceLen);
            offset+=pieceLen;
//            int lenInput=input.length;
//            bb.get(data,0,lenInput-1);
            dataMap.put(i,data);
        }

    }

    public int splitByte(byte[] b){

        ByteBuffer bb = ByteBuffer.wrap(b);

        int offset=0;
        int nbBlock=(int) (b.length/ (128*128)
                + (b.length%(128*128)==0 ? 0 :1)); // number of pieces
        int sizelastBlock=b.length%(128*128);
        int lastBlock=nbBlock-1;
        int len=128*128;

        for(int i=0;i<=nbBlock-1;i++){
            if(i==lastBlock){
                len=sizelastBlock; //taille de lastpiece
            }

            byte[] data=new byte[len];
            System.arraycopy(b, offset, data, 0, len);
            offset+=len;
//            int lenInput=input.length;
//            bb.get(data,0,lenInput-1);
            dataMap.put(i,data);
        }
        return nbBlock;
    }

    public byte[] getBlock(int index,int length,int begin){
        byte[] result = new byte[length];
        byte[] piece=dataMap.get(index);
        System.arraycopy(piece, begin, result, 0, length);
        return result;
    }
}
