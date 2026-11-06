package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.sub_thread;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.GroupShiftUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.ProbabilityDistributionUtils;
import hnu.dll.utils.FormatFileName;
import hnu.dll.utils.io.ListWriteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class runInputGeneratorThread implements Runnable {
    private Integer startIndex;
    private Integer endIndex;
    private String basicOutputDir;
    private List<String> probabilityStrList;
    private List<String> userIDList;
    private List<String> positionStringList;
    private CountDownLatch latch;

    public runInputGeneratorThread(Integer startIndex, Integer endIndex, String basicOutputDir, List<String> probabilityStrList, List<String> userIDList, List<String> positionStringList, CountDownLatch latch) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.basicOutputDir = basicOutputDir;
        this.probabilityStrList = probabilityStrList;
        this.userIDList = userIDList;
        this.positionStringList = positionStringList;
        this.latch = latch;
    }

    private void generateRunInputByBatch() {
        // 生成从timeStamp[startIndex]到timeStamp[endIndex]之间的runInput值
        String tempTimeStampStr, tempData;
        Integer chosenIndex;
        String outputFilePath;
        // 取值域大小由 status.txt 的元素个数决定，不再固定为 2
        Integer domainSize = this.positionStringList.size();
        Double[] cumulativeArray = null;
        List<Double> tempProbabilityDistribution;
        List<String> tempWritingDataList;
        Integer totalTimeStampSize = this.probabilityStrList.size();
        String[] tempProbabilityDataStringArray;
        boolean groupShiftEnabled = GroupShiftUtils.isEnabled();
        List<GroupShiftUtils.GroupSpec> groupSpecList = groupShiftEnabled
                ? GroupShiftUtils.getGroupSpecList() : new ArrayList<>();
        int[] userGroupIndexArray = null;
        List<Double> groupProbabilityOffsetList = null;
        if (groupShiftEnabled) {
            userGroupIndexArray = GroupShiftUtils.getGroupIndexArray(userIDList.size());
            groupProbabilityOffsetList = new ArrayList<>(groupSpecList.size());
            for (int groupIndex = 0; groupIndex < groupSpecList.size(); groupIndex++) {
                groupProbabilityOffsetList.add(GroupShiftUtils.getProbabilityOffset(groupIndex));
            }
        }
        for (int ind = startIndex; ind <= endIndex; ++ind) {
            tempProbabilityDataStringArray = probabilityStrList.get(ind).split(",");
            tempTimeStampStr = tempProbabilityDataStringArray[0];
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicOutputDir, "timestamp_"+ FormatFileName.getFormatNumberString(Integer.valueOf(tempTimeStampStr), 0, totalTimeStampSize) +".txt");
            Double mainStatusProbability = Double.valueOf(tempProbabilityDataStringArray[1]);
            Double[][] groupCumulativeArray = null;
            if (!groupShiftEnabled) {
                tempProbabilityDistribution = ProbabilityDistributionUtils.getDistribution(mainStatusProbability, domainSize);
                cumulativeArray = ProbabilityDistributionUtils.getCumulativeDistribution(tempProbabilityDistribution);
            } else {
                groupCumulativeArray = new Double[groupSpecList.size()][];
                for (int groupIndex = 0; groupIndex < groupSpecList.size(); groupIndex++) {
                    tempProbabilityDistribution = ProbabilityDistributionUtils.getDistribution(
                            GroupShiftUtils.getShiftedMainStatusProbability(
                                    mainStatusProbability,
                                    groupProbabilityOffsetList.get(groupIndex)),
                            domainSize);
                    groupCumulativeArray[groupIndex] =
                            ProbabilityDistributionUtils.getCumulativeDistribution(
                                    tempProbabilityDistribution);
                }
            }
            tempWritingDataList = new ArrayList<>();
            for (int userIndex = 0; userIndex < userIDList.size(); userIndex++) {
                String userID = userIDList.get(userIndex);
                Double[] selectedCumulativeArray;
                if (groupShiftEnabled) {
                    int groupIndex = userGroupIndexArray[userIndex];
                    selectedCumulativeArray = groupCumulativeArray[groupIndex];
                } else {
                    selectedCumulativeArray = cumulativeArray;
                }
                // RandomUtil 内部按 array.length 处理并做归一化，取值域大小任意
                chosenIndex = RandomUtil.getRandomIndexGivenCumulativeCountPoint(selectedCumulativeArray);
                tempData = StringUtil.join(",", userID, positionStringList.get(chosenIndex), tempTimeStampStr);
                tempWritingDataList.add(tempData);
            }
            ListWriteUtils.writeList(outputFilePath, tempWritingDataList, ",");
        }
    }

    @Override
    public void run() {
        generateRunInputByBatch();
        System.out.println("Thread " + Thread.currentThread().getName() + " end...");
        this.latch.countDown();
    }
}
