package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
import hnu.dll._config.Constant;
import hnu.dll._config.ConfigureUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * group shift 合成实验的公共配置与分组逻辑。
 *
 * <p>数据生成、用户参数生成和评估真值都通过同一组连续用户区间划分，
 * 从而保证每个用户的隐私条件和分布偏移完全一致。</p>
 */
public class GroupShiftUtils {

    public static class GroupSpec {
        private final int index;
        private final double ratio;
        private final double privacyBudget;
        private final int windowSize;
        private final double offsetScale;

        public GroupSpec(int index, double ratio, double privacyBudget,
                         int windowSize, double offsetScale) {
            this.index = index;
            this.ratio = ratio;
            this.privacyBudget = privacyBudget;
            this.windowSize = windowSize;
            this.offsetScale = offsetScale;
        }

        public int getIndex() {
            return index;
        }

        public double getRatio() {
            return ratio;
        }

        public double getPrivacyBudget() {
            return privacyBudget;
        }

        public int getWindowSize() {
            return windowSize;
        }

        public double getOffsetScale() {
            return offsetScale;
        }
    }

    public static boolean isEnabled() {
        return ConfigureUtils.isGroupShiftExperiment();
    }

    /**
     * 是否在 group shift 实验中禁止跨分布借用。
     * 默认保留原始 Plus 的跨组借用逻辑，只有显式传入系统属性时才启用隔离版本。
     */
    public static boolean isCrossGroupBorrowingDisabled() {
        return isEnabled() && Boolean.parseBoolean(
                System.getProperty("exp.shift.disable.cross.group", "false"));
    }

    public static List<GroupSpec> getGroupSpecList() {
        List<Double> ratioList = ConfigureUtils.getGroupShiftGroupRatios();
        List<Double> budgetList = ConfigureUtils.getGroupShiftGroupBudgets();
        List<Integer> windowList = ConfigureUtils.getGroupShiftGroupWindowSizes();
        List<Double> offsetScaleList = ConfigureUtils.getGroupShiftGroupOffsetScales();

        if (ratioList.isEmpty() || budgetList.isEmpty()
                || windowList.isEmpty() || offsetScaleList.isEmpty()) {
            ratioList = new ArrayList<>();
            ratioList.add(ConfigureUtils.getGroupShiftHighPrivacyRatio());
            ratioList.add(1D - ratioList.get(0));
            budgetList = new ArrayList<>();
            budgetList.add(ConfigureUtils.getGroupShiftHighPrivacyBudget());
            budgetList.add(ConfigureUtils.getGroupShiftLowPrivacyBudget());
            windowList = new ArrayList<>();
            windowList.add(ConfigureUtils.getGroupShiftHighPrivacyWindowSize());
            windowList.add(ConfigureUtils.getGroupShiftLowPrivacyWindowSize());
            offsetScaleList = new ArrayList<>();
            offsetScaleList.add(1D);
            offsetScaleList.add(-1D);
        }

        int groupCount = ratioList.size();
        if (budgetList.size() != groupCount
                || windowList.size() != groupCount
                || offsetScaleList.size() != groupCount) {
            throw new IllegalArgumentException(
                    "group shift ratios, budgets, windows and offsets must have equal lengths");
        }

        double ratioSum = 0D;
        List<GroupSpec> result = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) {
            double ratio = ratioList.get(i);
            int windowSize = windowList.get(i);
            if (ratio < 0D || ratio > 1D) {
                throw new IllegalArgumentException("group shift ratio must be in [0, 1]");
            }
            if (budgetList.get(i) <= 0D || windowSize <= 0) {
                throw new IllegalArgumentException("group shift privacy parameters must be positive");
            }
            ratioSum += ratio;
            result.add(new GroupSpec(i, ratio, budgetList.get(i),
                    windowSize, offsetScaleList.get(i)));
        }
        if (Math.abs(ratioSum - 1D) > 1E-8) {
            throw new IllegalArgumentException(
                    "group shift ratios must sum to 1, but got " + ratioSum);
        }
        return result;
    }

    public static int[] getGroupSizes(int totalUserSize) {
        if (totalUserSize < 0) {
            throw new IllegalArgumentException("totalUserSize must be non-negative");
        }
        List<GroupSpec> groupSpecList = getGroupSpecList();
        int[] result = new int[groupSpecList.size()];
        int assignedSize = 0;
        for (int i = 0; i < result.length; i++) {
            if (i == result.length - 1) {
                result[i] = totalUserSize - assignedSize;
            } else {
                result[i] = (int) Math.round(
                        totalUserSize * groupSpecList.get(i).getRatio());
                result[i] = Math.max(0, Math.min(
                        totalUserSize - assignedSize, result[i]));
            }
            assignedSize += result[i];
        }
        return result;
    }

    public static int getGroupIndex(int userIndex, int totalUserSize) {
        if (userIndex < 0 || userIndex >= totalUserSize) {
            throw new IllegalArgumentException("userIndex is outside the user range");
        }
        int[] groupSizes = getGroupSizes(totalUserSize);
        int accumulatedSize = 0;
        for (int groupIndex = 0; groupIndex < groupSizes.length; groupIndex++) {
            accumulatedSize += groupSizes[groupIndex];
            if (userIndex < accumulatedSize) {
                return groupIndex;
            }
        }
        return groupSizes.length - 1;
    }

    public static int[] getGroupIndexArray(int totalUserSize) {
        int[] groupSizes = getGroupSizes(totalUserSize);
        int[] result = new int[totalUserSize];
        int groupIndex = 0;
        int groupEnd = groupSizes.length == 0 ? 0 : groupSizes[0];
        for (int userIndex = 0; userIndex < totalUserSize; userIndex++) {
            while (groupIndex < groupSizes.length - 1 && userIndex >= groupEnd) {
                groupIndex++;
                groupEnd += groupSizes[groupIndex];
            }
            result[userIndex] = groupIndex;
        }
        return result;
    }

    public static Double getProbabilityOffset(int groupIndex) {
        List<GroupSpec> groupSpecList = getGroupSpecList();
        if (groupIndex < 0 || groupIndex >= groupSpecList.size()) {
            throw new IllegalArgumentException("invalid group index " + groupIndex);
        }
        double direction = "reverse".equals(ConfigureUtils.getGroupShiftDirection())
                ? -1D : 1D;
        return ConfigureUtils.getGroupShiftProbabilityDelta()
                * groupSpecList.get(groupIndex).getOffsetScale() * direction;
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

    /** Backward-compatible two-group helper. */
    public static Double getProbabilityOffset(Boolean highPrivacyGroup) {
        return getProbabilityOffset(highPrivacyGroup ? 0 : getGroupSpecList().size() - 1);
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

    private static int getMaximumSamplingSize(Integer groupSize) {
        return (int) Math.floor(groupSize * 0.5D);
    }

    private static int getApproximateSamplingSizePerWindow(Integer groupSize, Integer windowSize) {
        if (windowSize == null || windowSize <= 0) {
            return 0;
        }
        return (int) Math.floor(getMaximumSamplingSize(groupSize) * 1.0D / windowSize);
    }

    public static void validateSamplingConfiguration(Integer userSize) {
        int[] groupSizes = getGroupSizes(userSize);
        List<GroupSpec> groupSpecList = getGroupSpecList();
        int diagnosticLowerBound = Constant.PopulationLowerBound * 2;
        for (int groupIndex = 0; groupIndex < groupSizes.length; groupIndex++) {
            int approximateSamplingSize = getApproximateSamplingSizePerWindow(
                    groupSizes[groupIndex], groupSpecList.get(groupIndex).getWindowSize());
            if (groupSizes[groupIndex] == 0 || approximateSamplingSize < diagnosticLowerBound) {
                System.err.println("[group-shift warning] group=" + groupIndex
                        + ", size=" + groupSizes[groupIndex]
                        + ", approximateSamplingSize=" + approximateSamplingSize
                        + ", diagnosticBound=" + diagnosticLowerBound);
            }
        }
    }

    public static void writeGroupShiftInfo(String basicDataPath, Integer userSize) {
        if (!isEnabled()) {
            return;
        }
        int[] groupSizes = getGroupSizes(userSize);
        List<GroupSpec> groupSpecList = getGroupSpecList();
        validateSamplingConfiguration(userSize);
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT,
                basicDataPath, "basic_info", "group_shift_info.txt");
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
        basicWrite.writeOneLine("group_count," + groupSpecList.size());
        int startIndex = 0;
        for (int groupIndex = 0; groupIndex < groupSpecList.size(); groupIndex++) {
            GroupSpec groupSpec = groupSpecList.get(groupIndex);
            int endIndex = startIndex + groupSizes[groupIndex] - 1;
            basicWrite.writeOneLine("group_" + groupIndex + "_ratio," + groupSpec.getRatio());
            basicWrite.writeOneLine("group_" + groupIndex + "_size," + groupSizes[groupIndex]);
            basicWrite.writeOneLine("group_" + groupIndex + "_user_index_range,"
                    + startIndex + "-" + endIndex);
            basicWrite.writeOneLine("group_" + groupIndex + "_budget," + groupSpec.getPrivacyBudget());
            basicWrite.writeOneLine("group_" + groupIndex + "_window," + groupSpec.getWindowSize());
            basicWrite.writeOneLine("group_" + groupIndex + "_offset_scale," + groupSpec.getOffsetScale());
            basicWrite.writeOneLine("group_" + groupIndex + "_approx_sampling_size_per_window,"
                    + getApproximateSamplingSizePerWindow(
                    groupSizes[groupIndex], groupSpec.getWindowSize()));
            startIndex = endIndex + 1;
        }
        basicWrite.endWriting();
    }

    /**
     * 写出每个时间戳、每个隐私组的无扰动真实分布，供组级 AMSE/AJSD 评估使用。
     */
    public static void writeGroupShiftTruth(String basicDataPath,
                                             List<String> probabilityStrList,
                                             Integer domainSize) {
        if (!isEnabled()) {
            return;
        }
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT,
                basicDataPath, "basic_info", "group_shift_truth.txt");
        BasicWrite basicWrite = new BasicWrite();
        basicWrite.startWriting(outputPath);
        List<GroupSpec> groupSpecList = getGroupSpecList();
        StringBuilder header = new StringBuilder("timestamp,group,main_probability");
        for (int domainIndex = 0; domainIndex < domainSize; domainIndex++) {
            header.append(",truth_").append(domainIndex);
        }
        basicWrite.writeOneLine(header.toString());
        for (String probabilityStr : probabilityStrList) {
            String[] probabilityData = probabilityStr.split(",");
            int timestamp = Integer.parseInt(probabilityData[0]);
            double mainProbability = Double.parseDouble(probabilityData[1]);
            for (int groupIndex = 0; groupIndex < groupSpecList.size(); groupIndex++) {
                double shiftedProbability = getShiftedMainStatusProbability(
                        mainProbability, getProbabilityOffset(groupIndex));
                List<Double> distribution = ProbabilityDistributionUtils.getDistribution(
                        shiftedProbability, domainSize);
                StringBuilder line = new StringBuilder();
                line.append(timestamp).append(",").append(groupIndex)
                        .append(",").append(shiftedProbability);
                for (Double value : distribution) {
                    line.append(",").append(value);
                }
                basicWrite.writeOneLine(line.toString());
            }
        }
        basicWrite.endWriting();
    }
}
