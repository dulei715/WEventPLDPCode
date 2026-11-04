package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
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
     * 写出本轮实验的分组说明，便于从结果目录回溯数据分布和隐私参数的对应关系。
     */
    public static void writeGroupShiftInfo(String basicDataPath, Integer userSize) {
        if (!isEnabled()) {
            return;
        }
        Integer highPrivacyGroupSize = getHighPrivacyGroupSize(userSize);
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
        basicWrite.writeOneLine("high_privacy_user_index_range,0-" + (highPrivacyGroupSize - 1));
        basicWrite.writeOneLine("high_privacy_budget," + ConfigureUtils.getGroupShiftHighPrivacyBudget());
        basicWrite.writeOneLine("high_privacy_window_size," + ConfigureUtils.getGroupShiftHighPrivacyWindowSize());
        basicWrite.writeOneLine("low_privacy_user_index_range," + highPrivacyGroupSize + "-" + (userSize - 1));
        basicWrite.writeOneLine("low_privacy_budget," + ConfigureUtils.getGroupShiftLowPrivacyBudget());
        basicWrite.writeOneLine("low_privacy_window_size," + ConfigureUtils.getGroupShiftLowPrivacyWindowSize());
        basicWrite.endWriting();
    }
}
