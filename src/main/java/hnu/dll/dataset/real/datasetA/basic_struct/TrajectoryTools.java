package hnu.dll.dataset.real.datasetA.basic_struct;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TrajectoryTools {
    private static List<String> sampleData(List<String> inputList, Double ratio) {
       List<String> resultList = new ArrayList<>();
        for (String data : inputList) {
            if (RandomUtil.isChosen(ratio)) {
                resultList.add(data);
            }
        }
        return resultList;
    }
    public static void sampleData(String inputFilePathDir, String outputFilePathDir, Double ratio, int bufferSize) {
        File inputFileDirectory = new File(inputFilePathDir);
        File[] fileArray = inputFileDirectory.listFiles();
        BasicRead basicRead = new BasicRead();
        BasicWrite basicWrite = new BasicWrite();
        String outputFilePath;
        List<String> tempReadList, tempWriteList;
        for (File file : fileArray) {
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, outputFilePathDir, file.getName());
           basicRead.startReading(file.getAbsolutePath());
           basicWrite.startWriting(outputFilePath);
           while ((tempReadList = basicRead.readGivenLineSize(bufferSize)).size() >= bufferSize) {
               tempWriteList = sampleData(tempReadList, ratio);
               basicWrite.writeStringListWithoutSize(tempWriteList);
           }
           tempWriteList = sampleData(tempReadList, ratio);
           basicWrite.writeStringListWithoutSize(tempWriteList);
           basicRead.endReading();
           basicWrite.endWriting();
        }
    }


}
