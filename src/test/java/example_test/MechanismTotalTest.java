package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.metric.Measurement;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationDistribution;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateRePerturbDistributionPlus;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationAbsorptionPlus;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationDistributionPlus;
import hnu.dll.utils.BasicUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class MechanismTotalTest {
    public static Double[] candidatePrivacyBudgetArray;
    public static Integer[] candidateWindowSizeArray;
    public static Integer maxTimeSlotSize;

    public Random random;
    public Integer totalUserSize;
    public Set<String> dataType;
    public List<Integer> windowSizeList;
    public List<Double> privacyBudgetList;

    public List[] timeSlotDataArray;

    public static List[] generateRandomDataIndex(Integer dataTypeSize, Integer userSize, Integer timeSlotSize, Random random) {
        List[] result = new List[timeSlotSize];
        for (int i = 0; i < timeSlotSize; i++) {
            result[i] = RandomUtil.getRandomIntegerList(0, dataTypeSize - 1, userSize, random);
        }
        return result;
    }

    @BeforeClass
    public static void beforeClass() {
        candidatePrivacyBudgetArray = new Double[]{0.5, 1.0, 1.5, 2.0, 2.5};
        candidateWindowSizeArray = new Integer[]{10, 40, 70, 100, 130};
//        maxTimeSlotSize = 100;
        maxTimeSlotSize = 10000;
    }

    @Before
    public void before() {
        this.random = new Random(0);
        this.totalUserSize = 20000;
        this.dataType = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));
        List<Integer> randomBudgetIndexList = RandomUtil.getRandomIntegerList(0, candidatePrivacyBudgetArray.length - 1, this.totalUserSize, this.random);
        List<Integer> randomWindowSizeIndexList = RandomUtil.getRandomIntegerList(0, candidateWindowSizeArray.length - 1, this.totalUserSize, this.random);
        this.privacyBudgetList = BasicArrayUtil.getElementListInGivenIndexes(candidatePrivacyBudgetArray, randomBudgetIndexList);
        this.windowSizeList = BasicArrayUtil.getElementListInGivenIndexes(candidateWindowSizeArray, randomWindowSizeIndexList);
        this.timeSlotDataArray = generateRandomDataIndex(this.dataType.size(), this.totalUserSize, maxTimeSlotSize, this.random);
    }

    @Test
    public void pLPDTest() {
        PLDPPopulationDistributionPlus pLPDPlus = new PLDPPopulationDistributionPlus(this.dataType, this.privacyBudgetList, this.windowSizeList, Constant.PopulationLowerBound, this.random);
        pLPDPlus.initializeOptimalParameters();
//        Integer optimalSamplingSize = pLPDPlus.getOptimalSamplingSize();
//        System.out.println(optimalSamplingSize);

        Integer timeSlotSize = 10;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println("time slot " + i);
            CombinePair<Boolean, Map<Integer, Double>> resultPair = pLPDPlus.updateNextPublicationResult(this.timeSlotDataArray[i]);

            Boolean whetherPublishingNew = resultPair.getKey();
            Map<Integer, Double> statistic = resultPair.getValue();
            System.out.println(whetherPublishingNew);
            MyPrint.showMap(statistic, ";");
            MyPrint.showSplitLine("*", 150);
        }
    }

    @Test
    public void pLPATest() {
        PLDPPopulationAbsorptionPlus pLPAPlus = new PLDPPopulationAbsorptionPlus(this.dataType, this.privacyBudgetList, this.windowSizeList, this.random);
        pLPAPlus.initializeOptimalParameters();
//        Integer optimalSamplingSize = pLPDPlus.getOptimalSamplingSize();
//        System.out.println(optimalSamplingSize);

        Integer timeSlotSize = 10;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println("time slot " + i);
            CombinePair<Boolean, Map<Integer, Double>> resultPair = pLPAPlus.updateNextPublicationResult(this.timeSlotDataArray[i]);
            Boolean whetherPublishingNew = resultPair.getKey();
            Map<Integer, Double> statistic = resultPair.getValue();
            System.out.println(whetherPublishingNew);
            MyPrint.showMap(statistic, ";");
            MyPrint.showSplitLine("*", 150);
        }
    }

    @Test
    public void nonPersonalizedDistributionTest() {
        Double privacyBudget = this.privacyBudgetList.get(this.privacyBudgetList.size() / 2);
        Integer windowSize = this.windowSizeList.get(this.windowSizeList.size() / 2);
        Integer userSize = this.privacyBudgetList.size();
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(this.dataType, privacyBudget, windowSize, userSize, Constant.PopulationLowerBound, this.random);
//        Integer optimalSamplingSize = pLPDPlus.getOptimalSamplingSize();
//        System.out.println(optimalSamplingSize);

        Integer timeSlotSize = 100;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println("time slot " + i);
            CombinePair<Boolean, Map<Integer, Double>> resultPair = lPD.updateNextPublicationResult(this.timeSlotDataArray[i]);
            Boolean whetherPublishingNew = resultPair.getKey();
            Map<Integer, Double> statistic = resultPair.getValue();
            System.out.println(whetherPublishingNew);
            MyPrint.showMap(statistic, ";");
            MyPrint.showSplitLine("*", 150);
        }
    }
    @Test
    public void ablateRePerturbDistributionPlusTest() {

        AblateRePerturbDistributionPlus ablateRPDP = new AblateRePerturbDistributionPlus(this.dataType, this.privacyBudgetList, this.windowSizeList, Constant.PopulationLowerBound, this.random);
//        Integer optimalSamplingSize = pLPDPlus.getOptimalSamplingSize();
//        System.out.println(optimalSamplingSize);

        Integer timeSlotSize = 100;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println("time slot " + i);
            CombinePair<Boolean, Map<Integer, Double>> resultPair = ablateRPDP.updateNextPublicationResult(this.timeSlotDataArray[i]);
            Boolean whetherPublishingNew = resultPair.getKey();
            Map<Integer, Double> statistic = resultPair.getValue();
            System.out.println(whetherPublishingNew);
            MyPrint.showMap(statistic, ";");
            MyPrint.showSplitLine("*", 150);
        }
    }

    @Test
    public void mechanismTest0() throws InterruptedException {
        Integer timeSlotSize = maxTimeSlotSize;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        List<Integer> domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, dataType.size() - 1);

        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);

        List<Double> budgetChangeList = Arrays.asList(BasicArrayUtil.getIncreaseDoubleNumberArray(0.5, 10, 40.5));
        Integer trajectoryUserSize = this.totalUserSize;
        Integer windowSizeDefault = 120;
        List<LDPPopulationDistribution> lpdMechanismList = new ArrayList<>(budgetChangeList.size());
        List<Double> finalError = new ArrayList<>(budgetChangeList.size());
        for (Double budget : budgetChangeList) {
            lpdMechanismList.add(new LDPPopulationDistribution(dataType, budget, windowSizeDefault, trajectoryUserSize, Constant.PopulationLowerBound, random));
            finalError.add(0D);
        }


        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println(i);
            dataList = this.timeSlotDataArray[i];

            Map<Integer, Double> nonPrivacyMap = nonPrivacyMechanism.updateNextPublicationResult(dataList).getValue();
            List<Double> nonPrivacyResultList = BasicUtils.toSortedListByKeys(nonPrivacyMap, domainIndexList, 0D);
            for (int j = 0; j < lpdMechanismList.size(); j++) {
                LDPPopulationDistribution lpd = lpdMechanismList.get(j);
                Map<Integer, Double> tempStatistic = lpd.updateNextPublicationResult(dataList).getValue();
                List<Double> tempStatisticList = BasicUtils.toSortedListByKeys(tempStatistic, domainIndexList, 0D);
                Double tempError = Measurement.get2NormSquareError(tempStatisticList, nonPrivacyResultList);
//                Double tempError = Measurement.getMRE(nonPrivacyResultList, tempStatisticList);
                finalError.set(j, finalError.get(j) + tempError);
            }


            System.out.println(i);
            MyPrint.showList(budgetChangeList, "; ");
            MyPrint.showList(finalError, "; ");




            MyPrint.showSplitLine("*", 150);

            Thread.sleep(5000);
        }
    }


    @Test
    public void mechanismTest1() throws InterruptedException {
        Integer timeSlotSize = maxTimeSlotSize;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        List<Integer> domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, dataType.size() - 1);

        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);

        Double privacyBudget = 1.5D;
        Integer trajectoryUserSize = this.totalUserSize;
        Integer windowSizeDefault = 120;
        LDPPopulationDistribution lpdMechanism = new LDPPopulationDistribution(dataType, privacyBudget, windowSizeDefault, trajectoryUserSize, Constant.PopulationLowerBound, random);
        Double finalError = 0D;


        Double nonFinalPrivacyError = 0D;


        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println(i);
            dataList = this.timeSlotDataArray[i];

            Map<Integer, Double> realStatisticMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithStatisticList(dataList));
            List<Double> realStatisticList = BasicUtils.toSortedListByKeys(realStatisticMap, domainIndexList, 0D);


            Map<Integer, Double> nonPrivacyMap = nonPrivacyMechanism.updateNextPublicationResult(dataList).getValue();
            List<Double> nonPrivacyResultList = BasicUtils.toSortedListByKeys(nonPrivacyMap, domainIndexList, 0D);

            nonFinalPrivacyError += Measurement.get2NormSquareError(realStatisticList, nonPrivacyResultList);


            Map<Integer, Double> tempStatistic = lpdMechanism.updateNextPublicationResult(dataList).getValue();
            List<Double> tempStatisticList = BasicUtils.toSortedListByKeys(tempStatistic, domainIndexList, 0D);
            Double tempError = Measurement.get2NormSquareError(tempStatisticList, nonPrivacyResultList);
//                Double tempError = Measurement.getMRE(nonPrivacyResultList, tempStatisticList);
            finalError += tempError;



            System.out.println(i);
            System.out.println("nonFinalError: " + nonFinalPrivacyError);
            System.out.println("tempError: " + tempError + "; finalError: " + finalError);


            MyPrint.showSplitLine("*", 150);

//            Thread.sleep(5000);
        }
    }

    @Test
    public void mechanismTest2() throws InterruptedException {
        Integer timeSlotSize = maxTimeSlotSize;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        List<Integer> domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, dataType.size() - 1);

        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);

        List<Double> budgetChangeList = Arrays.asList(new Double[]{1.5, 40D});
        Integer trajectoryUserSize = this.totalUserSize;
        Integer windowSizeDefault = 12;
        List<LDPPopulationDistribution> lpdMechanismList = new ArrayList<>(budgetChangeList.size());
        List<Double> finalError = new ArrayList<>(budgetChangeList.size());
        for (Double budget : budgetChangeList) {
            lpdMechanismList.add(new LDPPopulationDistribution(dataType, budget, windowSizeDefault, trajectoryUserSize, Constant.PopulationLowerBound, random));
            finalError.add(0D);
        }


        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println(i);
            dataList = this.timeSlotDataArray[i];

            if (i == 1000) {
                System.out.println("1000了！");
            }

            Map<Integer, Double> nonPrivacyMap = nonPrivacyMechanism.updateNextPublicationResult(dataList).getValue();
            List<Double> nonPrivacyResultList = BasicUtils.toSortedListByKeys(nonPrivacyMap, domainIndexList, 0D);
            for (int j = 0; j < lpdMechanismList.size(); j++) {
                LDPPopulationDistribution lpd = lpdMechanismList.get(j);
                Map<Integer, Double> tempStatistic = lpd.updateNextPublicationResult(dataList).getValue();
                List<Double> tempStatisticList = BasicUtils.toSortedListByKeys(tempStatistic, domainIndexList, 0D);
                Double tempError = Measurement.get2NormSquareError(tempStatisticList, nonPrivacyResultList);
//                Double tempError = Measurement.getMRE(nonPrivacyResultList, tempStatisticList);
                finalError.set(j, finalError.get(j) + tempError);
            }


            System.out.println(i);
            MyPrint.showList(budgetChangeList, "; ");
            MyPrint.showList(finalError, "; ");




            MyPrint.showSplitLine("*", 150);

//            Thread.sleep(5000);
        }
    }

}
