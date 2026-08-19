package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public class MechanismUtils {



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
        Map<Double, Integer> perturbedCountGroupedByEpsilon = BasicUtils.getGroupDataCount(perturbedData);
//        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(perturbedEpsilonCount, perturbedData, domainSize);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(perturbedData, domainSize);


        // 3. PFO.Estimate 估计
        Map<Double, Double> samplingAggregationWeightedMap = PFOUtils.getAggregationWeightMap(perturbedCountGroupedByEpsilon, domainSize);
        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(groupedDataMap.keySet(), domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, samplingAggregationWeightedMap);
        return new CombinePair<>(BasicUtils.normalizeValues(estimationMap), perturbedCountGroupedByEpsilon);
    }

    @Deprecated
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
//        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(rePerturbedEpsilonCount, rePerturbData, domainSize);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(rePerturbData, domainSize);


        // 4. PFO.Estimate 估计
        Map<Double, Double> samplingAggregationWeightedMap = PFOUtils.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(groupedDataMap.keySet(), domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, samplingAggregationWeightedMap);
        return new CombinePair<>(BasicUtils.normalizeValues(estimationMap), rePerturbedEpsilonCount);
    }


}
