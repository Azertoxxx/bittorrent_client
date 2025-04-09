package leecher;

import messages.*;

import Bencode.BEncodedValue;
import utils.Peer;
import tracker.Tracker;

import utils.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static utils.TorrentUtils.createRandomList;
import static utils.TorrentUtils.getFromRandomList;

public class LeecherMultiClients {

    private static final int TIMEOUT = 3000; // Wait timeout (milliseconds)
    private static final int BUFSIZE = 256;  // Buffer size (bytes)

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        Torrent torrent = new Torrent(args[0]); // args[0] : path of the .torrent file

        //Creation of the file
        RandomAccessFile file = FileBuilding.createFile("downloaded_"+torrent.getName(), torrent);
        //File file = new File(/*args[1]*/"downloaded_" + torrent.getName()); // arg[1] : path where you want to download

        byte[] length = null ;
        byte[] index  = null ;
        byte[] begin  = null ;

        String info_hash = torrent.getInfo_hash();
        String peer_id = torrent.getPeer_id();

        int nbPiece = TorrentUtils.getNbPiece(torrent);
        System.out.println("nombre de pieces : "+nbPiece);
        int nbBlock = TorrentUtils.getNbBlock(torrent);
        System.out.println("nombre de blocks par pièce : "+nbBlock);

        Piece[] pieces = new Piece[nbPiece*nbBlock];
        // randIndex est une ArrayList de nombre aléatoire entre 0 et nbPiece-1 . A lire dans l'ordre
        HashSet<Integer> randIndex = createRandomList(nbPiece);
       // HashMap<Piece,Integer> pieceReceivedInDisorder = new HashMap<>();

        //Request GET
        Tracker tracker = new Tracker(torrent);
        String urlString = tracker.generateUrlString(true);
        Map<String, BEncodedValue> document = tracker.requestHttpGet(urlString);

        //Lists of IPs and Ports of Peers
        String[] peersIp = tracker.getPeersIP(document); // the first one should be our client, so we don't take it
        int[] peersPorts = tracker.getPeersPorts(document);

        //CurrentPeer
        Peer currentPeer = new Peer(peer_id);

        // Create a selector to multiplex listening sockets and connections
        Selector selector = Selector.open();

        // Create listening socket channel for each port and register selector
        for (int i = 1; i < peersPorts.length; i++) {
            SocketChannel clntChan = SocketChannel.open();
            clntChan.configureBlocking(false);
            clntChan.register(selector, SelectionKey.OP_CONNECT);
            //launch a connection to a peer
            clntChan.connect(new InetSocketAddress(peersIp[i], peersPorts[i]));
        }
        String protocolCase = null;
        int iPieceRequest=0;
        int jPieceRequest=0;

        while (true)
        { // Run forever, processing available I/O operations
            // Wait for some channel to be ready (or timeout)
            if (selector.select(3000) == 0) { // returns # of ready chans
                System.out.print(".");
                continue;
            }
            // Get iterator on set of keys with I/O to process
            Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

            while (keyIter.hasNext()) {
                SelectionKey key = keyIter.next(); // Key is bit mask
                Peer peer = (Peer) key.attachment();
                ByteBuffer readBuf = ByteBuffer.allocate(65536);
                readBuf.clear();
                Integer offset = 0;

                if (key.isConnectable()) {

                    SocketChannel clntChan = (SocketChannel) key.channel(); //Finishes the process of connecting a socket channel
                    clntChan.finishConnect(); // Faire des choses une fois connecté (genre envoyer un HS par exemple ou ...)

                    /** HANDSHAKE */
                    HandshakeCoder coder = new HandshakeCoder();
                    Handshake handshakeMsg = new Handshake(info_hash, peer_id);
                    byte[] encodedMsg = coder.toWire(handshakeMsg);
                    ByteBuffer writeBuf = ByteBuffer.wrap(encodedMsg);
                    clntChan.write(writeBuf);

                    protocolCase = "ReceptionHandshake";
                    key.interestOps(SelectionKey.OP_READ);
                }
                if (key.isReadable()) {
                    /*
                    * RECEPTION DE MESSAGE EN ATTENTE (par un peer)
                    * */
                    SocketChannel clntChan = (SocketChannel) key.channel();

                        switch (protocolCase) {

                            case "ReceptionHandshake":

                                            /** RECEPTION HANDSHAKE */
                                            byte[] encodedMsg = TorrentUtils.ReceptionMessage(clntChan,readBuf);
                                            HandshakeCoder coder = new HandshakeCoder();
                                            Handshake handshakeMsg = coder.fromWire(encodedMsg);

                                            /** RECEPTION BITFIELD */
                                            byte[] encodedMsg2 = TorrentUtils.ReceptionMessage(clntChan,readBuf);
                                            Bitfield coder2 = new Bitfield();
                                            Bitfield bitfieldMsg = coder2.fromWire(encodedMsg2);

                                            /** ATTACHE DES PIECES AU PEER */
                                            BitFieldManager bitFieldManager = new BitFieldManager(bitfieldMsg, nbPiece);
                                            //peer.setPieceHave(bitFieldManager.convertArrayToList(bitfieldMsg.getBitfield()));
                                            //System.out.println("[BT RECEPTION] : " + TorrentUtils.bytesToHex(readBuf.array()));
                                            protocolCase = "SendInterested";
                                            break;


                            case "ReceptionUnchoke":

                                            /** RECEPTION UNCHOKE */
                                            byte[] encodedMsg4 = TorrentUtils.ReceptionMessage(clntChan,readBuf);
                                            Unchoke coder4 = new Unchoke();
                                            coder4.fromWire(encodedMsg4);

                                            /** GESTION TYPE DE REQUÊTE (gros fichier ou petit fichier) */
                                            if (torrent.getTotalLength() <= torrent.getPieceLength()) {
                                                protocolCase = "SmallFileRequest";
                                                length = TorrentUtils.hexStringToByteArray(String.format("%08X", torrent.getTotalLength()));
                                                index = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));
                                                begin = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));
                                            } else {
                                                protocolCase = "BigFileRequestSend";
                                            }
                                            break;


                            case "SmallFileGetPiece":

                                            /** RECEPTION PIECE */
                                            byte[] encodedMsg6 = TorrentUtils.ReceptionMessage(clntChan,readBuf);
                                            Piece coder6 = new Piece();
                                            Piece pieceMsg = coder6.fromWire(encodedMsg6);

                                            /** INITIALISATION FILE */
                                            pieces[0] = pieceMsg;
                                            FileBuilding.writeRAF(file,pieces[0].getBlock(),0);
                                            protocolCase = "SmallFileHave";

                                            break;

                            case "BigFileRequestGet":

                                            length = TorrentUtils.hexStringToByteArray(String.format("%08X", 16384));
                                            begin = TorrentUtils.hexStringToByteArray(String.format("%08X", jPieceRequest*16384));
                                            Integer randomI = getFromRandomList(randIndex,iPieceRequest);

                                            /** RECEPTION PIECE */
                                            byte[] encodedMsg6bf = TorrentUtils.ReceptionMessage(clntChan,readBuf);
                                            Piece coder6bf = new Piece();
                                            Piece pieceMsgbf = coder6bf.fromWire(encodedMsg6bf);

                                            FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, jPieceRequest*16384 + randomI*torrent.getPieceLength());
                                            jPieceRequest++;

                                            protocolCase = "BigFileRequestSend";
                                            break;
                            case "closeFile":

                                            /** Fermeture du fichier */
                                             //file.close();
                                            protocolCase = "SendNotInterested";
                                            break;
                            default:
                                            break;
                        }
                    key.interestOps(SelectionKey.OP_WRITE);
                }

                    if (key.isValid() && key.isWritable()) {

                        /** SOCKET DISPONIBLE POUR ECRITURE - CHANNEL OUVERTE */
                        SocketChannel clntChan = (SocketChannel) key.channel();

                        /*** SWITCH CASE - PARTIE SEND ***/

                        switch (protocolCase) {
                            case "SendInterested":

                                        Intersted interstedMsg = new Intersted();
                                        byte[] encodedMsg3 = interstedMsg.toWire(interstedMsg);
                                        ByteBuffer writeBuf = ByteBuffer.wrap(encodedMsg3);
                                        clntChan.write(writeBuf);
                                        protocolCase = "ReceptionUnchoke";
                                        break;

                            case "SmallFileRequest":

                                        Request requestMsg = new Request(length, index, begin);
                                        byte[] encodedMsg5 = requestMsg.toWire(requestMsg);
                                        //framer.frameMsg(encodedMsg5, out);
                                        ByteBuffer writeBuf1 = ByteBuffer.wrap(encodedMsg5);
                                        clntChan.write(writeBuf1);
                                        protocolCase = "SmallFileGetPiece";
                                        break;

                            case "SmallFileHave":

                                        Have haveMsg = new Have(index);
                                        byte[] encodedMsg7 = haveMsg.toWire(haveMsg);
                                        ByteBuffer writeBuf7 = ByteBuffer.wrap(encodedMsg7);
                                        clntChan.write(writeBuf7);
                                        protocolCase = "SendNotInterested";
                                        break;

                            case "BigFileRequestSend":

                                        int counter = 0; //indice pour se déplacer dans pièces
                                        Integer randomI = getFromRandomList(randIndex, iPieceRequest); // indice de pièce aléatoire

                                        if(iPieceRequest<nbPiece) {
                                            index = TorrentUtils.hexStringToByteArray(String.format("%08X", randomI));

                                            if (iPieceRequest != nbPiece - 1) {
                                                // Envoi de blocks de 2*4000
                                                if (jPieceRequest < nbBlock) {
                                                    //System.out.println("[CAS NORMAL - non fini]");
                                                    TorrentUtils.SendMessage("REQUEST",clntChan, TorrentUtils.hexStringToByteArray(String.format("%08X", 16384)), index, TorrentUtils.hexStringToByteArray(String.format("%08X", jPieceRequest * 16384)) );
                                                    protocolCase = "BigFileRequestGet";
                                                    break;
                                                }
                                                else {
                                                    //System.out.println("[CAS NORMAL - Have Piece d'index : (" + index + ") ] ");
                                                    jPieceRequest = 0;
                                                    iPieceRequest++;
                                                    TorrentUtils.SendMessage("HAVE",clntChan, null, index, null);
                                                    protocolCase = "BigFileRequestSend";
                                                    break;
                                                }
                                            }
                                            else {
                                                // ***** DERNIERE PIECE ENTIERE ********* //
                                                if (torrent.getTotalLength() == nbPiece * torrent.getPieceLength()) { // dernière pièçe entière
                                                    if (jPieceRequest < nbBlock) {
                                                        //System.out.println("[DERNIERE PIECE ENTIERE - dernière pièce - non fini]");
                                                        TorrentUtils.SendMessage("REQUEST",clntChan, TorrentUtils.hexStringToByteArray(String.format("%08X", 16384)), index, TorrentUtils.hexStringToByteArray(String.format("%08X", jPieceRequest * 16384)) );
                                                        protocolCase = "BigFileRequestGet";
                                                        break;

                                                    } else {
                                                        //System.out.println("[DERNIERE PIECE ENTIERE - Have Piece d'index : (" + index + ") ] ");
                                                        jPieceRequest = 0;
                                                        iPieceRequest++;
                                                        TorrentUtils.SendMessage("HAVE",clntChan, null, index, null);
                                                        protocolCase = "closeFile";
                                                        break;
                                                    }


                                                }
                                                // *********** DERNIERE PIECE PLUS PETITE QU'UN BLOCK ************ //
                                                else if (torrent.getTotalLength() - (nbPiece - 1) * torrent.getPieceLength() <= 16384) { // dernière pièce plus petite qu'un block


                                                    if (jPieceRequest < nbBlock) {
                                                        //System.out.println("[DERNIERE PIECE PLUS PETITE QU'UN BLOCK - dernière pièce - non fini]");
                                                        TorrentUtils.SendMessage("REQUEST",clntChan, TorrentUtils.hexStringToByteArray(String.format("%08X", torrent.getTotalLength() - (nbPiece - 1) * torrent.getPieceLength())), index,TorrentUtils.hexStringToByteArray(String.format("%08X", 0)));
                                                        protocolCase = "BigFileRequestGet";
                                                        break;

                                                    } else {
                                                        //System.out.println("[DERNIERE PIECE PLUS PETITE QU'UN BLOCK - Have derniere Piece d'index : (" + index + ") ] ");
                                                        jPieceRequest = 0;
                                                        iPieceRequest++;
                                                        TorrentUtils.SendMessage("HAVE",clntChan, null, index, null);
                                                        protocolCase = "closeFile";
                                                        break;
                                                    }

                                                }
                                                /****** DERNIERE PIECE PLUS GRAND QU'UN BLOCK MAIS PAS DE 2 BLOCKS **************/
                                                else if (torrent.getTotalLength() - (nbPiece - 1) * torrent.getPieceLength() > 16384
                                                        && torrent.getTotalLength() - (nbPiece - 1) * torrent.getPieceLength() < torrent.getPieceLength()) { // dernière pièce qui fait plus d'un block mais pas 2


                                                    if (jPieceRequest < nbBlock) {
                                                        //System.out.println(" [DERNIERE PIECE PLUS GRAND QU'UN BLOCK MAIS PAS DE 2 BLOCKS - dernière pièce plus grand qu'un block mais < 2 blocks - non fini]");
                                                        TorrentUtils.SendMessage("REQUEST",clntChan, TorrentUtils.hexStringToByteArray(String.format("%08X", 16384)), index,TorrentUtils.hexStringToByteArray(String.format("%08X", 0)));
                                                        protocolCase = "BigFileRequestGet";
                                                        break;

                                                    } else {
                                                        //System.out.println("[DERNIERE PIECE PLUS GRAND QU'UN BLOCK MAIS PAS DE 2 BLOCKS - Have derniere Piece + grande qu'un block mais < à 2 blocks  d'index : (" + index + ") ] ");
                                                        jPieceRequest = 0;
                                                        iPieceRequest++;
                                                        TorrentUtils.SendMessage("HAVE",clntChan, null, index, null);
                                                        protocolCase = "closeFile";
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        else{
                                            protocolCase = "closeFile";
                                            break;
                                        }


                                break;
                            case "closeFile":
                               // file.close();
                                protocolCase = "SendNotInterested";
                                break;
                            case "SendNotInterested":
                                NotInterested notInterestedMsg = new NotInterested();
                                byte[] encodedMsg8 = notInterestedMsg.toWire(notInterestedMsg);
                                ByteBuffer writeBuf8 = ByteBuffer.wrap(encodedMsg8);
                                clntChan.write(writeBuf8);
                                protocolCase = "null"; // on sors après
                                break;
                            default: break;
                        }


                        //j'aimerai qu'on m'avertisse au prochain select si des données sont arrivées
                        //sur cette socket
                        if(protocolCase != "SendNotInterested" && protocolCase != "BigFileRequestSend")
                        {
                            key.interestOps(SelectionKey.OP_READ);
                        }else if(protocolCase == "BigFileRequestSend") {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }else{
                            key.cancel();
                            //socketChannel.close();
                            //return;
                        }
                    }
                    keyIter.remove(); // remove from set of selected keys

            }
        }
    }
}
