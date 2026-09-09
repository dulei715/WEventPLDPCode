package hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.statistic.StatisticTool;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.AblationPLPMechanismContainingBAPUEOnly;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class PLPDContainingBAPUEOnly extends AblationPLPMechanismContainingBAPUEOnly {

    protected Integer populationSizeLowerBound;

    protected GroupAwareOptimalSelectionStruct pubGroupAwareOptimalSelectionStruct;
    protected List<Map<Integer, Double>> currentPubEstimationMapList;
    protected List<List<Double>> currentPubEstimationListList;
    protected List<Double> currentPubUnbiasedVarianceList;

    public PLPDContainingBAPUEOnly(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Integer populationSizeLowerBound, boolean model, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, model, random);
        this.populationSizeLowerBound = populationSizeLowerBound;
    }


    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        int tempRecyclePeriod;
        int tempGroupInnerSize, tempOptimalSamplingSize, remainingPopulationSize, tempPotentialPublicationUserSize, tempSamplingSize;
        List<Integer> potentialPublicationUserSizeList = new ArrayList<>(super.groupSize);
        List<Integer> tempSamplingDataIndexList;
        List<Double> prePublicationVarianceList = new ArrayList<>(super.groupSize);
//        List<PrivacyGroupTriple> publicationPrivacyGroupTripleList = new ArrayList<>(super.groupSize);
        Map<Integer, Double> tempInfo;
        Map<Integer, Double> finalEstimateMap, finalEstimateNormalizedMap;
        Double tempPreVariance;
        for (int i = 0; i < super.groupSize; i++) {
            tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
            remainingPopulationSize = tempGroupInnerSize / 2 - super.publicationSubMechanismHistoryQueueList.get(i).getReverseSizeSum(super.recycleWindowSize - 1);
            tempPotentialPublicationUserSize = remainingPopulationSize / 2;     // 这里用了整数除法截断性质实现向下取整
            potentialPublicationUserSizeList.add(tempPotentialPublicationUserSize);
            if (tempPotentialPublicationUserSize < this.populationSizeLowerBound) {
                prePublicationVarianceList.add(Double.POSITIVE_INFINITY);
                continue;
            }
            tempPreVariance = FOUtils.getGPRRVarianceBySamplingSize(super.newPrivacyBudgetGroupList.get(i), tempGroupInnerSize / 2, tempPotentialPublicationUserSize, super.domainSize);
            prePublicationVarianceList.add(tempPreVariance);

        }
        boolean populationEnough = BasicArrayUtil.getIntegerMinValue(potentialPublicationUserSizeList) >= this.populationSizeLowerBound;
        List<SimpleMatrix> preCorrectedWeightList = null;
        Double errorPlus = Double.POSITIVE_INFINITY;
        if (populationEnough) {
            preCorrectedWeightList = ProposedSchemeUtils.getCorrectedWeight(super.sourceMap, super.currentDisEstimationMapList,
                    super.currentDisUnbiasedVarianceList, prePublicationVarianceList, Constant.EigenvaluesLowerBound);
            errorPlus = ProposedSchemeUtils.getBAPUEError(super.piList, super.sourceMap, preCorrectedWeightList,
                    prePublicationVarianceList, super.currentDisEstimationListList, super.currentDisUnbiasedVarianceList, this.domainSize);
        }

        Boolean flag;
        Set<Integer> tempGroupCandidateUserIndexSet, tempSamplingUserIndexSetForPublication;
        Double tempPrivacyBudget, tempEstimationNormSquare, tempPubUnbiasedVariance;
        this.currentPubEstimationMapList = new ArrayList<>(super.groupSize);
        this.currentPubEstimationListList = new ArrayList<>(super.groupSize);
        this.currentPubUnbiasedVarianceList = new ArrayList<>(super.groupSize);
        List<Double> tempNormalizedEstimationList;
        List<SimpleMatrix> finalWeightList;
        if (populationEnough && dissimilarity > errorPlus) {
            flag = true;
            for (int i = 0; i < super.groupSize; i++) {
                tempGroupCandidateUserIndexSet = super.candidateUserIndexSetList.get(i);
                int availablePopulationSize = tempGroupCandidateUserIndexSet.size();
                tempSamplingSize = potentialPublicationUserSizeList.get(i);
                if (tempSamplingSize > tempGroupCandidateUserIndexSet.size()) {
                    System.err.printf("Warning: group %d publication size %d exceeds available population %d, clamped.\n",
                            i, tempSamplingSize, tempGroupCandidateUserIndexSet.size());
                    tempSamplingSize = tempGroupCandidateUserIndexSet.size();
                }
                tempSamplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(tempGroupCandidateUserIndexSet, tempSamplingSize, super.random);
                tempGroupCandidateUserIndexSet.removeAll(tempSamplingUserIndexSetForPublication);
                super.publicationSubMechanismHistoryQueueList.get(i).offer(tempSamplingUserIndexSetForPublication);

                tempPrivacyBudget = super.newPrivacyBudgetGroupList.get(i);
                tempSamplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, tempSamplingUserIndexSetForPublication);
                tempInfo = MechanismUtils.gRRRaw(tempPrivacyBudget, tempSamplingDataIndexList, super.domainSize, super.random);
                this.currentPubEstimationMapList.add(tempInfo);
                tempNormalizedEstimationList = BasicUtils.toSortedListByKeys(tempInfo, this.domainIndexList, 0D);
                this.currentPubEstimationListList.add(tempNormalizedEstimationList);

                tempEstimationNormSquare = BasicCalculation.get2NormSquare(tempInfo.values());
                tempPubUnbiasedVariance = FOUtils.getUnbiasedVariance(tempPrivacyBudget, availablePopulationSize, tempSamplingSize, this.domainSize, tempEstimationNormSquare);
                this.currentPubUnbiasedVarianceList.add(tempPubUnbiasedVariance);
            }

            if (super.model == AdvancedPLPMechanism.ModelPost) {
                finalWeightList = ProposedSchemeUtils.getCorrectedWeight(super.sourceMap, this.currentPubEstimationMapList, this.currentPubUnbiasedVarianceList, this.currentPubUnbiasedVarianceList, Constant.EigenvaluesLowerBound);
            } else {
                finalWeightList = preCorrectedWeightList;
            }

            List<Map<Integer, Double>> finalGroupEstimateList = ProposedSchemeUtils.biasedAwarePersonalizedUtilityEnhancement(super.sourceMap, this.currentPubEstimationMapList, finalWeightList);
            finalEstimateMap = StatisticTool.getWeightedStatisticMap(finalGroupEstimateList, super.piList);
            super.lastCombinedRawEstimation = finalEstimateMap;
            finalEstimateNormalizedMap = BasicUtils.normalizeValues(finalEstimateMap);
            super.lastCombinedEstimation = finalEstimateNormalizedMap;
        } else {
            flag = false;
            finalEstimateNormalizedMap = super.lastCombinedEstimation;
            for (int i = 0; i < super.groupSize; i++) {
                super.publicationSubMechanismHistoryQueueList.get(i).offer(new HashSet<>());
            }
        }
        Set<Integer> tempSamplingRecycle, tempPublicationRecycle;
        if (super.currentTime >= super.recycleWindowSize) {
            for (int i = 0; i < super.groupSize; i++) {
                tempSamplingRecycle = super.samplingSubMechanismHistoryQueueList.get(i).getFirst();
                tempPublicationRecycle = super.publicationSubMechanismHistoryQueueList.get(i).getFirst();
                super.candidateUserIndexSetList.get(i).addAll(tempSamplingRecycle);
                if (tempPublicationRecycle != null) {
                    super.candidateUserIndexSetList.get(i).addAll(tempPublicationRecycle);
                }
            }
        }


        return new CombinePair<>(flag, finalEstimateNormalizedMap);

    }

    @Override
    public String getSimpleName() {
        if (super.model == AdvancedPLPMechanism.ModelPre) {
            return Constant.PLPDAblateContainingBAPUESchemeNamePre;
        }
        return Constant.PLPDAblateContainingBAPUESchemeNamePost;
    }
}
