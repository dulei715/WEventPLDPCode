package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public class FOUtils {

    public static Double getGRRQ(Double epsilon, Integer domainSize) {
        Double expEpsilon = Math.exp(epsilon);
        Double q = 1 / (expEpsilon + domainSize - 1);
        return q;
    }

    public static Double getGRRP(Double epsilon, Integer domainSize) {
        Double expEpsilon = Math.exp(epsilon);
        Double p = expEpsilon / (expEpsilon + domainSize - 1);
        return p;
    }

    public static Integer gRRSinglePerturb(Double epsilon, Integer realDataIndex, Integer domainSize, Random random) {
        Double p = getGRRP(epsilon, domainSize);
        Boolean chosen = RandomUtil.isChosen(p, random);
        if (chosen) {
            return realDataIndex;
        }
        int newIndex = random.nextInt(domainSize - 1);
        if (newIndex >= realDataIndex) {
            newIndex += 1;
        }
        return newIndex;
    }

    public static List<Integer> gRRPerturb(List<Integer> originalData, Double epsilon, Integer domainSize, Random random) {
        List<Integer> obfuscatedDataList;
        Integer obfuscatedData;
        obfuscatedDataList = new ArrayList<>(originalData.size());
        for (Integer data : originalData) {
            obfuscatedData = gRRSinglePerturb(epsilon, data, domainSize, random);
            obfuscatedDataList.add(obfuscatedData);
        }
        return obfuscatedDataList;
    }

    public static Map<Integer, Double> getAggregation(List<Integer> obfuscatedList, Integer domainSize) {
        Map<Integer, Integer> countMap;
        Map<Integer, Double> statisticMap;
        List<Integer> dataIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, domainSize - 1);
        countMap = BasicArrayUtil.getUniqueListWithCountList(obfuscatedList, dataIndexList);
        statisticMap = BasicUtils.getStatisticByCount(countMap);
        return statisticMap;
    }

    public static Map<Integer, Double> getEstimation(Map<Integer, Double> obfuscatedEstimation, Double parameterP, Double parameterQ) {
        Double tempStatistic, tempEstimation;
        Integer valueIndex;
        Map<Integer, Double> result = new TreeMap<>();
        for (Map.Entry<Integer, Double> innerEntry : obfuscatedEstimation.entrySet()) {
            valueIndex = innerEntry.getKey();
            tempStatistic = innerEntry.getValue();
            tempEstimation = (tempStatistic - parameterQ) / (parameterP - parameterQ);
            result.put(valueIndex, tempEstimation);
        }
        return result;
    }

    public static Double getLDPVarianceSum(Double privacyBudget, Integer userSize, Integer domainSize) {
        Double totalVariance = 0D;
        BasicPair<Double, Double> tempPair;
        Double tempParam = Math.exp(privacyBudget) - 1;
        // todo: 这里改正了LDP-IDS (SIGMOD 2022)中的err计算问题。这里应该在第二项多乘个1/d
        //  按理说err = 1/d \sum_{k=1}^{d} Var = (d-2+e^{\epsilon})/(N(e^{\epsilon}-1)) + (d-2)/(N(e^{\epsilon}-1)) * 1/d
        //  因此 \sum var 应该是如下表达
        totalVariance = domainSize * (tempParam - 1 + domainSize) / (userSize * Math.pow(tempParam, 2)) + (domainSize - 2) / (userSize * tempParam);
        return totalVariance;
    }

    public static Double getLDPApproximateVariance(Double privacyBudget, Integer userSize, Integer domainSize) {
        BasicPair<Double, Double> tempPair;
        Double tempParam = Math.exp(privacyBudget) - 1;
        return (tempParam - 1 + domainSize) / (userSize * Math.pow(tempParam, 2));
    }

    /**
     * 调用之前要保证前两个数据各项的对应关系
     * @param estimationList
     * @param lastEstimationList
     * @param pldpVarianceSum
     * @return
     */
    public static Double getDissimilarity(List<Double> estimationList, List<Double> lastEstimationList, Double pldpVarianceSum) {
        int size = estimationList.size();
        Double differSum = 0D;
        for (int i = 0; i < size; i++) {
            differSum += Math.pow(estimationList.get(i) - lastEstimationList.get(i), 2);
        }
        return (differSum - pldpVarianceSum) / size;
    }

    public static Double getGRRError(Double privacyBudget, Integer sampleSize, Integer domainSize) {
        Double pldpVariance = getLDPVarianceSum(privacyBudget, sampleSize, domainSize);
        return pldpVariance / domainSize;
    }

    public static Double getGRRApproximateError(Double privacyBudget, Integer sampleSize, Integer domainSize) {
        return getLDPApproximateVariance(privacyBudget, sampleSize, domainSize);
    }

}
