package hnu.dll.utils.io;

import cn.edu.dll.io.write.BasicWrite;

import java.util.List;

public class ListWriteUtils {
    public static <T> void writeList(String outputPath, List<T> data, String elementSplit) {
        BasicWrite basicWrite = new BasicWrite(elementSplit);
        basicWrite.startWriting(outputPath);
        basicWrite.writeStringListWithoutSize(data);
        basicWrite.endWriting();
    }
}
