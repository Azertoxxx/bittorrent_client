package messages;

public class Handshake {
    private static final String pstr = "BitTorrent protocol";
    private static final int pstrlen = 19;
    private static final byte[] reserved = new byte[8];
    private String info_hash;
    private String peer_id;
    public static final String CHARSETNAME = "ISO_8859_1";
    public static final String DELIMSTR = " ";

    public Handshake(String info_hash, String peer_id) {
        this.info_hash = info_hash;
        this.peer_id = peer_id;
        for(int i=0; i<reserved.length; i++){
            reserved[i]=0;
        }
    }

    public static String getPstr() {
        return pstr;
    }

    public static int getPstrlen() {
        return pstrlen;
    }

    public static byte[] getReserved(){
        return reserved;
    }

    public String getInfo_hash() {
        return info_hash;
    }

    public String getPeer_id() {
        return peer_id;
    }

    public String toString(){
        return this.pstrlen +  this.pstr + this.info_hash + this.peer_id;
    }




}
