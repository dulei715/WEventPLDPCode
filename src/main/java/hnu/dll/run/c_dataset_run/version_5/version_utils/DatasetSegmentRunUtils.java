package hnu.dll.run.c_dataset_run.version_5.version_utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.LocationGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterParallelRun;
import hnu.dll.run.b_parameter_run.FixedSegmentBasicParameterSerialRun;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.utils.io.UserParameterIOUtils;
import hnu.dll.run.utils.structs.UserParameter;
import hnu.dll.utils.file.FileUtils;
import hnu.dll.utils.filters.NumberTxtFilter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class DatasetSegmentRunUtils {

    /**
     * 当前实验使用的配置标签，对应 parameter_config.xml 中
     * independentVariables 下各 attribute 的 name（如 "default" / "case1" / "fixed"）。
     *
     * 通过 JVM 参数 -Dexp.tag=xxx 指定，未指定时为 "default"。
     * 这样不同的实验可以读取配置文件的不同参数组，而不需要每次改配置：
     *   主实验              java ... SinMainRun 0                 -> 用 default（完整 variance）
     *   维度可扩展性实验     java -Dexp.tag=fixed ... SinMainRun 0 -> 用 fixed（只跑默认 epsilon / w）
     */
    public static String getExperimentTag() {
        return ConfigureUtils.getExperimentTag();
    }

    public static void basicDatasetRun(
            String basicPath,
            String groupParameterDirectoryName, String personalizedParameterFileName,
            Integer singleBatchSize,
            Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        /**
         * groupParameterFileName + / + {p_{budget}_w_{windowsize}} + / + personalizedParameterFileName 构成用户参数文件目录
         */
        String experimentTag = getExperimentTag();
        Double budgetDefault = ConfigureUtils.getIndependentSinglePrivacyBudget(experimentTag);
        Integer windowSizeDefault = ConfigureUtils.getIndependentSingleWindowSize(experimentTag);
        List<Double> budgetChangeList;
        List<Integer> windowSizeChangeList;
        if (ConfigureUtils.isGroupShiftExperiment()) {
            // Shift generates one parameter file containing all privacy groups.
            // Always use the configured single-value directory for this mode.
            budgetChangeList = Collections.singletonList(budgetDefault);
            windowSizeChangeList = Collections.singletonList(windowSizeDefault);
        } else {
            budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList(experimentTag);
            windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList(experimentTag);
        }

        String inputDataFileName, parameterFileName;
        List<UserParameter> userParameterList;

        Runnable tempRunnable;
        Thread tempThread;

        Map<Integer, Integer> userToIndexMap = UserGroupGenerator.getUserToIndexMap(basicPath);
        Map<String, String> locationToMappedStrMap = LocationGroupGenerator.getLocationToMappedStrMap(basicPath);

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = FileUtils.listFilesByFileName(dirFile, new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;


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
                inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, groupParameterDirectoryName, parameterFileName, personalizedParameterFileName);
                userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(
                        basicPath, locationToMappedStrMap, /** 路径和文件相关参数 */
                        singleBatchSize, /** 传入 的batch 大小参数 */
                        budget, windowSizeDefault, userParameterList, userToIndexMap, /** 用户参数 */
                        timeStampDataFiles, startIndex, endIndex, segmentID, /** 当前 segement 对应的时间段数据 */
                        new Random(random.nextInt()), /** 这里设置每个线程独立的seed，防止竞争冲突，以及大到可复现 */
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
                inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, groupParameterDirectoryName, parameterFileName, personalizedParameterFileName);
                userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                tempRunnable =  new FixedSegmentBasicParameterParallelRun(
                        basicPath, locationToMappedStrMap,
                        singleBatchSize,
                        budgetDefault, windowSize, userParameterList, userToIndexMap,
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
        String experimentTag = getExperimentTag();
        // 用 single 配置取默认值，而不是取 variance 的第 3 个元素（单值 variance 时会越界）
        Double budgetDefault = ConfigureUtils.getIndependentSinglePrivacyBudget(experimentTag);
        Integer windowSizeDefault = ConfigureUtils.getIndependentSingleWindowSize(experimentTag);
        List<Double> budgetChangeList;
        List<Integer> windowSizeChangeList;
        if (ConfigureUtils.isGroupShiftExperiment()) {
            budgetChangeList = Collections.singletonList(budgetDefault);
            windowSizeChangeList = Collections.singletonList(windowSizeDefault);
        } else {
            budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList(experimentTag);
            windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList(experimentTag);
        }

        File dirFile = new File(basicPath, "runInput");
        File[] timeStampDataFiles = FileUtils.listFilesByFileName(dirFile, new NumberTxtFilter());
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
                String parameterFileName = ParameterGroupInitializeUtils.toPathName(budget, windowSizeDefault);
                String inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, Constant.GroupParameterDirectoryName, parameterFileName, Constant.PersonalizedParameterFileName);
                List<UserParameter> userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                serialRun = new FixedSegmentBasicParameterSerialRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, userParameterList, userSize, timeStampDataFiles, startIndex, random, endIndex, segmentID);
//                serialRun = new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, timeStampDataFiles, startIndex, endIndex, segmentID);
                serialRun.runSegmentBatch();
                System.out.println("Start budget change segmentBatch running with segment " + segmentID);

            }
        }

        for (int i = 0; i < windowSizeChangeList.size(); i++) {
            // 跳过与默认值相同的windowSize（与并行版一致，不再依赖下标 2）
            if (windowSizeChangeList.get(i).equals(windowSizeDefault)) {
                continue;
            }
            for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
                startIndex = segmentIndex;
                endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);
                String parameterFileName = ParameterGroupInitializeUtils.toPathName(budgetDefault, windowSizeChangeList.get(i));
                String inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, Constant.GroupParameterDirectoryName, parameterFileName, Constant.PersonalizedParameterFileName);
                List<UserParameter> userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);
                serialRun = new FixedSegmentBasicParameterSerialRun(basicPath, dataTypeFileName, singleBatchSize, budgetDefault, windowSizeChangeList.get(i), userParameterList, userSize, timeStampDataFiles, startIndex, random, endIndex, segmentID);
                serialRun.runSegmentBatch();
                System.out.println("Start window size segmentBatch running with segment " + segmentID);

            }
        }
    }




//    public static void enhancedDatasetRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        List<Double> budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
//        List<Integer> windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");
//
//        Double budgetDefault = budgetChangeList.get(2);
//        Integer windowSizeDefault = windowSizeChangeList.get(2);
//
//        Runnable tempRunnable;
//        Thread tempThread;
//
//        File dirFile = new File(basicPath, "runInput");
//        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
//        int totalFileSize = timeStampDataFiles.length;
//
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
//        Integer segmentUnitSize = independentData.getValue();
//
//        Integer startIndex, endIndex;
//        Integer segmentID = 0;
//        Integer segmentSize = (int) Math.ceil(totalFileSize * 1.0 / segmentUnitSize);
//        Integer totalSubThreadSize = segmentSize * (budgetChangeList.size() + windowSizeChangeList.size() - 1);
//        CountDownLatch latch = new CountDownLatch(totalSubThreadSize);
//        for (int segmentIndex = 0; segmentIndex < totalFileSize; segmentIndex+=segmentUnitSize, ++segmentID) {
//            startIndex = segmentIndex;
//            endIndex = Math.min(startIndex + segmentUnitSize - 1, totalFileSize - 1);
//
//            CountDownLatch innerLatch = new CountDownLatch(budgetChangeList.size() + windowSizeChangeList.size() - 1);
//
//            for (Double budget : budgetChangeList) {
//                tempRunnable =  new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budget, windowSizeDefault, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
//                tempThread = new Thread(tempRunnable);
//                tempThread.start();
//                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);
//
//            }
//
//
//            for (int i = 0; i < windowSizeChangeList.size(); i++) {
//                if (i == 2) {
//                    continue;
//                }
//                Integer windowSize = windowSizeChangeList.get(i);
//                tempRunnable =  new FixedSegmentEnhancedParameterParallelRun(basicPath, dataTypeFileName, singleBatchSize, budgetDefault, windowSize, userSize, timeStampDataFiles, startIndex, endIndex, segmentID, random, latch, innerLatch);
//                tempThread = new Thread(tempRunnable);
//                tempThread.start();
//                System.out.println("Start thread " + tempThread.getName() + " with id " + tempThread.getId() + " in segment " + segmentID);
//            }
//
//            try {
//                innerLatch.await();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
