/**
 * Torrent is a class that allows to read a .torrent file
 * and to extract information from it
 */
package utils;

import Bencode.BDecoder;
import Bencode.BEncodedValue;
import Bencode.BEncoder;
import Bencode.InvalidBEncodingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

public class Torrent {
    private File torrentFile;
    private FileInputStream inputStream;
    private BDecoder reader;
    private Map<String, BEncodedValue> document;

    public Torrent(String torrent) throws IOException {
        this.torrentFile = new File(torrent);
        this.inputStream = new FileInputStream(torrentFile);
        this.reader = new BDecoder(inputStream);
        this.document = reader.decodeMap().getMap();
    }

    /**
     * return the url of the tracker
     * @return
     * @throws InvalidBEncodingException
     */
    public String getAnnounce() throws InvalidBEncodingException {
        return document.get("announce").getString();
    }

    /**
     * return the infos of the file
     * @return
     * @throws InvalidBEncodingException
     */
    public Map getDocument() throws InvalidBEncodingException {
        return document.get("info").getMap();
    }

    public String getName() throws InvalidBEncodingException {
        Map info = document.get("info").getMap();
        BEncodedValue totalLengthBencoded = (BEncodedValue) info.get("name");
        String name = totalLengthBencoded.getString();

        return name;

    }

    /**
     * return the info encoded in sha1
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public String getInfo_hash() throws IOException{
        Map info = document.get("info").getMap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BEncoder.encode(info, baos);
        String mapAsString = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);

        byte[] fileInfoSha1 = DigestUtils.sha1(mapAsString.getBytes(StandardCharsets.ISO_8859_1));
        String info_hash = TorrentUtils.bytesToHex(fileInfoSha1);
        return info_hash;
    }
    
    
    public byte [] getInfo_hash_to_bytearray() throws IOException, NoSuchAlgorithmException {
        Map info = document.get("info").getMap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BEncoder.encode(info, baos);

        return DigestUtils.sha1(baos.toByteArray());
        }

    public String escapeInfo_Hash(String info_hash){
        StringBuilder output = new StringBuilder("");
        for(int i=0; i<info_hash.length(); i+=2){
            String byteValue = info_hash.substring(i,i+2);
            if(byteValue.equals("2D")
                    || byteValue.equals("2E")
                    || byteValue.equals("7E")
                    || byteValue.equals("5F")
                    || (Integer.parseInt(byteValue,16) >= Integer.parseInt("30",16)
                    && Integer.parseInt(byteValue,16) <= Integer.parseInt("39",16))
                    || (Integer.parseInt(byteValue,16) >= Integer.parseInt("41",16)
                    && Integer.parseInt(byteValue,16) <= Integer.parseInt("5A",16))
                    || (Integer.parseInt(byteValue,16) >= Integer.parseInt("61",16)
                    && Integer.parseInt(byteValue,16) <= Integer.parseInt("7A",16))){
                output.append((char) Integer.parseInt(byteValue, 16));
            }
            else{
                output.append("%");
                output.append(byteValue);
            }
        }
        return output.toString();
    }

    public String getPeer_id(){
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

    public String getPort(){
        return "6881";
    }

    public String getUploaded(){
        return "0";
    }

    public String getDownloaded(){
        return "0";
    }

    public String getLeft(){
        return "0";
    }

    public int getTotalLength() throws InvalidBEncodingException {
        Map info = document.get("info").getMap();
        BEncodedValue totalLengthBencoded = (BEncodedValue) info.get("length");
        int totalLength = totalLengthBencoded.getInt();

        return totalLength;
    }

    public int getPieceLength() throws InvalidBEncodingException {
        Map info = document.get("info").getMap();
        BEncodedValue piecelLengthBencoded = (BEncodedValue) info.get("piece length");
        int pieceLength = piecelLengthBencoded.getInt();

        return pieceLength;
    }

    public byte[] getPieces() throws InvalidBEncodingException {
        Map info = document.get("info").getMap();
        BEncodedValue piecesBencoded = (BEncodedValue) info.get("pieces");
        byte[] pieces = piecesBencoded.getBytes();

        return pieces;
    }

}
