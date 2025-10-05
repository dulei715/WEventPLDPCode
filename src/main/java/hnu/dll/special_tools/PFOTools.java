package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.FrequencyOracle;
import cn.edu.dll.struct.BasicPair;
import cn.edu.dll.struct.CombinePair;
import hnu.dll.structure.OptimalSelectionStruct;

import java.util.*;

public class PFOTools {
    /**
     * 1. 根据不同的隐私预算集合和取值域计算GRR机制下参数Q的集合
     * @param privacyBudgetSet
     * @param valueDomainSize
     * @return
     */
    public static Map<Double, Double> getGeneralRandomResponseParameterQ(Set<Double> privacyBudgetSet, Integer valueDomainSize) {
        Map<Double, Double> qMap = new TreeMap<>();
        for (Double budget : privacyBudgetSet) {
            qMap.put(budget, 1.0 / (Math.exp(budget) + valueDomainSize - 1));
        }
        return qMap;
    }

    /**
     * 2. 根据不同的隐私预算集合和取值域计算GRR机制下参数P的集合
     * @param qMap
     * @return
     */
    public static Map<Double, Double> getGeneralRandomResponseParameterP(Map<Double, Double> qMap) {
        Map<Double, Double> pMap = new TreeMap<>();
        Double budget;
        for (Map.Entry<Double, Double> entry : qMap.entrySet()) {
            budget = entry.getKey();
            pMap.put(budget, Math.exp(budget) * entry.getValue());
        }
        return pMap;
    }



    /**
     * 3. 获取GRR的方差
     * @param epsilon
     * @param userSize
     * @param domainSize
     * @return
     */
    public static Double getGeneralizedRandomResponseApproximateVariance(Double epsilon, Integer userSize, Integer domainSize) {
        Double ePowEpsilon = Math.exp(epsilon);
        return userSize * (ePowEpsilon + domainSize - 2) / Math.pow(ePowEpsilon - 1, 2);
    }

    /**
     * 4. 获取个性化聚合参数
     * @param distinctBudgetCountMap
     * @param domainSize
     * @return
     */
    public static Map<Double, Double> getAggregationWeightMap(Map<Double, Integer> distinctBudgetCountMap, Integer domainSize) {
        TreeMap<Double, Double> distinctVarianceMap = new TreeMap<>();
        Double epsilon, variance;
        Integer userSize;
        Double totalVariance = 0D;
        Map<Double, Double> aggregationWeightMap;
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            epsilon = entry.getKey();
            userSize = distinctBudgetCountMap.get(epsilon);
            variance = getGeneralizedRandomResponseApproximateVariance(epsilon, userSize, domainSize);
            distinctVarianceMap.put(epsilon, variance);
            totalVariance += variance;
        }
        aggregationWeightMap = new TreeMap<>();
        for (Map.Entry<Double, Double> entry : distinctVarianceMap.entrySet()) {
            epsilon = entry.getKey();
            variance = entry.getValue();
            aggregationWeightMap.put(epsilon, variance / totalVariance);
        }
        return aggregationWeightMap;
    }


//    public static List<Double> getEstimation(List<Double> obfuscatedCountList, List<Double> distinctBudgetCount, List<Double> qList, List<Double> pList) {
//        Double paramA = 0D, paramB = 0D;
//        int size = distinctBudgetCount.size();
//        Double tempG;
//        Double tempQ;
//        int sizeRes = obfuscatedCountList.size();
//        List<Double> result = new ArrayList<>(sizeRes);
//        for (int i = 0; i < size; i++) {
//            tempG = distinctBudgetCount.get(i);
//            tempQ = qList.get(i);
//            paramA += tempG * tempQ;
//            paramB += tempG * (pList.get(i) - tempQ);
//        }
//        for (Double tempFrequency : obfuscatedCountList) {
//            result.add((tempFrequency-paramA) / paramB);
//        }
//        return result;
//    }

    /**
     * 5. 获取估计值
     * @param obfucatedEstimationList
     * @param totalUserSize
     * @param distinctBudgetCountMap
     * @param distinctPMap
     * @param distinctQMap
     * @param aggregationWeightMap
     * @return
     */
    public static List<Double> getEstimation(List<Double> obfucatedEstimationList, Integer totalUserSize, Map<Double, Integer> distinctBudgetCountMap, Map<Double, Double> distinctPMap, Map<Double, Double> distinctQMap, Map<Double, Double> aggregationWeightMap) {
        Double paramA = 0D, paramB = 0D;
        Double tempG;
        Double tempQ, tempP;
        Double tempWeight;
        Set<Double> distinctEpsilonSet = distinctBudgetCountMap.keySet();
        List<Double> result = new ArrayList<>(distinctEpsilonSet.size());
        for (Double epsilon : distinctEpsilonSet) {
            tempG = distinctBudgetCountMap.get(epsilon) * 1.0 / totalUserSize;
            tempQ = distinctQMap.get(epsilon);
            tempP = distinctPMap.get(epsilon);
            tempWeight = aggregationWeightMap.get(epsilon);
            paramA += tempWeight * tempG * tempQ;
            paramB += tempWeight * tempG * (tempP - tempQ);
        }
        for (Double tempObfuscatedEstimation : obfucatedEstimationList) {
            result.add((tempObfuscatedEstimation-paramA) / paramB);
        }
        return result;
    }

    /**
     * 获取每个取值的方差之和
     * @param distinctBudgetCountMap
     * @param distinctPMap
     * @param distinctQMap
     * @param aggregationWeightMap
     * @param userSize
     * @param domainSize
     * @return
     */
    public static Double getPLDPVarianceSum(Map<Double, Integer> distinctBudgetCountMap, Map<Double, Double> distinctPMap, Map<Double, Double> distinctQMap, Map<Double, Double> aggregationWeightMap, Integer userSize, Integer domainSize) {
        Double paramA = 0D, paramB = 0D, paramC = 0D;
        Double tempG, budget, personalizedWeight, result;
        Double tempQ, tempP;
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            tempG = entry.getValue() * 1.0 / userSize;
            budget = entry.getKey();
            tempQ = distinctQMap.get(budget);
            tempP = distinctPMap.get(budget);
            personalizedWeight = aggregationWeightMap.get(budget);
            paramA += personalizedWeight * personalizedWeight * tempG * (1 - tempQ) * tempQ;
            paramB += personalizedWeight * tempG * (tempP - tempQ);
            paramC += personalizedWeight * personalizedWeight * tempG * (1 - tempP - tempQ) * (tempP - tempQ);
        }
        paramB = userSize * paramB * paramB;
        result = (domainSize * paramA + paramC) / paramB;
        return result;
    }

//    public static Double getPLDPVarianceSumStar(Integer valueSize, List<Double> distinctBudgetFrequencyList, List<Double> qList, List<Double> pList, Integer sampleSize) {
//        Double result = 0D, paramA = 0D, paramB = 0D;
//        int size = distinctBudgetFrequencyList.size();
//        Double tempG;
//        Double tempQ;
//        for (int i = 0; i < size; i++) {
//            tempG = distinctBudgetFrequencyList.get(i);
//            tempQ = qList.get(i);
//            paramA += tempG * (1 - tempQ) * tempQ;
//            paramB += tempG * (pList.get(i) - tempQ);
//        }
//        paramA *= valueSize;
//        paramB = sampleSize * paramB * paramB;
//        return paramA / paramB;
//    }

    /**
     * 6. 计算Error
     * @param distinctBudgetCountMap
     * @param userSize
     * @param sampleSize
     * @param domainSize
     * @return
     */
    public static Double getGPRRError(Map<Double, Integer> distinctBudgetCountMap, Integer userSize, Integer sampleSize, Integer domainSize) {
        Map<Double, Double> distinctQMap = getGeneralRandomResponseParameterQ(distinctBudgetCountMap.keySet(), domainSize);
        Map<Double, Double> distinctPMap = getGeneralRandomResponseParameterP(distinctQMap);
        Map<Double, Double> aggregationWeightMap = getAggregationWeightMap(distinctBudgetCountMap, domainSize);
        Double sampleVariance = (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
        Double pldpVariance = getPLDPVarianceSum(distinctBudgetCountMap, distinctPMap, distinctQMap, aggregationWeightMap, sampleSize, domainSize);
        return (sampleVariance + pldpVariance) / domainSize;
    }

    /**
     * 7. 计算dissimilarity
     * @param estimationList
     * @param lastEstimationList
     * @param pldpVarianceSum
     * @return
     */
    public static Double getDissimilarity(List<Double> estimationList, List<Double> lastEstimationList, Double pldpVarianceSum) {
        int size = estimationList.size();
        Double differSum = 0D;
        for (int i = 0; i < size; i++) {
            differSum += estimationList.get(i) - lastEstimationList.get(i);
        }
        return (differSum - pldpVarianceSum) / size;
    }

    /**
     * 8. 扰动
     * @param originalDataMap
     * @return
     */
    public static Map<Double, List<Integer>> perturb(Map<Double, List<Integer>> originalDataMap, Integer domainSize, Random random) {
        Double epsilon;
        List<Integer> originalDataList;
        List<Integer> obfuscatedDataList;
        FrequencyOracle<Integer, Integer> frequencyOracle;
        Integer obfuscatedData;
        Map<Double, List<Integer>> result = new TreeMap<>();
        for (Map.Entry<Double, List<Integer>> entry : originalDataMap.entrySet()) {
            epsilon = entry.getKey();
            originalDataList = entry.getValue();
            obfuscatedDataList = new ArrayList<>(originalDataList.size());
//            frequencyOracle = this.distinctFrequencyOracleMap.get(epsilon);
            for (Integer data : originalDataList) {
//                obfuscatedData = frequencyOracle.perturb(data);
                obfuscatedData = FOTools.gRRPerturb(epsilon, data, domainSize, random);
                obfuscatedDataList.add(obfuscatedData);
            }
            result.put(epsilon, obfuscatedDataList);
        }
        return result;
    }


    /**
     * 9. 计算重扰动
     * @param perturbedDataMap
     * @return
     */
    public static CombinePair<Map<Double, List<Integer>>, Integer> rePerturb(TreeMap<Double, List<Integer>> perturbedDataMap, Integer domainSize, Random random) {
        Integer totalSize = 0;
        // 保证 epsilon 从小到大排列
        List<Double> epsilonSortedList = new ArrayList<>(perturbedDataMap.keySet());
        Integer epsilonListSize = epsilonSortedList.size();
//        FrequencyOracle<Integer, Integer> smallFO, largeFO;
        Double smallEpsilon, largeEpsilon, smallP, smallQ, largeP, largeQ;
        BasicPair<Double, Double> tempPair;
        Double alpha;
        Integer rePerturbIndex;
        List<Integer> currentObfuscatedList, tempObfuscatedList;
        TreeMap<Double, List<Integer>> enhancedMap = new TreeMap<>();
        List<Integer> enhancedIndexList;
        for (int i = 0; i < epsilonListSize; ++i) {
            enhancedIndexList = new ArrayList<>();
            smallEpsilon = epsilonSortedList.get(i);
//            smallFO = this.distinctFrequencyOracleMap.get(smallEpsilon);
//            smallP = smallFO.getP();
            smallP = FOTools.getGRRP(smallEpsilon, domainSize);
//            smallQ = smallFO.getQ();
            smallQ = FOTools.getGRRQ(smallEpsilon, domainSize);
            currentObfuscatedList = perturbedDataMap.get(epsilonSortedList.get(i));
            enhancedIndexList.addAll(currentObfuscatedList);
            for (int j = i + 1; j < epsilonListSize; ++j) {
                largeEpsilon = epsilonSortedList.get(j);
//                largeFO = this.distinctFrequencyOracleMap.get(largeEpsilon);
//                largeP = largeFO.getP();
//                largeQ = largeFO.getQ();
                largeP = FOTools.getGRRP(largeEpsilon, domainSize);
                largeQ = FOTools.getGRRQ(largeEpsilon, domainSize);
                // todo:这里只实现了GRR的enhancement
                tempPair = PerturbUtils.getGRRRePerturbParameters(smallP, smallQ, largeP, largeQ, domainSize);
                alpha = tempPair.getKey();
//                beta = tempPair.getValue();
//                rePerturbIndex = PerturbUtils.grrPerturb(domainSize, i, alpha, random);
                tempObfuscatedList = perturbedDataMap.get(epsilonSortedList.get(j));
                for (Integer tempIndex : tempObfuscatedList) {
                    rePerturbIndex = FOTools.gRRPerturb(alpha, tempIndex, domainSize, random);
                    enhancedIndexList.add(rePerturbIndex);
                }

            }
            enhancedMap.put(smallEpsilon, enhancedIndexList);
            totalSize += enhancedIndexList.size();
        }
        return new CombinePair<>(enhancedMap, totalSize);
    }

    /**
     * 10. 选择最优 sampling size
     * @param samplingSizeRequirementList
     * @param privacyBudgetList
     * @param domainSize
     * @return
     */
    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, Integer domainSize) {
        LinkedHashMap<Integer, Integer> uniqueSamplingSizeMap = BasicArrayUtil.getUniqueListWithCountList(samplingSizeRequirementList);
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer originalSamplingSize;
        Double originalBudget;
        LinkedHashMap<Double, Integer> newUniqueBudgetCountMap, finalUniqueBudgetCountMap = null;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null;
        for (Integer uniqueSamplingSize : uniqueSamplingSizeSet) {
            tempBudgetList = new ArrayList<>();
            for (int i = 0; i < userSize; ++i) {
                originalSamplingSize = samplingSizeRequirementList.get(i);
                originalBudget = privacyBudgetList.get(i);
                if (originalSamplingSize < uniqueSamplingSize) {
//                    tempBudgetList.add(originalBudget * originalSamplingSize / uniqueSamplingSize);
                    tempBudgetList.add(originalBudget / Math.ceil(uniqueSamplingSize * 1.0 / originalSamplingSize));
                } else {
                    tempBudgetList.add(originalBudget);
                }
            }

            newUniqueBudgetCountMap = BasicArrayUtil.getUniqueListWithCountList(tempBudgetList);
//            newUniqueBudgetStatisticMap = new HashMap<>();
//            for (Map.Entry<Double, Integer> entry : newUniqueBudgetCountMap.entrySet()) {
//                newUniqueBudgetStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / userSize);
//            }
            tempError = PFOTools.getGPRRError(newUniqueBudgetCountMap, userSize, uniqueSamplingSize, domainSize);
            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalBudgetList = tempBudgetList;
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalError, finalBudgetList);
    }


    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

    }
}
