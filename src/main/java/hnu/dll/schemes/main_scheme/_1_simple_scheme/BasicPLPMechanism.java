package hnu.dll.schemes.main_scheme._1_simple_scheme;

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
import hnu.dll.structure.selection_struct.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class BasicPLPMechanism extends PersonalizedMechanism {
    protected Random random;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Double> newPrivacyBudgetList;
    protected List<Integer> originalWindowSizeList;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected OptimalSelectionStruct optimalSelectionStruct;
    protected Integer recycleWindowSize;

    protected Set<Integer> candidateUserIndexSet;

    protected Map<Integer, Double> lastRawReleaseEstimation;
    protected Map<Integer, Double> lastReleaseEstimation;

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

    public BasicPLPMechanism(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> originalWindowSizeList, Random random) {
        this.currentTime = 0;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
        this.lastRawReleaseEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.lastReleaseEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.originalPrivacyBudgetList = originalPrivacyBudgetList;
        this.originalWindowSizeList = originalWindowSizeList;
        if (this.originalPrivacyBudgetList.size() != this.originalWindowSizeList.size()) {
            throw new RuntimeException("The user size in privacy budget list and window size list are not equal!");
        }
        this.userSize = this.originalWindowSizeList.size();
        this.candidateUserIndexSet = new HashSet<>();
        this.candidateUserIndexSet.addAll(BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.userSize - 1));
        this.random = random;
        this.distinctBudgetList = BasicArrayUtil.getUniqueList(this.originalPrivacyBudgetList);
        this.distinctWindowSizeList = BasicArrayUtil.getUniqueList(this.originalWindowSizeList);
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

    public Integer getRecycleWindowSize() {
        return this.recycleWindowSize;
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
    public Integer getOptimalSamplingSize() {
        return this.optimalSelectionStruct.getOptimalSamplingSize();
    }
    public List<Double> getOptimalNewBudgetList() {
        return this.optimalSelectionStruct.getNewPrivacyBudgetList();
    }

    public List<Integer> getOriginalWindowSizeList() {
        return originalWindowSizeList;
    }

    public Map<Integer, Double> getReleaseNoiseCountMap() {
        return this.lastReleaseEstimation;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    public void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
        List<Integer> samplingSizeList = BasicSchemeUtils.getSamplingSizeList(this.originalWindowSizeList);
        this.optimalSelectionStruct = ProposedSchemeUtils.optimalPopulationSelection(samplingSizeList, this.originalPrivacyBudgetList, this.originalWindowSizeList, 0.5, this.domainSize);
        this.newPrivacyBudgetList = this.optimalSelectionStruct.getNewPrivacyBudgetList();
//        this.optimalWindowSize = this.windowSizeList.get(this.optimalSelectionStruct.getOptimalSamplingSizeIndex());
        this.recycleWindowSize = this.optimalSelectionStruct.getPopulationRecycleWindowSize();
        // 这里初始化历史窗口大小为 w_opt
        this.samplingSubMechanismHistoryQueue = new HistoryPopulationQueue(this.recycleWindowSize);
        this.publicationSubMechanismHistoryQueue = new HistoryPopulationQueue(this.recycleWindowSize);
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

        CombinePair<Map<Integer, Double>, Map<Double, Integer>> gPRRInfo = MechanismUtils.gPRRRaw(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random);
        Map<Integer, Double> rawEstimationMap = gPRRInfo.getKey();
        Map<Double, Integer> rePerturbedRawEpsilonCount = gPRRInfo.getValue();

        List<Double> rawEstimationList = BasicUtils.toSortedListByKeys(rawEstimationMap, this.domainIndexList, 0D);

        Double pldpVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(rePerturbedRawEpsilonCount, this.domainSize);
        List<Double> lastReleaseEstimationList = BasicUtils.toSortedListByKeys(this.lastRawReleaseEstimation, this.domainIndexList, 0D);
        return PFOUtils.getDissimilarity(rawEstimationList, lastReleaseEstimationList, pldpVarianceSum);
    }

    // M_{r,t}
    protected abstract CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity);



    public abstract String getSimpleName();

}
