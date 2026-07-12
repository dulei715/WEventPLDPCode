package hnu.dll.schemes.main_scheme._0_proposed_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.OptimalSelectionStruct;

import java.util.*;

public class ProposedSchemeUtils {
    /**
     * 整体选择最优 sampling size
     * @param samplingSizeRequirementList
     * @param privacyBudgetList
     * @param domainSize
     * @return
     */
    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, Integer domainSize) {
        Map<Integer, Integer> uniqueSamplingSizeMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithFirstAppearanceIndex(samplingSizeRequirementList));
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer originalSamplingSize;
        Double originalBudget;
        LinkedHashMap<Double, Double> newUniqueBudgetStatisticMap;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null, finalSamplingSizeIndex = null;
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

            newUniqueBudgetStatisticMap = BasicArrayUtil.getUniqueListWithStatisticList(tempBudgetList);
            tempError = PFOUtils.getGPRRErrorByGroupUserRatio(newUniqueBudgetStatisticMap, userSize, uniqueSamplingSize, domainSize);
            //todo: for test
            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
//            MyPrint.showList(tempBudgetList, "; ");
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalBudgetList = tempBudgetList;
                finalSamplingSizeIndex = uniqueSamplingSizeMap.get(finalSamplingSize);
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalError, finalBudgetList, finalSamplingSizeIndex);
    }

    public static OptimalSelectionStruct groupAwareOptimalPopulationSelection(List<>) {

    }
}
