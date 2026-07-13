package hnu.dll.schemes.main_scheme._0_proposed_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;

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

    protected static Set<Integer> getCandidateSamplingSizeSet(Integer windowSize, Integer samplingSize, Integer samplingSizeLowerBound) {
        Set<Integer> candidateSamplingSizeSet = new TreeSet<>();
        int i = 1;
        Integer tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
        for(; i < windowSize && tempCandidateSamplingSize < samplingSizeLowerBound; i++) {
            tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
        }


        for (; i < windowSize && tempCandidateSamplingSize <= samplingSize; i++) {
            candidateSamplingSizeSet.add(tempCandidateSamplingSize);
            tempCandidateSamplingSize = (int)Math.floor((i+1)*samplingSize*1.0/windowSize);
        }
        return candidateSamplingSizeSet;
    }

    public static OptimalSelectionStruct groupAwareOptimalPopulationSelection(List<PrivacyGroupTriple> privacyGroupTripleList, Double samplingRatio, Integer domainSize) {
        List<Double> errorList = new ArrayList<>();
        List<Integer> optimalSamplingSizeList = new ArrayList<>();
        List<Double> effectivePrivacyBudgetList = new ArrayList<>();
        int groupListSize = privacyGroupTripleList.size(), tempWindowSize;
        int tempMaximumSamplingSize, repeatSize;
        double tempPrivacyBudget, tempEffectiveBudget;
        Set<Integer> tempCandidateSamplingSizeSet;
        for (PrivacyGroupTriple privacyGroupTriple : privacyGroupTripleList) {
            tempMaximumSamplingSize = (int)Math.ceil(privacyGroupTriple.getUserSize() * samplingRatio);
            tempWindowSize = privacyGroupTriple.getWindowSize();
            tempPrivacyBudget = privacyGroupTriple.getEpsilon();
            tempCandidateSamplingSizeSet = getCandidateSamplingSizeSet(tempWindowSize, tempMaximumSamplingSize, 1);
            for (Integer candidateSamplingSize : tempCandidateSamplingSizeSet) {
                repeatSize = (int) Math.ceil(candidateSamplingSize * tempWindowSize * 1.0 / tempMaximumSamplingSize);
                tempEffectiveBudget = tempPrivacyBudget / repeatSize;
                xxx
            }
        }
    }



























}
