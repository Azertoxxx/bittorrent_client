package utils;

import Bencode.InvalidBEncodingException;
import messages.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class TorrentUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    //convert bytearray into a string
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    //convert hexString into a bytearray
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
//    public static String byteArrayToURLString(byte in[]) {
//        byte ch = 0x00;
//        int i = 0;
//        if (in == null || in.length <= 0) {
//            return null;
//        }
//
//        String pseudo[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
//                          "A", "B", "C", "D", "E", "F"};
//        StringBuffer out = new StringBuffer(in.length * 2);
//
//        while (i < in.length) {
//            // First check to see if we need ASCII or HEX
//            if ((in[i] >= '0' && in[i] <= '9')
//                || (in[i] >= 'a' && in[i] <= 'z')
//                || (in[i] >= 'A' && in[i] <= 'Z') || in[i] == '$'
//                || in[i] == '-' || in[i] == '_' || in[i] == '.'
//                || in[i] == '+' || in[i] == '!') {
//                out.append((char) in[i]);
//                i++;
//            } else {
//                out.append('%');
//                ch = (byte) (in[i] & 0xF0); // Strip off high nibble
//                ch = (byte) (ch >>> 4); // shift the bits down
//                ch = (byte) (ch & 0x0F); // must do this is high order bit is
//                // on!
//                out.append(pseudo[(int) ch]); // convert the nibble to a
//                // String Character
//                ch = (byte) (in[i] & 0x0F); // Strip off low nibble
//                out.append(pseudo[(int) ch]); // convert the nibble to a
//                // String Character
//                i++;
//            }
//        }
//
//        String rslt = new String(out);
//
//        return rslt;
//    }

    //function for URL encoding info_hash and peer_id
    //like https://wiki.theory.org/BitTorrentSpecification#Tracker_HTTP.2FHTTPS_Protocol
    //info_hash and peer_id must be given as bytearray
    public static String byteArrayToURLString(byte in[]) {

        String resultat= "";
        String BYTE_ENCODING="ISO-8859-1";
        try {
            resultat = URLEncoder.encode(new String(in, BYTE_ENCODING), BYTE_ENCODING).replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultat;

    }

    public static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static HashSet<Integer> createRandomList(Integer max) {
        Random rand = new Random();
        HashSet<Integer> array = new LinkedHashSet<>();
        while (array.size() != max) {
            int randomIndex = rand.nextInt(max);
                array.add(randomIndex);
        }
        //System.out.println("[test]RandomLinkedHashSet : ");
        /*for(Integer var2: array){
            System.out.println(var2);
        }*/
        return array;
    }
    public static Integer getFromRandomList(HashSet<Integer> array,Integer j){
        Integer compteur = 0;
        for(Integer var2: array){
            if(compteur == j) return var2;
            else compteur++;
        }
        return -1;// = erreur
    }

    public static int getNbPiece(Torrent torrent) throws InvalidBEncodingException {
        return (int) (torrent.getTotalLength()/ torrent.getPieceLength()
                + (torrent.getTotalLength()%torrent.getPieceLength()==0 ? 0 :1));
    }

    public static int getNbBlock(Torrent torrent) throws InvalidBEncodingException {
        return (int) (torrent.getPieceLength() / 16384);
    }

    public static byte[] ReceptionMessage(SocketChannel clntChan, ByteBuffer readBuf) throws IOException {

        // lecture 1er bit  = taille
        int bytesRcvd2 = clntChan.read(readBuf); // renvoi le nombre de byte lu
        if (bytesRcvd2 == -1) {
            throw new SocketException("Connection closed prematurely");
        }
        int totalBytesRcvd2 = bytesRcvd2;
        //System.out.println("[DEBUG] : {Offset : " + offset + " ; totalBytesRcvd : " + totalBytesRcvd2 + " }");

        byte[] dst2 = new byte[totalBytesRcvd2];
        readBuf.get(dst2, 0, totalBytesRcvd2); // ByteBuffer get(byte[] dst, int offset, int length)
        return readBuf.array();


    }

    public static void SendMessage(String s, SocketChannel clntChan, byte[] l, byte[] index, byte[] b) throws IOException {
        switch (s)
        {
            case "HAVE" :
                Have haveMsgbf = new Have(index);
                byte[] encodedMsg7bf = haveMsgbf.toWire(haveMsgbf);
                //framer.frameMsg(encodedMsg7bf,out);
                ByteBuffer writeBuf7bf = ByteBuffer.wrap(encodedMsg7bf);
                clntChan.write(writeBuf7bf);
                break;
            case "REQUEST" : Request requestMsgbf = new Request(l, index, b);
                byte[] encodedMsg5bf = requestMsgbf.toWire(requestMsgbf);
                //framer.frameMsg(encodedMsg5bf, out);
                ByteBuffer writeBuf5bf = ByteBuffer.wrap(encodedMsg5bf);
                clntChan.write(writeBuf5bf);
                break;
            default : break;
        }


}
}
