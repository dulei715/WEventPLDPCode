package hnu.dll.run.c_dataset_run.utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.read.BasicRead;
import hnu.dll.run.b_parameter_run.utils.InputDataStruct;

import java.util.*;

public class DatasetParameterUtils {
//    private static Map<String, >
    public static List<String> getDataTypeList(String basicPath, String dataTypeFileName) {
        String filePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", dataTypeFileName);
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        List<String> data = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        return data;
    }
    public static Set<String> getDataTypeSet(String basicPath, String dataTypeFileName) {
        String filePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", dataTypeFileName);
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        Set<String> data = new HashSet<>(basicRead.readAllWithoutLineNumberRecordInFile());
        basicRead.endReading();
        return data;
    }

    @Deprecated
    public static List<Integer> getData(String dataPath, List<String> dataType) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(dataPath);
        List<String> strDataList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        InputDataStruct bean;
        String location;
        TreeMap<String, Boolean> tempMap;
        List<Integer> resultList = new ArrayList<>();
        Integer tempElement;
        for (String str : strDataList) {
            bean = InputDataStruct.toBean(basicRead.split(str));
            location = bean.getLocation();
            tempElement = dataType.indexOf(location);
            resultList.add(tempElement);
        }
        return resultList;
    }
    public static List<Integer> getDataMappedToIndex(String dataPath, List<String> dataType, Map<Integer, Integer> userIDToIndexMap) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(dataPath);
        List<String> strDataList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        InputDataStruct bean;
        String location;
        TreeMap<String, Boolean> tempMap;

        List<Integer> resultList = BasicArrayUtil.getInitializedList(0, userIDToIndexMap.size());
        Integer tempElement, userIndex;
        for (String str : strDataList) {
            // 保证读到的文件中所有的user出现且只出现一次
            bean = InputDataStruct.toBean(basicRead.split(str));
            userIndex = userIDToIndexMap.get(bean.getUserID());
            location = bean.getLocation();
            tempElement = dataType.indexOf(location);
            resultList.add(userIndex, tempElement);
        }
        return resultList;
    }



}
