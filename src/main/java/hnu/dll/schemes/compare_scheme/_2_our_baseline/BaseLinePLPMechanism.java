package hnu.dll.schemes.compare_scheme._2_our_baseline;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class BaseLinePLPMechanism extends Mechanism {
    protected Random random;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Integer> windowSizeList;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected Integer chosenWindowSize;
    protected Integer chosenSamplingSize;

    protected Set<Integer> candidateUserIndexSet;

    protected Map<Integer, Double> lastReleaseEstimation;

    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;

    List<Integer> calculationPopulationIndexList;
    List<Integer> publicationPopulationIndexList;


    public BaseLinePLPMechanism(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
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
        this.candidateUserIndexSet = new HashSet<>();
        this.candidateUserIndexSet.addAll(BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.userSize - 1));
        this.random = random;
    }


    public List<Double> getOriginalPrivacyBudgetList() {
        return originalPrivacyBudgetList;
    }


    public Integer getChosenWindowSize() {
        return this.chosenWindowSize;
    }

    /**
     * 关于window 和 sampling size的选择信息
     * @return
     */


    public List<Integer> getWindowSizeList() {
        return windowSizeList;
    }

    public Map<Integer, Double> getReleaseNoiseCountMap() {
        return this.lastReleaseEstimation;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    public void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
        this.chosenWindowSize = BasicArrayUtil.getIntegerMaxValue(this.windowSizeList);
        // Integer 除法保证其向下取整
        this.chosenSamplingSize = this.userSize / (2 * this.chosenWindowSize);
        // 这里初始化历史窗口大小为 w_opt - 1
        this.samplingSubMechanismHistoryQueue = new HistoryPopulationQueue(this.chosenWindowSize);
        this.publicationSubMechanismHistoryQueue = new HistoryPopulationQueue(this.chosenWindowSize);
    }

    public CombinePair<Boolean, Map<Integer, Double>> updateNextPublicationResult(List<Integer> nextDataIndexList) {

        ++this.currentTime;
        // M_{t,s}
        Double dissimilarity = samplingSubMechanism(nextDataIndexList);

        // M_{t,r}
        return reportingSubMechanism(nextDataIndexList, dissimilarity);
    }


    /**
     * M_{s,t}
     * @param nextDataIndexList 要求该数据顺序和用户信息顺序一致
     * @return
     */
    protected Double samplingSubMechanism(List<Integer> nextDataIndexList) {
        Set<Integer> samplingUserIndexSetForCalculation = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, this.chosenSamplingSize, random);
        this.candidateUserIndexSet.removeAll(samplingUserIndexSetForCalculation);
        this.samplingSubMechanismHistoryQueue.offer(samplingUserIndexSetForCalculation);
        // 获取当前 privacy budget 列表
        List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.originalPrivacyBudgetList, samplingUserIndexSetForCalculation);
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
