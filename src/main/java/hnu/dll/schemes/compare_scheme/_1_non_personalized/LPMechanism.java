package hnu.dll.schemes.compare_scheme._1_non_personalized;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public abstract class LPMechanism extends Mechanism {
    protected Random random;

    protected int currentTime;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected Double privacyBudget;
    protected Integer windowSize;
    protected Integer samplingSize;

    protected Set<Integer> candidateUserIndexSet;

    protected Map<Integer, Double> lastReleaseEstimation;

    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;



    public LPMechanism(Set<String> dataTypeSet, Double privacyBudget, Integer windowSize, Integer userSize, Random random) {
        this.currentTime = -1;
        this.domainSize = dataTypeSet.size();
        this.domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.domainSize - 1);
        this.lastReleaseEstimation = MapUtils.getInitializedMap(this.domainIndexList, 0D);
        this.privacyBudget = privacyBudget;
        this.windowSize = windowSize;
        this.userSize = userSize;
        this.candidateUserIndexSet = new HashSet<>();
        this.candidateUserIndexSet.addAll(BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, this.userSize - 1));
        this.random = random;
    }


    public Double getPrivacyBudget() {
        return this.privacyBudget;
    }


    public Integer getWindowSize() {
        return this.windowSize;
    }




    public Map<Integer, Double> getReleaseNoiseCountMap() {
        return this.lastReleaseEstimation;
    }

    /**
     * 调用optimalPopulationSelection来初始化最优sample，最优window size以及新的privacy budget
     */
    public void initializeOptimalParameters() {
        // 1. 初始化基本的 M_{t,s}信息
        // Integer 除法保证其向下取整
        this.samplingSize = this.userSize / (2 * this.windowSize);
        // 这里初始化历史窗口大小为 w_opt - 1
        this.samplingSubMechanismHistoryQueue = new HistoryPopulationQueue(this.windowSize);
        this.publicationSubMechanismHistoryQueue = new HistoryPopulationQueue(this.windowSize);
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
        Set<Integer> samplingUserIndexSetForCalculation = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, this.samplingSize, random);
        this.candidateUserIndexSet.removeAll(samplingUserIndexSetForCalculation);
        this.samplingSubMechanismHistoryQueue.offer(samplingUserIndexSetForCalculation);
        // 获取当前 data 列表
        List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForCalculation);

        Map<Integer, Double> normalizedEstimationMap = MechanismUtils.gRR(this.privacyBudget, samplingDataIndexList, this.domainSize, this.random);

        List<Double> normalizedEstimationList = BasicUtils.toSortedListByKeys(normalizedEstimationMap, this.domainIndexList, 0D);

        Double pldpVarianceSum = FOUtils.getLDPVarianceSum(this.privacyBudget, this.samplingSize, this.domainSize);
        List<Double> lastReleaseEstimationList = BasicUtils.toSortedListByKeys(this.lastReleaseEstimation, this.domainIndexList, 0D);
        return FOUtils.getDissimilarity(normalizedEstimationList, lastReleaseEstimationList, pldpVarianceSum);
    }

    // M_{r,t}
    protected abstract CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity);



    public abstract String getSimpleName();

}
