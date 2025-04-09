package messages;

import utils.TorrentUtils;

import java.io.*;

public class HandshakeCoder {

    public static final String CHARSETNAME = "ISO_8859_1";
    public static final String DELIMSTR = " ";


    public byte[] toWire(Handshake handshake) throws IOException {
        // *--  Récup Strings --*
        String handshakePstr = handshake.getPstr();
        String handshakeInfo_hash = handshake.getInfo_hash();
        String handshakePeer_id = handshake.getPeer_id();
        // *-- Mise en forme byte --*
        byte[] data1 = handshakePstr.getBytes();
        byte[] reserved = handshake.getReserved();
        byte[] data2 = TorrentUtils.hexStringToByteArray(handshakeInfo_hash);
        byte[] data3 = TorrentUtils.hexStringToByteArray(handshakePeer_id);;
        // *-- outputStream =  concaténation de la sortie --*
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(handshake.getPstrlen());
        outputStream.write(data1);
        outputStream.write(reserved);
        outputStream.write(data2);
        outputStream.write(data3);
        outputStream.flush(); // vider
        byte[] data = outputStream.toByteArray(); // envoi
        return data; // retour concaténation
    }

    public Handshake fromWire(byte[] message) throws IOException {
        ByteArrayInputStream bs = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bs);

        int pstrlen = in.readByte();

        byte[] pstrByte = in.readNBytes(19);

        byte[] reservedByte = in.readNBytes(8);

        byte[] info_hashByte = in.readNBytes(20);
        String info_hash = TorrentUtils.bytesToHex(info_hashByte);

        byte[] peer_idByte = in.readNBytes(20);
        String peer_id = TorrentUtils.bytesToHex(peer_idByte);

//        System.out.println("*--- Handshake FromWire Received ---* ");
//        String pstrByteS = new String(pstrByte);
//        String reservedByteS = new String(reservedByte);
//        System.out.println("*--- [pstrlen] : "+pstrlen+"\n*--- [pstrByte] : "+pstrByteS+"\n*--- [reservedByte] : "+reservedByteS+"\n*--- [infohash] : "+info_hash+"\n*--- [peer_id] : "+peer_id);
        return new Handshake(info_hash,peer_id);
    }
}
