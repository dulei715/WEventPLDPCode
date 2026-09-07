package hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.statistic.StatisticTool;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.BasicMethodUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.AblationPLPMechanismContainingBAPUEOnly;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.utils.BasicUtils;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class PLPAContainingBAPUEOnly extends AblationPLPMechanismContainingBAPUEOnly {

    protected Integer populationSizeLowerBound;

    protected List<Integer> lastPublicationTimeList;
    protected List<Integer> lastPublicationPopulationSizeList;
    protected List<Map<Integer, Double>> lastPubEstimationMapList;
    protected List<List<Double>> lastPubEstimationListList;
    protected List<Double> lastPubUnbiasedVarianceList;

    protected List<Map<Integer, Double>> currentPubEstimationMapList;
    protected List<List<Double>> currentPubEstimationListList;
    protected List<Double> currentPubUnbiasedVarianceList;

    public PLPAContainingBAPUEOnly(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Integer populationSizeLowerBound, boolean model, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, model, random);
        this.populationSizeLowerBound = populationSizeLowerBound;
        this.lastPublicationTimeList = new ArrayList<>(super.groupSize);
        this.lastPublicationPopulationSizeList = new ArrayList<>(super.groupSize);
        this.lastPubEstimationMapList = new ArrayList<>(super.groupSize);
        this.lastPubEstimationListList = new ArrayList<>(super.groupSize);
        this.lastPubUnbiasedVarianceList = new ArrayList<>(super.groupSize);
        for (int i = 0; i < super.groupSize; i++) {
            Map<Integer, Double> initializedEstimateMap = MapUtils.getInitializedMap(super.domainIndexList, 0D);
            this.lastPublicationTimeList.add(-1);
            this.lastPublicationPopulationSizeList.add(0);
            this.lastPubEstimationMapList.add(initializedEstimateMap);
            this.lastPubEstimationListList.add(BasicUtils.toSortedListByKeys(initializedEstimateMap, super.domainIndexList, 0D));
            this.lastPubUnbiasedVarianceList.add(0D);
        }
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        Set<Integer> activeGroupIndexSet = new HashSet<>(), reusedGroupIndexSet = new HashSet<>();
        List<Integer> potentialPublicationUserSizeList = new ArrayList<>(Collections.nCopies(super.groupSize, 0));
        List<Map<Integer, Double>> mixedPublicationEstimateMapList = new ArrayList<>(super.groupSize);
        List<List<Double>> mixedPublicationEstimateListList = new ArrayList<>(super.groupSize);
        List<Double> mixedPublicationVarianceList = new ArrayList<>(super.groupSize);
        List<Double> preEstimationVarianceList = new ArrayList<>(super.groupSize);

        int tempOptimalSamplingSize, tempNullifiedLength, tempAbsorbedLength, tempPotentialPublicationUserSize;
        int tempLastPublicationTime, tempLastPublicationPopulationSize, tempGroupInnerSize;
        Double tempPrePublicationVariance;
        for (int i = 0; i < super.groupSize; i++) {
            tempOptimalSamplingSize = super.newSamplingSizeGroupList.get(i);
            tempLastPublicationTime = this.lastPublicationTimeList.get(i);
            tempLastPublicationPopulationSize = this.lastPublicationPopulationSizeList.get(i);
            tempNullifiedLength = tempLastPublicationPopulationSize / tempOptimalSamplingSize - 1;

            if (super.currentTime - tempLastPublicationTime <= tempNullifiedLength) {
                reusedGroupIndexSet.add(i);
                mixedPublicationEstimateMapList.add(this.lastPubEstimationMapList.get(i));
                mixedPublicationEstimateListList.add(this.lastPubEstimationListList.get(i));
                mixedPublicationVarianceList.add(this.lastPubUnbiasedVarianceList.get(i));
                preEstimationVarianceList.add(this.lastPubUnbiasedVarianceList.get(i));
            } else {
                activeGroupIndexSet.add(i);
                tempAbsorbedLength = super.currentTime - tempLastPublicationTime - tempNullifiedLength;
                tempPotentialPublicationUserSize = tempOptimalSamplingSize * Math.min(tempAbsorbedLength, super.recycleWindowSize);
                potentialPublicationUserSizeList.set(i, tempPotentialPublicationUserSize);

                tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
                if (tempPotentialPublicationUserSize < this.populationSizeLowerBound) {
                    tempPrePublicationVariance = Double.POSITIVE_INFINITY;
                } else {
                    tempPrePublicationVariance = FOUtils.getGPRRVarianceBySamplingSize(
                            super.newPrivacyBudgetGroupList.get(i), tempGroupInnerSize / 2,
                            tempPotentialPublicationUserSize, super.domainSize);
                }
                mixedPublicationEstimateMapList.add(super.currentDisEstimationMapList.get(i));
                mixedPublicationEstimateListList.add(super.currentDisEstimationListList.get(i));
                mixedPublicationVarianceList.add(tempPrePublicationVariance);
                preEstimationVarianceList.add(super.currentDisUnbiasedVarianceList.get(i));
            }
        }

        boolean populationEnough = true;
        for (Integer activeGroupIndex : activeGroupIndexSet) {
            if (potentialPublicationUserSizeList.get(activeGroupIndex) < this.populationSizeLowerBound) {
                populationEnough = false;
                break;
            }
        }

        List<SimpleMatrix> preCorrectedWeightList = null;
        Double errorPlus = Double.POSITIVE_INFINITY;
        List<Double> gammaParameterList;
        if (!activeGroupIndexSet.isEmpty() && populationEnough) {
            preCorrectedWeightList = ProposedSchemeUtils.getPartialAbsorptionCorrectedWeight(
                    super.sourceMap, mixedPublicationEstimateMapList, super.currentDisEstimationMapList,
                    preEstimationVarianceList, mixedPublicationVarianceList,
                    super.currentDisUnbiasedVarianceList, reusedGroupIndexSet, Constant.EigenvaluesLowerBound);
            gammaParameterList = BasicMethodUtils.getGammaParameterList(super.piList, super.sourceMap, preCorrectedWeightList);
            errorPlus = ProposedSchemeUtils.getPartialAbsorptionBAPUEError(
                    super.piList, gammaParameterList,
                    mixedPublicationEstimateListList, mixedPublicationVarianceList,
                    super.currentDisEstimationListList, super.currentDisUnbiasedVarianceList,
                    reusedGroupIndexSet, super.domainSize);
        }

        Boolean flag;
        Map<Integer, Double> finalEstimateNormalizedMap;
        this.currentPubEstimationMapList = new ArrayList<>(mixedPublicationEstimateMapList);
        this.currentPubEstimationListList = new ArrayList<>(mixedPublicationEstimateListList);
        this.currentPubUnbiasedVarianceList = new ArrayList<>(mixedPublicationVarianceList);

        if (!activeGroupIndexSet.isEmpty() && populationEnough && dissimilarity > errorPlus) {
            flag = true;
            for (int i = 0; i < super.groupSize; i++) {
                if (!activeGroupIndexSet.contains(i)) {
                    super.publicationSubMechanismHistoryQueueList.get(i).offer(new HashSet<>());
                    continue;
                }

                Set<Integer> tempGroupCandidateUserIndexSet = super.candidateUserIndexSetList.get(i);
                int availablePopulationSize = tempGroupCandidateUserIndexSet.size();
                int tempSamplingSize = potentialPublicationUserSizeList.get(i);
                Set<Integer> tempSamplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(
                        tempGroupCandidateUserIndexSet, tempSamplingSize, super.random);
                tempGroupCandidateUserIndexSet.removeAll(tempSamplingUserIndexSetForPublication);
                super.publicationSubMechanismHistoryQueueList.get(i).offer(tempSamplingUserIndexSetForPublication);

                List<Integer> tempSamplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(
                        nextDataIndexList, tempSamplingUserIndexSetForPublication);
                Double tempPrivacyBudget = super.newPrivacyBudgetGroupList.get(i);
                Map<Integer, Double> tempInfo = MechanismUtils.gRRRaw(
                        tempPrivacyBudget, tempSamplingDataIndexList, super.domainSize, super.random);
                List<Double> tempEstimationList = BasicUtils.toSortedListByKeys(tempInfo, super.domainIndexList, 0D);
                Double tempEstimationNormSquare = BasicCalculation.get2NormSquare(tempInfo.values());
                Double tempPubUnbiasedVariance = FOUtils.getUnbiasedVariance(
                        tempPrivacyBudget, availablePopulationSize, tempSamplingSize,
                        super.domainSize, tempEstimationNormSquare);

                this.currentPubEstimationMapList.set(i, tempInfo);
                this.currentPubEstimationListList.set(i, tempEstimationList);
                this.currentPubUnbiasedVarianceList.set(i, tempPubUnbiasedVariance);
                this.lastPublicationTimeList.set(i, super.currentTime);
                this.lastPublicationPopulationSizeList.set(i, tempSamplingSize);
                this.lastPubEstimationMapList.set(i, tempInfo);
                this.lastPubEstimationListList.set(i, tempEstimationList);
                this.lastPubUnbiasedVarianceList.set(i, tempPubUnbiasedVariance);
            }

            List<SimpleMatrix> finalWeightList;
            if (super.model == AdvancedPLPMechanism.ModelPost) {
                finalWeightList = ProposedSchemeUtils.getPartialAbsorptionCorrectedWeight(
                        super.sourceMap, this.currentPubEstimationMapList, super.currentDisEstimationMapList,
                        this.currentPubUnbiasedVarianceList, this.currentPubUnbiasedVarianceList,
                        super.currentDisUnbiasedVarianceList, reusedGroupIndexSet, Constant.EigenvaluesLowerBound);
            } else {
                finalWeightList = preCorrectedWeightList;
            }

            List<Map<Integer, Double>> finalGroupEstimateList = ProposedSchemeUtils.biasedAwarePersonalizedUtilityEnhancement(
                    super.sourceMap, this.currentPubEstimationMapList, finalWeightList);
            Map<Integer, Double> finalEstimateMap = StatisticTool.getWeightedStatisticMap(finalGroupEstimateList, super.piList);
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
            return Constant.PLPAAblateContainingBAPUESchemeNamePre;
        }
        return Constant.PLPAAblateContainingBAPUESchemeNamePost;
    }
}
