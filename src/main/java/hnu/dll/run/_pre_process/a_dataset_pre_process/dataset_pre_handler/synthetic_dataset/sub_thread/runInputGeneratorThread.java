package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.sub_thread;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
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
        Double[] cumulativeArray;
        List<Double> tempProbabilityDistribution;
        List<String> tempWritingDataList;
        Integer totalTimeStampSize = this.probabilityStrList.size();
        String[] tempProbabilityDataStringArray;
        for (int ind = startIndex; ind <= endIndex; ++ind) {
            tempProbabilityDataStringArray = probabilityStrList.get(ind).split(",");
            tempTimeStampStr = tempProbabilityDataStringArray[0];
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicOutputDir, "timestamp_"+ FormatFileName.getFormatNumberString(Integer.valueOf(tempTimeStampStr), 0, totalTimeStampSize) +".txt");
            tempProbabilityDistribution = ProbabilityDistributionUtils.getDistribution(Double.valueOf(tempProbabilityDataStringArray[1]), domainSize);
            cumulativeArray = ProbabilityDistributionUtils.getCumulativeDistribution(tempProbabilityDistribution);
            tempWritingDataList = new ArrayList<>();
            for (String userID : userIDList) {
                // RandomUtil 内部按 array.length 处理并做归一化，取值域大小任意
                chosenIndex = RandomUtil.getRandomIndexGivenCumulativeCountPoint(cumulativeArray);
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
