package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.statistic.StatisticTool;
import hnu.dll.structure.stream_data.StreamDataElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BooleanStreamDataElementUtils {

    protected static TreeMap<String, List<Boolean>> extractSubData(List<StreamDataElement<Boolean>> dataList) {
        TreeMap<String, List<Boolean>> treeMapData = new TreeMap<>();
        List<String> keyList = dataList.get(0).getKeyList();
        List<Boolean> tempValueList;
        for (int j = 0; j < keyList.size(); j++) {
            treeMapData.put(keyList.get(j), new ArrayList<Boolean>());
        }
        StreamDataElement<Boolean> tempElement;
        TreeMap<String, Boolean> tempDataMap;
        for (int i = 0; i < dataList.size(); i++) {
            tempElement = dataList.get(i);
            tempDataMap = tempElement.getDataMap();
            for (Map.Entry<String, Boolean> entry : tempDataMap.entrySet()) {
                tempValueList = treeMapData.get(entry.getKey());
                tempValueList.add(entry.getValue());
            }
        }
        return treeMapData;
    }

    public static TreeMap<String, Integer> getCountByGivenElementType(Boolean elementValue, List<StreamDataElement<Boolean>> dataList, List<Integer> indexList) {
        TreeMap<String, List<Boolean>> typeDataTreeMap = extractSubData(dataList);
        Map<Boolean, Integer> tempMap;
        String tempKey;
        TreeMap<String, Integer> result = new TreeMap<>();
        for (Map.Entry<String, List<Boolean>> entry : typeDataTreeMap.entrySet()) {
            tempKey = entry.getKey();
            tempMap = StatisticTool.countHistogramNumberByGivenElementType(elementValue, entry.getValue(), indexList);
            result.put(tempKey, tempMap.get(elementValue));
        }
        return result;
    }

    public static TreeMap<String, Integer> getCountByGivenElementType(Boolean elementValue, List<StreamDataElement<Boolean>> dataList) {
        TreeMap<String, List<Boolean>> typeDataTreeMap = extractSubData(dataList);
        Map<Boolean, Integer> tempMap;
        String tempKey;
        TreeMap<String, Integer> result = new TreeMap<>();
        for (Map.Entry<String, List<Boolean>> entry : typeDataTreeMap.entrySet()) {
            tempKey = entry.getKey();
            tempMap = StatisticTool.countHistogramNumberByGivenElementType(elementValue, entry.getValue());
            result.put(tempKey, tempMap.get(elementValue));
        }
        return result;
    }

}
