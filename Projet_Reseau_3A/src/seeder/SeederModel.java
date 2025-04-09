package seeder;


import Bencode.InvalidBEncodingException;
import utils.Peer;
import utils.Torrent;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class SeederModel {
    private int port;
    private ServerSocket servSocket;
    private Peer myPeer;
    private Peer otherPeer;
    private File fileUpload;
    private Torrent fileTorrent;

    public SeederModel() throws IOException {
        this.servSocket=new ServerSocket(6881);
        this.myPeer=new Peer();
        myPeer.setPeer_choking(0);
        otherPeer=new Peer();
    }

    public void setFileUpload(File fileUpload) {
        this.fileUpload = fileUpload;
    }

    public void setFileUpload(String filename)  { fileUpload=new File(filename);
    }

    public void setFileTorrent(String torrentName){
        try {
            this.fileTorrent=new Torrent(torrentName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Peer getMyPeer() {
        return myPeer;
    }

    public int calculNbPiece() throws InvalidBEncodingException {
        int nbPiece = (int) (fileTorrent.getTotalLength()/ fileTorrent.getPieceLength()
                + (fileTorrent.getTotalLength()%fileTorrent.getPieceLength()==0 ? 0 :1)); // number of pieces
//        int nbBlock = (int) ((fileTorrent.getPieceLength()) / 16384);
        return nbPiece;
    }

    public File getFileUpload() {
        return fileUpload;
    }

    public Torrent getFileTorrent() {
        return fileTorrent;
    }

    public ServerSocket getServSocket() {
        return servSocket;
    }

    public Peer getOtherPeer() {
        return otherPeer;
    }

    public void setOtherPeer(Peer otherPeer) {
        this.otherPeer = otherPeer;
    }
}
