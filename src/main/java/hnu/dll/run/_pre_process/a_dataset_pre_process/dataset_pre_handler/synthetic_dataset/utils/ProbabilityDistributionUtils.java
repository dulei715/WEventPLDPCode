package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import hnu.dll._config.ConfigureUtils;

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
     * Zipf 分布的指数 alpha。
     * alpha <= 0 表示"除主状态外其余状态均分"（即改造前的默认行为，向后兼容）；
     * alpha > 0 表示其余状态按 Zipf 权重 (rank^-alpha) 递减分配，模拟真实场景的长尾分布。
     */
    private static Double zipfAlpha = 0D;

    public static void setZipfAlpha(Double alpha) {
        zipfAlpha = (alpha == null) ? 0D : alpha;
    }

    public static Double getZipfAlpha() {
        return zipfAlpha;
    }

    /**
     * 按当前实验标签（-Dexp.tag，默认 default）从 parameter_config.xml 的
     * independentVariables/ZipfAlpha 中读取 Zipf 指数。
     * 这样不同实验可以用不同的分布形态：主实验用 default(0, 均匀分布)，
     * 维度可扩展性实验用 fixed(1.2, Zipf 长尾)。
     * 未配置时返回 0（均匀分布），保证旧配置不受影响。
     */
    public static void configureZipfAlpha() {
        setZipfAlpha(ConfigureUtils.getIndependentSingleZipfAlpha(ConfigureUtils.getExperimentTag()));
    }

    /**
     * 由主状态概率构造整个取值域上的概率分布（使用当前的 zipfAlpha）。
     *
     * @param mainStatusProbability 主状态概率 p(t)
     * @param domainSize            取值域大小 d（即 status.txt 中的元素个数）
     * @return 长度为 d、元素和为 1 的概率分布
     */
    public static List<Double> getDistribution(Double mainStatusProbability, Integer domainSize) {
        return getDistribution(mainStatusProbability, domainSize, zipfAlpha);
    }

    /**
     * 由主状态概率构造整个取值域上的概率分布。
     *
     * <p>分配规则：主状态占 p(t)，其余 d-1 个状态分掉剩下的 1-p(t)。
     * 当 zipfAlpha <= 0 时其余状态均分（与原二值语义一致）；
     * 当 zipfAlpha > 0 时其余状态按 Zipf 权重递减分配（热门状态概率大、冷门状态概率小）。</p>
     *
     * <p>注意：d = 2 时只有 1 个"其余状态"，无论 alpha 取何值都退化为 [1-p, p]，
     * 因此该实现对已有的二值实验完全向后兼容。</p>
     *
     * @param mainStatusProbability 主状态概率 p(t)
     * @param domainSize            取值域大小 d
     * @param zipfAlpha             Zipf 指数，<= 0 表示均匀分布
     * @return 长度为 d、元素和为 1 的概率分布
     */
    public static List<Double> getDistribution(Double mainStatusProbability, Integer domainSize, Double zipfAlpha) {
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
        for (int i = 0; i < domainSize; i++) {
            result.add(0D);
        }
        result.set(mainIndex, tempProbability);

        int restSize = domainSize - 1;
        if (restSize <= 0) {
            return result;
        }
        double restTotal = 1D - tempProbability;
        List<Integer> restIndexList = new ArrayList<>(restSize);
        for (int i = 0; i < domainSize; i++) {
            if (i != mainIndex) {
                restIndexList.add(i);
            }
        }

        if (zipfAlpha == null || zipfAlpha <= 0D) {
            double restProbability = restTotal / restSize;
            for (Integer index : restIndexList) {
                result.set(index, restProbability);
            }
            return result;
        }

        double[] weightArray = new double[restSize];
        double weightSum = 0D;
        for (int k = 0; k < restSize; k++) {
            weightArray[k] = Math.pow(k + 1D, -zipfAlpha);
            weightSum += weightArray[k];
        }
        if (weightSum <= 0D) {
            double restProbability = restTotal / restSize;
            for (Integer index : restIndexList) {
                result.set(index, restProbability);
            }
            return result;
        }
        for (int k = 0; k < restSize; k++) {
            result.set(restIndexList.get(k), restTotal * weightArray[k] / weightSum);
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
