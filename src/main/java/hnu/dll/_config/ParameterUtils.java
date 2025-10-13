package hnu.dll._config;

import cn.edu.dll.basic.*;
import cn.edu.dll.struct.pair.IdentityCombinePair;

import java.util.*;

public class ParameterUtils {

    public static List<Double> getRandomPrivacyBudget(Double lowerBound, Double upperBound, int size, Random random) {
        List<Double> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(RandomUtil.getRandomDouble(lowerBound, upperBound, random));
        }
        return result;
    }
    public static List<String> getRandomDoubleList(List<Integer> userIDList, Double lowerBound, Double upperBound, Random random) {
        List<String> result = new ArrayList<>(userIDList.size());
        Double tempDouble;
        for (Integer userID : userIDList) {
            tempDouble = RandomUtil.getRandomDouble(lowerBound, upperBound, random);
            result.add(StringUtil.join(",", userID, NumberUtil.roundFormat(tempDouble, 2)));
        }
        return result;
    }
    public static List<String> getRandomDoubleListInCandidateListWithStatistic(List<Integer> userIDList, List<Double> candidateList, List<Double> resultStatistic) {
        List<String> result = new ArrayList<>(userIDList.size());
        Double tempDouble;
        Integer tempIndex;
        for (Integer userID : userIDList) {
//            tempDouble = RandomUtil.getRandomDouble(lowerBound, upperBound);
            tempIndex = RandomUtil.getRandomIndexGivenStatisticPoint(resultStatistic.toArray(new Double[0]));
            tempDouble = candidateList.get(tempIndex);
            result.add(StringUtil.join(",", userID, tempDouble));
        }
        return result;
    }
    public static List<String> getRandomIntegerList(List<Integer> userIDList, Integer lowerBound, Integer upperBound, Random random) {
        List<String> result = new ArrayList<>(userIDList.size());
        for (Integer userID : userIDList) {
            result.add(StringUtil.join(",", userID, RandomUtil.getRandomInteger(lowerBound, upperBound, random)));
        }
        return result;
    }
    public static List<String> getRandomIntegerListInCandidateListWithStatistic(List<Integer> userIDList, List<Integer> candidateList, List<Double> resultStatistic) {
        List<String> result = new ArrayList<>(userIDList.size());
        Integer tempIndex, tempValue;
        for (Integer userID : userIDList) {
            tempIndex = RandomUtil.getRandomIndexGivenStatisticPoint(resultStatistic.toArray(new Double[0]));
            result.add(StringUtil.join(",", userID, candidateList.get(tempIndex)));
        }
        return result;
    }

    public static List<Double> generateDoubleList(Double lowerBound, Double upperBound, int typeSize, int groupElementSize, int sizeUpperBound) {
        List<Double> resultList = new ArrayList<>();
        Double step = (upperBound - lowerBound) / (typeSize - 1);
        if (step <= 0) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(lowerBound);
            }
            return resultList;
        }
        int currentSize = 0;
        for (double value = lowerBound; value <= upperBound; value+=step) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(value);
                ++currentSize;
                if (currentSize >= sizeUpperBound) {
                    return resultList;
                }
            }
        }
        return resultList;
    }

    public static List<Double> generateDoubleList(List<Double> doubleTypeList, Integer totalSize, Double... ratios) {
        Integer typeSize = doubleTypeList.size();
        Integer ratioSize = ratios.length;
        Double tempDouble, tempRatio;
        Integer tempSize;
        List<Double> resultList = new ArrayList<>();
        if (typeSize > 1) {
            Double ratioSum = BasicArrayUtil.getSum(ratios);
            if (ratioSize != typeSize - 1 || ratioSum > 1) {
                throw new RuntimeException("The ratios are invalid!");
            }
            for (int i = 0; i < ratios.length; i++) {
                tempDouble = doubleTypeList.get(i);
                tempSize = (int) (Math.round(totalSize * ratios[i]));
                for (int j = 0; j < tempSize; j++) {
                    resultList.add(tempDouble);
                }
            }
            tempRatio = 1.0 - ratioSum;
        } else {
            tempRatio = 1.0;
        }
        tempDouble = doubleTypeList.get(doubleTypeList.size()-1);
        tempSize = (int) (Math.round(totalSize * tempRatio));
        for (int j = 0; j < tempSize; j++) {
            resultList.add(tempDouble);
        }
        return resultList;
    }

    public static <T> List<T> generateRandomList(IdentityCombinePair<T> doublePair, Integer totalSize, Double ratio, Random random) {
        T firstElement, secondElement;
        Integer tempSize;
        List<T> resultList = new ArrayList<>();
        if (ratio < 0 || ratio > 1) {
            throw new RuntimeException("The ratios are invalid!");
        }
        firstElement = doublePair.getKey();
        secondElement = doublePair.getValue();
        tempSize = (int) (Math.round(totalSize * ratio));
        List<Integer> firstElementIndexList = RandomUtil.getRandomIntegerListWithoutRepeat(0, totalSize - 1, tempSize, random);
        Set<Integer> firstElementIndexSet = new HashSet<>(firstElementIndexList);
        for (int i = 0; i < totalSize; i++) {
            if (firstElementIndexSet.contains(i)) {
                resultList.add(firstElement);
            } else {
                resultList.add(secondElement);
            }
        }
        return resultList;
    }
    public static List<Double> generateRandomDoubleList(Double lowerBound, Double upperBound, int typeSize, int groupElementSize, int sizeUpperBound, Random random) {
        List<Double> resultList = new ArrayList<>();
        Double[] doubleTypeList = RandomUtil.getRandomDoubleArray(lowerBound, upperBound, typeSize, random);
        int currentSize = 0;
        for (Double value : doubleTypeList) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(value);
                ++currentSize;
                if (currentSize >= sizeUpperBound) {
                    return resultList;
                }
            }
        }
        return resultList;
    }
    public static List<Integer> generateDoubleList(Integer lowerBound, Integer upperBound, int typeSize, int groupElementSize, int sizeUpperBound) {
        List<Integer> resultList = new ArrayList<>();
        Integer step = (upperBound - lowerBound) / (typeSize - 1);
        if (step <= 0) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(lowerBound);
            }
            return resultList;
        }
        int currentSize = 0;
        for (int value = lowerBound; value <= upperBound; value+=step) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(value);
                ++currentSize;
                if (currentSize >= sizeUpperBound) {
                    return resultList;
                }
            }
        }
        return resultList;
    }
    public static List<Integer> generateRandomIntegerList(Integer lowerBound, Integer upperBound, int typeSize, int groupElementSize, int sizeUpperBound, Random random) {
        List<Integer> resultList = new ArrayList<>();
        Integer[] doubleTypeList = RandomUtil.getRandomIntegerArray(lowerBound, upperBound, typeSize, random);
        int currentSize = 0;
        for (Integer value : doubleTypeList) {
            for (int i = 0; i < groupElementSize; i++) {
                resultList.add(value);
                ++currentSize;
                if (currentSize >= sizeUpperBound) {
                    return resultList;
                }
            }
        }
        return resultList;
    }

    public static List<Integer> generateIntegerList(List<Integer> integerList, Integer totalSize, Double... ratios) {
        Integer typeSize = integerList.size();
        Integer ratioSize = ratios.length;
        Integer tempInteger, tempSize;
        Double tempRatio;
        List<Integer> resultList = new ArrayList<>();
        if (typeSize > 1) {
            Double ratioSum = BasicArrayUtil.getSum(ratios);
            if (ratioSize != typeSize - 1 || ratioSum > 1) {
                throw new RuntimeException("The ratios are invalid!");
            }
            for (int i = 0; i < ratios.length; i++) {
                tempInteger = integerList.get(i);
                tempSize = (int) (Math.round(totalSize * ratios[i]));
                for (int j = 0; j < tempSize; j++) {
                    resultList.add(tempInteger);
                }
            }
            tempRatio = 1.0 - ratioSum;
        } else {
            tempRatio = 1.0;
        }
        tempInteger = integerList.get(integerList.size()-1);
        tempSize = (int) (Math.round(totalSize * tempRatio));
        for (int j = 0; j < tempSize; j++) {
            resultList.add(tempInteger);
        }
        return resultList;
    }




    /**
     * for forward and backward privacy
     */

    // 这里doubleListLowerBound是按照typeSize和sizeUpperBound分好的组（每组中，元素对应的doubleListLowerBound相等）
    /*
     * 返回的结果外层List代表timestamp，内层list代表user
     */
    public static List<List<Double>> generateRandomDoubleList(List<Double> doubleListLowerBound, Double upperBound, int elementSizeInAGroup, int resultSize, Random random) {
        List<List<Double>> result = new ArrayList<>(resultSize);
        List<Double> tempList;
        int rowSize = doubleListLowerBound.size();
        int i = 0;
        Double tempLowerBound;
        while (i < rowSize) {
            tempLowerBound = doubleListLowerBound.get(i);
            tempList = generateRandomDoubleList(tempLowerBound, upperBound, 1, resultSize, resultSize, random);
            for (int j = 0; j < elementSizeInAGroup && i < rowSize; ++j, ++i) {
                result.add(tempList);
            }
        }
        return BasicCalculation.getTransposition(result);
    }

    /*
     * 这里 doubleTypeListForOneItem 代表一个item里出现的 double的种类数，每个item完全一样
     */
    public static List<List<Double>> generateBudgetListListWithIdentityUsersAndTwoTypeBudgetsInRandomTimestamps(IdentityCombinePair<Double> doublePair, int itemSize, int resultSize, Double ratio, Random random) {
        if (ratio < 0 || ratio > 1) {
            throw new RuntimeException("Invalid ratio!");
        }
        List<List<Double>> resultList = new ArrayList<>();
        List<Double> itemDoubleList = generateRandomList(doublePair, resultSize, ratio, random);
        for (int i = 0; i < itemSize; i++) {
            resultList.add(itemDoubleList);
        }
        return BasicCalculation.getTransposition(resultList);
    }
    public static List<List<Integer>> generateWindowSizeListListWithIdentityUsersAndTwoTypeWindowSizeInRandomTimestamps(IdentityCombinePair<Integer> integerPair, int itemSize, int resultSize, Double ratio, Random random) {
        if (ratio < 0 || ratio > 1) {
            throw new RuntimeException("Invalid ratio!");
        }
        List<List<Integer>> resultList = new ArrayList<>();
        List<Integer> itemIntegerList = generateRandomList(integerPair, resultSize, ratio, random);
        for (int i = 0; i < itemSize; i++) {
            resultList.add(itemIntegerList);
        }
        return BasicCalculation.getTransposition(resultList);
    }

    public static List<List<Integer>> generateRandomIntegerList(Integer lowerBound, List<Integer> integerListUpperBound, int elementSizeInAGroup, int resultSize, Random random) {
        List<List<Integer>> result = new ArrayList<>(resultSize);
        List<Integer> tempList;
        int rowSize = integerListUpperBound.size();
        int i = 0;
        Integer tempUpperBound;
        while (i < rowSize) {
            tempUpperBound = integerListUpperBound.get(i);
            tempList = generateRandomIntegerList(lowerBound, tempUpperBound, 1, resultSize, resultSize, random);
            for (int j = 0; j < elementSizeInAGroup && i < rowSize; ++j, ++i) {
                result.add(tempList);
            }
        }
        return BasicCalculation.getTransposition(result);
    }

    public static List<List<Double>> generateRandomDoubleListWithDifferBound(List<Double> doubleListLowerBound, Double differLowerBound, Double differUpperBound, int elementSizeInAGroup, int resultSize, Random random) {
        List<List<Double>> result = new ArrayList<>(resultSize);
        List<Double> tempList;
        int rowSize = doubleListLowerBound.size();
        int i = 0;
        Double tempLowerBound;
        while (i < rowSize) {
            tempLowerBound = doubleListLowerBound.get(i);
            tempList = generateRandomDoubleList(tempLowerBound + differLowerBound, tempLowerBound + differUpperBound, 1, resultSize, resultSize, random);
            for (int j = 0; j < elementSizeInAGroup && i < rowSize; ++j, ++i) {
                result.add(tempList);
            }
        }
        return BasicCalculation.getTransposition(result);
    }

    public static List<List<Double>> generateRandomDifferenceDoubleList(Double differenceLowerBound, Double differneceUpperBound, int typeSize, int groupElementSize, int sizeUpperBound, int resultSize, Random random) {
        List<List<Double>> result = new ArrayList<>();
        List<Double> tempList;
        for (int i = 0; i < resultSize; i++) {
            tempList = generateRandomDoubleList(differenceLowerBound, differneceUpperBound, typeSize, groupElementSize, sizeUpperBound, random);
            result.add(tempList);
        }
        return result;
    }





}
