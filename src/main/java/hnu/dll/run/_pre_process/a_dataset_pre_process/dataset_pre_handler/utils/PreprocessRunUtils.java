package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.TxtFilter;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll.utils.FormatFileName;
import hnu.dll.utils.io.ListReadUtils;

import java.io.File;
import java.util.*;

public class PreprocessRunUtils {
    public static final String LowBoundKey = "lowerBound";
    public static final String UpperBoundKey = "upperBound";
    public static void updateSubMapValue(Map<Integer, Map<String, Long>> map, Integer userID, Long value) {
        Map<String, Long> boundMap = map.get(userID);
        Long tempBoundLower, tempBoundUpper;
        if (boundMap == null) {
            boundMap = new HashMap<>();
            boundMap.put(LowBoundKey, Long.MAX_VALUE);
            boundMap.put(UpperBoundKey, 0L);
            map.put(userID, boundMap);
        }
        tempBoundLower = boundMap.get(LowBoundKey);
        tempBoundUpper = boundMap.get(UpperBoundKey);
        if (value < tempBoundLower) {
            boundMap.put(LowBoundKey, value);
        }
        if (value > tempBoundUpper) {
            boundMap.put(UpperBoundKey, value);
        }
    }
    public static void updateLatestTimeSlotData(Map<Integer, BasicPair<Long, String>> map, Integer userID, Long timeSlot, String position) {
        BasicPair<Long, String> originalData = map.get(userID);
        if (originalData == null) {
            originalData = new BasicPair<>(timeSlot, position);
            map.put(userID, originalData);
        } else {
            Long originalTimeSlot = originalData.getKey();
            if (timeSlot > originalTimeSlot) {
                map.put(userID, new BasicPair<>(timeSlot, position));
            }
        }
    }
    public static void updateMostOriginalTimeSlotData(Map<Integer, BasicPair<Long, String>> map, Integer userID, Long timeSlot, String position) {
        BasicPair<Long, String> originalData = map.get(userID);
        if (originalData == null) {
            originalData = new BasicPair<>(timeSlot, position);
            map.put(userID, originalData);
        } else {
            Long originalTimeSlot = originalData.getKey();
            if (timeSlot < originalTimeSlot) {
                map.put(userID, new BasicPair<>(timeSlot, position));
            }
        }
    }
    public static Map<Integer, BasicPair<Long, String>> copyUserTimeSlotLocationMap(Map<Integer, BasicPair<Long, String>> originalMap) {
        Map<Integer, BasicPair<Long, String>> result = new TreeMap<>();
        BasicPair<Long, String> originalPair, newValue;
        for (Map.Entry<Integer, BasicPair<Long, String>> entry : originalMap.entrySet()) {
            originalPair = entry.getValue();
            newValue = new BasicPair<>(originalPair.getKey(), originalPair.getValue());
            result.put(entry.getKey(), newValue);
        }
        return result;
    }
    /**
     * 及支持形如xxx_yyy.txt
     * 其中yyy是文件编号
     * @param fileName
     * @return
     */
    public static String extractNumberString(String fileName) {
        String[] splitString = fileName.split("_");
        return splitString[splitString.length-1].split("\\.")[0];
    }

    // for test
    public static Map<Integer, Map<String, Double>> format(Map<Integer, Map<String, Long>> rawMap, Long startTag) {
        Map<Integer, Map<String, Double>> result = new HashMap<>();
        for (Map.Entry<Integer, Map<String, Long>> rawEntry : rawMap.entrySet()) {
            Integer rawKey = rawEntry.getKey();
            Map<String, Long> rawValue = rawEntry.getValue();
            Map<String, Double> newValue = new HashMap<>();
            for (Map.Entry<String, Long> innerRawEntry : rawValue.entrySet()) {
                String innerRawKey = innerRawEntry.getKey();
                Long innerRawValue = innerRawEntry.getValue();
                Double innerNewValue = (innerRawValue - startTag) / 1000.0 / 60.0 / 60 / 24;
                newValue.put(innerRawKey, innerNewValue);
            }
            result.put(rawKey,newValue);
        }
        return result;
    }

    public static void recordUserInfo(String datasetPath, String readFromDirFileName, String outputUserFileName) {
        String basicDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, readFromDirFileName);
        File basicDirFile = new File(basicDirPath);
        File fistFile = basicDirFile.listFiles(new TxtFilter())[0];
        String userReadDirPath = fistFile.getAbsolutePath();

        BasicRead basicRead = new BasicRead(",");
        List<String> tempData, userIDList = new ArrayList<>();
        basicRead.startReading(userReadDirPath);
        tempData = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String userIDString;
        for (String line : tempData) {
            userIDString = basicRead.split(line)[0];
            userIDList.add(userIDString);
        }
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", outputUserFileName));
        basicWrite.writeStringListWithoutSize(userIDList);
        basicWrite.endWriting();
    }

    public static void recordTimeStampInfo(String datasetPath, String readFromDirFileName) {
        String timeStampReadDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, readFromDirFileName);

        TreeSet<Integer> timeStampSet = new TreeSet<>();
        File dirFile = new File(timeStampReadDirPath);
        File[] files = dirFile.listFiles(new TxtFilter());
        for (File file : files) {
            timeStampSet.add(Integer.valueOf(FormatFileName.extractNumString(file.getName(), "_", ".")));
        }
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "time_stamp.txt"));
        basicWrite.writeStringListWithoutSize(new ArrayList<>(timeStampSet));
        basicWrite.endWriting();
    }

    public static List<Integer> getUserTypeIDList(String dataSetDir, String fileName) {
        String userTypeIDPath = StringUtil.join(ConstantValues.FILE_SPLIT, dataSetDir, "basic_info", fileName);
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(userTypeIDPath);
        List<String> strList = basicRead.readAllWithoutLineNumberRecordInFile();
        List<Integer> userTypeIDList = new ArrayList<>();
        for (String str : strList) {
            userTypeIDList.add(Integer.valueOf(str));
        }
        basicRead.endReading();
        return userTypeIDList;
    }

    public static List<Integer> getTimeStampListByRunInput(String datasetPath) {
        String fileDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "runInput_raw");
//        System.out.println(fileDir);
        File file = new File(fileDir);
        File[] fileArray = file.listFiles(new TxtFilter());
//        System.out.println(fileArray[0].getName() + "; " + fileArray[fileArray.length-1].getName());
        String timeStampStr;
        List<Integer> resultList = new ArrayList<>();
        for (File innerfile : fileArray) {
//            System.out.println(innerfile.getName());
            timeStampStr = FormatFileName.extractNumString(innerfile.getName(), "_", ".");
            resultList.add(Integer.valueOf(timeStampStr));
        }
        return resultList;
    }
    public static List<Integer> getTimeStampList(String datasetPath) {
        String timeStampFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "time_stamp.txt");
        List<String> strList = ListReadUtils.readAllDataList(timeStampFilePath, ",");
        List<Integer> resultList = new ArrayList<>(strList.size());
        for (String str : strList) {
            resultList.add(Integer.valueOf(str));
        }
        return resultList;
    }
}
