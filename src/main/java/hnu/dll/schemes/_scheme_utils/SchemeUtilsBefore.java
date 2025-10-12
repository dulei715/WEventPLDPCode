package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll.special_tools.PFOUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Deprecated
public class SchemeUtilsBefore {

    public static BasicPair<Integer, Double> optimalPopulationSelection(List<Integer> samplingList, List<Double> privacyBudgetRequirementList, Integer domainSize) {
        Double optimalError = Double.MAX_VALUE;
        Integer optimalSamplingSize = -1;
        List<Integer> uniqueSamplingList = BasicArrayUtil.getUniqueList(samplingList);
//        LinkedHashMap<Double, Double> uniqueBudgetStatisticMap = BasicArrayUtil.getUniqueListWithStatisticList(privacyBudgetRequirementList);
//        List<Double> uniqueBudgetList = BasicArrayUtil.toList(uniqueBudgetStatisticMap.keySet());
//        List<Double> uniqueBudgetStatistic = BasicArrayUtil.toList(uniqueBudgetStatisticMap.values());
        List<Double> newBudgetList, newUniqueBudgetList;
        List<Integer> newUniqueBudgetCount;
        LinkedHashMap<Double, Integer> newUniqueBudgetCountMap;
        Integer currentSamplingSize, currentUniqueSamplingSize;
        Double currentPrivacyBudget, tempError;

        // for test
        MyPrint.showSplitLine("#", 100);
        System.out.print("unique sampling list: ");
        MyPrint.showList(uniqueSamplingList);

        int userSize = samplingList.size();
        int uniqueSamplingListSize = uniqueSamplingList.size();
        for (int i = 0; i < uniqueSamplingListSize; i++) {
            currentUniqueSamplingSize = uniqueSamplingList.get(i);
            newBudgetList = new ArrayList<>();

            // for test
            MyPrint.showSplitLine("*", 150);
            System.out.println("current unique sampling size: " + currentUniqueSamplingSize);

            for (int j = 0; j < userSize; j++) {
                currentPrivacyBudget = privacyBudgetRequirementList.get(j);
                currentSamplingSize = samplingList.get(j);
                if (currentSamplingSize < currentUniqueSamplingSize) {
                    newBudgetList.add(currentPrivacyBudget * currentSamplingSize / currentUniqueSamplingSize);
                } else {
                    newBudgetList.add(currentPrivacyBudget);
                }
            }

            // for test
            MyPrint.showList(newBudgetList);

            newUniqueBudgetCountMap = BasicArrayUtil.getUniqueListWithCountList(newBudgetList);
            tempError = PFOUtils.getGPRRErrorBySpecificUsers(newUniqueBudgetCountMap, userSize, currentUniqueSamplingSize, domainSize);

            // for test
            System.out.println("current error: " + tempError);

            if (tempError < optimalError) {
                optimalError = tempError;
                optimalSamplingSize = currentUniqueSamplingSize;
            }

        }
        return new BasicPair<>(optimalSamplingSize, optimalError);
    }
}
