package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public class MechanismUtils {
    /**
     * 10. 选择最优 sampling size
     * @param samplingSizeRequirementList
     * @param privacyBudgetList
     * @param domainSize
     * @return
     */
    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, Integer domainSize) {
        Map<Integer, Integer> uniqueSamplingSizeMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithFirstAppearanceIndex(samplingSizeRequirementList));
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer originalSamplingSize;
        Double originalBudget;
        LinkedHashMap<Double, Double> newUniqueBudgetStatisticMap;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null, finalSamplingSizeIndex = null;
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

            newUniqueBudgetStatisticMap = BasicArrayUtil.getUniqueListWithStatisticList(tempBudgetList);
            tempError = PFOUtils.getGPRRErrorByGroupUserRatio(newUniqueBudgetStatisticMap, userSize, uniqueSamplingSize, domainSize);
//            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalBudgetList = tempBudgetList;
                finalSamplingSizeIndex = uniqueSamplingSizeMap.get(finalSamplingSize);
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalError, finalBudgetList, finalSamplingSizeIndex);
    }


    public static Map<Integer, Double> gRR(Double privacyBudget, List<Integer> samplingDataIndexList, Integer domainSize, Random random) {
        // 1. PFO.Perturb 扰动
//        Map<Double, List<Integer>> groupedDataMap = BasicUtils.groupByEpsilon(samplingPrivacyBudgetList, samplingDataIndexList);
        List<Integer> perturbedData = FOUtils.gRRPerturb(samplingDataIndexList, privacyBudget, domainSize, random);


        // 2. PFO.Aggregate 聚合
        Map<Integer, Double> aggregation = FOUtils.getAggregation(perturbedData, domainSize);


        // 3. PFO.Estimate 估计
        Double parameterQ = FOUtils.getGRRQ(privacyBudget, domainSize);
        Double parameterP = FOUtils.getGRRP(privacyBudget, domainSize);
        Map<Integer, Double> estimationMap = FOUtils.getEstimation(aggregation, parameterP, parameterQ);
        return BasicUtils.normalizeValues(estimationMap);
    }


    public static CombinePair<Map<Integer, Double>, Map<Double, Integer>> gPRR(List<Double> samplingPrivacyBudgetList, List<Integer> samplingDataIndexList, Integer domainSize, Random random) {
        // 1. PFO.Perturb 扰动
        Map<Double, List<Integer>> groupedDataMap = BasicUtils.groupByEpsilon(samplingPrivacyBudgetList, samplingDataIndexList);
        Map<Double, List<Integer>> perturbedData = PFOUtils.perturb(groupedDataMap, domainSize, random);


        // 2. PFO.Aggregate 聚合
        Map<Double, Integer> perturbedEpsilonCount = BasicUtils.getGroupDataCount(perturbedData);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(perturbedEpsilonCount, perturbedData, domainSize);


        // 3. PFO.Estimate 估计
        Map<Double, Double> samplingAggregationWeightedMap = PFOUtils.getAggregationWeightMap(perturbedEpsilonCount, domainSize);
        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(groupedDataMap.keySet(), domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, samplingAggregationWeightedMap);
        return new CombinePair<>(BasicUtils.normalizeValues(estimationMap), perturbedEpsilonCount);
    }

    public static CombinePair<Map<Integer, Double>, Map<Double, Integer>> enhancedGPRR(List<Double> samplingPrivacyBudgetList, List<Integer> samplingDataIndexList, Integer domainSize, Random random) {
        // 1. PFO.Perturb 扰动
        Map<Double, List<Integer>> groupedDataMap = BasicUtils.groupByEpsilon(samplingPrivacyBudgetList, samplingDataIndexList);
        Map<Double, List<Integer>> perturbedData = PFOUtils.perturb(groupedDataMap, domainSize, random);

        // 2. PFO.RePerturb 重扰动
        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOUtils.rePerturb(sortedPerturbedData, domainSize, random);
        Map<Double, List<Integer>> rePerturbData = rePerturbResult.getKey();

        // 3. PFO.Aggregate 聚合
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbData);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(rePerturbedEpsilonCount, rePerturbData, domainSize);


        // 4. PFO.Estimate 估计
        Map<Double, Double> samplingAggregationWeightedMap = PFOUtils.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(groupedDataMap.keySet(), domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, samplingAggregationWeightedMap);
        return new CombinePair<>(BasicUtils.normalizeValues(estimationMap), rePerturbedEpsilonCount);
    }


}
