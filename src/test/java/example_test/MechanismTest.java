package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.foImp.GeneralizedRandomizedResponse;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.BasicPair;
import cn.edu.dll.struct.CombinePair;
import hnu.dll.special_tools.PFOTools;
import hnu.dll.special_tools.PersonalizedFrequencyOracle;
import hnu.dll.special_tools.impl.GeneralizedPersonalizedRandomResponse;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class MechanismTest {
    public Random random;
    public List<Integer> windowSizeList;
    public List<Double> budgetList;
    public List<Integer> samplingSizeList;
    public List<Integer> positionIndexList;

    public Map<Double, Integer> budgetCountMap;
    public Map<Integer, Integer> windowSizeCountMap;

    Integer domainSize;

    Integer userSize;

    public PersonalizedFrequencyOracle<GeneralizedRandomizedResponse> pfo;

    public static final Double[] BudgetCandidate = new Double[]{
            0.2, 0.4, 0.6, 0.8
    };

    public static final String[] PositionCandidate = new String[] {
            "A", "B", "C", "D", "E"
    };

    public void initializeParameters() {
        this.domainSize = 5;
        this.userSize = 2000;
        budgetCountMap = BasicArrayUtil.getUniqueListWithCountList(this.budgetList);
        windowSizeCountMap = BasicArrayUtil.getUniqueListWithCountList(this.windowSizeList);
        this.pfo = new GeneralizedPersonalizedRandomResponse(domainSize, new TreeMap<>(budgetCountMap), GeneralizedRandomizedResponse.class);
        samplingSizeList = new ArrayList<>(userSize);
        for (Integer windowSize : windowSizeList) {
            samplingSizeList.add((int)Math.floor(userSize * 1.0 / (2 * windowSize)));
        }
    }

//    @Before
//    public void init() {
//        this.windowSizeList = new ArrayList<>();
//        this.budgetList = new ArrayList<>();
//        this.samplingSizeList = new ArrayList<>();
//        int userRepeatSize = 125;
//        this.userSize = userRepeatSize * 16;
//        for (int i = 0; i < userRepeatSize; ++i) {
//            this.windowSizeList.add(3);     // 1 *
//            this.windowSizeList.add(2);     // 2
//            this.windowSizeList.add(3);     // 3
//            this.windowSizeList.add(3);     // 4
//            this.windowSizeList.add(1);     // 5
//            this.windowSizeList.add(2);     // 6
//            this.windowSizeList.add(4);     // 7
//            this.windowSizeList.add(2);     // 8
//            this.windowSizeList.add(3);     // 9
//            this.windowSizeList.add(1);     // 10
//            this.windowSizeList.add(2);     // 11
//            this.windowSizeList.add(3);     // 12 *
//            this.windowSizeList.add(2);     // 13
//            this.windowSizeList.add(2);     // 14
//            this.windowSizeList.add(3);     // 15
//            this.windowSizeList.add(3);     // 16 *
//
//            this.budgetList.add(0.2);
//            this.budgetList.add(0.4);
//            this.budgetList.add(0.6);
//            this.budgetList.add(0.2);
//            this.budgetList.add(0.2);
//            this.budgetList.add(0.8);
//            this.budgetList.add(0.6);
//            this.budgetList.add(0.2);
//            this.budgetList.add(0.4);
//            this.budgetList.add(0.6);
//            this.budgetList.add(0.4);
//            this.budgetList.add(0.2);
//            this.budgetList.add(0.6);
//            this.budgetList.add(0.8);
//            this.budgetList.add(0.6);
//            this.budgetList.add(0.2);
//        }
//
//        initializeParameters();
//
//    }

    @Before
    public void init() {
        this.random = new Random(2);
        this.windowSizeList = new ArrayList<>();
        Integer userSize = 2000;
        this.budgetList = new ArrayList<>();
        Integer tempInteger;
        Integer budgetCandidateSize = BudgetCandidate.length;
        Integer positionCandidateSize = PositionCandidate.length;
        for (int i = 0; i < userSize; ++i) {
            tempInteger = random.nextInt(budgetCandidateSize) + 1;
            if (tempInteger == 4) {
                tempInteger = random.nextDouble() < 0.01 ? 4 : 3;
            }
//            else if (tempInteger == 1) {
//                tempInteger = RandomUtil.isChosen(0.01) ? 1 : 2;
//            }
            windowSizeList.add(tempInteger);
            tempInteger = random.nextInt(budgetCandidateSize);
            if (tempInteger == 0) {
                tempInteger = random.nextDouble() < 0.5 ? 0 : 1;
            }
            budgetList.add(BudgetCandidate[tempInteger]);
        }
        initializeParameters();

        positionIndexList = new ArrayList<>();
        for (int i = 0; i < userSize; ++i) {
            tempInteger = random.nextInt(positionCandidateSize);
            positionIndexList.add(tempInteger);
        }
    }

    @Test
    public void budgetCountTest() {
        MyPrint.showMap(budgetCountMap);
    }
    @Test
    public void windowSizeCountTest() {
        MyPrint.showMap(windowSizeCountMap);
    }
    @Test
    public void parametersTest() {
        Map<Double, Double> distinctQMap = this.pfo.getDistinctQMap();
        Map<Double, Double> distinctPMap = this.pfo.getDistinctPMap();
        MyPrint.showMap(distinctQMap);
        MyPrint.showSplitLine("*", 150);
        MyPrint.showMap(distinctPMap);
    }

    @Test
    public void budgetStatisticTest() {
        Integer totalCount = 0;
        Map<Double, Double> budgetStatisticMap = new TreeMap<>();
        for (Map.Entry<Double, Integer> entry : budgetCountMap.entrySet()) {
            totalCount += entry.getValue();
        }
        for (Map.Entry<Double, Integer> entry : budgetCountMap.entrySet()) {
            budgetStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / totalCount);
        }
        MyPrint.showMap(budgetStatisticMap);
    }
    @Test
    public void windowSizeStatisticTest() {
        Integer totalCount = 0;
        Map<Integer, Double> windowSizeStatisticMap = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : windowSizeCountMap.entrySet()) {
            totalCount += entry.getValue();
        }
        for (Map.Entry<Integer, Integer> entry : windowSizeCountMap.entrySet()) {
            windowSizeStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / totalCount);
        }
        MyPrint.showMap(windowSizeStatisticMap);
    }
    @Test
    public void dataStatisticTest() {
//        List<Integer> countList = new ArrayList<>(this.domainSize);

    }

    @Test
    public void samplingSizeTest() {
        MyPrint.showList(samplingSizeList);
        MyPrint.showSplitLine("*", 150);
        LinkedHashMap<Integer, Integer> samplingSizeCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingSizeList);
        MyPrint.showMap(samplingSizeCountMap);
    }

    @Test
    public void optimalSamplingSizeTest() {
        OptimalSelectionStruct optimalSelectionStruct = PFOTools.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);

        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Map<BasicPair<Integer, Double>, Integer> newPairCount = BasicUtils.countUniquePair(this.windowSizeList, newPrivacyBudgetList);
        MyPrint.showMap(newPairCount);
    }

    @Test
    public void positionTest() {
        System.out.println(this.positionIndexList);
        LinkedHashMap<Integer, Integer> positionCountMap = BasicArrayUtil.getUniqueListWithCountList(this.positionIndexList);
        MyPrint.showMap(positionCountMap);
    }

    @Test
    public void positionSubsetStatisticTest() {
        Integer samplingSize = 333;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(0, samplingSize);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        System.out.println(subPositionList);
        LinkedHashMap<String, Integer> positionCountMap = BasicArrayUtil.getUniqueListWithCountList(subPositionList);
        MyPrint.showMap(positionCountMap);
        MyPrint.showSplitLine("*", 150);
//        Integer totalUserSize = 0;
//        for (Integer count : positionCountMap.values()) {
//            totalUserSize += count;
//        }
//        System.out.println("total subset user size: " + totalUserSize);
        Map<String, Double> positionStatisticMap = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : positionCountMap.entrySet()) {
            positionStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / samplingSize);
        }
        MyPrint.showMap(positionStatisticMap);
    }

    @Test
    public void pLBDOriginTest() {
        Map<Double, Double> distinctQMap = this.pfo.getDistinctQMap();
        Map<Double, Double> distinctPMap = this.pfo.getDistinctPMap();
        Map<Double, Double> aggregationWeightMap = this.pfo.getAggregationWeightMap();



        OptimalSelectionStruct optimalSelectionStruct = PFOTools.optimalPopulationSelection(samplingSizeList, budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
        MyPrint.showSplitLine("*", 150);

        // todo: add xxx

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double newError = optimalSelectionStruct.getError();

        Map<Double, Integer> newBudgetCountMap = BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList);
//        PFOTools.rePerturb()
        // todo: add rePerturb
        Map<Double, Double> newDistinctQMap = PFOTools.getGeneralRandomResponseParameterQ(newBudgetCountMap.keySet(), domainSize);
        Map<Double, Double> newDistinctPMap = PFOTools.getGeneralRandomResponseParameterP(newDistinctQMap);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(newBudgetCountMap, domainSize);

        Double originalPLDPVarianceSum = PFOTools.getPLDPVarianceSum(budgetCountMap, distinctPMap, distinctQMap, aggregationWeightMap, userSize, domainSize);
        Double newPLDPVarianceSum = PFOTools.getPLDPVarianceSum(newBudgetCountMap, newDistinctPMap, newDistinctQMap, newAggregationWeightMap, userSize, domainSize);


//        MyPrint.showMap(distinctPMap);
//        MyPrint.showMap(distinctQMap);
        System.out.println(originalPLDPVarianceSum);
        System.out.println(newPLDPVarianceSum);



        MyPrint.showSplitLine("*", 120);
        List<Double> estimationList = new ArrayList<>();
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0D, this.domainSize);
        estimationList.add(0.22);
        estimationList.add(0.2);
        estimationList.add(0.18);
        estimationList.add(0.22);
        estimationList.add(0.18);
        Double dissimilarity = PFOTools.getDissimilarity(estimationList, lastEstimationList, originalPLDPVarianceSum);
        System.out.println(dissimilarity);
//        PFOTools.getGPRRError()
    }

    @Test
    public void pLBDTest() {
        Map<Double, Double> distinctQMap = this.pfo.getDistinctQMap();
        Map<Double, Double> distinctPMap = this.pfo.getDistinctPMap();
        Map<Double, Double> aggregationWeightMap = this.pfo.getAggregationWeightMap();
//        System.out.println("q map: ");
//        MyPrint.showMap(distinctQMap);
//        System.out.println("p map: ");
//        MyPrint.showMap(distinctPMap);
//        MyPrint.showSplitLine("*", 150);

//        MyPrint.showMap(new TreeMap<>(this.budgetCountMap));
//        MyPrint.showMap(BasicUtils.getStatisticByCount(this.budgetCountMap));
//        MyPrint.showSplitLine("*", 150);

        OptimalSelectionStruct optimalSelectionStruct = PFOTools.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
//        System.out.println(optimalSelectionStruct);
//        MyPrint.showSplitLine("*", 150);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();



        // time slot 1
        Integer samplingSize = 333;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(0, samplingSize);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(0, samplingSize);
//        System.out.println(subPositionList);
//        System.out.println(subNewPrivacyBudgetList);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(positionIndexCountMap), "; ");
//        System.out.println("budgetCountMap:");
//        MyPrint.showMap(budgetCountMap, "; ");
//        System.out.println("budget statistic map:");
//        MyPrint.showMap(BasicUtils.getStatisticByCount(budgetCountMap), "; ");
        Set<Double> newBudgetSet = subBudgetCountMap.keySet();

        MyPrint.showSplitLine("*", 150);

        Map<Double, List<Integer>> groupDataMap = BasicUtils.groupByEpsilon(subNewPrivacyBudgetList, subPositionIndexList);
//        System.out.println("groupDataMap:");
//        MyPrint.showMap(groupDataMap);
        Map<Double, List<Integer>> perturbedData = PFOTools.perturb(groupDataMap, domainSize, this.random);
//        System.out.println("perturbedData:");
//        MyPrint.showMap(perturbedData);
        Map<Integer, Integer> perturbedCountMap = BasicUtils.getCountMapByGroup(perturbedData);
        System.out.println("perturbedCountMap:");
        MyPrint.showMap(perturbedCountMap, "; ");
        Map<Integer, Double> perturbedStatisticMap = BasicUtils.getStatisticByCount(perturbedCountMap);
        MyPrint.showMap(perturbedStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);

        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOTools.rePerturb(sortedPerturbedData, domainSize, random);
        Map<Double, List<Integer>> rePerturbMap = rePerturbResult.getKey();
        Integer newTotalUserSize = rePerturbResult.getValue();
//        System.out.println("rePerturbMap");
//        MyPrint.showMap(rePerturbMap);
        System.out.println("totalUserSize: " + newTotalUserSize);
        Map<Integer, Integer> rePerturbCountMap = BasicUtils.getCountMapByGroup(rePerturbMap);
        Map<Integer, Double> rePerturbStatisticMap = BasicUtils.getStatisticByCount(rePerturbCountMap);
        MyPrint.showMap(rePerturbStatisticMap);

        MyPrint.showSplitLine("*", 150);


        Map<Double, Double> newParameterQ = PFOTools.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOTools.getGeneralRandomResponseParameterP(newParameterQ);
        System.out.println("newParameterQ:");
        MyPrint.showMap(newParameterQ);
        System.out.println("newParameterP:");
        MyPrint.showMap(newParameterP);
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbMap);
        System.out.println("rePerturbedEpsilonCount:");
        MyPrint.showMap(rePerturbedEpsilonCount);
        Map<Double, Double> newAggregationWeightMap = PFOTools.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap:");
        MyPrint.showMap(newAggregationWeightMap);
        Map<Integer, Double> estimationMap = PFOTools.getEstimation(rePerturbStatisticMap, newTotalUserSize, rePerturbedEpsilonCount, newParameterP, newParameterQ, newAggregationWeightMap);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap);
    }

}
