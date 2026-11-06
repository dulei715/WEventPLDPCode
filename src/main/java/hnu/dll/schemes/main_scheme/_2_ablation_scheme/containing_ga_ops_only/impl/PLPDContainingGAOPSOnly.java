package hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ga_ops_only.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.matrix.SimpleVectorUtil;
import cn.edu.dll.statistic.StatisticTool;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ga_ops_only.AblationPLPMechanismContainingGAOPSOnly;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class PLPDContainingGAOPSOnly extends AblationPLPMechanismContainingGAOPSOnly {

    protected Integer populationSizeLowerBound;

    protected GroupAwareOptimalSelectionStruct pubGroupAwareOptimalSelectionStruct;
    protected List<Map<Integer, Double>> currentPubEstimationMapList;
    protected List<List<Double>> currentPubEstimationListList;
    protected List<Double> currentPubUnbiasedVarianceList;

    public PLPDContainingGAOPSOnly(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Integer populationSizeLowerBound, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
        this.populationSizeLowerBound  =  populationSizeLowerBound;
    }


    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        int tempGroupInnerSize, remainingPopulationSize, tempPotentialPublicationUserSize, tempSamplingSize;
        List<Integer> potentialPublicationUserSizeList = new ArrayList<>(super.groupSize);
        List<Integer> tempSamplingDataIndexList;
        List<Double> prePublicationVarianceList = new ArrayList<>(super.groupSize);
        Map<Integer, Double> tempInfo;
        Map<Integer, Double> finalEstimateMap, finalEstimateNormalizedMap;
        Double tempPreVariance;
        for (int i = 0; i < super.groupSize; i++) {
            tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
            remainingPopulationSize = tempGroupInnerSize / 2 - super.publicationSubMechanismHistoryQueueList.get(i).getReverseSizeSum(super.newWindowSizeGroupList.get(i) - 1);
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
//        List<SimpleMatrix> preCorrectedWeightList = null;
        Double errorPlus = Double.POSITIVE_INFINITY;
        if (populationEnough) {
            errorPlus = SimpleVectorUtil.getInnerProduct(prePublicationVarianceList, super.piList, 2D) / super.domainSize;
        }

        boolean flag;
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

            finalEstimateMap = StatisticTool.getWeightedStatisticMap(this.currentPubEstimationMapList, super.piList);
            super.lastGroupEstimationList = this.currentPubEstimationMapList;
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
        for (int i = 0; i < super.groupSize; i++) {
            if (super.currentTime >= super.newWindowSizeGroupList.get(i)) {
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
        return Constant.PLPDAblateContainingGAOPSchemeName;
    }
}
