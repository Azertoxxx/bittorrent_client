package leecher;

import utils.Peer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PieceManager {
    private List<Integer> requested;
    private List<Integer> downloaded;
    private int nbPieceTotal;

    public PieceManager(int nbPieceTotal){
        requested=new ArrayList<>();
        downloaded=new ArrayList<>();
        this.nbPieceTotal=nbPieceTotal;
    }

    /**
     * Cette fonction calcul la piece suivant a telecharger. Il est a utiliser a chaque reception de requete Have.
     * Il met a jour la liste de piece recu de Peer courant
     * @param currentPeer : le peer qui a recu Have
     * @param otherPeer   : le peer qui a envoyer Have
     * @param pieceReceived: index de Piece recu
     * @return int:index de piece suivant a telecharger
     * */
    public int nextPieceToRequest(Peer currentPeer,int pieceReceived,Peer otherPeer){
        currentPeer.getPieceHave().add(pieceReceived);
        Iterator it=otherPeer.getPieceHave().iterator(); //get all indexPiece that otherPeer has
        int pieceToRequest= (int) it.next(); //initiliaze first element
        while(requested.contains(pieceToRequest) && it.hasNext() ) { //while the next piece is already requested && has piece
            pieceToRequest= (int) it.next(); //get next piece
        }
        if(pieceToRequest==pieceReceived && requested.contains(pieceToRequest)){ //verify if piece already requested -> no more can be done
            return -1;
        }else{
            requested.add(pieceToRequest);
            return pieceToRequest;
        }
    }



    public boolean requestDone(){
        if(requested.size()!=nbPieceTotal){
            return false;
        }
        return true;
    }

    public List<Integer> getRequested() {
        return requested;
    }

    public void setRequested(List<Integer> requested) {
        this.requested = requested;
    }

    public List<Integer> getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(List<Integer> downloaded) {
        this.downloaded = downloaded;
    }

    public int getNbPieceTotal() {
        return nbPieceTotal;
    }

    public void setNbPieceTotal(int nbPieceTotal) {
        this.nbPieceTotal = nbPieceTotal;
    }
}
