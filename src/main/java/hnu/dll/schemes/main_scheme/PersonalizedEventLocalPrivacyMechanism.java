package hnu.dll.schemes.main_scheme;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes._scheme_utils.*;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.HistoryUserSizeQueue;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.structure.stream_data.StreamDataElement;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class PersonalizedEventLocalPrivacyMechanism extends Mechanism {
    protected Random random;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Double> newPrivacyBudgetList;
    protected List<Integer> windowSizeList;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected OptimalSelectionStruct optimalSelectionStruct;
    protected Integer optimalWindowSize;

    protected Set<Integer> candidateUserIndexSet;

    protected Map<Integer, Double> lastReleaseEstimation;

    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;

    List<Integer> calculationPopulationIndexList;
    List<Integer> publicationPopulationIndexList;


    public PersonalizedEventLocalPrivacyMechanism(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        this.currentTime = -1;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
        this.lastReleaseEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.originalPrivacyBudgetList = originalPrivacyBudgetList;
        this.windowSizeList = windowSizeList;
        if (this.originalPrivacyBudgetList.size() != this.windowSizeList.size()) {
            throw new RuntimeException("The user size in privacy budget list and window size list are not equal!");
        }
        this.userSize = this.windowSizeList.size();
        this.candidateUserIndexSet.addAll(BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.userSize - 1));
        this.random = random;
    }

//    protected abstract void setCalculationPrivacyBudgetList();
//
//    protected abstract void setPublicationPrivacyBudgetList();

    protected abstract void setCalculationParameters();
    protected abstract void setPublicationParameters();


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

    public List<Integer> getWindowSizeList() {
        return windowSizeList;
    }

    public Map<Integer, Double> getReleaseNoiseCountMap() {
        return this.lastReleaseEstimation;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    protected void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
        setCalculationParameters();
        List<Integer> samplingSizeList = BasicSchemeUtils.getSamplingSizeList(this.windowSizeList);
        this.optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(samplingSizeList, this.originalPrivacyBudgetList, this.domainSize);
        this.newPrivacyBudgetList = this.optimalSelectionStruct.getNewPrivacyBudgetList();
        this.optimalWindowSize = this.windowSizeList.get(this.optimalSelectionStruct.getOptimalSamplingSizeIndex());
        // 这里初始化历史窗口大小为 w_opt - 1
        this.samplingSubMechanismHistoryQueue = new HistoryPopulationQueue(this.optimalWindowSize);
        this.publicationSubMechanismHistoryQueue = new HistoryPopulationQueue(this.optimalWindowSize);
    }

    public boolean updateNextPublicationResult(List<StreamDataElement<Boolean>> nextDataElementList) {

        ++this.currentTime;
        // M_{t,s}
        Double dissimilarity = samplingSubMechanism(nextDataElementList);

        // M_{t,r}
        return reportSubMechanism(nextDataElementList, dissimilarity);
    }


    /**
     * M_{t,s}
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

        CombinePair<Map<Integer, Double>, Map<Double, Integer>> enhancedGPRRInfo = enhancedGPRR(samplingPrivacyBudgetList, samplingDataIndexList);
        Map<Integer, Double> normalizedEstimationMap = enhancedGPRRInfo.getKey();
        Map<Double, Integer> rePerturbedEpsilonCount = enhancedGPRRInfo.getValue();

        List<Double> normalizedEstimationList = BasicUtils.toSortedListByKeys(normalizedEstimationMap, this.domainIndexList, 0D);

        Double pldpVarianceSum = PFOUtils.getPLDPVarianceSumBySpecificUsers(rePerturbedEpsilonCount, this.domainSize);
        List<Double> lastReleaseEstimationList = BasicUtils.toSortedListByKeys(this.lastReleaseEstimation, this.domainIndexList, 0D);
        return PFOUtils.getDissimilarity(normalizedEstimationList, lastReleaseEstimationList, pldpVarianceSum);
    }

    // M_{t,r}
    protected CombinePair<Boolean, Map<Integer, Double>> reportSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {

        Integer remainingPublicationUserSize = this.userSize / 2 - this.publicationSubMechanismHistoryQueue.getReverseSizeSum(this.optimalWindowSize - 1);
        Integer publicationSamplingSize = remainingPublicationUserSize / 2;
        Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
        // 获取当前 privacy budget 列表
        List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.newPrivacyBudgetList, samplingUserIndexSetForPublication);
        // 获取当前 data 列表
        List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);
        Map<Double, Integer> groupCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingPrivacyBudgetList);

        Double error = PFOUtils.getGPRRErrorBySpecificUsers(groupCountMap, this.userSize, publicationSamplingSize, this.domainSize);

        Map<Integer, Double> normalizedEstimation;

        Boolean flag;

        if (dissimilarity > error) {
            flag = true;
            this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
            normalizedEstimation = enhancedGPRR(samplingPrivacyBudgetList, samplingDataIndexList).getKey();
            this.lastReleaseEstimation = normalizedEstimation;
            this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);
        } else {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
        }

        // recycle
        Set samplingRecycle = this.samplingSubMechanismHistoryQueue.getFirst();
        Set publicationRecycle = this.publicationSubMechanismHistoryQueue.getFirst();
        this.candidateUserIndexSet.addAll(samplingRecycle);
        this.candidateUserIndexSet.addAll(publicationRecycle);

        return new CombinePair<>(flag, normalizedEstimation);
    }

    private CombinePair<Map<Integer, Double>, Map<Double, Integer>> enhancedGPRR(List<Double> samplingPrivacyBudgetList, List<Integer> samplingDataIndexList) {
        // 1. PFO.Perturb 扰动
        Map<Double, List<Integer>> groupedDataMap = BasicUtils.groupByEpsilon(samplingPrivacyBudgetList, samplingDataIndexList);
        Map<Double, List<Integer>> perturbedData = PFOUtils.perturb(groupedDataMap, this.domainSize, this.random);

        // 2. PFO.RePerturb 重扰动
        TreeMap<Double, List<Integer>> sortedPerturbedData = new TreeMap<>(perturbedData);
        CombinePair<Map<Double, List<Integer>>, Integer> rePerturbResult = PFOUtils.rePerturb(sortedPerturbedData, this.domainSize, this.random);
        Map<Double, List<Integer>> rePerturbData = rePerturbResult.getKey();

        // 3. PFO.Aggregate 聚合
        Map<Double, Integer> rePerturbedEpsilonCount = BasicUtils.getGroupDataCount(rePerturbData);
        Map<Double, Map<Integer, Double>> aggregation = PFOUtils.getAggregation(rePerturbedEpsilonCount, rePerturbData, this.domainSize);


        // 4. PFO.Estimate 估计
        Map<Double, Double> samplingAggregationWeightedMap = PFOUtils.getAggregationWeightMap(rePerturbedEpsilonCount, this.domainSize);
        Map<Double, Double> newParameterQ = PFOUtils.getGeneralRandomResponseParameterQ(groupedDataMap.keySet(), this.domainSize);
        Map<Double, Double> newParameterP = PFOUtils.getGeneralRandomResponseParameterP(newParameterQ);
        Map<Integer, Double> estimationMap = PFOUtils.getEstimation(aggregation, newParameterP, newParameterQ, samplingAggregationWeightedMap);
        return new CombinePair<>(BasicUtils.normalizeValues(estimationMap), rePerturbedEpsilonCount);
//        List<Double> rawEstimationList = BasicUtils.toSortedListByKeys(estimationMap, this.domainIndexList, 0D);
//        List<Double> normalizedEstimationList = Normalization.normalizedBySimplexProjection(rawEstimationList);
    }

    public abstract String getSimpleName();

}
