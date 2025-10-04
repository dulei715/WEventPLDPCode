package hnu.dll.utils;

import cn.edu.dll.struct.BasicPair;

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
}
