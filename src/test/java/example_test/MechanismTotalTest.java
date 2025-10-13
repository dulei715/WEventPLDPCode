package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationAbsorptionPlus;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationDistributionPlus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        maxTimeSlotSize = 100;
    }

    @Before
    public void before() {
        this.random = new Random(0);
        this.totalUserSize = 2000;
        this.dataType = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));
        List<Integer> randomBudgetIndexList = RandomUtil.getRandomIntegerList(0, candidatePrivacyBudgetArray.length - 1, this.totalUserSize, this.random);
        List<Integer> randomWindowSizeIndexList = RandomUtil.getRandomIntegerList(0, candidateWindowSizeArray.length - 1, this.totalUserSize, this.random);
        this.privacyBudgetList = BasicArrayUtil.getElementListInGivenIndexes(candidatePrivacyBudgetArray, randomBudgetIndexList);
        this.windowSizeList = BasicArrayUtil.getElementListInGivenIndexes(candidateWindowSizeArray, randomWindowSizeIndexList);
        this.timeSlotDataArray = generateRandomDataIndex(this.dataType.size(), this.totalUserSize, maxTimeSlotSize, this.random);
    }

    @Test
    public void pLPDTest() {
        PLDPPopulationDistributionPlus pLPDPlus = new PLDPPopulationDistributionPlus(this.dataType, this.privacyBudgetList, this.windowSizeList, this.random);
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

}
