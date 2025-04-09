package leecher;


import messages.Bitfield;
import utils.TorrentUtils;

import java.util.List;

public class TestBitfield {

    public static void main(String[] args)  {
        byte[] array= TorrentUtils.hexStringToByteArray("c8");
        int nbPiece=5;
        Bitfield bf=new Bitfield(array);
        BitFieldManager bfM=new BitFieldManager(bf,nbPiece);
        List list=bfM.getListPiece();
        System.out.println(list);
    }
}
