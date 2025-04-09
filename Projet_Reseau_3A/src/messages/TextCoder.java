package messages;

import java.io.IOException;

public interface TextCoder {
     byte[] toWire(Message msg) throws IOException;
     Bitfield fromWire(byte[] message) throws IOException;

}
