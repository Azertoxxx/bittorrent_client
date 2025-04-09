package Bencode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TestBencode {
    public static void main(String[] args) throws IOException {

        //Read the torrent file
        File torrentFile = new File(args[0]);
        FileInputStream inputStream = new FileInputStream(torrentFile);
        BDecoder reader = new BDecoder(inputStream);
        Map<String, BEncodedValue> document = reader.decodeMap().getMap();

        //extract info from the file
        String announce = document.get("announce").getString(); // Strings
        System.out.println("Announce :");
        System.out.println(announce);
        Map<String, BEncodedValue> info = document.get("info").getMap(); // Maps
        System.out.println("contenu du fichier torrent :");
        System.out.println(info);

        //System.out.println(info.get("pieces").getString());
        //System.out.println(info.get("piece length").getInt());

        //List<BEncodedValue> files = info.get("files").getList(); // Lists
        //System.out.println(files);

        if (document.containsKey("info")) {
            System.out.println("The info field exists");
        }

    }
}
