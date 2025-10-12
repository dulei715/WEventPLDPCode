package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.foImp.GeneralizedRandomizedResponse;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.BasicPair;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.special_tools.PersonalizedFrequencyOracle;
import hnu.dll.special_tools.impl.GeneralizedPersonalizedRandomResponse;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import hnu.dll.utils.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class MechanismBasicTest {
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
        budgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.budgetList));
        windowSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.windowSizeList));
        this.pfo = new GeneralizedPersonalizedRandomResponse(domainSize, new TreeMap<>(budgetCountMap), GeneralizedRandomizedResponse.class);
        samplingSizeList = new ArrayList<>(userSize);
        for (Integer windowSize : windowSizeList) {
            samplingSizeList.add((int)Math.floor(userSize * 1.0 / (2 * windowSize)));
        }
    }

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
//            if (tempInteger == 4) {
//                tempInteger = random.nextDouble() < 0.01 ? 4 : 3;
//            }
            if (tempInteger == 1) {
                tempInteger = random.nextDouble() < 0.5 ? 1 : 3;
            }
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
        Map<Integer, Integer> samplingSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(samplingSizeList));
        MyPrint.showMap(samplingSizeCountMap);
    }

    @Test
    public void optimalSamplingSizeTest() {
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
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
        TreeMap<String, Integer> positionCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionList));
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



        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(samplingSizeList, budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
        MyPrint.showSplitLine("*", 150);

        // todo: add xxx

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double newError = optimalSelectionStruct.getError();

        Map<Double, Integer> newBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList));
        System.out.println("newBudgetCountMap:");
        MyPrint.showMap(newBudgetCountMap);
        MyPrint.showSplitLine("*", 150);
//        PFOTools.rePerturb()
        // todo: add rePerturb
        Map<Double, Double> newDistinctQMap = PFOUtils.getGeneralRandomResponseParameterQ(newBudgetCountMap.keySet(), domainSize);
        Map<Double, Double> newDistinctPMap = PFOUtils.getGeneralRandomResponseParameterP(newDistinctQMap);
        Map<Double, Double> newAggregationWeightMap = PFOUtils.getAggregationWeightMap(newBudgetCountMap, domainSize);

        System.out.println("newAggregationWeightedMap:");
        MyPrint.showMap(newAggregationWeightMap);

        MyPrint.showSplitLine("*", 150);

        Double originalPLDPVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(budgetCountMap, domainSize);
        Double newPLDPVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(newBudgetCountMap, domainSize);


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
        Double dissimilarity = PFOUtils.getDissimilarity(estimationList, lastEstimationList, originalPLDPVarianceSum);
        System.out.println(dissimilarity);
//        PFOTools.getGPRRError()
    }

    private void showPositionBudgetInformation(List<Integer> positionIndexList, List<Double> privacyBudgetList) {
        MyPrint.showSplitLine("*", 150);
        TreeMap<Integer, Integer> positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(positionIndexList));
        TreeMap<Double, Integer> subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(privacyBudgetList));
        System.out.println("positionIndexCountMap:");
        MyPrint.showMap(positionIndexCountMap, "; ");
        System.out.println("position index statistic map:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(positionIndexCountMap), "; ");
        System.out.println("subBudgetCountMap:");
        MyPrint.showMap(subBudgetCountMap);
        System.out.println("subBudgetStatisticMap:");
        MyPrint.showMap(BasicUtils.getStatisticByCount(subBudgetCountMap), "; ");
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

        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
        MyPrint.showSplitLine("*", 150);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();



        // time slot 1
        Integer samplingSize = 333;
        Integer rightIndexExclude = samplingSize;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(0, rightIndexExclude);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(0, rightIndexExclude);
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
        System.out.println("groupDataMap:");
        MyPrint.showMap(groupDataMap);
        System.out.println("groupDataCount:");
        Map<Integer, Integer> groupDataCount = BasicUtils.getCountMapByGroup(groupDataMap);
        MyPrint.showMap(groupDataCount);
        Map<Double, List<Integer>> perturbedData = PFOUtils.perturb(groupDataMap, domainSize, this.random);
//        System.out.println("perturbedData:");
//        MyPrint.showMap(perturbedData);
        Map<Integer, Integer> perturbedCountMap = BasicUtils.getCountMapByGroup(perturbedData);
        System.out.println("perturbedCountMap:");
        MyPrint.showMap(perturbedCountMap, "; ");
        Map<Integer, Double> perturbedStatisticMap = BasicUtils.getStatisticByCount(perturbedCountMap);
        MyPrint.showMap(perturbedStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);

        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOUtils.rePerturb(sortedPerturbedData, domainSize, random);
        Map<Double, List<Integer>> rePerturbMap = rePerturbResult.getKey();
        Integer newTotalUserSize = rePerturbResult.getValue();
//        System.out.println("rePerturbMap");
//        MyPrint.showMap(rePerturbMap);
        System.out.println("totalUserSize: " + newTotalUserSize);
        Map<Integer, Integer> rePerturbCountMap = BasicUtils.getCountMapByGroup(rePerturbMap);
        Map<Integer, Double> rePerturbStatisticMap = BasicUtils.getStatisticByCount(rePerturbCountMap);
        System.out.println("rePerturbStatisticMap:");
        MyPrint.showMap(rePerturbStatisticMap);

        MyPrint.showSplitLine("*", 150);


        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(newBudgetSet, domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        System.out.println("newParameterQ:");
        MyPrint.showMap(newParameterQ);
        System.out.println("newParameterP:");
        MyPrint.showMap(newParameterP);
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbMap);
        System.out.println("rePerturbedEpsilonCount:");
        MyPrint.showMap(rePerturbedEpsilonCount);
        Map<Double, Double> newAggregationWeightMap = PFOUtils.getAggregationWeightMap(rePerturbedEpsilonCount, domainSize);
        System.out.println("newAggregationWeightedMap:");
        MyPrint.showMap(newAggregationWeightMap);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(rePerturbedEpsilonCount, rePerturbMap, domainSize);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, newAggregationWeightMap);
        System.out.println("estimationMap:");
        MyPrint.showMap(estimationMap);
        MyPrint.showSplitLine("*", 150);

        Double pldpVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(rePerturbedEpsilonCount, domainSize);
        System.out.println("pldpVarianceSum: " + pldpVarianceSum);
        List<Double> estimationList = new ArrayList<>(estimationMap.values());
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0D, estimationList.size());
        Double dissimilarity = PFOUtils.getDissimilarity(estimationList, lastEstimationList, pldpVarianceSum);
        System.out.println("dissimilarity: " + dissimilarity);
        MyPrint.showSplitLine("*", 150);



        Integer leftIndex = samplingSize;
        samplingSize = (int)Math.floor(userSize/2/2);
        rightIndexExclude += samplingSize;
        subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        System.out.println("n_pp,opt = " + samplingSize);
        System.out.println("leftIndex = " + leftIndex);
        System.out.println("rightIndexExclude = " + rightIndexExclude);
        positionIndexCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subPositionIndexList));
        subBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(subNewPrivacyBudgetList));
//        MyPrint.showMap(positionIndexCountMap);
//        MyPrint.showMap(subBudgetCountMap);
        showPositionBudgetInformation(subPositionIndexList, subNewPrivacyBudgetList);
//        PFOTools.
        groupDataMap = BasicUtils.groupByEpsilon(subNewPrivacyBudgetList, subPositionIndexList);
        System.out.println("group map:");
        MyPrint.showMap(groupDataMap);
        Double gprrError = PFOUtils.getGPRRErrorBySpecificUsers(subBudgetCountMap, userSize, samplingSize, domainSize);
        System.out.println(gprrError);
        MyPrint.showSplitLine("*", 150);

        perturbedData = PFOUtils.perturb(groupDataMap, domainSize, random);
        perturbedCountMap = BasicUtils.getCountMapByGroup(perturbedData);
        System.out.println("perturbedCountMap:");
        MyPrint.showMap(perturbedCountMap, "; ");
        perturbedStatisticMap = BasicUtils.getStatisticByCount(perturbedCountMap);
        MyPrint.showMap(perturbedStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);

    }

    @Test
    public void pLBDTestEnhancedSlot1() {
        System.out.println("OriginalBudgetCount:");
        MyPrint.showMap(budgetCountMap, "; ");
        System.out.println("OriginalWindowSizeCount:");
        MyPrint.showMap(windowSizeCountMap, "; ");
        System.out.println("samplingSizeList:");
        Map<Integer, Integer> samplingSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.samplingSizeList));
        MyPrint.showMap(samplingSizeCountMap, "; ");

        MyPrint.showSplitLine("*", 150);
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        System.out.println("optimalSamplingSize: " + optimalSamplingSize);
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();
        TreeMap<Double, Integer> newTotalBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList));
        System.out.println("newTotalBudgetCountMap:");
        MyPrint.showMap(newTotalBudgetCountMap, "; ");
        Map<Double, Double> newTotalBudgetStatisticMap = BasicUtils.getStatisticByCount(newTotalBudgetCountMap);
        System.out.println("NewTotalBudgetStatisticMap:");
        MyPrint.showMap(newTotalBudgetStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> originalPairMap = BasicUtils.countUniquePair(this.windowSizeList, this.budgetList);
        System.out.println("original pair map:");
        MyPrint.showMap(originalPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> newPairMap = BasicUtils.countUniquePair(this.windowSizeList, newPrivacyBudgetList);
        System.out.println(newPrivacyBudgetList.contains(0.1));
        System.out.println("new pair map:");
        MyPrint.showMap(newPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);



        // time slot 1
        Integer leftIndex = 0;
        Integer samplingSize = optimalSamplingSize;
        Integer rightIndexExclude = leftIndex + samplingSize;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        Double dissimilarity = TestUtils.showSMechanismInformation(subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);

        leftIndex = rightIndexExclude;
        samplingSize = (int)Math.floor(userSize/2/2);
        rightIndexExclude = leftIndex + samplingSize;
        subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        TestUtils.showRMechanismInformation(dissimilarity, subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);
    }

    @Test
    public void pLBDTestEnhancedSlot2() {
        System.out.println("OriginalBudgetCount:");
        MyPrint.showMap(budgetCountMap, "; ");
        System.out.println("OriginalWindowSizeCount:");
        MyPrint.showMap(windowSizeCountMap, "; ");
        System.out.println("samplingSizeList:");
        Map<Integer, Integer> samplingSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.samplingSizeList));
        MyPrint.showMap(samplingSizeCountMap, "; ");

        MyPrint.showSplitLine("*", 150);
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        System.out.println("optimalSamplingSize: " + optimalSamplingSize);
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();
        TreeMap<Double, Integer> newTotalBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList));
        System.out.println("newTotalBudgetCountMap:");
        MyPrint.showMap(newTotalBudgetCountMap, "; ");
        Map<Double, Double> newTotalBudgetStatisticMap = BasicUtils.getStatisticByCount(newTotalBudgetCountMap);
        System.out.println("NewTotalBudgetStatisticMap:");
        MyPrint.showMap(newTotalBudgetStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> originalPairMap = BasicUtils.countUniquePair(this.windowSizeList, this.budgetList);
        System.out.println("original pair map:");
        MyPrint.showMap(originalPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> newPairMap = BasicUtils.countUniquePair(this.windowSizeList, newPrivacyBudgetList);
        System.out.println(newPrivacyBudgetList.contains(0.1));
        System.out.println("new pair map:");
        MyPrint.showMap(newPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);



        // time slot 2
        Integer leftIndex = 833;
        Integer samplingSize = optimalSamplingSize;
        Integer rightIndexExclude = leftIndex + samplingSize;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        Double dissimilarity = TestUtils.showSMechanismInformation(subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);

        Integer reportingRemainUserSize = 500;

        leftIndex = rightIndexExclude;
        samplingSize = (int)Math.floor(reportingRemainUserSize*1.0/2);
        rightIndexExclude = leftIndex + samplingSize;
        subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        TestUtils.showRMechanismInformation(dissimilarity, subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);
    }
    @Test
    public void pLBDTestEnhancedSlot3() {
        System.out.println("OriginalBudgetCount:");
        MyPrint.showMap(budgetCountMap, "; ");
        System.out.println("OriginalWindowSizeCount:");
        MyPrint.showMap(windowSizeCountMap, "; ");
        System.out.println("samplingSizeList:");
        Map<Integer, Integer> samplingSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.samplingSizeList));
        MyPrint.showMap(samplingSizeCountMap, "; ");

        MyPrint.showSplitLine("*", 150);
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        System.out.println("optimalSamplingSize: " + optimalSamplingSize);
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();
        TreeMap<Double, Integer> newTotalBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList));
        System.out.println("newTotalBudgetCountMap:");
        MyPrint.showMap(newTotalBudgetCountMap, "; ");
        Map<Double, Double> newTotalBudgetStatisticMap = BasicUtils.getStatisticByCount(newTotalBudgetCountMap);
        System.out.println("NewTotalBudgetStatisticMap:");
        MyPrint.showMap(newTotalBudgetStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> originalPairMap = BasicUtils.countUniquePair(this.windowSizeList, this.budgetList);
        System.out.println("original pair map:");
        MyPrint.showMap(originalPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> newPairMap = BasicUtils.countUniquePair(this.windowSizeList, newPrivacyBudgetList);
        System.out.println(newPrivacyBudgetList.contains(0.1));
        System.out.println("new pair map:");
        MyPrint.showMap(newPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);



        // time slot 3
        Integer leftIndex = 1166;
        Integer samplingSize = optimalSamplingSize;
        Integer rightIndexExclude = leftIndex + samplingSize;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        Double dissimilarity = TestUtils.showSMechanismInformation(subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);

        Integer reportingRemainUserSize = 500;

        leftIndex = rightIndexExclude;
        samplingSize = (int)Math.floor(reportingRemainUserSize*1.0/2);
        rightIndexExclude = leftIndex + samplingSize;
        subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        TestUtils.showRMechanismInformation(dissimilarity, subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);
    }


    public void pLBATestEnhancedSlot1() {
        System.out.println("OriginalBudgetCount:");
        MyPrint.showMap(budgetCountMap, "; ");
        System.out.println("OriginalWindowSizeCount:");
        MyPrint.showMap(windowSizeCountMap, "; ");
        System.out.println("samplingSizeList:");
        Map<Integer, Integer> samplingSizeCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(this.samplingSizeList));
        MyPrint.showMap(samplingSizeCountMap, "; ");

        MyPrint.showSplitLine("*", 150);
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);

        Integer optimalSamplingSize = optimalSelectionStruct.getOptimalSamplingSize();
        System.out.println("optimalSamplingSize: " + optimalSamplingSize);
        List<Double> newPrivacyBudgetList = optimalSelectionStruct.getNewPrivacyBudgetList();
        Double optimalError = optimalSelectionStruct.getError();
        TreeMap<Double, Integer> newTotalBudgetCountMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithCountList(newPrivacyBudgetList));
        System.out.println("newTotalBudgetCountMap:");
        MyPrint.showMap(newTotalBudgetCountMap, "; ");
        Map<Double, Double> newTotalBudgetStatisticMap = BasicUtils.getStatisticByCount(newTotalBudgetCountMap);
        System.out.println("NewTotalBudgetStatisticMap:");
        MyPrint.showMap(newTotalBudgetStatisticMap, "; ");
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> originalPairMap = BasicUtils.countUniquePair(this.windowSizeList, this.budgetList);
        System.out.println("original pair map:");
        MyPrint.showMap(originalPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);


        Map<BasicPair<Integer, Double>, Integer> newPairMap = BasicUtils.countUniquePair(this.windowSizeList, newPrivacyBudgetList);
        System.out.println(newPrivacyBudgetList.contains(0.1));
        System.out.println("new pair map:");
        MyPrint.showMap(newPairMap, "; ", 8, ConstantValues.LINE_SPLIT);
        MyPrint.showSplitLine("*", 150);



        // time slot 1
        Integer leftIndex = 0;
        Integer samplingSize = optimalSamplingSize;
        Integer rightIndexExclude = leftIndex + samplingSize;
        List<Integer> subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        List<String> subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        List<Double> subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        Double dissimilarity = TestUtils.showSMechanismInformation(subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);

        leftIndex = rightIndexExclude;
        samplingSize = (int)Math.floor(userSize/2/2);
        rightIndexExclude = leftIndex + samplingSize;
        subPositionIndexList = this.positionIndexList.subList(leftIndex, rightIndexExclude);
        subPositionList = BasicUtils.getElementListByIndex(PositionCandidate, subPositionIndexList);
        subNewPrivacyBudgetList = newPrivacyBudgetList.subList(leftIndex, rightIndexExclude);
        TestUtils.showRMechanismInformation(dissimilarity, subPositionIndexList, subNewPrivacyBudgetList, userSize, domainSize, random);

    }

}
