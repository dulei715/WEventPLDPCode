package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
import hnu.dll._config.Constant;
import hnu.dll._config.ConfigureUtils;

import java.io.File;

/**
 * group shift 合成实验的公共配置与分组逻辑。
 *
 * <p>高隐私组和低隐私组使用同一组用户下标划分，数据生成和参数生成都调用这里，
 * 从而保证"哪一批用户属于哪一种隐私条件"完全一致。</p>
 */
public class GroupShiftUtils {

    public static boolean isEnabled() {
        return ConfigureUtils.isGroupShiftExperiment();
    }

    public static Integer getHighPrivacyGroupSize(Integer totalUserSize) {
        if (totalUserSize == null || totalUserSize < 0) {
            throw new IllegalArgumentException("totalUserSize must be non-negative, but got " + totalUserSize);
        }
        int result = (int) Math.round(totalUserSize * ConfigureUtils.getGroupShiftHighPrivacyRatio());
        return Math.max(0, Math.min(totalUserSize, result));
    }

    public static boolean isHighPrivacyGroup(Integer userIndex, Integer highPrivacyGroupSize) {
        return userIndex != null && highPrivacyGroupSize != null && userIndex < highPrivacyGroupSize;
    }

    public static Double getProbabilityOffset(Boolean highPrivacyGroup) {
        Double delta = ConfigureUtils.getGroupShiftProbabilityDelta();
        boolean positiveDirection = "positive".equals(ConfigureUtils.getGroupShiftDirection());
        boolean whetherIncrease = highPrivacyGroup ? positiveDirection : !positiveDirection;
        return whetherIncrease ? delta : -delta;
    }

    public static Double getShiftedMainStatusProbability(Double mainStatusProbability, Double offset) {
        double result = mainStatusProbability + offset;
        if (result < 0D) {
            return 0D;
        }
        if (result > 1D) {
            return 1D;
        }
        return result;
    }

    /**
     * 返回 GA-OPS 在 samplingRatio=0.5 时给某一组设置的最大采样量。
     * 该值用于实验诊断，不参与算法决策。
     */
    private static int getMaximumSamplingSize(Integer groupSize) {
        return (int) Math.floor(groupSize * 0.5D);
    }

    /**
     * 粗略估计一个组在其原始窗口下的可用采样量。
     * GA-OPS 还会在候选集合中进一步搜索，因此这里的值只用于识别
     * “采样量可能贴近下限”的退化配置。
     */
    private static int getApproximateSamplingSizePerWindow(Integer groupSize, Integer windowSize) {
        if (windowSize == null || windowSize <= 0) {
            return 0;
        }
        return (int) Math.floor(getMaximumSamplingSize(groupSize) * 1.0D / windowSize);
    }

    /**
     * 检查 group shift 的分组规模和窗口设置，并把可能退化的配置明确打印出来。
     * 该检查只产生诊断信息，不改变采样或隐私预算的计算。
     */
    public static void validateSamplingConfiguration(Integer userSize) {
        Integer highPrivacyGroupSize = getHighPrivacyGroupSize(userSize);
        Integer lowPrivacyGroupSize = userSize - highPrivacyGroupSize;
        Integer highPrivacyWindowSize = ConfigureUtils.getGroupShiftHighPrivacyWindowSize();
        Integer lowPrivacyWindowSize = ConfigureUtils.getGroupShiftLowPrivacyWindowSize();
        int highApproximateSamplingSize = getApproximateSamplingSizePerWindow(
                highPrivacyGroupSize, highPrivacyWindowSize);
        int lowApproximateSamplingSize = getApproximateSamplingSizePerWindow(
                lowPrivacyGroupSize, lowPrivacyWindowSize);

        if (highPrivacyGroupSize == 0 || lowPrivacyGroupSize == 0) {
            System.err.println("[group-shift warning] one privacy group is empty: high="
                    + highPrivacyGroupSize + ", low=" + lowPrivacyGroupSize);
        }
        int diagnosticLowerBound = Constant.PopulationLowerBound * 2;
        if (highApproximateSamplingSize < diagnosticLowerBound
                || lowApproximateSamplingSize < diagnosticLowerBound) {
            System.err.println("[group-shift warning] a group may be starved by its window: "
                    + "highApprox=" + highApproximateSamplingSize
                    + ", lowApprox=" + lowApproximateSamplingSize
                    + ", diagnosticBound=" + diagnosticLowerBound
                    + ". Consider reducing the window or increasing the group size.");
        }
    }

    /**
     * 写出本轮实验的分组说明，便于从结果目录回溯数据分布和隐私参数的对应关系。
     */
    public static void writeGroupShiftInfo(String basicDataPath, Integer userSize) {
        if (!isEnabled()) {
            return;
        }
        Integer highPrivacyGroupSize = getHighPrivacyGroupSize(userSize);
        Integer lowPrivacyGroupSize = userSize - highPrivacyGroupSize;
        Integer highPrivacyWindowSize = ConfigureUtils.getGroupShiftHighPrivacyWindowSize();
        Integer lowPrivacyWindowSize = ConfigureUtils.getGroupShiftLowPrivacyWindowSize();
        int highMaximumSamplingSize = getMaximumSamplingSize(highPrivacyGroupSize);
        int lowMaximumSamplingSize = getMaximumSamplingSize(lowPrivacyGroupSize);
        int highApproximateSamplingSize = getApproximateSamplingSizePerWindow(
                highPrivacyGroupSize, highPrivacyWindowSize);
        int lowApproximateSamplingSize = getApproximateSamplingSizePerWindow(
                lowPrivacyGroupSize, lowPrivacyWindowSize);
        validateSamplingConfiguration(userSize);
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicDataPath, "basic_info", "group_shift_info.txt");
        File outputFile = new File(outputPath);
        File parentFile = outputFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        BasicWrite basicWrite = new BasicWrite();
        basicWrite.startWriting(outputPath);
        basicWrite.writeOneLine("experiment_tag,shift");
        basicWrite.writeOneLine("shift_delta," + ConfigureUtils.getGroupShiftProbabilityDelta());
        basicWrite.writeOneLine("shift_direction," + ConfigureUtils.getGroupShiftDirection());
        basicWrite.writeOneLine("high_privacy_group_ratio," + ConfigureUtils.getGroupShiftHighPrivacyRatio());
        basicWrite.writeOneLine("high_privacy_group_size," + highPrivacyGroupSize);
        basicWrite.writeOneLine("high_privacy_user_index_range,0-" + (highPrivacyGroupSize - 1));
        basicWrite.writeOneLine("high_privacy_budget," + ConfigureUtils.getGroupShiftHighPrivacyBudget());
        basicWrite.writeOneLine("high_privacy_window_size," + highPrivacyWindowSize);
        basicWrite.writeOneLine("high_privacy_max_sampling_size_at_ratio_0.5," + highMaximumSamplingSize);
        basicWrite.writeOneLine("high_privacy_approx_sampling_size_per_window," + highApproximateSamplingSize);
        basicWrite.writeOneLine("low_privacy_group_size," + lowPrivacyGroupSize);
        basicWrite.writeOneLine("low_privacy_user_index_range," + highPrivacyGroupSize + "-" + (userSize - 1));
        basicWrite.writeOneLine("low_privacy_budget," + ConfigureUtils.getGroupShiftLowPrivacyBudget());
        basicWrite.writeOneLine("low_privacy_window_size," + lowPrivacyWindowSize);
        basicWrite.writeOneLine("low_privacy_max_sampling_size_at_ratio_0.5," + lowMaximumSamplingSize);
        basicWrite.writeOneLine("low_privacy_approx_sampling_size_per_window," + lowApproximateSamplingSize);
        basicWrite.endWriting();
    }
}
