package hnu.dll.run._pre_process.b_parameter_pre_process.version_2_decrete.parameter_generator.sub_thread;

import cn.edu.dll.basic.NumberUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll.utils.FormatFileName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class DiscretePrivacyBudgetWithinTimeRangeGenerator implements Runnable {
    private String outputFileDir;
    private List<Integer> timeStampList;
    private List<Double> privacyBudgetCandidateList;
    private Double remainBackwardPrivacyLowerBound;
    private Double remainBackwardPrivacyUpperBound;
    private List<BasicPair<Integer, Double>> userBudgetList;
    private Integer startIndex;
    private Integer endIndex;
    private CountDownLatch latch;

    public DiscretePrivacyBudgetWithinTimeRangeGenerator(String outputFileDir, List<Integer> timeStampList, List<Double> privacyBudgetCandidateList, Double remainBackwardPrivacyLowerBound, Double remainBackwardPrivacyUpperBound, List<BasicPair<Integer, Double>> userBudgetList, Integer startIndex, Integer endIndex, CountDownLatch latch) {
        this.outputFileDir = outputFileDir;
        this.timeStampList = timeStampList;
        this.privacyBudgetCandidateList = privacyBudgetCandidateList;
//        this.privacyBudgetRatioList = privacyBudgetRatioList;
        this.remainBackwardPrivacyLowerBound = remainBackwardPrivacyLowerBound;
        this.remainBackwardPrivacyUpperBound = remainBackwardPrivacyUpperBound;
        this.userBudgetList = userBudgetList;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.latch = latch;
    }

    private void generatePrivacyBudgetWithinGivenTimeRange() {
        String tempOutputFilePath;
        Integer timeStampListSize = timeStampList.size();
        String formatFileNameID;
        BasicWrite basicWrite = new BasicWrite(",");
        String tempString;
        Integer timeStampID, tempIndex;
        List<Double> subCandidateBudgetValueList, subCandidateBudgetRatioList;
        Double tempBudgetLowerBound;
        for (int i = startIndex; i <= endIndex; ++i) {
            timeStampID = timeStampList.get(i);
            formatFileNameID = FormatFileName.getFormatNumberString(timeStampID, 0, timeStampListSize);
            tempOutputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, "timestamp_" + formatFileNameID + ".txt");
            basicWrite.startWriting(tempOutputFilePath);
            for (BasicPair<Integer, Double> userBudgetLowerBoundPair : userBudgetList) {
                Double tempRandomDouble = RandomUtil.getRandomDouble(remainBackwardPrivacyLowerBound, remainBackwardPrivacyUpperBound);
//                Double tempRandomDouble2 = RandomUtil.getRandomDouble(userBudgetLowerBoundPair.getValue(), privacyUpperBound);
                tempBudgetLowerBound = userBudgetLowerBoundPair.getValue();
                tempIndex = privacyBudgetCandidateList.indexOf(tempBudgetLowerBound);
                if (tempIndex < 0) {
                    throw new RuntimeException("Illegal privacy budget!");
                }
                int size = privacyBudgetCandidateList.size() - tempIndex;
                subCandidateBudgetValueList = new ArrayList<>();
                subCandidateBudgetRatioList = new ArrayList<>();
                for (int j = tempIndex; j < privacyBudgetCandidateList.size(); j++) {
                    subCandidateBudgetValueList.add(privacyBudgetCandidateList.get(j));
                    subCandidateBudgetRatioList.add(1.0/size);
                }
                Integer index = RandomUtil.getRandomIndexGivenStatisticPoint(subCandidateBudgetRatioList.toArray(new Double[0]));
                Double tempRandomDouble2 = subCandidateBudgetValueList.get(index);
                tempString = StringUtil.join(",", userBudgetLowerBoundPair.getKey(), NumberUtil.roundFormat(tempRandomDouble, 2), NumberUtil.roundFormat(tempRandomDouble2, 2));
                basicWrite.writeOneLine(tempString);
            }
            basicWrite.endWriting();
        }
    }
    @Override
    public void run() {
        generatePrivacyBudgetWithinGivenTimeRange();
        this.latch.countDown();
    }
}
