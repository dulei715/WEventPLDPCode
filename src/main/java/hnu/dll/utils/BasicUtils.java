package hnu.dll.utils;

import cn.edu.dll.differential_privacy.ldp.consistent.Normalization;
import cn.edu.dll.struct.pair.BasicPair;

import java.util.*;

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

    public static <T> Map<T, Double> getStatisticByCount(Map<T, Integer> data) {
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

    public static <T,S> Map<T, Map<S, Double>> getGroupStatisticByCount(Map<T, Map<S, Integer>> data) {
        Map<T, Map<S, Double>> result = new TreeMap<>();
        T tempT;
        Map<S, Integer> tempCountMap;
        Map<S, Double> tempStatisticMap;
        for (Map.Entry<T, Map<S, Integer>> entry : data.entrySet()) {
            tempT = entry.getKey();
            tempCountMap = entry.getValue();
            tempStatisticMap = getStatisticByCount(tempCountMap);
            result.put(tempT, tempStatisticMap);
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
    public static Map<Double, List<Integer>> groupByEpsilon(List<Double> epsilonList, List<Integer> dataList) {
        int size = epsilonList.size();
        Map<Double, List<Integer>> result = new TreeMap<>();
        if (size != dataList.size()) {
            throw new RuntimeException("传入数据长度不一致！");
        }
        for (int i = 0; i < size; ++i) {
            result.computeIfAbsent(epsilonList.get(i), k->new ArrayList<>()).add(dataList.get(i));
        }
        return result;
    }

    public static <T> Map<T, Integer> getCountMapByGroup(Map<Double, List<T>> groupData) {
        Map<T, Integer> result = new TreeMap<>();
        Integer tempCount;
        for (List<T> dataInGroup : groupData.values()) {
            for (T element : dataInGroup) {
                tempCount = result.getOrDefault(element, 0);
                ++tempCount;
                result.put(element, tempCount);
            }
        }
        return result;
    }

    public static <K,V> Map<K, Integer> getGroupDataCount(Map<K, List<V>> data) {
        K tempK;
        Integer tempSize;
        Map<K, Integer> result = new TreeMap<>();
        for (Map.Entry<K, List<V>> entry : data.entrySet()) {
            tempK = entry.getKey();
            tempSize = entry.getValue().size();
            result.put(tempK, tempSize);
        }
        return result;
    }

    public static <K, V> List<V> toSortedListByKeys(Map<K, V> data, Collection<K> shownKeyCollection, V defaultValue) {
        TreeSet<K> sortedSet = new TreeSet<>(shownKeyCollection);
        List<V> result = new ArrayList<>(sortedSet.size());
        for (K key : sortedSet) {
            result.add(data.getOrDefault(key, defaultValue));
        }
        return result;
    }

    public static <K> Map<K, Double> normalizeValues(Map<K, Double> dataMap) {
        LinkedHashMap<K, Double> map = new LinkedHashMap<>(dataMap);
        Map<K, Double> resultMap = new HashMap<>();
        List<Double> valueList = new ArrayList<>(map.values());
        List<Double> normalizedValueList = Normalization.normalizedBySimplexProjection(valueList);
        Set<K> keySet = map.keySet();
        int index = 0;
        for (K key : keySet) {
            resultMap.put(key, normalizedValueList.get(index));
            ++index;
        }
        return resultMap;
    }

}
