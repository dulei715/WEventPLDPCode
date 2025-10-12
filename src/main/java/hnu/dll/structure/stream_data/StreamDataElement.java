package hnu.dll.structure.stream_data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class StreamDataElement<T extends Comparable<T>> {

    protected TreeMap<String, T> dataMap;

    public StreamDataElement(TreeMap<String, T> dataMap) {
        this.dataMap = dataMap;
    }

    public StreamDataElement(List<T> valueList) {
        this.dataMap = new TreeMap<>();
        for (int i = 0; i < valueList.size(); i++) {
            this.dataMap.put("key_"+i, valueList.get(i));
        }
    }

    public TreeMap<String, T> getDataMap() {
        return dataMap;
    }

    public T getValue(String key) {
        return this.dataMap.get(key);
    }

    public List<String> getKeyList() {
        return new ArrayList<>(this.dataMap.keySet());
    }

    public List<T> getValueList() {
        return new ArrayList<>(this.dataMap.values());
    }



//    public TreeMap<String, Integer> getCountByGivenElementType(T elementValue, List<StreamDataElement<T>> dataList, List<Integer> indexList) {
//        TreeMap<String, List<T>> typeDataTreeMap = extractSubData(dataList);
//        Map<T, Integer> tempMap;
//        String tempKey;
//        TreeMap<String, Integer> result = new TreeMap<>();
//        for (Map.Entry<String, List<T>> entry : typeDataTreeMap.entrySet()) {
//            tempKey = entry.getKey();
//            tempMap = StatisticTool.countHistogramNumberByGivenElementType(elementValue, entry.getValue(), indexList);
//            result.put(tempKey, tempMap.get(elementValue));
//        }
//        return result;
//    }

    @Override
    public String toString() {
        return "StreamDataElement{" +
                "dataMap=" + dataMap +
                '}';
    }


}
