package leecher;

import messages.Bitfield;
import utils.Peer;
import utils.TorrentUtils;
import java.util.Random;

import java.util.LinkedList;
import java.util.List;

public class TestPieceManager {

    public static void main(String[] args)  {
        Random rand = new Random();
        Peer leecher=new Peer();
        Peer seeder1=new Peer("seeder1");
        Peer seeder2=new Peer("seeder2");
        int nbPiece=8;
        String bf1="F0";
        String bf2="0F";
        BitFieldManager bitFieldManager1=new BitFieldManager(
                new Bitfield(
                        TorrentUtils.hexStringToByteArray(bf1)
                ),
                nbPiece);
        BitFieldManager bitFieldManager2=new BitFieldManager(
                new Bitfield(
                        TorrentUtils.hexStringToByteArray(bf2)
                ),
                nbPiece);
        BitFieldManager bitFieldManager3=new BitFieldManager(
                new Bitfield(
                        TorrentUtils.hexStringToByteArray("00")
                ),
                nbPiece);
        seeder1.setPieceHave(bitFieldManager1.getListPiece());
        seeder2.setPieceHave(bitFieldManager2.getListPiece());
        leecher.setPieceHave(bitFieldManager3.getListPiece());

        List<Peer> listSeeder = new LinkedList<>();
        listSeeder.add(seeder2);
        listSeeder.add(seeder1);

        PieceManager pieceManager=new PieceManager(nbPiece);
        Peer currentPeer;
        int pieceRecu=0; //aucun piece recu
        pieceManager.getRequested().add(pieceRecu);
        while(!pieceManager.requestDone()){
            System.out.println("pice recu  "+pieceRecu);
            if(pieceRecu>=0 && pieceRecu<=3){
                currentPeer=seeder1;
            }else{
                currentPeer=seeder2;
            }
            int next=pieceManager.nextPieceToRequest(leecher,pieceRecu,currentPeer);
            if(next==-1){
                System.out.println("peer "+currentPeer.getPeer_id()+" est termine");
                pieceRecu++;
                pieceManager.getRequested().add(pieceRecu);
            }else{
                System.out.println("next piece from "+currentPeer.getPeer_id()+" is "+next+".");
                pieceRecu=next;
            }
        }
    }


}
