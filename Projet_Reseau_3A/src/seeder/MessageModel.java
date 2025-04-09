package seeder;

import java.io.EOFException;
import java.io.IOException;

public class MessageModel {
    byte[] msg;
    private String type;

    public MessageModel(int length){
        msg=new byte[length];
        type="inconnu";
    }
    public MessageModel(int length, byte[] msg){
        this.msg=new byte[length];
        this.msg=msg;
        type=determineType();

    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
    public boolean isNotInterested() throws IOException {
        int val=Byte.compare(msg[0],(byte)3);
        boolean res=  val==0;
        return res;
    }

    public boolean isInterested() throws IOException {
        int val=Byte.compare(msg[0],(byte)2);
        boolean res=  val==0;
        return res;

    }

    public boolean isType(byte msgId) throws IOException {
        int val=Byte.compare(msg[0],msgId);
        return val==0;

    }

    public boolean isRequest() throws IOException {
        int val=Byte.compare(msg[0],(byte)6);
        boolean res=  val==0;
        return res;

    }

    public boolean isCancel() throws IOException {
        int val=Byte.compare(msg[0],(byte)8);
        boolean res=  val==0;
        return res;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String determineType(){
        byte msgId=msg[0];
        String res="inconnu";
        if(Byte.compare(msgId,(byte)3)==0){res= "NotInterested";}
        if(Byte.compare(msgId,(byte)6)==0){res= "Request";}
        if(Byte.compare(msgId,(byte)2)==0){res= "Interested";}
        if(Byte.compare(msgId,(byte)8)==0){res= "Cancel";}
        if(Byte.compare(msgId,(byte)4)==0){res= "Have";}
        if(Byte.compare(msgId,(byte)0)==0){res= "Choke";}
        if(Byte.compare(msgId,(byte)1)==0){res= "Unchoke";}
        return res;
    }

}
