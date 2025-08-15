package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersonalizedFrequencyOracleTools {
    public static List<Double> getGeneralRandomResponseParameterQ(List<Double> privacyBudgetList, Integer valueDomainSize) {
        int size = privacyBudgetList.size();
        List<Double> qList = new ArrayList<>(size);
        for (Double budget : privacyBudgetList) {
            qList.add(1.0 / (Math.exp(budget) + valueDomainSize - 1));
        }
        return qList;
    }

    public static List<Double> getGeneralRandomResponseParameterP(List<Double> qList, List<Double> privacyBudgetList) {
        int size = qList.size();
        List<Double> pList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pList.add(Math.exp(privacyBudgetList.get(i)) * qList.get(i));
        }
        return pList;
    }

    public static List<Double> getEstimation(List<Double> obfuscatedCountList, List<Double> distinctBudgetCount, List<Double> qList, List<Double> pList) {
        Double paramA = 0D, paramB = 0D;
        int size = distinctBudgetCount.size();
        Double tempG;
        Double tempQ;
        int sizeRes = obfuscatedCountList.size();
        List<Double> result = new ArrayList<>(sizeRes);
        for (int i = 0; i < size; i++) {
            tempG = distinctBudgetCount.get(i);
            tempQ = qList.get(i);
            paramA += tempG * tempQ;
            paramB += tempG * (pList.get(i) - tempQ);
        }
        for (Double tempFrequency : obfuscatedCountList) {
            result.add((tempFrequency-paramA) / paramB);
        }
        return result;
    }

    public static Double getPLDPVarianceSum(Integer valueSize, List<Double> distinctBudgetFrequencyList, List<Double> qList, List<Double> pList, Integer userSize) {
        Double paramA = 0D, paramB = 0D, paramC = 0D;
        int size = distinctBudgetFrequencyList.size();
        Double tempG;
        Double tempQ, tempP;
        for (int i = 0; i < size; i++) {
            tempG = distinctBudgetFrequencyList.get(i);
            tempQ = qList.get(i);
            tempP = pList.get(i);
            paramA += tempG * (1 - tempQ) * tempQ;
            paramB += tempG * (tempP - tempQ);
            paramC += tempG * (1 - tempP - tempQ) * (tempP - tempQ);
        }
        paramA *= valueSize;
        paramB = userSize * paramB * paramB;
        return (paramA + paramC) / paramB;
    }

    public static Double getPLDPVarianceSumStar(Integer valueSize, List<Double> distinctBudgetFrequencyList, List<Double> qList, List<Double> pList, Integer sampleSize) {
        Double result = 0D, paramA = 0D, paramB = 0D;
        int size = distinctBudgetFrequencyList.size();
        Double tempG;
        Double tempQ;
        for (int i = 0; i < size; i++) {
            tempG = distinctBudgetFrequencyList.get(i);
            tempQ = qList.get(i);
            paramA += tempG * (1 - tempQ) * tempQ;
            paramB += tempG * (pList.get(i) - tempQ);
        }
        paramA *= valueSize;
        paramB = sampleSize * paramB * paramB;
        return paramA / paramB;
    }

    public static Double getError(List<Double> distinctEpsilonList, List<Double> distinctBudgetFrequency, Integer userSize, Integer sampleSize, Integer valueDomainSize) {
        List<Double> qList = getGeneralRandomResponseParameterQ(distinctEpsilonList, valueDomainSize);
        List<Double> pList = getGeneralRandomResponseParameterP(qList, distinctEpsilonList);
        Double sampleVariance = (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
        Double pldpVariance = getPLDPVarianceSum(valueDomainSize, distinctBudgetFrequency, qList, pList, sampleSize);
        return (sampleVariance + pldpVariance) / valueDomainSize;
    }

    public static Double getDissimilarity(List<Double> estimationList, List<Double> lastEstimationList, Double pldpVarianceSum) {
        int size = estimationList.size();
        Double differSum = 0D;
        for (int i = 0; i < size; i++) {
            differSum += estimationList.get(i) - lastEstimationList.get(i);
        }
        return (differSum - pldpVarianceSum) / size;
    }

    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
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
}
