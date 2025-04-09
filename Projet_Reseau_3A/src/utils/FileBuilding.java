package utils;

import messages.Piece;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileBuilding {

    public static RandomAccessFile createFile(String name, Torrent torrent) throws IOException {
        RandomAccessFile file = new RandomAccessFile(name, "rw");
        file.setLength(torrent.getTotalLength());
        return file;
    }

    public static void writeBlockToTheRightPlace(RandomAccessFile file, Piece block, int offset) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(block.getBlock());
        byte[] blocks = outputStream.toByteArray();
        writeRAF(file, blocks, offset);
    }

    public static void writeRAF(RandomAccessFile file, byte [] array, int offset) throws IOException {
        file.seek(offset);
        file.write(array);
    }
}
