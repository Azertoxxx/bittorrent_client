package leecher;

import Bencode.BEncodedValue;
import messages.*;
import tracker.Tracker;
import utils.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static utils.TorrentUtils.createRandomList;
import static utils.TorrentUtils.getFromRandomList;

public class LeecherMonoClient {
    public static void main(String[] args) throws Exception {
//        if (args.length != 3) { // Test for correct # of args
//            throw new IllegalArgumentException("Parameter(s): <Server> <Port> <Torrent>");
//        }
        Torrent torrent = new Torrent(args[0]);

        //Request GET
        Tracker tracker = new Tracker(torrent);
        String urlString = tracker.generateUrlString(true);
        Map<String, BEncodedValue> document = tracker.requestHttpGet(urlString);

        //Lists of IPs and Ports of Peers
        String[] peersIp = tracker.getPeersIP(document); // the first one should be our client, so we don't take it
        int[] peersPorts = tracker.getPeersPorts(document);

        //String destAddr = args[0]; // Destination address
        //int destPort = Integer.parseInt(args[1]); // Destination port

        String destAddr = peersIp[1]; // Destination address
        int destPort = peersPorts[1]; // Destination port

        Socket sock = new Socket(destAddr, destPort);
        OutputStream out = sock.getOutputStream();

        HandshakeCoder coder = new HandshakeCoder();
        LengthFramer framer = new LengthFramer(sock.getInputStream());

        String info_hash = torrent.getInfo_hash();
        String peer_id = torrent.getPeer_id();

        boolean endOfProtocol = false;
        String protocolCase = "SendHandshake";

        int nbPiece = TorrentUtils.getNbPiece(torrent);
        int nbBlock = TorrentUtils.getNbBlock(torrent);
        System.out.println("totalLength : "+torrent.getTotalLength());
        System.out.println("pieceLength : "+torrent.getPieceLength());
        System.out.println("nbPiece : "+nbPiece);
        System.out.println("nbBlock : "+nbBlock);
        Piece[] pieces = new Piece[nbPiece*nbBlock];

        //Creation of the file
        RandomAccessFile file = FileBuilding.createFile("downloaded_"+torrent.getName(), torrent);

        byte[] length = null ;
        byte[] index  = null ;
        byte[] begin  = null ;

        // randIndex est une ArrayList de nombre aléatoire entre 0 et nbPiece-1 . A lire dans l'ordre
        HashSet<Integer> randIndex = createRandomList(nbPiece);
        //HashMap<Piece,Integer> pieceReceivedInDisorder = new HashMap<>();
        while (endOfProtocol != true){

            switch(protocolCase){

                case "SendHandshake" :
                                            Handshake handshakeMsg = new Handshake(info_hash, peer_id);
                                            byte[] encodedMsg = coder.toWire(handshakeMsg);
                                            framer.frameMsg(encodedMsg, out);
                                            //System.out.println("handshake msg send : ");
                                            //System.out.println(handshakeMsg);
                                            protocolCase="ReceptionHandshake";
                                            break;
                case "ReceptionHandshake":  encodedMsg = framer.nextMsgHandshake();
                                            handshakeMsg = coder.fromWire(encodedMsg);
                                            //System.out.println("handshakeMsg : ");
                                            //System.out.println(handshakeMsg.toString());
                                            protocolCase="ReceptionBitfield";
                                            break;
                case "ReceptionBitfield":   Bitfield coder2 = new Bitfield();
                                            byte[] encodedMsg2 = framer.nextMsg();
                                            Bitfield bitfieldMsg = coder2.fromWire(encodedMsg2);
                                            protocolCase="SendInterested";
                                            break;
                case "SendInterested":      Intersted interstedMsg = new Intersted();
                                            byte[] encodedMsg3 = interstedMsg.toWire(interstedMsg);
                                            framer.frameMsg(encodedMsg3, out);
                                            protocolCase="ReceptionUnchoke";
                                            break;
                case "ReceptionUnchoke":    Unchoke coder4 = new Unchoke();
                                            byte[] encodedMsg4 = framer.nextMsg();
                                            coder4.fromWire(encodedMsg4);
                                            if(torrent.getTotalLength()<= torrent.getPieceLength()){
                                                protocolCase = "SmallFileRequest";
                                                length = TorrentUtils.hexStringToByteArray(String.format("%08X", torrent.getTotalLength()));
                                                index = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));
                                                begin = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));
                                            }
                                            else{
                                                protocolCase = "BigFileRequest";
                                            }
                                            break;
                case "SmallFileRequest" :   Request requestMsg = new Request(length,index,begin);
                                            byte[] encodedMsg5 = requestMsg.toWire(requestMsg);
                                            framer.frameMsg(encodedMsg5, out);

                                            Piece coder6 = new Piece();
                                            byte[] encodedMsg6 = framer.nextMsg();
                                            Piece pieceMsg = coder6.fromWire(encodedMsg6);
                                            pieces[0]=pieceMsg;
                                            FileBuilding.writeRAF(file,pieces[0].getBlock(),0);
                                            

                                            Have haveMsg = new Have(index);
                                            byte[] encodedMsg7 = haveMsg.toWire(haveMsg);
                                            framer.frameMsg(encodedMsg7,out);
                                            protocolCase = "SendNotInterested";
                                            break;
                case"BigFileRequest" :

                                            int counter = 0; //indice pour se déplacer dans pièces
                                            for(int i=0; i<nbPiece; i++){
                                                Integer randomI = getFromRandomList(randIndex,i);
                                                index = TorrentUtils.hexStringToByteArray(String.format("%08X", randomI));

                                                if(randomI != nbPiece-1){
                                                    // Envoi de blocks de 2*4000

                                                    for(int j=0; j<nbBlock; j++){
                                                        length = TorrentUtils.hexStringToByteArray(String.format("%08X", 16384));
                                                        begin = TorrentUtils.hexStringToByteArray(String.format("%08X", j*16384));

                                                        Request requestMsgbf = new Request(length,index,begin);
                                                        byte[] encodedMsg5bf = requestMsgbf.toWire(requestMsgbf);
                                                        framer.frameMsg(encodedMsg5bf, out);


                                                        Piece coder6bf = new Piece();
                                                        byte[] encodedMsg6bf = framer.nextMsg();
                                                        Piece pieceMsgbf = coder6bf.fromWire(encodedMsg6bf);

                                                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                                        outputStream.write(pieceMsgbf.getBlock());
                                                        byte[] blocks = outputStream.toByteArray();
                                                        //System.out.println("piece length "+torrent.getPieceLength()+" block length"+blocks.length+" ");
                                                        //System.out.println("offset: "+ (j*16384 + randomI*torrent.getPieceLength()));

                                                        FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, j*16384 + randomI*torrent.getPieceLength());
                                                    }

                                                    Have haveMsgbf = new Have(index);
                                                    byte[] encodedMsg7bf = haveMsgbf.toWire(haveMsgbf);
                                                    framer.frameMsg(encodedMsg7bf,out);
                                                }
                                                else{
                                                    if(torrent.getTotalLength() == nbPiece* torrent.getPieceLength()){ // dernière pièçe entière
                                                        for(int j=0; j<nbBlock; j++){
                                                            length = TorrentUtils.hexStringToByteArray(String.format("%08X", 16384));
                                                            begin = TorrentUtils.hexStringToByteArray(String.format("%08X", j*16384));

                                                            Request requestMsgbf = new Request(length,index,begin);

                                                            byte[] encodedMsg5bf = requestMsgbf.toWire(requestMsgbf);
                                                            framer.frameMsg(encodedMsg5bf, out);


                                                            Piece coder6bf = new Piece();
                                                            byte[] encodedMsg6bf = framer.nextMsg();
                                                            Piece pieceMsgbf = coder6bf.fromWire(encodedMsg6bf);

                                                            FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, j*16384 + randomI*torrent.getPieceLength());

                                                        }

                                                        Have haveMsgbf = new Have(index);

                                                        byte[] encodedMsg7bf = haveMsgbf.toWire(haveMsgbf);
                                                        framer.frameMsg(encodedMsg7bf,out);

                                                    }
                                                    else if(torrent.getTotalLength() - (nbPiece-1) * torrent.getPieceLength()<= 16384){ // dernière pièce plus petite qu'un block
                                                        length = TorrentUtils.hexStringToByteArray(String.format("%08X", torrent.getTotalLength() - (nbPiece-1) * torrent.getPieceLength()));
                                                        begin = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));

                                                        Request requestMsgbf = new Request(length,index,begin);

                                                        byte[] encodedMsg5bf = requestMsgbf.toWire(requestMsgbf);
                                                        framer.frameMsg(encodedMsg5bf, out);

                                                        Piece coder6bf = new Piece();
                                                        byte[] encodedMsg6bf = framer.nextMsg();
                                                        Piece pieceMsgbf = coder6bf.fromWire(encodedMsg6bf);

                                                        FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, randomI*torrent.getPieceLength());

                                                        Have haveMsgbf = new Have(index);

                                                        byte[] encodedMsg7bf = haveMsgbf.toWire(haveMsgbf);
                                                        framer.frameMsg(encodedMsg7bf,out);
                                                    }
                                                    else if(torrent.getTotalLength() - (nbPiece-1) * torrent.getPieceLength() > 16384
                                                            && torrent.getTotalLength() - (nbPiece-1) * torrent.getPieceLength() < torrent.getPieceLength()){ // dernière pièce qui fait plus d'un block mais pas 2
                                                        length = TorrentUtils.hexStringToByteArray(String.format("%08X", 16384));
                                                        begin = TorrentUtils.hexStringToByteArray(String.format("%08X", 0));

                                                        Request requestMsgbf = new Request(length,index,begin);

                                                        byte[] encodedMsg5bf = requestMsgbf.toWire(requestMsgbf);
                                                        framer.frameMsg(encodedMsg5bf, out);

                                                        Piece coder6bf = new Piece();
                                                        byte[] encodedMsg6bf = framer.nextMsg();
                                                        Piece pieceMsgbf = coder6bf.fromWire(encodedMsg6bf);

                                                        FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, randomI*torrent.getPieceLength());

                                                        length = TorrentUtils.hexStringToByteArray(String.format("%08X", torrent.getTotalLength() - (nbPiece-1) * torrent.getPieceLength() - 16384));
                                                        begin = TorrentUtils.hexStringToByteArray(String.format("%08X", 16384));

                                                        requestMsg = new Request(length,index,begin);

                                                        encodedMsg5 = requestMsg.toWire(requestMsg);
                                                        framer.frameMsg(encodedMsg5, out);

                                                        coder6 = new Piece();
                                                        encodedMsg6 = framer.nextMsg();
                                                        pieceMsg = coder6.fromWire(encodedMsg6);

                                                        FileBuilding.writeBlockToTheRightPlace(file, pieceMsgbf, 16384 + randomI*torrent.getPieceLength());

                                                        Have haveMsgbf = new Have(index);

                                                        byte[] encodedMsg7bf = haveMsgbf.toWire(haveMsgbf);
                                                        framer.frameMsg(encodedMsg7bf,out);
                                                    }
                                                }

                                            }

                                            protocolCase = "closeFile";
                                            break;

                case "closeFile":           file.close();
                                            protocolCase = "SendNotInterested";
                                            break;
                case "SendNotInterested" :  NotInterested notInterestedMsg = new NotInterested();


                                            byte[] encodedMsg8 = notInterestedMsg.toWire(notInterestedMsg);
                                            framer.frameMsg(encodedMsg8, out);
                                            endOfProtocol = true;
                                            break;
                default:break;

            }


        }

        sock.close();
    }
}
