package hnu.dll.run.c_dataset_run.version_5.version_utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.ParameterUtils;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterParallelRun;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterSerialRun;
import hnu.dll.run.b_parameter_run.FixedSegmentEnhancedParameterParallelRun;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import hnu.dll.run2.utils.io.UserParameterIOUtils;
import hnu.dll.run2.utils.structs.UserParameter;
import hnu.dll.utils.filters.NumberTxtFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class DatasetSegmentRunUtils {
    public static void basicDatasetRun(
            String basicPath, String dataTypeFileName, String groupParameterFileName, String personalizedParameterFileName,
            Integer singleBatchSize,
            Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        /**
         * groupParameterFileName + / + {p_{budget}_w_{windowsize}} + / + personalizedParameterFileName 构成用户参数文件目录
         */
        List<Double> budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
        List<Integer> windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");
        Double budgetDefault = ConfigureUtils.getIndependentSinglePrivacyBudget("default");
        Integer windowSizeDefault = ConfigureUtils.getIndependentSingleWindowSize("default");

        String inputDataFileName, parameterFileName;
        List<UserParameter> userParameterList;

        Runnable tempRunnable;
        Thread tempThread;

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;

        Set<String> dataType = DatasetParameterUtils.getDataTypeSet(basicPath, dataTypeFileName);

//        Integer segmentUnitSize = 4;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
        Integer segmentUnitSize = independentData.getValue();

        Integer startIndex, endIndex;
        Integer segmentID = 0;
        Integer segmentSize = (int) Math.ceil(totalFileSize * 1.0 / segmentUnitSize);
        Integer totalSubThreadSize = segmentSize * (budgetChangeList.size() + windowSizeChangeList.size() - 1);
        CountDownLatch latch = new CountDownLatch(totalSubThreadSize);
        /**
         * 这里维持两个CountDownLatch:
         *  1. 内层 innerLatch
         *  2. 外层 latch
         * 其中内层 innerLatch达到9个放行（每个segment等待9个(budget-windowSize)执行对执行完毕再开始下个segment）
         * 外层等待数是9*
         */
        for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
            // 记录当前segment的开始位置
            startIndex = segmentIndex;
            // 记录当前segment的结束位置
            endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);

            CountDownLatch innerLatch = new CountDownLatch(budgetChangeList.size() + windowSizeChangeList.size() - 1);

            for (Double budget : budgetChangeList) {
                parameterFileName = ParameterGroupInitializeUtils.toPathName(budget, windowSizeDefault);
                inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, groupParameterFileName, parameterFileName, personalizedParameterFileName);
                userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(
                        basicPath, dataType, /** 路径和文件相关参数 */
                        singleBatchSize, /** 传入 的batch 大小参数 */
                        budget, windowSizeDefault, userParameterList, /** 用户参数 */
                        timeStampDataFiles, startIndex, endIndex, segmentID, /** 当前 segement 对应的时间段数据 */
                        random,
                        latch, innerLatch);
                tempThread = new Thread(tempRunnable);
                tempThread.start();
                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);

            }
            for (Integer windowSize : windowSizeChangeList) {
                if (windowSize.equals(windowSizeDefault)) {
                    continue;
                }
                parameterFileName = ParameterGroupInitializeUtils.toPathName(budgetDefault, windowSize);
                inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, groupParameterFileName, parameterFileName, personalizedParameterFileName);
                userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(
                        basicPath, dataType,
                        singleBatchSize,
                        budgetDefault, windowSize, userParameterList,
                        timeStampDataFiles, startIndex, endIndex, segmentID,
                        random,
                        latch, innerLatch);
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
