package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 把生成函数给出的标量概率（主状态概率 p(t)）扩展为整个取值域（domainSize = d）上的概率分布。
 *
 * <p>语义：p(t) 是"主状态"的概率，取值域中其余 d-1 个状态均分剩下的 1-p(t)。</p>
 *
 * <p>当 d = 2 时退化为 [1-p, p]，与原先二值（status ∈ {0,1}）的语义完全一致，
 * 因此该扩展对已有的二值实验是向后兼容的。</p>
 */
public class ProbabilityDistributionUtils {

    /**
     * 主状态在取值域中的下标，固定为 1。
     * 这样在 domainSize = 2 时，分布为 [1-p, p]，与改造前的二值语义保持一致。
     */
    public static final int MAIN_STATUS_INDEX = 1;

    /**
     * 由主状态概率构造整个取值域上的概率分布。
     *
     * @param mainStatusProbability 主状态概率 p(t)
     * @param domainSize            取值域大小 d（即 status.txt 中的元素个数）
     * @return 长度为 d、元素和为 1 的概率分布
     */
    public static List<Double> getDistribution(Double mainStatusProbability, Integer domainSize) {
        if (domainSize == null || domainSize <= 0) {
            throw new IllegalArgumentException("domainSize must be positive, but got " + domainSize);
        }
        List<Double> result = new ArrayList<>(domainSize);
        if (domainSize == 1) {
            result.add(1D);
            return result;
        }
        double tempProbability = mainStatusProbability;
        if (tempProbability < 0D) {
            tempProbability = 0D;
        } else if (tempProbability > 1D) {
            tempProbability = 1D;
        }
        int mainIndex = Math.min(MAIN_STATUS_INDEX, domainSize - 1);
        double restProbability = (1D - tempProbability) / (domainSize - 1);
        for (int i = 0; i < domainSize; i++) {
            result.add(i == mainIndex ? tempProbability : restProbability);
        }
        return result;
    }

    /**
     * 把概率分布转换成累积分布，用于按累积概率抽样。
     * 最后一个元素强制置为 1：RandomUtil 抽样时会按最后一个元素做归一化，
     * 置 1 可以让归一化退化为恒等变换，同时避免浮点误差导致抽样落在分布之外。
     *
     * @param distribution 概率分布
     * @return 累积分布数组
     */
    public static Double[] getCumulativeDistribution(List<Double> distribution) {
        Double[] cumulativeArray = new Double[distribution.size()];
        double sum = 0D;
        for (int i = 0; i < distribution.size(); i++) {
            sum += distribution.get(i);
            cumulativeArray[i] = sum;
        }
        cumulativeArray[cumulativeArray.length - 1] = 1D;
        return cumulativeArray;
    }
}
