package hnu.dll.utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.differential_privacy.ldp.consistent.Normalization;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.CombinePair;
import hnu.dll.special_tools.PFOTools;

import java.util.*;

public class TestUtils {
    public static Double showSMechanismInformation(List<Integer> subPositionIndexList, List<Double> subNewPrivacyBudgetList, Integer totalUserSize, Integer domainSize, Random random) {
        MyPrint.showSplitLine("*", 150);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
        Integer samplingSize = subPositionIndexList.size();
        System.out.println("1.1. 抽样出来的用户位置数据的实际信息:");
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        Map<Integer, Double> realIndexStatistic = BasicUtils.getStatisticByCount(positionIndexCountMap);
        ArrayList<Double> realIndexList = new ArrayList<>(realIndexStatistic.values());
        MyPrint.showMap(realIndexStatistic, "; ");

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

//        MyPrint.showSplitLine("*", 150);
        System.out.println("如果不重扰动，直接进行估计:");
        Map<Double, Double> newParameterQ = PFOTools.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOTools.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Double, Double> aggregationWeightMapWithoutRePerturb = PFOTools.getAggregationWeightMap(subBudgetCountMap, domainSize);
        Double errorWithoutRePerturb = PFOTools.getGPRRErrorBySpecificUsers(subBudgetCountMap, totalUserSize, samplingSize, domainSize);
        Map<Double, Map<Integer, Double>> aggregationWithoutRePerturb = PFOTools.getAggregation(subBudgetCountMap, perturbedData, domainSize);
        Map<Integer, Double> estimationWithoutRePerturb = PFOTools.getEstimation(aggregationWithoutRePerturb, newParameterP, newParameterQ, aggregationWeightMapWithoutRePerturb);
        System.out.println("aggregation weighted map without re-perturb:");
        MyPrint.showMap(aggregationWeightMapWithoutRePerturb, "; ");
        System.out.println("error without re-perturb: " + errorWithoutRePerturb);
        ArrayList<Double> estimationWithoutRePerturbList = new ArrayList<>(estimationWithoutRePerturb.values());
        Double twoNorm = BasicCalculation.get2Norm(estimationWithoutRePerturbList, realIndexList);
        System.out.println("two norm without re-perturb: " + twoNorm);
        List<Double> normalizedEstimationWithoutRePerturbList = Normalization.normalizedBySimplexProjection(estimationWithoutRePerturbList);
        twoNorm = BasicCalculation.get2Norm(normalizedEstimationWithoutRePerturbList, realIndexList);
        System.out.println("two norm (normalized) without re-perturb: " + twoNorm);
        System.out.println("estimation without re-perturb:");
        MyPrint.showMap(estimationWithoutRePerturb, "; ");
        System.out.println("normalized estimation list without re-perturb:");
        MyPrint.showList(normalizedEstimationWithoutRePerturbList, "; ");


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

//        System.out.println("newParameterQ:");
//        MyPrint.showMap(newParameterQ);
//        System.out.println("newParameterP:");
//        MyPrint.showMap(newParameterP);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap (alpha):");
        MyPrint.showMap(newAggregationWeightMap, "; ");
        Map<Double, Map<Integer, Double>> aggregation = PFOTools.getAggregation(rePerturbedEpsilonCount, rePerturbMap, domainSize);
        Double grrError = PFOTools.getGPRRErrorBySpecificUsers(rePerturbedEpsilonCount, totalUserSize, samplingSize, domainSize);
        System.out.println("grrError: " + grrError);
        Map<Integer, Double> estimationMap = PFOTools.getEstimation(aggregation, newParameterP, newParameterQ, newAggregationWeightMap);
        ArrayList<Double> estimationList = new ArrayList<>(estimationMap.values());
        twoNorm = BasicCalculation.get2Norm(estimationList, realIndexList);
        System.out.println("two norm: " + twoNorm);
        List<Double> normalizedEstimationList = Normalization.normalizedBySimplexProjection(estimationList);
        MyPrint.showList(normalizedEstimationList, "; ");
        twoNorm = BasicCalculation.get2Norm(normalizedEstimationList, realIndexList);
        System.out.println("two norm (normalized): " + twoNorm);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap, "; ");
        System.out.println("normalized estimation list:");
        MyPrint.showList(normalizedEstimationList, "; ");


        MyPrint.showSplitLine("*", 150);
        System.out.println("1.6. dissimilarity相关计算");
        Double pldpVarianceSum = PFOTools.getPLDPVarianceSumBySpecificUsers(rePerturbedEpsilonCount, domainSize);
        System.out.println("pldpVarianceSum: " + pldpVarianceSum);
        estimationList = new ArrayList<>(estimationMap.values());
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0D, estimationList.size());
        Double dissimilarity = PFOTools.getDissimilarity(estimationList, lastEstimationList, pldpVarianceSum);
        System.out.println("dissimilarity: " + dissimilarity);
        MyPrint.showSplitLine("*", 150);

        return dissimilarity;
    }


    public static void showRMechanismInformation(Double dissimilarity, List<Integer> subPositionIndexList, List<Double> subNewPrivacyBudgetList, Integer totalUserSize, Integer domainSize, Random random) {


        MyPrint.showSplitLine("*", 150);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
        System.out.println("2.2. 抽样出来的用户位置数据的实际信息:");
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        Map<Integer, Double> realIndexStatistic = BasicUtils.getStatisticByCount(positionIndexCountMap);
        MyPrint.showMap(realIndexStatistic, "; ");
        ArrayList<Double> realIndexList = new ArrayList<>(realIndexStatistic.values());
        MyPrint.showList(realIndexList, "; ");

        MyPrint.showSplitLine("*", 150);
        System.out.println("2.3. 抽样出来的用户隐私预算的统计信息:");
        System.out.println("subBudgetCountMap:");
        MyPrint.showMap(subBudgetCountMap, "; ");
        System.out.println("sub budget statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(subBudgetCountMap), "; ");


        MyPrint.showSplitLine("*", 150);
        System.out.println("2.1. Error信息:");
        Integer samplingSize = subPositionIndexList.size();
        System.out.println("sampling size (n_pp,opt): " + samplingSize);
//        Double gprrError = PFOTools.getGPRRErrorByGroupUserRatio(originalGroupRatioMap, originalTotalUserSize, samplingSize, domainSize);
        Double gprrError = PFOTools.getGPRRErrorBySpecificUsers(subBudgetCountMap, totalUserSize, samplingSize, domainSize);
        System.out.println("dissimilarity: " + dissimilarity);
        System.out.println("grrError: " + gprrError);
        String judge = dissimilarity > gprrError ? "dis > err" : "dis <= err";
        System.out.println(judge);


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

        System.out.println("如果不重扰动，直接进行估计:");
        Map<Double, Double> newParameterQ = PFOTools.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOTools.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Double, Double> aggregationWeightMapWithoutRePerturb = PFOTools.getAggregationWeightMap(subBudgetCountMap, domainSize);
        Double errorWithoutRePerturb = PFOTools.getGPRRErrorBySpecificUsers(subBudgetCountMap, totalUserSize, samplingSize, domainSize);
        Map<Double, Map<Integer, Double>> aggregationWithoutRePerturb = PFOTools.getAggregation(subBudgetCountMap, perturbedData, domainSize);
        Map<Integer, Double> estimationWithoutRePerturb = PFOTools.getEstimation(aggregationWithoutRePerturb, newParameterP, newParameterQ, aggregationWeightMapWithoutRePerturb);
        System.out.println("aggregation weighted map without re-perturb:");
        MyPrint.showMap(aggregationWeightMapWithoutRePerturb, "; ");
        System.out.println("error without re-perturb: " + errorWithoutRePerturb);
        ArrayList<Double> estimationWithoutRePerturbList = new ArrayList<>(estimationWithoutRePerturb.values());
        Double twoNorm = BasicCalculation.get2Norm(estimationWithoutRePerturbList, realIndexList);
        System.out.println("two norm without re-perturb: " + twoNorm);
        List<Double> normalizedEstimationWithoutRePerturbList = Normalization.normalizedBySimplexProjection(estimationWithoutRePerturbList);
        twoNorm = BasicCalculation.get2Norm(normalizedEstimationWithoutRePerturbList, realIndexList);
        System.out.println("two norm (normalized) without re-perturb: " + twoNorm);
        System.out.println("estimation without re-perturb:");
        MyPrint.showMap(estimationWithoutRePerturb, "; ");
        System.out.println("normalized estimation list without re-perturb:");
        MyPrint.showList(normalizedEstimationWithoutRePerturbList, "; ");



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
//        System.out.println("newParameterQ:");
//        MyPrint.showMap(newParameterQ);
//        System.out.println("newParameterP:");
//        MyPrint.showMap(newParameterP);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap (alpha):");
        MyPrint.showMap(newAggregationWeightMap, "; ");
        Map<Double, Map<Integer, Double>> aggregation = PFOTools.getAggregation(rePerturbedEpsilonCount, rePerturbMap, domainSize);
        Double grrError = PFOTools.getGPRRErrorBySpecificUsers(rePerturbedEpsilonCount, totalUserSize, samplingSize, domainSize);
        System.out.println("grrError: " + grrError);
        Map<Integer, Double> estimationMap = PFOTools.getEstimation(aggregation, newParameterP, newParameterQ, newAggregationWeightMap);
        ArrayList<Double> estimationList = new ArrayList<>(estimationMap.values());
        System.out.println("estimation list:");
        MyPrint.showList(estimationList, "; ");
        twoNorm = BasicCalculation.get2Norm(estimationList, realIndexList);
        System.out.println("two norm: " + twoNorm);
        List<Double> normalizedEstimationList = Normalization.normalizedBySimplexProjection(estimationList);
        System.out.println("normalized estimation list:");
        MyPrint.showList(normalizedEstimationList, "; ");
        twoNorm = BasicCalculation.get2Norm(normalizedEstimationList, realIndexList);
        System.out.println("two norm (normalized): " + twoNorm);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap, "; ");
//        System.out.println("normalized estimation list:");
//        MyPrint.showList(normalizedEstimationList, "; ");
    }


}
