package hnu.dll.utils;

import cn.edu.dll.struct.BasicPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BasicUtils {
    public static <T extends Comparable<T>, S extends Comparable<S>> Map<BasicPair<T,S>, Integer> countUniquePair(List<T> firstElementList, List<S> secondElementList) {
        Map<BasicPair<T,S>, Integer> result = new TreeMap<>();
        BasicPair<T,S> tempKey;
        Integer tempCount;
        T firstElement;
        S secondElement;
        int size = firstElementList.size();
        if (size != secondElementList.size()) {
            throw new RuntimeException("传入的两个列标长度不一致！");
        }
        for (int i = 0; i < size; ++i) {
            firstElement = firstElementList.get(i);
            secondElement = secondElementList.get(i);
            tempKey = new BasicPair<>(firstElement, secondElement);
            tempCount = result.getOrDefault(tempKey, 0);
            ++tempCount;
            result.put(tempKey, tempCount);
        }
        return result;
    }

    public static  <T> Map<T, Double> getStatisticByCount(Map<T, Integer> data) {
        Map<T, Double> result = new TreeMap<>();
        Integer totalCount = 0;
        for (Integer count : data.values()) {
            totalCount += count;
        }
        for (Map.Entry<T, Integer> entry : data.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * 1.0 / totalCount);
        }
        return result;
    }

    public static <T> List<T> getElementListByIndex(List<T> elementList, List<Integer> indexList) {
        List<T> result = new ArrayList<>(indexList.size());
        for (Integer index : indexList) {
            result.add(elementList.get(index));
        }
        return result;
    }
    public static <T> List<T> getElementListByIndex(T[] data, List<Integer> indexList) {
        List<T> result = new ArrayList<>(indexList.size());
        for (Integer index : indexList) {
            result.add(data[index]);
        }
        return result;
    }
}
