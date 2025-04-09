package utils.converter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ConvertFileToArray {
    byte[] dataArray;

    public ConvertFileToArray(File f){
        try {
            dataArray= FileUtils.readFileToByteArray(f);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public byte[] getDataArray() {
        return dataArray;
    }
}
