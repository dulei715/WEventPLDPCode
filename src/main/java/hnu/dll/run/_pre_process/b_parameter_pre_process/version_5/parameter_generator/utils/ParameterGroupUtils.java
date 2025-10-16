package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.utils;

import cn.edu.dll.basic.StringUtil;
import hnu.dll.utils.io.ListReadUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParameterGroupUtils {
    public static List<String> getUserIDType(Integer userTypeSize) {
        List<String> userTypeStringList = new ArrayList<>();
        for (int i = 0; i < userTypeSize; i++) {
            userTypeStringList.add(String.valueOf(i));
        }
        return userTypeStringList;
    }

//    public static List<String> getUserIDTypeRatio(List<Double> typeRatioList) {
//        List<String> userIDTypeRatioList = new ArrayList<>();
//        for (int i = 0; i < typeRatioList.size(); i++) {
//            userIDTypeRatioList.add(String.format("%d,%.2f", i, typeRatioList.get(i)));
//        }
//        return userIDTypeRatioList;
//    }


    public static List<String> getUserToTypeInAverage(String userIDInputPath, String userTypeIDInputPath) {

        String tempOutputString;
        List<String> userIDDataList = ListReadUtils.readAllDataList(userIDInputPath, ",");
        List<String> userTypeIDDataList = ListReadUtils.readAllDataList(userTypeIDInputPath, ",");
        Iterator<String> userIDIterator = userIDDataList.iterator();
        Integer groupElementSize = (int)Math.ceil(userIDDataList.size() * 1.0 / userTypeIDDataList.size());
        String tempUserID;
        List<String> userToTypeData = new ArrayList<>();

        for (String typeStr : userTypeIDDataList) {
            for (int i = 0; i < groupElementSize && userIDIterator.hasNext(); i++) {
                tempUserID = userIDIterator.next();
                tempOutputString = StringUtil.join(",", tempUserID, typeStr);
                userToTypeData.add(tempOutputString);
            }
        }
        return userToTypeData;
    }


    public static List<String> getUserToIndex(String userIDInputPath) {

        String tempOutputString;
        List<String> userIDDataList = ListReadUtils.readAllDataList(userIDInputPath, ",");
        List<String> userToIndexTypeData = new ArrayList<>();

        int index = 0;
        for (String userID : userIDDataList) {
            tempOutputString = StringUtil.join(",", userID, index);
            userToIndexTypeData.add(tempOutputString);
            ++index;
        }
        return userToIndexTypeData;
    }

    public static List<String> getLocationToIndex(String locationNameInputPath, Integer indexSize, Random random) {
        String tempOutputString;
        List<String> locationNameDataList = ListReadUtils.readAllDataList(locationNameInputPath, ",");
        List<String> locationToIndexDataList = new ArrayList<>();
        int realIndexSize = locationNameDataList.size();
        if (realIndexSize <= indexSize) {
            for (int i = 0; i < realIndexSize; ++i) {
                String locationName = locationNameDataList.get(i);
                tempOutputString = StringUtil.join(",", locationName, i);
                locationToIndexDataList.add(tempOutputString);
            }
        } else {
            for (String locationName : locationNameDataList) {
                tempOutputString = StringUtil.join(",", locationName, random.nextInt(indexSize));
                locationToIndexDataList.add(tempOutputString);
            }
        }
        return locationToIndexDataList;
    }


//    public static List<String> getUserToTypeByRatio(List<String> userIDDataList, List<String> userTypeIDRatioDataList) {
//
//        String tempOutputString;
//        Iterator<String> userIDIterator = userIDDataList.iterator();
//        Integer tempGroupElementSize, userSize = userIDDataList.size();
//        String tempUserID, tempTypeID;
//        List<String> userToTypeData = new ArrayList<>();
//        Double tempRatio;
//        String[] tempSplitArray;
//        double ratioSum = 0;
//        for (String userTypeIDRatio : userTypeIDRatioDataList) {
//            ratioSum += Double.parseDouble(userTypeIDRatio.split(",")[1]);
//        }
//        for (String userTypeIDRatio : userTypeIDRatioDataList) {
//            tempSplitArray = userTypeIDRatio.split(",");
//            tempTypeID = tempSplitArray[0];
//            tempRatio = Double.valueOf(tempSplitArray[1]);
//            tempGroupElementSize = (int) Math.round(userSize / ratioSum * tempRatio);
//            for (int i = 0; i < tempGroupElementSize && userIDIterator.hasNext(); i++) {
//                tempUserID = userIDIterator.next();
//                tempOutputString = StringUtil.join(",", tempUserID, Integer.valueOf(tempTypeID));
//                userToTypeData.add(tempOutputString);
//            }
//        }
//        return userToTypeData;
//    }
//    public static List<String> getUserToTypeByRatio(String userIDInputPath, String userTypeIDRatioInputPath) {
//
//        List<String> userIDDataList = ListReadUtils.readAllDataList(userIDInputPath, ",");
//        List<String> userTypeIDRatioDataList = ListReadUtils.readAllDataList(userTypeIDRatioInputPath, ",");
//        return getUserToTypeByRatio(userIDDataList, userTypeIDRatioDataList);
//    }



}
