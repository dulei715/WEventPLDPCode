package hnu.dll.utils.io;

import cn.edu.dll.io.read.BasicRead;

import java.util.List;

public class ListReadUtils {
    public static List<String> readAllDataList(String inputPath, String elementSplit) {
        BasicRead basicRead = new BasicRead(elementSplit);
        basicRead.startReading(inputPath);
        List<String> data = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        return data;
    }
}
