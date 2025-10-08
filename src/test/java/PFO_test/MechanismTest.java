package PFO_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.BasicPair;
import hnu.dll.special_tools.PersonalizedLocalWindowSizeMechanism;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static hnu.dll.special_tools.PersonalizedFrequencyOracleTools.*;

public class MechanismTest {
    @Test
    public void pLPDTest() throws InstantiationException, IllegalAccessException {
        List<Double> distinctEpsilonList = Arrays.asList(0.2,0.4,0.6,0.8);
//        List<Integer> distinctBudgetCount = Arrays.asList(6, 2, 2, 4, 2);
        List<Double> distinctBudgetFrequency = Arrays.asList(0.35,0.25,0.25,0.15);
        Integer userSize = 1000;

        Integer sampleSize = 125;
//        Integer sampleSize = 2;
        Integer valueDomainSize = 5;
//        Integer valueDomainSize = 2;
        Double variance = getError(distinctEpsilonList, distinctBudgetFrequency, userSize, sampleSize, valueDomainSize);
        System.out.println(variance);
        List<Double> obfuscatedListA = Arrays.asList(0.22,0.2,0.18,0.22,0.18);
        List<Double> obfuscatedListB = Arrays.asList(0.23,0.19,0.19,0.21,0.18);
        List<Double> qList = getGeneralRandomResponseParameterQ(distinctEpsilonList, valueDomainSize);
        List<Double> pList = getGeneralRandomResponseParameterP(qList, distinctEpsilonList);
        Double pldpVarianceSum = getPLDPVarianceSum(valueDomainSize, distinctBudgetFrequency, qList, pList, userSize);
        List<Double> estimationListA = getEstimation(obfuscatedListA, distinctBudgetFrequency, qList, pList);
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0.0, estimationListA.size());
        MyPrint.showList(estimationListA);
        Integer sampleSizeForError = userSize / 2 / 2;
        Double error = getError(distinctEpsilonList, distinctBudgetFrequency, userSize, sampleSizeForError, valueDomainSize);
        System.out.println(error);

        Double dissimilarity = getDissimilarity(estimationListA, lastEstimationList, pldpVarianceSum);
        System.out.println(dissimilarity);

        List<Double> estimationListB = getEstimation(obfuscatedListB, distinctBudgetFrequency, qList, pList);
        MyPrint.showList(estimationListB);

    }

    @Test
    public void pLPATest() throws InstantiationException, IllegalAccessException {
        List<Double> distinctEpsilonList = Arrays.asList(0.2,0.4,0.6,0.8);
//        List<Integer> distinctBudgetCount = Arrays.asList(6, 2, 2, 4, 2);
        List<Double> distinctBudgetFrequency = Arrays.asList(0.35,0.25,0.25,0.15);
        Integer userSize = 1000;

        Integer sampleSize = 125;
//        Integer sampleSize = 2;
        Integer valueDomainSize = 5;
//        Integer valueDomainSize = 2;
        Double variance = getError(distinctEpsilonList, distinctBudgetFrequency, userSize, sampleSize, valueDomainSize);
        System.out.println(variance);
        List<Double> obfuscatedListA = Arrays.asList(0.22,0.2,0.18,0.22,0.18);
        List<Double> obfuscatedListB = Arrays.asList(0.23,0.19,0.19,0.21,0.18);
        List<Double> qList = getGeneralRandomResponseParameterQ(distinctEpsilonList, valueDomainSize);
        List<Double> pList = getGeneralRandomResponseParameterP(qList, distinctEpsilonList);

        Double pldpVarianceSum = getPLDPVarianceSum(valueDomainSize, distinctBudgetFrequency, qList, pList, userSize);
        List<Double> estimationListA = getEstimation(obfuscatedListA, distinctBudgetFrequency, qList, pList);
        List<Double> lastEstimationList = BasicArrayUtil.getInitializedList(0.0, estimationListA.size());
        MyPrint.showList(estimationListA);
        Integer sampleSizeForError = userSize / 2 / 2 * 2;
        Double error = getError(distinctEpsilonList, distinctBudgetFrequency, userSize, sampleSizeForError, valueDomainSize);
        System.out.println(error);

        Double dissimilarity = getDissimilarity(estimationListA, lastEstimationList, pldpVarianceSum);
        System.out.println(dissimilarity);

        List<Double> estimationListB = getEstimation(obfuscatedListB, distinctBudgetFrequency, qList, pList);
        MyPrint.showList(estimationListB);

    }

    @Test
    public void optimalSamplingTest() throws InstantiationException, IllegalAccessException {
        List<Double> userEpsilonList = Arrays.asList(0.1, 0.4, 0.4, 0.1, 0.4, 0.4, 0.8, 0.8, 0.8, 0.4);
//        List<Double> distinctEpsilonList = Arrays.asList(0.1,0.4,0.8);
        LinkedHashMap<Double, Integer> uniqueEpsilonStatistic = BasicArrayUtil.getUniqueListWithCountList(userEpsilonList);
//        List<Integer> distinctBudgetCount = Arrays.asList(6, 2, 2, 4, 2);
//        List<Double> distinctBudgetFrequency = Arrays.asList(0.2,0.5,0.3);
        Integer userSize = userEpsilonList.size();
        Integer valueDomainSize = 2;
        List<Double> distinctEpsilonList = BasicArrayUtil.toList(uniqueEpsilonStatistic.keySet());
        List<Double> qList = getGeneralRandomResponseParameterQ(distinctEpsilonList, valueDomainSize);
        List<Double> pList = getGeneralRandomResponseParameterP(qList, distinctEpsilonList);

        MyPrint.showList(qList);
        MyPrint.showList(pList);

        List<Integer> userSamplingList = Arrays.asList(3, 5, 2, 8, 2, 5, 6, 5, 8, 2);
//        List<Integer> distinctSamplingSize = Arrays.asList(3, 5, 2, 8, 6);
//        List<Double> distinctSampingFrequency = Arrays.asList(0.2,0.5,0.3);l
        LinkedHashMap<Integer, Integer> samplingSizeStatistic = BasicArrayUtil.getUniqueListWithCountList(userSamplingList);
        List<Integer> distinctSamplingSize = BasicArrayUtil.toList(samplingSizeStatistic.keySet());
        List<Integer> distinctSamplingCount = BasicArrayUtil.toList(samplingSizeStatistic.values());
        MyPrint.showList(distinctSamplingSize);
        MyPrint.showList(distinctSamplingCount);

        BasicPair<Integer, Double> result = PersonalizedLocalWindowSizeMechanism.optimalPopulationSelection(userSamplingList, userEpsilonList, valueDomainSize);
        System.out.println(result.getKey());
        System.out.println(result.getValue());


    }
}
