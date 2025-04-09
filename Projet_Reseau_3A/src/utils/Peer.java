package utils;

import java.util.LinkedList;
import java.util.List;

public class Peer {
    private int am_choking;
    private int peer_choking;
    private int am_interested;
    private int peer_interested;
    private String peer_id;
    private String port;
    private String adressIP;
    private List<Integer> pieceHave;
    private List<Peer> peerInterested;

    public Peer(){
        am_choking = 1;
        peer_choking = 1;
        am_interested=0;
        peer_interested=0;
        peer_id=generatePeerId();
        pieceHave=new LinkedList<>();
    }


    //TODO
    public String generatePeerId() {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";
        StringBuilder sb = new StringBuilder(40);
        for (int i = 0; i < 40; i++){
            int index = (int)(AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    public Peer(String peer_id) {
        am_choking = 1;
        peer_choking = 1;
        am_interested = 0;
        peer_interested = 0;
        this.peer_id = peer_id;
    }
        /**
         * return true if client is choking a peer
         * */
    public boolean isChoking(){
        return am_choking==1;
    }

    /**
     * return true if client is interested in the peer
     * */
    public boolean isInterested(){
        return am_interested==1;
    }
    /**
     * return true if a peer is choking this client
     * */
    public boolean isChoked(){
        return peer_choking==1;
    }
    /**
     * return true if a peer is interested in this client
     * */
    public boolean peerInterested(){
        return peer_interested==1;
    }

    /**
     * A block is uploaded by a client when the client is not choking a peer,
     * and that peer is interested in the client.*/
    public boolean isBusy(){
        if(isChoked()||isChoking()){
            return true;
        }else
            return false;
    }

    public void setAm_choking(int am_choking) {
        this.am_choking = am_choking;
    }

    public void setPeer_choking(int peer_choking) {
        this.peer_choking = peer_choking;
    }

    public void setAm_interested(int am_interested) {
        this.am_interested = am_interested;
    }

    public void setPeer_interested(int peer_interested) {
        this.peer_interested = peer_interested;
    }

    public void setPeer_id(String peer_id) {
        this.peer_id = peer_id;
    }

    public String getPeer_id() {
        return peer_id;
    }

    public List<Integer> getPieceHave() {
        return pieceHave;
    }

    public void setPieceHave(List<Integer> pieceHave) {
        this.pieceHave = pieceHave;
    }

    public List<Peer> getPeerInterested() {
        return peerInterested;
    }

    public void setPeerInterested(List<Peer> peerInterested) {
        this.peerInterested = peerInterested;
    }
}
