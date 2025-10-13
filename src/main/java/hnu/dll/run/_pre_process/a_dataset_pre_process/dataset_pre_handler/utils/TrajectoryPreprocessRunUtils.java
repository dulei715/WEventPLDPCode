package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.TxtFilter;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.Constant;
import hnu.dll.dataset.real.datasetA.handled_struct.TrajectoryComplicatedBean;
import hnu.dll.dataset.real.datasetA.handled_struct.TrajectorySimplifiedBean;

import java.io.File;
import java.util.*;

public class TrajectoryPreprocessRunUtils {




    public static Map<Integer, Map<String, Long>> getBoundMap(File[] files) {
        Map<Integer, Map<String, Long>> result = new HashMap<>();
        BasicRead basicRead = new BasicRead(",");
        List<String> tempData;
        TrajectorySimplifiedBean tempBean;
        for (File file : files) {
            basicRead.startReading(file.getAbsolutePath());
            tempData = basicRead.readAllWithoutLineNumberRecordInFile();
            for (String item : tempData) {
                tempBean = TrajectorySimplifiedBean.toBean(basicRead.split(item));
                PreprocessRunUtils.updateSubMapValue(result, tempBean.getUserID(), tempBean.getTimestamp());
            }
        }
        return result;
    }



    public static Map<Integer, BasicPair<Long, String>> getInitialUserTimeSlotLocationMap(File[] files) {
        List<String> tempData;
        BasicRead basicRead = new BasicRead(",");
        TrajectoryComplicatedBean tempBean;
        Map<Integer, BasicPair<Long, String>> result = new TreeMap<>();
        for (File file : files) {
            basicRead.startReading(file.getAbsolutePath());
            tempData = basicRead.readAllWithoutLineNumberRecordInFile();
            for (String dataLine : tempData) {
                tempBean = TrajectoryComplicatedBean.toBean(basicRead.split(dataLine));
                PreprocessRunUtils.updateMostOriginalTimeSlotData(result, tempBean.getUserID(), tempBean.getTimestamp(), tempBean.getAreaIndex()+"");
            }
            basicRead.endReading();
        }
        return result;
    }

    public static String toSimpleTrajectoryString(Map.Entry<Integer, BasicPair<Long, String>> entry) {
        StringBuilder stringBuilder = new StringBuilder();
        BasicPair<Long, String> pairValue = entry.getValue();
        stringBuilder.append(entry.getKey()).append(",");
        stringBuilder.append(pairValue.getValue()).append(",");
        stringBuilder.append(pairValue.getKey());
        return stringBuilder.toString();
    }

    public static Map<Integer, BasicPair<Long, String>> getInitialUserTimeSlotLocationMap(String directoryPath) {
        File file = new File(directoryPath);
        File[] files = file.listFiles();
        return getInitialUserTimeSlotLocationMap(files);
    }

//    public static List<Integer> getUserIDList(String userIDDir) {
//        File file = new File(userIDDir);
//        String[] fileNameArray = file.list();
//        List<Integer> result = new ArrayList<>(fileNameArray.length);
//        for (String fileName : fileNameArray) {
//            result.add(Integer.valueOf(fileName.split("\\.")[0]));
//        }
//        return result;
//    }
    public static List<Integer> getUserIDList() {
        String filePathDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "runInput");
        File dirFile = new File(filePathDir);
        String filePath = dirFile.listFiles(new TxtFilter())[0].getAbsolutePath();
        List<Integer> result = new ArrayList<>();
        List<String> tempDataList;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        for (String str : tempDataList) {
            result.add(Integer.valueOf(basicRead.split(str)[0]));
        }
        return result;
    }

//    public static List<Integer> getTimeStampList() {
//        String fileDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "runInput");
//        File file = new File(fileDir);
//        File[] fileArray = file.listFiles(new TxtFilter());
//        String timeStampStr;
//        List<Integer> resultList = new ArrayList<>();
//        for (File innerfile : fileArray) {
//            timeStampStr = FormatFileName.extractNumString(innerfile.getName(), "_", ".");
//            resultList.add(Integer.valueOf(timeStampStr));
//        }
//        return resultList;
//    }



    public static void main(String[] args) {
//        List<Integer> result = getUserIDList();
        List<Integer> result = PreprocessRunUtils.getTimeStampList(Constant.trajectoriesFilePath);
        MyPrint.showList(result);
        System.out.println(result.size());
    }
}
