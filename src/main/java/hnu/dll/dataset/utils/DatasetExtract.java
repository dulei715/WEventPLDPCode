package hnu.dll.dataset.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;

import java.io.File;
import java.util.List;

public class DatasetExtract {
    public static void combineAndFilter(String basicPath, String directoryName, String outputFileName) {
        BasicWrite basicWrite = new BasicWrite();
        String directoryStr = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, directoryName);
        File directoryFile = new File(directoryStr);
        String[] fileNameArray = directoryFile.list();
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, outputFileName);
        List<String> tempList;
        basicWrite.startWriting(outputPath);
        for (int i = 1; i <= fileNameArray.length; i++) {
            tempList = CSVReadEnhanced.readStringData(StringUtil.join(ConstantValues.FILE_SPLIT, directoryStr, String.valueOf(i).concat(".txt")));
            basicWrite.writeStringListWithoutSize(tempList);
        }
        basicWrite.endWriting();
    }
    public static void filter(String basicPath, String directoryName) {
        BasicWrite basicWrite = new BasicWrite();
        String directoryStr = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, directoryName);
        String directoryFilterStr = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, directoryName+"_filter");
        File directoryFile = new File(directoryStr);
        String[] fileNameArray = directoryFile.list();
        String outputPath, tempFileName;
        List<String> tempList;
        for (int i = 1; i <= fileNameArray.length; i++) {
            tempFileName = String.valueOf(i).concat(".txt");
            tempList = CSVReadEnhanced.readStringData(StringUtil.join(ConstantValues.FILE_SPLIT, directoryStr, tempFileName));
            if (tempList.isEmpty()) {
                continue;
            }
            outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, directoryFilterStr, tempFileName);
            basicWrite.startWriting(outputPath);
            basicWrite.writeStringListWithoutSize(tempList);
            basicWrite.endWriting();
        }
    }

    public static void main(String[] args) {
        String basicPath = "E:\\1.学习\\4.数据集\\3.dataset_for_dynamic_w_event\\0_dataset\\T-drive Taxi Trajectories";
        String directoryName = "taxi_log_2008_by_id";
        String outputFileName = "combineData.csv";
//        combineAndFilter(basicPath, directoryName, outputFileName);
        filter(basicPath, directoryName);
    }

}
