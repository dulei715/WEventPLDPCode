package hnu.dll.schemes.main_scheme._3_advanced_scheme.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.statistic.StatisticTool;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class PLPDPlus extends AdvancedPLPMechanism {

    protected GroupAwareOptimalSelectionStruct pubGroupAwareOptimalSelectionStruct;
    protected List<Map<Integer, Double>> currentPubEstimationMapList;
    protected List<List<Double>> currentPubEstimationListList;
    protected List<Double> currentPubUnbiasedVarianceList;

    public PLPDPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, boolean model, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, model, random);
    }


    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        int tempRecyclePeriod;
        int tempGroupInnerSize, tempOptimalSamplingSize, remainingPopulationSize, tempPotentialPublicationUserSize, tempSamplingSize;
        List<Integer> groupOptimalSamplingSizeList = super.disGroupAwareOptimalSelectionStruct.getOptimalSamplingSizeList(), potentialPublicationUserSizeList = new ArrayList<>(super.groupSize);
        List<Integer> tempSamplingDataIndexList;
        List<Double> prePublicationVarianceList = new ArrayList<>(super.groupSize);
//        List<PrivacyGroupTriple> publicationPrivacyGroupTripleList = new ArrayList<>(super.groupSize);
        Map<Integer, Double> tempInfo;
        Map<Integer, Double> finalEstimateMap;
        Double tempPreVariance;
        for (int i = 0; i < super.groupSize; i++) {
            tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
//            tempOptimalSamplingSize = groupOptimalSamplingSizeList.get(i);
//            tempRecyclePeriod = Math.max(1, tempGroupInnerSize / tempOptimalSamplingSize);  // 这里用了整数除法截断性质实现向下取整
            remainingPopulationSize = tempGroupInnerSize / 2 - super.publicationSubMechanismHistoryQueueList.get(i).getReverseSizeSum(super.newWindowSizeGroupList.get(i) - 1);
            tempPotentialPublicationUserSize = remainingPopulationSize / 2;     // 这里用了整数除法截断性质实现向下取整
            potentialPublicationUserSizeList.add(tempPotentialPublicationUserSize);
//            publicationPrivacyGroupTripleList.add(new PrivacyGroupTriple(super.newWindowSizeGroupList.get(i), super.distinctBudgetList.get(i), potentialPublicationUserSize));
            tempPreVariance = FOUtils.getGPRRVarianceBySamplingSize(super.newPrivacyBudgetGroupList.get(i), tempGroupInnerSize / 2, tempPotentialPublicationUserSize, super.domainSize);
            prePublicationVarianceList.add(tempPreVariance);

        }
//        this.pubGroupAwareOptimalSelectionStruct = ProposedSchemeUtils.groupAwareOptimalPopulationSelection(publicationPrivacyGroupTripleList, 1.0, super.domainSize);
//        List<Double> optimalErrorList = this.pubGroupAwareOptimalSelectionStruct.getOptimalErrorList();
//        List<Integer> optimalPubSamplingSizeList = this.pubGroupAwareOptimalSelectionStruct.getOptimalSamplingSizeList();
//        List<Double> optimalEffectivePublicationPrivacyBudgetList = this.pubGroupAwareOptimalSelectionStruct.getOptimalEffectivePrivacyBudgetList();
//        List<Double> prePublicationVarianceList = BasicArrayUtil.getLinearTransformToList(optimalErrorList, super.domainSize, 0);
        List<SimpleMatrix> preCorrectedWeightList = ProposedSchemeUtils.getCorrectedWeight(super.sourceMap, super.currentDisEstimationMapList, super.currentDisUnbiasedVarianceList, prePublicationVarianceList, Constant.EigenvaluesLowerBound);
        Double errorPlus = ProposedSchemeUtils.getBAPUEError(super.piList, super.sourceMap, preCorrectedWeightList, prePublicationVarianceList, super.currentDisEstimationListList, super.currentDisUnbiasedVarianceList, this.domainSize);
        Boolean flag;
        Set<Integer> tempGroupCandidateUserIndexSet, tempSamplingUserIndexSetForPublication;
        Double tempPrivacyBudget, tempEstimationNormSquare, tempPubUnbiasedVariance;
        this.currentPubEstimationMapList = new ArrayList<>(super.groupSize);
        this.currentPubEstimationListList = new ArrayList<>(super.groupSize);
        this.currentDisUnbiasedVarianceList = new ArrayList<>(super.groupSize);
        List<Double> tempNormalizedEstimationList;
        List<SimpleMatrix> finalWeightList;
        if (dissimilarity > errorPlus && BasicArrayUtil.getIntegerMinValue(potentialPublicationUserSizeList) >= Constant.PopulationLowerBound) {
            flag = true;
            for (int i = 0; i < super.groupSize; i++) {
                tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
                tempGroupCandidateUserIndexSet = super.candidateUserIndexSetList.get(i);
                tempSamplingSize = potentialPublicationUserSizeList.get(i);
                tempSamplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(tempGroupCandidateUserIndexSet, tempSamplingSize, super.random);
                tempGroupCandidateUserIndexSet.removeAll(tempSamplingUserIndexSetForPublication);
                super.publicationSubMechanismHistoryQueueList.get(i).offer(tempSamplingUserIndexSetForPublication);

                tempPrivacyBudget = super.newPrivacyBudgetGroupList.get(i);
                tempSamplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, tempSamplingUserIndexSetForPublication);
                tempInfo = MechanismUtils.gRR(tempPrivacyBudget, tempSamplingDataIndexList, super.domainSize, super.random);
                this.currentPubEstimationMapList.add(tempInfo);
                tempNormalizedEstimationList = BasicUtils.toSortedListByKeys(tempInfo, this.domainIndexList, 0D);
                this.currentPubEstimationListList.add(tempNormalizedEstimationList);

                tempEstimationNormSquare = BasicCalculation.get2NormSquare(tempInfo.values());
                tempPubUnbiasedVariance = FOUtils.getUnbiasedVariance(tempPrivacyBudget, tempGroupInnerSize, tempSamplingSize, this.domainSize, tempEstimationNormSquare);
                this.currentPubUnbiasedVarianceList.add(tempPubUnbiasedVariance);
            }

            if (super.model == AdvancedPLPMechanism.ModelPost) {
                finalWeightList = ProposedSchemeUtils.getCorrectedWeight(super.sourceMap, this.currentPubEstimationMapList, this.currentPubUnbiasedVarianceList, this.currentPubUnbiasedVarianceList, Constant.EigenvaluesLowerBound);
            } else {
                finalWeightList = preCorrectedWeightList;
            }

            List<Map<Integer, Double>> finalGroupEstimateList = ProposedSchemeUtils.biasedAwarePersonalizedUtilityEnhancement(super.sourceMap, this.currentPubEstimationMapList, finalWeightList);
            finalEstimateMap = StatisticTool.getWeightedStatisticMap(finalGroupEstimateList, super.piList);
            super.lastCombinedEstimation = finalEstimateMap;
        } else {
            flag = false;
            finalEstimateMap = super.lastCombinedEstimation;
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


        return new CombinePair<>(flag, finalEstimateMap);

    }

    @Override
    public String getSimpleName() {
        return "PLPD+";
    }
}
