package tracker;

import Bencode.BDecoder;
import Bencode.BEncodedValue;
import utils.Torrent;
import utils.TorrentUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Tracker {
    private Torrent torrentFile;
    private int uploaded;
    private int downloaded;

    public Tracker(Torrent torrentFile){
        this.torrentFile = torrentFile;
        this.uploaded = 0;
        this.downloaded = 0;
    }

    public String generateUrlString(boolean leecher) throws IOException, NoSuchAlgorithmException {
        String urlString = /*torrentFile.getAnnounce()*/ "http://127.0.0.1:6969/announce"
                + "?info_hash=" + TorrentUtils.byteArrayToURLString(torrentFile.getInfo_hash_to_bytearray())
                + "&peer_id=" + TorrentUtils.byteArrayToURLString(TorrentUtils.hexStringToByteArray(torrentFile.getPeer_id()))
                + "&port=" + torrentFile.getPort()
                + "&uploaded=" + uploaded
                + "&downloaded=" + downloaded;
        if(leecher){ urlString += torrentFile.getTotalLength(); }
        else{urlString += "0";}
        urlString += "&event=" + "started";
        System.out.println("url :" + urlString);
        return urlString;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public int getUploaded() {
        return uploaded;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public Map<String, BEncodedValue> requestHttpGet(String urlString) throws IOException {
            Map<String, BEncodedValue> document = null;
            try{
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.getResponseCode();
                BDecoder reader = new BDecoder((con.getInputStream()));
                document = reader.decodeMap().getMap();
                con.disconnect();
        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
            return document;
    }

    public String[] getPeersIP(Map<String, BEncodedValue> document) throws IOException {
        byte[] peers = document.get("peers").getBytes();
        int nbPeers = peers.length / 6;
        String peersHex = TorrentUtils.bytesToHex(peers);
        String[] peersIp = new String[nbPeers];

        int j = 0;
        for (int i = 0; i < 2 * peers.length; i += 12) {
                String s1 = new StringBuilder().append(peersHex.charAt(i)).append(peersHex.charAt(i + 1)).toString();
                String s2 = new StringBuilder().append(peersHex.charAt(i + 2)).append(peersHex.charAt(i + 3)).toString();
                String s3 = new StringBuilder().append(peersHex.charAt(i + 4)).append(peersHex.charAt(i + 5)).toString();
                String s4 = new StringBuilder().append(peersHex.charAt(i + 6)).append(peersHex.charAt(i + 7)).toString();

                peersIp[j] = Integer.parseInt(s1, 16) + "."
                        + Integer.parseInt(s2, 16) + "."
                        + Integer.parseInt(s3, 16) + "."
                        + Integer.parseInt(s4, 16);
                j++;
        }
        return peersIp;
    }

    public int[] getPeersPorts(Map<String, BEncodedValue> document) throws IOException {
        byte[] peers = document.get("peers").getBytes();
        int nbPeers = peers.length / 6;
        String peersHex = TorrentUtils.bytesToHex(peers);
        int[] peersPorts = new int[nbPeers];

        int j = 0;
        String s5 = "";
        for (int i = 0; i < 2 * peers.length; i += 12) {
            s5 = new StringBuilder().append(peersHex.charAt(i + 8)).append(peersHex.charAt(i + 9))
                    .append(peersHex.charAt(i + 10)).append(peersHex.charAt(i + 11)).toString();
            peersPorts[j] = Integer.parseInt(s5, 16);
            j++;
        }
        return peersPorts;
    }

}
