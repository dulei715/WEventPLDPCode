package hnu.dll.schemes.main_scheme._3_advanced_scheme;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.PersonalizedMechanism;
import hnu.dll.schemes._scheme_utils.BasicSchemeUtils;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.structure.personalized_data.PrivacyGroupStatus;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.structure.selection_struct.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class AdvancedPLPMechanism extends PersonalizedMechanism {
    protected Random random;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Double> newPrivacyBudgetList;
    protected List<Integer> windowSizeList;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected GroupAwareOptimalSelectionStruct groupAwareOptimalSelectionStruct;
    protected Integer optimalWindowSize;

    protected Set<Integer> candidateUserIndexSet;

//    protected Map<Integer, Double> lastReleaseEstimation;
    // 记录上个有效发布时刻的数据状态，注意不同组的上个时刻可能不同
    protected List<PrivacyGroupStatus<Integer>> lastReleaseGroupStatusList;

    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;

    protected List<Double> distinctBudgetList;
    protected List<Integer> distinctWindowSizeList;



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

    public AdvancedPLPMechanism(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        this.currentTime = 0;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
//        this.lastReleaseEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.lastReleaseGroupStatusList = PrivacyGroupStatus.getIntializedPrivacyGroupStatusList(this.domainIndexList, this.userSize);
        this.originalPrivacyBudgetList = originalPrivacyBudgetList;
        this.windowSizeList = windowSizeList;
        if (this.originalPrivacyBudgetList.size() != this.windowSizeList.size()) {
            throw new RuntimeException("The user size in privacy budget list and window size list are not equal!");
        }
        this.userSize = this.windowSizeList.size();
        this.candidateUserIndexSet = new HashSet<>();
        this.candidateUserIndexSet.addAll(BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.userSize - 1));
        this.random = random;
        this.distinctBudgetList = BasicArrayUtil.getUniqueList(this.originalPrivacyBudgetList);
        this.distinctWindowSizeList = BasicArrayUtil.getUniqueList(this.windowSizeList);
        initializeOptimalParameters();
    }

//    protected abstract void setCalculationPrivacyBudgetList();
//
//    protected abstract void setPublicationPrivacyBudgetList();


    public List<Double> getOriginalPrivacyBudgetList() {
        return originalPrivacyBudgetList;
    }

    public List<Double> getNewPrivacyBudgetList() {
        return newPrivacyBudgetList;
    }

    public Integer getOptimalWindowSize() {
        return this.optimalWindowSize;
    }

    /**
     * 关于Optimal Population Selection相关信息
     * @return
     */
    public GroupAwareOptimalSelectionStruct getGroupAwareOptimalSelectionStruct() {
        return groupAwareOptimalSelectionStruct;
    }
    public List<Double> getOptimalPrivacyGroupErrorList() {
        return this.groupAwareOptimalSelectionStruct.getOptimalErrorList();
    }
    public List<Integer> getOptimalSamplingSizeList() {
        return this.groupAwareOptimalSelectionStruct.getOptimalSamplingSizeList();
    }
    public List<Double> getOptimalNewBudgetList() {
        return this.groupAwareOptimalSelectionStruct.getOptimalEffectivePrivacyBudgetList();
    }



    public List<Integer> getWindowSizeList() {
        return windowSizeList;
    }

    public List<PrivacyGroupStatus<Integer>> getLastReleaseGroupStatusList() {
        return this.lastReleaseGroupStatusList;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    public void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
        List<Integer> samplingSizeList = BasicSchemeUtils.getSamplingSizeList(this.windowSizeList);
        this.groupAwareOptimalSelectionStruct = ProposedSchemeUtils.optimalPopulationSelection(samplingSizeList, this.originalPrivacyBudgetList, this.domainSize);
        this.newPrivacyBudgetList = this.groupAwareOptimalSelectionStruct.getNewPrivacyBudgetList();
        this.optimalWindowSize = this.windowSizeList.get(this.groupAwareOptimalSelectionStruct.getOptimalSamplingSizeIndex());
        // 这里初始化历史窗口大小为 w_opt - 1
        this.samplingSubMechanismHistoryQueue = new HistoryPopulationQueue(this.optimalWindowSize);
        this.publicationSubMechanismHistoryQueue = new HistoryPopulationQueue(this.optimalWindowSize);
    }

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
        Integer optimalSamplingSize = this.getOptimalSamplingSize();
        Set<Integer> samplingUserIndexSetForCalculation = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, optimalSamplingSize, random);
        this.candidateUserIndexSet.removeAll(samplingUserIndexSetForCalculation);
        this.samplingSubMechanismHistoryQueue.offer(samplingUserIndexSetForCalculation);
        // 获取当前 privacy budget 列表
        List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.newPrivacyBudgetList, samplingUserIndexSetForCalculation);
        // 获取当前 data 列表
        List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForCalculation);

        CombinePair<Map<Integer, Double>, Map<Double, Integer>> gPRRInfo = MechanismUtils.gPRR(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random);
        Map<Integer, Double> normalizedEstimationMap = gPRRInfo.getKey();
        Map<Double, Integer> rePerturbedEpsilonCount = gPRRInfo.getValue();

        List<Double> normalizedEstimationList = BasicUtils.toSortedListByKeys(normalizedEstimationMap, this.domainIndexList, 0D);

        Double pldpVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(rePerturbedEpsilonCount, this.domainSize);
        List<Double> lastReleaseEstimationList = BasicUtils.toSortedListByKeys(this.lastReleaseEstimation, this.domainIndexList, 0D);
        return PFOUtils.getDissimilarity(normalizedEstimationList, lastReleaseEstimationList, pldpVarianceSum);
    }

    // M_{r,t}
    protected abstract CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity);



    public abstract String getSimpleName();
}
