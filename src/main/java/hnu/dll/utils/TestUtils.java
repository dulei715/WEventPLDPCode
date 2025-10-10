package hnu.dll.utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.CombinePair;
import hnu.dll.special_tools.PFOTools;

import java.util.*;

public class TestUtils {
    public static Double showSMechanismInformation(List<Integer> subPositionIndexList, List<Double> subNewPrivacyBudgetList, Integer domainSize, Random random) {
        MyPrint.showSplitLine("*", 150);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
        System.out.println("1.1. 抽样出来的用户位置数据的实际信息:");
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(positionIndexCountMap), "; ");

        MyPrint.showSplitLine("*", 150);
        System.out.println("1.2. 抽样出来的用户隐私预算的统计信息:");
        System.out.println("subBudgetCountMap:");
        MyPrint.showMap(subBudgetCountMap, "; ");
        System.out.println("sub budget statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(subBudgetCountMap), "; ");

        MyPrint.showSplitLine("*", 150);
        System.out.println("1.3. 扰动信息");
        Set<Double> newBudgetSet = subBudgetCountMap.keySet();
        Map<Double, List<Integer>> groupDataMap = BasicUtils.groupByEpsilon(subNewPrivacyBudgetList, subPositionIndexList);
//        System.out.println("groupDataCount:");
//        Map<Integer, Integer> groupDataCount = BasicUtils.getCountMapByGroup(groupDataMap);
//        MyPrint.showMap(groupDataCount);
        Map<Double, List<Integer>> perturbedData = PFOTools.perturb(groupDataMap, domainSize, random);
//        System.out.println("perturbedData:");
//        MyPrint.showMap(perturbedData);
        Map<Integer, Integer> perturbedCountMap = BasicUtils.getCountMapByGroup(perturbedData);
        System.out.println("perturbedCountMap:");
        MyPrint.showMap(perturbedCountMap, "; ");
        Map<Integer, Double> perturbedStatisticMap = BasicUtils.getStatisticByCount(perturbedCountMap);
        MyPrint.showMap(perturbedStatisticMap, "; ");



        MyPrint.showSplitLine("*", 150);
        System.out.println("1.4. 重扰动与聚合信息:");
        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOTools.rePerturb(sortedPerturbedData, domainSize, random);
        Map<Double, List<Integer>> rePerturbMap = rePerturbResult.getKey();
        Integer newTotalUserSize = rePerturbResult.getValue();
//        System.out.println("rePerturbMap");
//        MyPrint.showMap(rePerturbMap);
        System.out.println("totalUserSize: " + newTotalUserSize);
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbMap);
        System.out.println("rePerturbedEpsilonCount:");
        MyPrint.showMap(rePerturbedEpsilonCount, "; ");
        Map<Integer, Integer> rePerturbCountMap = BasicUtils.getCountMapByGroup(rePerturbMap);
        System.out.println("rePerturbCountMap:");
        MyPrint.showMap(rePerturbCountMap, "; ");
        Map<Integer, Double> rePerturbStatisticMap = BasicUtils.getStatisticByCount(rePerturbCountMap);
        System.out.println("rePerturbStatisticMap:");
        MyPrint.showMap(rePerturbStatisticMap, "; ");


        MyPrint.showSplitLine("*", 150);
        System.out.println("1.5. 无偏统计信息:");
        Map<Double, Double> newParameterQ = PFOTools.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOTools.getGeneralRandomResponseParameterP(newParameterQ);
//        System.out.println("newParameterQ:");
//        MyPrint.showMap(newParameterQ);
//        System.out.println("newParameterP:");
//        MyPrint.showMap(newParameterP);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap (alpha):");
        MyPrint.showMap(newAggregationWeightMap, "; ");
        Map<Double, Map<Integer, Double>> aggregation = PFOTools.getAggregation(rePerturbedEpsilonCount, rePerturbMap, domainSize);
        Map<Integer, Double> estimationMap = PFOTools.getEstimation(aggregation, newParameterP, newParameterQ, newAggregationWeightMap);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap, "; ");


        MyPrint.showSplitLine("*", 150);
        System.out.println("1.6. dissimilarity相关计算");
        Double pldpVarianceSum = PFOTools.getPLDPVarianceSumBySpecificUsers(rePerturbedEpsilonCount, domainSize);
        System.out.println("pldpVarianceSum: " + pldpVarianceSum);
        List<Double> estimationList = new ArrayList<>(estimationMap.values());
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0D, estimationList.size());
        Double dissimilarity = PFOTools.getDissimilarity(estimationList, lastEstimationList, pldpVarianceSum);
        System.out.println("dissimilarity: " + dissimilarity);
        MyPrint.showSplitLine("*", 150);

        return dissimilarity;
    }


    public static void showRMechanismInformation(Double dissimilarity, List<Integer> subPositionIndexList, List<Double> subNewPrivacyBudgetList, Integer originalTotalUserSize, Map<Double, Double> originalGroupRatioMap, Integer domainSize, Random random) {
        MyPrint.showSplitLine("*", 150);
        System.out.println("2.1. Error信息:");
        Integer samplingSize = subPositionIndexList.size();
        System.out.println("sampling size (n_pp,opt): " + samplingSize);
        Double gprrError = PFOTools.getGPRRErrorByGroupUserRatio(originalGroupRatioMap, originalTotalUserSize, samplingSize, domainSize);
        System.out.println("dissimilarity: " + dissimilarity);
        System.out.println("grrError: " + gprrError);
        String judge = dissimilarity > gprrError ? "dis > err" : "dis > err";
        System.out.println(judge);

        MyPrint.showSplitLine("*", 150);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
        System.out.println("2.2. 抽样出来的用户位置数据的实际信息:");
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(positionIndexCountMap), "; ");

        MyPrint.showSplitLine("*", 150);
        System.out.println("2.3. 抽样出来的用户隐私预算的统计信息:");
        System.out.println("subBudgetCountMap:");
        MyPrint.showMap(subBudgetCountMap, "; ");
        System.out.println("sub budget statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(subBudgetCountMap), "; ");




        MyPrint.showSplitLine("*", 150);
        System.out.println("2.4. 扰动信息");
        Set<Double> newBudgetSet = subBudgetCountMap.keySet();
        Map<Double, List<Integer>> groupDataMap = BasicUtils.groupByEpsilon(subNewPrivacyBudgetList, subPositionIndexList);
//        System.out.println("groupDataCount:");
//        Map<Integer, Integer> groupDataCount = BasicUtils.getCountMapByGroup(groupDataMap);
//        MyPrint.showMap(groupDataCount);
        Map<Double, List<Integer>> perturbedData = PFOTools.perturb(groupDataMap, domainSize, random);
//        System.out.println("perturbedData:");
//        MyPrint.showMap(perturbedData);
        Map<Integer, Integer> perturbedCountMap = BasicUtils.getCountMapByGroup(perturbedData);
        System.out.println("perturbedCountMap:");
        MyPrint.showMap(perturbedCountMap, "; ");
        Map<Integer, Double> perturbedStatisticMap = BasicUtils.getStatisticByCount(perturbedCountMap);
        MyPrint.showMap(perturbedStatisticMap, "; ");



        MyPrint.showSplitLine("*", 150);
        System.out.println("2.5. 重扰动与聚合信息:");
        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOTools.rePerturb(sortedPerturbedData, domainSize, random);
        Map<Double, List<Integer>> rePerturbMap = rePerturbResult.getKey();
        Integer newTotalUserSize = rePerturbResult.getValue();
//        System.out.println("rePerturbMap");
//        MyPrint.showMap(rePerturbMap);
        System.out.println("totalUserSize: " + newTotalUserSize);
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbMap);
        System.out.println("rePerturbedEpsilonCount:");
        MyPrint.showMap(rePerturbedEpsilonCount, "; ");
        Map<Integer, Integer> rePerturbCountMap = BasicUtils.getCountMapByGroup(rePerturbMap);
        System.out.println("rePerturbCountMap:");
        MyPrint.showMap(rePerturbCountMap, "; ");
        Map<Integer, Double> rePerturbStatisticMap = BasicUtils.getStatisticByCount(rePerturbCountMap);
        System.out.println("rePerturbStatisticMap:");
        MyPrint.showMap(rePerturbStatisticMap, "; ");


        MyPrint.showSplitLine("*", 150);
        System.out.println("2.6. 无偏统计信息:");
        Map<Double, Double> newParameterQ = PFOTools.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOTools.getGeneralRandomResponseParameterP(newParameterQ);
//        System.out.println("newParameterQ:");
//        MyPrint.showMap(newParameterQ);
//        System.out.println("newParameterP:");
//        MyPrint.showMap(newParameterP);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap (alpha):");
        MyPrint.showMap(newAggregationWeightMap, "; ");
        Map<Double, Map<Integer, Double>> aggregation = PFOTools.getAggregation(rePerturbedEpsilonCount, rePerturbMap, domainSize);
        Map<Integer, Double> estimationMap = PFOTools.getEstimation(aggregation, newParameterP, newParameterQ, newAggregationWeightMap);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap, "; ");
    }


}
