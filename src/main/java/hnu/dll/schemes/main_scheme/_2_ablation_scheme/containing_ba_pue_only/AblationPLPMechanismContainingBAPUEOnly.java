package hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.statistic.StatisticTool;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.PersonalizedMechanism;
import hnu.dll.schemes._scheme_utils.AdvancedSchemeUtils;
import hnu.dll.schemes._scheme_utils.BasicSchemeUtils;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.BasicMethodUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.structure.personalized_data.SimplePrivacyGroup;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.structure.selection_struct.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class AblationPLPMechanismContainingBAPUEOnly extends PersonalizedMechanism {


    protected Random random;
    protected boolean model;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Integer> originalWindowSizeList;

    protected Integer recycleWindowSize;
    protected Integer optimalTotalSamplingSize;
    //    protected List<Integer> newWindowSizeGroupList;
    protected List<Double> newPrivacyBudgetList;
    protected List<Double> newPrivacyBudgetGroupList;
    protected List<Integer> newSamplingSizeGroupList;

    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;
    protected Integer groupSize;

//    protected GroupAwareOptimalSelectionStruct disGroupAwareOptimalSelectionStruct;
    protected OptimalSelectionStruct optimalSelectionStruct;


    protected List<Set<Integer>> candidateUserIndexSetList;

//    protected Map<Integer, Double> lastReleaseEstimation;
    // 记录上个有效发布时刻的数据状态，注意不同组的上个时刻可能不同（PLPD+是相同的，但PLPA+是不同的）
//    protected List<PrivacyGroupStatus<Integer>> lastReleaseGroupStatusList;
    protected Map<Integer, Double> lastCombinedEstimation;
    protected Map<Integer, Double> lastCombinedRawEstimation;

    protected List<HistoryPopulationQueue> samplingSubMechanismHistoryQueueList;
    protected List<HistoryPopulationQueue> publicationSubMechanismHistoryQueueList;

    protected List<Double> distinctBudgetList;
    protected List<Integer> distinctWindowSizeList;
    protected List<SimplePrivacyGroup> privacyGroupList;
    protected List<Double> piList;
    protected Map<Integer, List<Integer>> sourceMap;

    protected List<Map<Integer, Double>> currentDisEstimationMapList;
    protected List<List<Double>> currentDisEstimationListList;
    protected List<Double> currentDisUnbiasedVarianceList;



    @Override
    public List<Double> getDistinctBudgetList() {
        return this.distinctBudgetList;
    }

    @Override
    public List<Integer> getDistinctWindowSizeList() {
        return distinctWindowSizeList;
    }

    @Override
    public List<Integer> getDomainIndexList() {
        return domainIndexList;
    }

    public AblationPLPMechanismContainingBAPUEOnly(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> originalWindowSizeList, boolean model, Random random) {
        this.currentTime = 0;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
        this.lastCombinedEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.lastCombinedRawEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.originalPrivacyBudgetList = originalPrivacyBudgetList;
        this.originalWindowSizeList = originalWindowSizeList;
        if (this.originalPrivacyBudgetList.size() != this.originalWindowSizeList.size()) {
            throw new RuntimeException("The user size in privacy budget list and window size list are not equal!");
        }
        this.userSize = this.originalWindowSizeList.size();
        this.random = random;
        this.model = model;
        this.distinctBudgetList = BasicArrayUtil.getUniqueList(this.originalPrivacyBudgetList);
        this.distinctWindowSizeList = BasicArrayUtil.getUniqueList(this.originalWindowSizeList);
        this.privacyGroupList = AdvancedSchemeUtils.getGroupSamplingSizeList(this.originalWindowSizeList, this.originalPrivacyBudgetList);
        this.sourceMap = BasicMethodUtils.constructDominatingGroupsAsIndex(this.privacyGroupList);
        initializeOptimalParameters();
    }



    public List<Double> getOriginalPrivacyBudgetList() {
        return originalPrivacyBudgetList;
    }


    public List<Integer> getNewSamplingSizeGroupList() {
        return this.newSamplingSizeGroupList;
    }

    /**
     * 关于Optimal Population Selection相关信息
     * @return
     */
    public OptimalSelectionStruct getOptimalSelectionStruct() {
        return optimalSelectionStruct;
    }

    public Double getOptimalError() {
        return this.optimalSelectionStruct.getError();
    }
    public Integer getOptimalTotalSamplingSize() {
        return this.optimalTotalSamplingSize;
    }
    public List<Integer> getOptimalSamplingSizeList() {
        return this.newSamplingSizeGroupList;
    }
    public List<Double> getNewPrivacyBudgetList() {
        return this.newPrivacyBudgetList;
    }



    public List<Integer> getOriginalWindowSizeList() {
        return originalWindowSizeList;
    }

    public Map<Integer, Double> getLastReleaseGroupStatusList() {
        return this.lastCombinedEstimation;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    public void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
//        this.disGroupAwareOptimalSelectionStruct = ProposedSchemeUtils.groupAwareOptimalPopulationSelection(this.privacyGroupList, 0.5, this.domainSize);
        List<Integer> samplingSizeList = BasicSchemeUtils.getSamplingSizeList(this.originalWindowSizeList);
        this.optimalSelectionStruct = ProposedSchemeUtils.optimalPopulationSelection(samplingSizeList, this.originalPrivacyBudgetList, this.originalWindowSizeList, 0.5, this.domainSize);
        this.newPrivacyBudgetList = this.optimalSelectionStruct.getNewPrivacyBudgetList();
        this.recycleWindowSize = this.optimalSelectionStruct.getPopulationRecycleWindowSize();
        this.optimalTotalSamplingSize = this.optimalSelectionStruct.getOptimalSamplingSize();

        this.groupSize = this.privacyGroupList.size();




//        this.newPrivacyBudgetGroupList = this.disGroupAwareOptimalSelectionStruct.getOptimalEffectivePrivacyBudgetList();
//        this.newSamplingSizeGroupList = this.disGroupAwareOptimalSelectionStruct.getOptimalSamplingSizeList();

//        this.newWindowSizeGroupList = this.disGroupAwareOptimalSelectionStruct.getEffectiveRecycleSizeList();
//        // 这里初始化历史窗口大小为其个性化窗口
////        int maxOptimalWindowSize = Collections.max(this.windowSizeGroupList);

        this.samplingSubMechanismHistoryQueueList = new ArrayList<>(this.groupSize);
        this.publicationSubMechanismHistoryQueueList = new ArrayList<>(this.groupSize);
        this.candidateUserIndexSetList = new ArrayList<>(this.groupSize);
        this.piList = new ArrayList<>(this.groupSize);
        SimplePrivacyGroup tempGroup;
        for (int i = 0; i < this.groupSize; i++) {
            this.samplingSubMechanismHistoryQueueList.add(new HistoryPopulationQueue<Integer>(this.recycleWindowSize));
            this.publicationSubMechanismHistoryQueueList.add(new HistoryPopulationQueue<Integer>(this.recycleWindowSize));
            tempGroup = this.privacyGroupList.get(i);
            Set<Integer> tempUserIndexSet = new HashSet<>(tempGroup.getUserIDSet());
            this.candidateUserIndexSetList.add(tempUserIndexSet);
            this.piList.add(tempGroup.getUserSize() * 1.0 / this.userSize);
        }
        this.newSamplingSizeGroupList = ProposedSchemeUtils.getSamplingSizeGroupListByLargestRemainder(this.optimalTotalSamplingSize, this.piList);
        // 每组配额按回收窗口取可持续上界 z_r <= floor(n_r / tau_opt)，避免小人数组被余数补偿+1后被抽空
        for (int i = 0; i < this.groupSize; i++) {
            int tempSustainableSamplingSize = this.privacyGroupList.get(i).getUserSize() / this.recycleWindowSize;
            if (this.newSamplingSizeGroupList.get(i) > tempSustainableSamplingSize) {
                this.newSamplingSizeGroupList.set(i, tempSustainableSamplingSize);
            }
        }

        this.newPrivacyBudgetGroupList = ProposedSchemeUtils.getNewGroupPrivacyBudgetList(this.privacyGroupList, this.recycleWindowSize);
    }

//    protected abstract Map<Integer, Double> getLastCombinedEstimation();

    public CombinePair<Boolean, Map<Integer, Double>> updateNextPublicationResult(List<Integer> nextDataIndexList) {

        ++this.currentTime;
        // M_{s,t}
        Double dissimilarity = samplingSubMechanism(nextDataIndexList);

        // M_{r,t}
        return reportingSubMechanism(nextDataIndexList, dissimilarity);
    }


    /**
     * M_{s,t}
     * @param nextDataIndexList 要求该数据顺序和用户信息顺序一致
     * @return
     */
    protected Double samplingSubMechanism(List<Integer> nextDataIndexList) {
        int tempSamplingSize;
        Set<Integer> tempSamplingUserIndexSetForCalculation;
        double tempPrivacyBudget, tempEstimationNormSquare, tempUnbiasedVariance, tempPi, totalPiWeightedUnbiasedVarianceSum = 0.0;
        List<Integer> tempSamplingDataIndexList;

        Map<Integer, Double> tempGRRInfo;
        List<Double> tempNormalizedEstimationList;

        this.currentDisEstimationMapList = new ArrayList<>(this.groupSize);
        this.currentDisEstimationListList = new ArrayList<>(this.groupSize);

        this.currentDisUnbiasedVarianceList = new ArrayList<>(this.groupSize);

        for (int i = 0; i < this.groupSize; i++) {
            tempSamplingSize = this.newSamplingSizeGroupList.get(i);
            tempPi = this.piList.get(i);

            Set<Integer> availableUserIndexSet = this.candidateUserIndexSetList.get(i);
            int availablePopulationSize = availableUserIndexSet.size();
            tempSamplingUserIndexSetForCalculation = RandomUtil.extractRandomElementWithoutRepeatFromSet(availableUserIndexSet, tempSamplingSize, this.random);
            availableUserIndexSet.removeAll(tempSamplingUserIndexSetForCalculation);

            this.samplingSubMechanismHistoryQueueList.get(i).offer(tempSamplingUserIndexSetForCalculation);
            // todo: 不用抽取epsilon list, 因为同一组的 epsilon 都一样
            tempPrivacyBudget = this.newPrivacyBudgetGroupList.get(i);
            tempSamplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, tempSamplingUserIndexSetForCalculation);
            tempGRRInfo = MechanismUtils.gRRRaw(tempPrivacyBudget, tempSamplingDataIndexList, this.domainSize, this.random);
            this.currentDisEstimationMapList.add(tempGRRInfo);
            tempNormalizedEstimationList = BasicUtils.toSortedListByKeys(tempGRRInfo, this.domainIndexList, 0D);
            currentDisEstimationListList.add(tempNormalizedEstimationList);

            tempEstimationNormSquare = BasicCalculation.get2NormSquare(tempGRRInfo.values());
            tempUnbiasedVariance = FOUtils.getUnbiasedVariance(tempPrivacyBudget, availablePopulationSize, tempSamplingSize, this.domainSize, tempEstimationNormSquare);
            this.currentDisUnbiasedVarianceList.add(tempUnbiasedVariance);
            totalPiWeightedUnbiasedVarianceSum += tempPi * tempPi * tempUnbiasedVariance;
        }

        List<Double> populationWeightedEstimationList = StatisticTool.getWeightedStatisticList(this.currentDisEstimationListList, this.piList);
        List<Double> lastReleaseEstimationList = BasicUtils.toSortedListByKeys(this.lastCombinedRawEstimation, this.domainIndexList, 0D);

        return PFOUtils.getDissimilarity(populationWeightedEstimationList, lastReleaseEstimationList, totalPiWeightedUnbiasedVarianceSum);
    }

    // M_{r,t}
    protected abstract CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity);



    public abstract String getSimpleName();
}
