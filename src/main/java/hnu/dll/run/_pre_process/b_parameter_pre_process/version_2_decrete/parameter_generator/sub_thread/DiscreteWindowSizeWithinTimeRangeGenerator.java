package hnu.dll.run._pre_process.b_parameter_pre_process.version_2_decrete.parameter_generator.sub_thread;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll.utils.FormatFileName;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class DiscreteWindowSizeWithinTimeRangeGenerator implements Runnable {
    private String outputFileDir;
    private List<Integer> timeStampList;
    private List<Integer> windowSizeCandidateList;
//    private List<Double> windowSizeRatioList;
    private Integer backwardWindowSizeLowerBound;
    private List<BasicPair<Integer, Integer>> userWSizeList;
    private Integer startIndex;
    private Integer endIndex;
    private Random random;
    private CountDownLatch latch;

    public DiscreteWindowSizeWithinTimeRangeGenerator(String outputFileDir, List<Integer> timeStampList, List<Integer> windowSizeCandidateList, Integer backwardWindowSizeLowerBound, List<BasicPair<Integer, Integer>> userWSizeList, Integer startIndex, Integer endIndex, Random random, CountDownLatch latch) {
        this.outputFileDir = outputFileDir;
        this.timeStampList = timeStampList;
        this.windowSizeCandidateList = windowSizeCandidateList;
//        this.windowSizeRatioList = windowSizeRatioList;
        this.backwardWindowSizeLowerBound = backwardWindowSizeLowerBound;
        this.userWSizeList = userWSizeList;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.random = random;
        this.latch = latch;
    }

    private void generateWindowSizeWithinTimeRange() {
        String tempOutputFilePath;
        Integer timeStampListSize = timeStampList.size();
        String formatFileNameID;
        BasicWrite basicWrite = new BasicWrite(",");
        String tempString;
        Integer timeStampID;
        List<Integer> subCandidateWindowSizeList;
        List<Double> subCandidateWindowSizeRatio;
        Integer subWindowSizeUpperBound, tempIndex;
//        for (Integer timeStampID : timeStampList) {
        for (int i = startIndex; i <= endIndex; ++i) {
            timeStampID = timeStampList.get(i);
            formatFileNameID = FormatFileName.getFormatNumberString(timeStampID, 0, timeStampListSize);
            tempOutputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, "timestamp_" + formatFileNameID + ".txt");
            basicWrite.startWriting(tempOutputFilePath);
            for (BasicPair<Integer, Integer> userWindowSizeUpperBoundPair : userWSizeList) {
                //todo: 这里把backward window size设置为最小
//                Integer tempRandomInteger = RandomUtil.getRandomInteger(backwardWindowSizeLowerBound, userWindowSizeUpperBoundPair.getValue());
                Integer tempRandomInteger = RandomUtil.getRandomInteger(backwardWindowSizeLowerBound, backwardWindowSizeLowerBound, random);
//                Integer tempRandomInteger2 = RandomUtil.getRandomInteger(windowSizeCandidateList, userWindowSizeUpperBoundPair.getValue());
//                Integer index = RandomUtil.getRandomIndexGivenStatisticPoint(windowSizeRatioList.toArray(new Double[0]));
                subWindowSizeUpperBound = userWindowSizeUpperBoundPair.getValue();
                tempIndex = windowSizeCandidateList.indexOf(subWindowSizeUpperBound);
                if (tempIndex < 0) {
                    throw new RuntimeException("Illegal budget!");
                }
                subCandidateWindowSizeList = new ArrayList<>();
                subCandidateWindowSizeRatio = new ArrayList<>();
                int subSize = tempIndex + 1;
                for (int j = 0; j <= tempIndex; j++) {
                     subCandidateWindowSizeList.add(windowSizeCandidateList.get(j));
                     subCandidateWindowSizeRatio.add(1.0/subSize);
                }
                Integer index = RandomUtil.getRandomIndexGivenStatisticPoint(subCandidateWindowSizeRatio.toArray(new Double[0]));
                Integer tempRandomInteger2 = subCandidateWindowSizeList.get(index);
                tempString = StringUtil.join(",", userWindowSizeUpperBoundPair.getKey(), tempRandomInteger, tempRandomInteger2);
                basicWrite.writeOneLine(tempString);
            }
            basicWrite.endWriting();
        }
    }

    @Override
    public void run() {
        generateWindowSizeWithinTimeRange();
        this.latch.countDown();
    }
}
