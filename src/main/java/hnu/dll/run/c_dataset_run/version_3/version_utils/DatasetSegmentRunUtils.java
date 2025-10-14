package hnu.dll.run.c_dataset_run.version_3.version_utils;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterParallelRun;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterSerialRun;
import hnu.dll.run.b_parameter_run.FixedSegmentEnhancedParameterParallelRun;
import hnu.dll.utils.filters.NumberTxtFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class DatasetSegmentRunUtils {
    public static void basicDatasetRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Double> budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
        List<Integer> windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");

        Double budgetDefault = budgetChangeList.get(2);
        Integer windowSizeDefault = windowSizeChangeList.get(2);

        Runnable tempRunnable;
        Thread tempThread;

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;

//        Integer segmentUnitSize = 4;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
        Integer segmentUnitSize = independentData.getValue();

        Integer startIndex, endIndex;
        Integer segmentID = 0;
        Integer segmentSize = (int) Math.ceil(totalFileSize * 1.0 / segmentUnitSize);
        Integer totalSubThreadSize = segmentSize * (budgetChangeList.size() + windowSizeChangeList.size() - 1);
        CountDownLatch latch = new CountDownLatch(totalSubThreadSize);
        for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
            startIndex = segmentIndex;
            endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);

            CountDownLatch innerLatch = new CountDownLatch(budgetChangeList.size() + windowSizeChangeList.size() - 1);

            for (Double budget : budgetChangeList) {
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
                tempThread = new Thread(tempRunnable);
                tempThread.start();
                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);

                //todo: for test
//            break;
            }


            for (int i = 0; i < windowSizeChangeList.size(); i++) {
                if (i == 2) {
                    continue;
                }
                Integer windowSize = windowSizeChangeList.get(i);
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budgetDefault, windowSize, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
                tempThread = new Thread(tempRunnable);
                tempThread.start();
                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);
            }

            try {
                innerLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void seriallyDatasetRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Double> budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
        List<Integer> windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");

        Double budgetDefault = budgetChangeList.get(2);
        Integer windowSizeDefault = windowSizeChangeList.get(2);

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;

        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
        Integer segmentUnitSize = independentData.getValue();

        Integer startIndex, endIndex;
        Integer segmentID = 0;
        FixedSegmentBasicParameterSerialRun serialRun;
        for (Double budget : budgetChangeList) {
            // 对每个budget执行所有的segment
            for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
                startIndex = segmentIndex;
                endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);
                serialRun = new FixedSegmentBasicParameterSerialRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, userSize, timeStampDataFiles, startIndex, random, endIndex, segmentID);
//                serialRun = new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, timeStampDataFiles, startIndex, endIndex, segmentID);
                serialRun.runSegmentBatch();
                System.out.println("Start budget change segmentBatch running with segment " + segmentID);

            }
        }

        for (int i = 0; i < windowSizeChangeList.size(); i++) {
            // 对除默认值以外的每个windowSize执行所有的segment
            if (i == 2) {
                continue;
            }
            for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
                startIndex = segmentIndex;
                endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);
                serialRun = new FixedSegmentBasicParameterSerialRun(basicPath, dataTypeFileName, singleBatchSize, budgetDefault, windowSizeChangeList.get(i), userSize, timeStampDataFiles, startIndex, random, endIndex, segmentID);
                serialRun.runSegmentBatch();
                System.out.println("Start window size segmentBatch running with segment " + segmentID);

            }
        }
    }




    public static void enhancedDatasetRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Double> budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
        List<Integer> windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");

        Double budgetDefault = budgetChangeList.get(2);
        Integer windowSizeDefault = windowSizeChangeList.get(2);

        Runnable tempRunnable;
        Thread tempThread;

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;

//        Integer segmentUnitSize = 4;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
        Integer segmentUnitSize = independentData.getValue();

        Integer startIndex, endIndex;
        Integer segmentID = 0;
        Integer segmentSize = (int) Math.ceil(totalFileSize * 1.0 / segmentUnitSize);
        Integer totalSubThreadSize = segmentSize * (budgetChangeList.size() + windowSizeChangeList.size() - 1);
        CountDownLatch latch = new CountDownLatch(totalSubThreadSize);
        for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
            startIndex = segmentIndex;
            endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);

            CountDownLatch innerLatch = new CountDownLatch(budgetChangeList.size() + windowSizeChangeList.size() - 1);

            for (Double budget : budgetChangeList) {
                tempRunnable =  new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
                tempThread = new Thread(tempRunnable);
                tempThread.start();
                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);

                //todo: for test
//            break;
            }


            for (int i = 0; i < windowSizeChangeList.size(); i++) {
                if (i == 2) {
                    continue;
                }
                Integer windowSize = windowSizeChangeList.get(i);
                tempRunnable =  new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budgetDefault, windowSize, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
                tempThread = new Thread(tempRunnable);
                tempThread.start();
                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);
            }

            try {
                innerLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
