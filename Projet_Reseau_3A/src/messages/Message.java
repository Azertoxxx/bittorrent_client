package messages;

import java.io.*;

public abstract class Message {
     byte[] len = new byte[4];
     byte id = 0;

    public byte[] getLen() {
        return len;
    }

    public byte getId() {
        return id;
    }


    public abstract  byte[] toWire(Message msg) throws IOException;
    public abstract Message fromWire(byte[] message) throws IOException;
}
