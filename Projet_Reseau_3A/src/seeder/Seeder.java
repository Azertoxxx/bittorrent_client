package seeder;

import data.DataMap;
import messages.*;
import tracker.GetHttpRequest;
import utils.Torrent;
import utils.TorrentUtils;
import utils.converter.ConvertFileToArray;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

public class Seeder {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 2) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <fileName> <torrentName>");
        }

        String filename=args[0];
        String torrentfilename=args[1];
        SeederModel seederModel=new SeederModel();
        seederModel.setFileUpload(filename);
        seederModel.setFileTorrent(torrentfilename);

        //Communicaion avec le tracker
        GetHttpRequest req = new GetHttpRequest(seederModel.getFileTorrent());

        //calculate nbPiece & nbBlock
        int nbPiece=seederModel.calculNbPiece();

        //calculate & map data
        DataMap dataMap= new DataMap();
        int pieceLen=seederModel.getFileTorrent().getPieceLength();
        File f=seederModel.getFileUpload();
        dataMap.splitFile(f,pieceLen,nbPiece);

        //calculate bitfield based on torrent.fulllength, pieceLen
        Bitfield bitfield=new Bitfield(nbPiece);


        HandshakeCoder coder = new HandshakeCoder();
        byte[] encodedMsg;


        while (true) {
            Socket clntSock = seederModel.getServSocket().accept();    //listen for connection & accept
            OutputStream out = clntSock.getOutputStream();
            System.out.println("Handling client at " + clntSock.getRemoteSocketAddress());
            // Change Length to Delim for a different framing strategy
            LengthFramer framer = new LengthFramer(clntSock.getInputStream());

            //receive handshake
            System.out.println("Receive handshake");
            encodedMsg = framer.nextMsgHandshake();
            Handshake handshakeMsg = coder.fromWire(encodedMsg);


            //send handshake
            System.out.println("Sending handshake");
            String info_hash_received = handshakeMsg.getInfo_hash();
            String peer_id = seederModel.getMyPeer().getPeer_id();
            Handshake handshakeSend = new Handshake(info_hash_received,peer_id);
            encodedMsg = coder.toWire(handshakeSend);
            framer.frameMsg(encodedMsg, out);

            //send bitfield
            System.out.println("Send bitfield");
            Bitfield bitfieldTextCoder=new Bitfield();
            byte[] fieldByte= bitfieldTextCoder.toWire(bitfield);
            framer.frameMsg(fieldByte,out);

            //receive bitfield
            System.out.println("Receive bitfield");
            byte[] fieldByteRes = framer.nextMsg();

            int pieceToSend=nbPiece;

            //receive interested
            System.out.println("Receive interested");
            Intersted interstedTextCoder = new Intersted();
            byte[] interestEncoded = framer.nextMsg();
            interstedTextCoder.fromWire(interestEncoded);

            //test receive and unchoke if client is interested
            seederModel.getMyPeer().setPeer_interested(1);
            seederModel.getMyPeer().setAm_choking(0);
            seederModel.getMyPeer().setPeer_choking(0);
            seederModel.getOtherPeer().setAm_interested(1);

            //send unchoke if not occupied &&client interested
            if(!seederModel.getMyPeer().isBusy() && seederModel.getOtherPeer().isInterested()){

                System.out.println("Send unchoke");
                Unchoke unchokeMsg = new Unchoke();
                Unchoke unchokeTextCoder = new Unchoke();
                byte[] unchokeEncoded = unchokeTextCoder.toWire(unchokeMsg);
                framer.frameMsg(unchokeEncoded, out);

            }else {
                //send choke if occcupied
                System.out.println("Send choke");
                Choke chokeMsg = new Choke();
                Choke chokeTextCoder = new Choke();
                byte[] chokeEncoded = chokeTextCoder.toWire(chokeMsg);
                framer.frameMsg(chokeEncoded, out);
                seederModel.getMyPeer().setPeer_choking(1);
            }

            MessageModel msgModel= framer.nextMsg2();
            while(!msgModel.isNotInterested()&& pieceToSend!=0){
                switch(msgModel.getType()){
                    case "Request":
                        //receive request
                        System.out.println("Receive request");
                        Request requestTextCoder= new Request();
                        Request request=requestTextCoder.fromWire(msgModel.getMsg());

                        //recuperer donnees de request
                        int index=TorrentUtils.fromByteArray(request.getIndex());
                        int length=TorrentUtils.fromByteArray(request.getLenght());
                        int begin=TorrentUtils.fromByteArray(request.getBegin());
                        byte[] beginByte=request.getBegin();
                        byte[] lenByte=request.getLenght();
                        byte[] indexByte=request.getIndex();

                        //creer requete Piece pour un block
                        System.out.println("Send block");
                        byte[] byteToSend=dataMap.getBlock(index,length,begin);
                        Piece pieceMsg=new Piece(indexByte,beginByte,byteToSend);
                        Piece pieceTextCoder = new Piece();
                        byte[] pieceEncoded = pieceTextCoder.toWire(pieceMsg);
                        framer.frameMsg(pieceEncoded, out);
                        msgModel= framer.nextMsg2();
                        break;

                    case "Have":
                        System.out.println("Receive have");
                        pieceToSend--;
                        msgModel= framer.nextMsg2();
                        break;

                    case "Cancel":
                        System.out.println("Receive cancel");
                        pieceToSend++;
                        msgModel= framer.nextMsg2();
                        break;

                    default:
                        System.out.println("Type of message is " + msgModel.getType());
                        break;
                }
            }

            //receive not interested
            NotInterested notInterestedTextCoder= new NotInterested();
            byte[] notInterestedEncoded = framer.nextMsg();
            NotInterested notInterested=notInterestedTextCoder.fromWire(notInterestedEncoded);

        }
    }

}

