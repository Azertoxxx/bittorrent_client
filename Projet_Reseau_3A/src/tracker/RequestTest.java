package tracker;

import utils.Torrent;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class RequestTest {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Torrent torrent = new Torrent(args[0]);

        String s = torrent.getInfo_hash();
        System.out.println(s);

        String announce = torrent.getAnnounce();
        System.out.println("Announce : ");
        System.out.println(announce);

        String info_hash = torrent.getInfo_hash();
        System.out.println("info_hash : " + info_hash);

        //String peer_id = torrent.getPeer_id();
        //System.out.println("peer_id : " + peer_id);

        GetHttpRequest req = new GetHttpRequest(torrent);
    }
}
