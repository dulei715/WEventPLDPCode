package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.map.MapUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MechanismErrorUtils {
    public static Double getCountError(Integer itemSize, TreeMap<Double, Double> epsilonRatioMap, Double epsilonTheta) {
        Double totalRatio = MapUtils.getValueSum(epsilonRatioMap);
        if (Math.abs(totalRatio - 1) > Math.pow(10, -6)) {
            throw new RuntimeException("The sum of epsilonRatioMap's value is not equal to 1!");
        }
        Double result = 0D, countVar = 0D, tempEpsilon, tempRatio, tempProbability, tempValue;
        for (Map.Entry<Double, Double> entry : epsilonRatioMap.entrySet()) {
            tempEpsilon = entry.getKey();
            if (tempEpsilon >= epsilonTheta) {
                break;
            }
            tempProbability = (Math.exp(tempEpsilon) - 1) / (Math.exp(epsilonTheta) - 1);
            tempRatio = entry.getValue();
            tempValue = itemSize * tempRatio * (1 - tempProbability);
            countVar += tempValue * tempProbability;
        }
        return  countVar;
    }

    public static Double getBiasError(Integer itemSize, TreeMap<Double, Double> epsilonRatioMap, Double epsilonTheta) {
        Double totalRatio = MapUtils.getValueSum(epsilonRatioMap);
        if (Math.abs(totalRatio - 1) > Math.pow(10, -6)) {
            throw new RuntimeException("The sum of epsilonRatioMap's value is not equal to 1!");
        }
        Double bias = 0D, tempEpsilon, tempRatio, tempProbability, tempValue;
        for (Map.Entry<Double, Double> entry : epsilonRatioMap.entrySet()) {
            tempEpsilon = entry.getKey();
            if (tempEpsilon >= epsilonTheta) {
                break;
            }
            tempProbability = (Math.exp(tempEpsilon) - 1) / (Math.exp(epsilonTheta) - 1);
            tempRatio = entry.getValue();
            tempValue = itemSize * tempRatio * (1 - tempProbability);
            bias += tempValue;
        }
        return  bias * bias;
    }

    public static Double getSampleError(Integer itemSize, TreeMap<Double, Double> epsilonRatioMap, Double epsilonTheta) {
        Double totalRatio = MapUtils.getValueSum(epsilonRatioMap);
        if (Math.abs(totalRatio - 1) > Math.pow(10, -6)) {
            throw new RuntimeException("The sum of epsilonRatioMap's value is not equal to 1!");
        }
        Double result = 0D, countVar = 0D, bias = 0D, tempEpsilon, tempRatio, tempProbability, tempValue;
        for (Map.Entry<Double, Double> entry : epsilonRatioMap.entrySet()) {
            tempEpsilon = entry.getKey();
            if (tempEpsilon >= epsilonTheta) {
                break;
            }
            tempProbability = (Math.exp(tempEpsilon) - 1) / (Math.exp(epsilonTheta) - 1);
            tempRatio = entry.getValue();
            tempValue = itemSize * tempRatio * (1 - tempProbability);
            countVar += tempValue * tempProbability;
            bias += tempValue;
        }
        result = countVar + bias * bias;
        return  result;
    }

    public static Double getSampleError(TreeMap<Double, Integer> epsilonCountMap, Double epsilonTheta) {
        Integer itemSize = MapUtils.getIntegerValueSum(epsilonCountMap);

        Double result = 0D, countVar = 0D, bias = 0D, tempEpsilon, tempProbability, tempValue;
        Integer tempCount;
        for (Map.Entry<Double, Integer> entry : epsilonCountMap.entrySet()) {
            tempEpsilon = entry.getKey();
            if (tempEpsilon >= epsilonTheta) {
                break;
            }
            tempProbability = (Math.exp(tempEpsilon) - 1) / (Math.exp(epsilonTheta) - 1);
            tempCount = entry.getValue();
            tempValue = tempCount * (1 - tempProbability);
            countVar += tempValue * tempProbability;
            bias += tempValue;
        }
        result = countVar + bias * bias;
        return  result;
    }

    public static Double getDPError(Double epsilon, Double sensitivity) {
        return 2.0 * Math.pow(sensitivity / epsilon, 2);
    }

    public static Double getDPError(Double epsilon) {
        return getDPError(epsilon, 1.0);
    }

    public static Double[] getMinimalEpsilonAndError(TreeMap<Double, Integer> epsilonCountMap) {
//        TreeMap<Double, Integer> epsilonCountMap = (TreeMap<Double, Integer>) StatisticTool.countHistogramNumber(privacyBudgetList);;
        Set<Double> epsilonSet = epsilonCountMap.keySet();
        Double minimalError = Double.MAX_VALUE, tempError;
        Double optimalEpsilon = null;
        for (Double epsilon : epsilonSet) {
            tempError = getSampleError(epsilonCountMap, epsilon) + getDPError(epsilon);
            if (tempError < minimalError) {
                minimalError = tempError;
                optimalEpsilon = epsilon;
            }
        }
        return new Double[]{optimalEpsilon, minimalError};
    }
}
