package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import hnu.dll.structure.OptimalSelectionStruct;

import java.util.*;

public class MechanismUtils {
    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, Integer domainSize) {
        LinkedHashMap<Integer, Integer> uniqueSamplingSizeMap = BasicArrayUtil.getUniqueListWithCountList(samplingSizeRequirementList);
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer originalSamplingSize;
        Double originalBudget;
        LinkedHashMap<Double, Integer> newUniqueBudgetCountMap, finalUniqueBudgetCountMap = null;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null;
        for (Integer uniqueSamplingSize : uniqueSamplingSizeSet) {
            tempBudgetList = new ArrayList<>();
            for (int i = 0; i < userSize; ++i) {
                originalSamplingSize = samplingSizeRequirementList.get(i);
                originalBudget = privacyBudgetList.get(i);
                if (originalSamplingSize < uniqueSamplingSize) {
//                    tempBudgetList.add(originalBudget * originalSamplingSize / uniqueSamplingSize);
                    tempBudgetList.add(originalBudget / Math.ceil(uniqueSamplingSize * 1.0 / originalSamplingSize));
                } else {
                    tempBudgetList.add(originalBudget);
                }
            }

            newUniqueBudgetCountMap = BasicArrayUtil.getUniqueListWithCountList(tempBudgetList);
//            newUniqueBudgetStatisticMap = new HashMap<>();
//            for (Map.Entry<Double, Integer> entry : newUniqueBudgetCountMap.entrySet()) {
//                newUniqueBudgetStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / userSize);
//            }
            tempError = PFOTools.getGPRRError(newUniqueBudgetCountMap, userSize, uniqueSamplingSize, domainSize);
            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalBudgetList = tempBudgetList;
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalError, finalBudgetList);
    }


}
