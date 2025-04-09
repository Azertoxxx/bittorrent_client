/**
 * Request HTTP GET for the tracker
 */

package tracker;

import Bencode.BDecoder;
import Bencode.BEncodedValue;
import utils.Torrent;
import utils.TorrentUtils;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class GetHttpRequest {

    public GetHttpRequest(Torrent torrentFile) throws IOException, NoSuchAlgorithmException {
        //url
        String urlString = /*torrentFile.getAnnounce()*/ "http://127.0.0.1:6969/announce"
                + "?info_hash=" + TorrentUtils.byteArrayToURLString(torrentFile.getInfo_hash_to_bytearray())
                + "&peer_id=" + TorrentUtils.byteArrayToURLString(TorrentUtils.hexStringToByteArray(torrentFile.getPeer_id()))
                + "&port=" + torrentFile.getPort()
                + "&uploaded=" + torrentFile.getUploaded()
                + "&downloaded=" + torrentFile.getDownloaded()
                + "&left=" + torrentFile.getLeft() // torrentFile.getTotalLength() pour le leecher
                + "&event=" + "started";
        System.out.println("url :" + urlString);

        //Creation of the request
        try{
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.getResponseCode();

            BDecoder reader = new BDecoder((con.getInputStream()));
            Map<String, BEncodedValue> document = reader.decodeMap().getMap();

            byte[] peers = document.get("peers").getBytes();
            int nbPeers = peers.length/6;

            String peersHex = TorrentUtils.bytesToHex(peers);
            System.out.println("Peers in hexa : "+peersHex);

            String[] peersIp = new String[nbPeers];
            int[] peersPorts = new int[nbPeers];

            for(int i=0; i< 2*peers.length; i+=12){
                for(int j=0; j<nbPeers; j++){

                    String s1 = new StringBuilder().append(peersHex.charAt(i)).append(peersHex.charAt(i+1)).toString();
                    String s2 = new StringBuilder().append(peersHex.charAt(i+2)).append(peersHex.charAt(i+3)).toString();
                    String s3 = new StringBuilder().append(peersHex.charAt(i+4)).append(peersHex.charAt(i+5)).toString();
                    String s4 = new StringBuilder().append(peersHex.charAt(i+6)).append(peersHex.charAt(i+7)).toString();
                    String s5 = new StringBuilder().append(peersHex.charAt(i+8)).append(peersHex.charAt(i+9))
                            .append(peersHex.charAt(i+10)).append(peersHex.charAt(i+11)).toString();

                     peersIp[j] = Integer.parseInt(s1,16) + "."
                             + Integer.parseInt(s2,16) + "."
                             + Integer.parseInt(s3,16) + "."
                             + Integer.parseInt(s4,16);

                     peersPorts[j] = Integer.parseInt(s5,16);
                }
            }

            con.disconnect();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        }
}
