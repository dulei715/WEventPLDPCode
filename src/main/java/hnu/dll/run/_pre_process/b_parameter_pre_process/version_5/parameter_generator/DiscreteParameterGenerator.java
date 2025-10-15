package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll._config.ParameterUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.PreprocessRunUtils;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.sub_thread.DiscretePrivacyBudgetWithinTimeRangeGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.sub_thread.DiscreteWindowSizeWithinTimeRangeGenerator;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run2.utils.UserParameterGenerationUtils;
import hnu.dll.run2.utils.io.UserParameterIOUtils;
import hnu.dll.run2.utils.structs.UserParameter;
import hnu.dll.utils.thread.ThreadUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class DiscreteParameterGenerator {
    /**
     * 参数存放目录：
     *      1. privacy budget (内部是变化的 privacy budget)
     *                |
     *                | -- file: [value 1: index 1, value 2: index 2, ..., value 5: index 5]
     *                |
     *                | -- dir:  index 1 (budget 下限，内部是n个user，每个user)
     *                        |
     *                        | -- file: [user 1: budget A, user 2: budget B,...] (用于personalized static methods)
     *                        |
     *                        | -- dir: timestamp 1
     *                                  |
     *                                  | -- file: [user1: budget A1, user2: budget B1, ...]
     *                        | -- dir: timestamp 2
     *                | -- dir:  value 2
     *                        |
     *                        | -- ...
     *                | -- dir:  value 3
     *                |...
     *      2. window size --
     *
     */

    public static void generateFixedPrivacyBudgetForAllUsers(String outputFileDir, String privacyBudgetFileNameForPersonalized, List<Integer> typeIDList, List<Double> candidateList, List<Double> resultStatistic) {
        List<String> fixedBudgetList = ParameterUtils.getRandomDoubleListInCandidateListWithStatistic(typeIDList, candidateList, resultStatistic);
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, privacyBudgetFileNameForPersonalized);
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(outputPath);
        basicWrite.writeStringListWithoutSize(fixedBudgetList);
        basicWrite.endWriting();
    }
    public static void generateFixedWindowSizeForAllUsers(String outputFileDir, String windowSizeFileNameForPersonalized, List<Integer> typeIDList, List<Integer> candidateList, List<Double> resultStatistic) {
        List<String> fixedBudgetList = ParameterUtils.getRandomIntegerListInCandidateListWithStatistic(typeIDList, candidateList, resultStatistic);
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, windowSizeFileNameForPersonalized);
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(outputPath);
        basicWrite.writeStringListWithoutSize(fixedBudgetList);
        basicWrite.endWriting();
    }

    public static void generatePrivacyBudgetForAllUsersWithTimeStamps(String outputFileDir, String privacyBudgetFileNameForPersonalized, List<Integer> timeStampList, List<Double> privacyBudgetCandidateList, Double remainBackwardPrivacyLowerBound, Double remainBackwardPrivacyUpperBound, Random random) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, privacyBudgetFileNameForPersonalized));
        List<String> userBudgetStringList = basicRead.readAllWithoutLineNumberRecordInFile();
        List<BasicPair<Integer, Double>> userBudgetList = new ArrayList<>(userBudgetStringList.size());
        String[] tempStringArray;
        for (String str : userBudgetStringList) {
            tempStringArray = basicRead.split(str);
            userBudgetList.add(new BasicPair<>(Integer.valueOf(tempStringArray[0]), Double.valueOf(tempStringArray[1])));
        }
//        generatePrivacyBudgetWithinGivenTimeRange(outputFileDir, timeStampList, upperBound, userBudgetList);
        int threadSize = 5;
        int timeStampListSize = timeStampList.size();
        List<Integer> threadStartIndexList = ThreadUtils.getThreadStartIndexList(threadSize, 0, timeStampListSize);
        Thread tempTread;
        Runnable tempRunnable;
        Integer startIndex, endIndex;
        threadSize = threadStartIndexList.size(); // 只是为了加强一下
        CountDownLatch latch = new CountDownLatch(threadSize);
        for (int i = 0; i < threadSize; i++) {
            startIndex = threadStartIndexList.get(i);
            if (i < threadSize - 1) {
                endIndex = threadStartIndexList.get(i+1) - 1;
            } else {
                endIndex = timeStampListSize - 1;
            }
            tempRunnable = new DiscretePrivacyBudgetWithinTimeRangeGenerator(outputFileDir, timeStampList, privacyBudgetCandidateList, remainBackwardPrivacyLowerBound, remainBackwardPrivacyUpperBound, userBudgetList, startIndex, endIndex, random, latch);
            tempTread = new Thread(tempRunnable);
            tempTread.start();
            System.out.println("Start privacy budget thread " + tempTread.getName() + " with id " + tempTread.getId());
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public static void generateWindowSizeForAllUsersWithTimeStamps(String outputFileDir, String windowSizeFileNameForPersonalized, List<Integer> timeStampList, List<Integer> windowSizeCandidateList, Integer backwardWindowSizeLowerBound, Random random) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(StringUtil.join(ConstantValues.FILE_SPLIT, outputFileDir, windowSizeFileNameForPersonalized));
        List<String> userWindowSizeStringList = basicRead.readAllWithoutLineNumberRecordInFile();
        List<BasicPair<Integer, Integer>> userWSizeList = new ArrayList<>(userWindowSizeStringList.size());
        String[] tempStringArray;
        for (String str : userWindowSizeStringList) {
            tempStringArray = basicRead.split(str);
            userWSizeList.add(new BasicPair<>(Integer.valueOf(tempStringArray[0]), Integer.valueOf(tempStringArray[1])));
        }
//        generateWindowSizeWithinTimeRange(outputFileDir, timeStampList, lowerBound, userWSizeList);
        int threadSize = 5;
        int timeStampListSize = timeStampList.size();
        List<Integer> threadStartIndexList = ThreadUtils.getThreadStartIndexList(threadSize, 0, timeStampListSize);
        Thread tempTread;
        Runnable tempRunnable;
        Integer startIndex, endIndex;
        threadSize = threadStartIndexList.size(); // 只是为了加强一下
        CountDownLatch latch = new CountDownLatch(threadSize);
        for (int i = 0; i < threadSize; i++) {
            startIndex = threadStartIndexList.get(i);
            if (i < threadSize - 1) {
                endIndex = threadStartIndexList.get(i+1) - 1;
            } else {
                endIndex = timeStampListSize - 1;
            }
            tempRunnable = new DiscreteWindowSizeWithinTimeRangeGenerator(outputFileDir, timeStampList, windowSizeCandidateList, backwardWindowSizeLowerBound, userWSizeList, startIndex, endIndex, random, latch);
            tempTread = new Thread(tempRunnable);
            tempTread.start();
            System.out.println("Start window size thread " + tempTread.getName() + " with id " + tempTread.getId());
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public static void generatePrivacyBudgetAndWindowSize(String dirPath, String parameterFileNameForPerson,
                                             final Integer userSize,
                                             final List<Double> privacyBudgetList, final Double defaultPrivacyBudget, final List<Double> candidateSortedBudgetList,
                                             final List<Integer> windowSizeList, final Integer defaultWindowSize, final List<Integer> candidateSortedWindowSizeList,
                                             Random random){
//        Double privacyUpperBound = ConfigureUtils.getPrivacyBudgetUpperBound();
        File tempDir;
        String tempFileAbsolutePath;

        String innerFileName;
        List<Double> tempSubBudgetList;
        List<Integer> tempSubWindowSizeList;
        List<UserParameter> tempUserParameterList;
        Integer focusIndex;
        for (Double tempBudget : privacyBudgetList) {
            innerFileName = ParameterGroupInitializeUtils.toPathName(tempBudget, defaultWindowSize);
            tempDir = new File(dirPath, innerFileName);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            // 根据候选列表抽出子列表
            focusIndex = candidateSortedBudgetList.indexOf(tempBudget);
            tempSubBudgetList = candidateSortedBudgetList.subList(focusIndex, candidateSortedBudgetList.size());
            tempUserParameterList = UserParameterGenerationUtils.generateUserParameterList(userSize, tempSubBudgetList, defaultWindowSize, random);
            tempFileAbsolutePath = StringUtil.join(ConstantValues.FILE_SPLIT, tempDir.getAbsolutePath(), parameterFileNameForPerson);
            UserParameterIOUtils.writeUserParameters(tempFileAbsolutePath, tempUserParameterList);
        }

        for (Integer tempWindowSize : windowSizeList) {
            if (tempWindowSize.equals(defaultWindowSize)) {
                continue;
            }
            innerFileName = ParameterGroupInitializeUtils.toPathName(defaultPrivacyBudget, tempWindowSize);
            tempDir = new File(dirPath, innerFileName);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            focusIndex = candidateSortedWindowSizeList.indexOf(tempWindowSize);
            tempSubWindowSizeList = candidateSortedWindowSizeList.subList(0, focusIndex + 1);
            tempUserParameterList = UserParameterGenerationUtils.generateUserParameterList(userSize, defaultPrivacyBudget, tempSubWindowSizeList, random);
            tempFileAbsolutePath = StringUtil.join(ConstantValues.FILE_SPLIT, tempDir.getAbsolutePath(), Constant.PersonalizedParameterFileName);
            UserParameterIOUtils.writeUserParameters(tempFileAbsolutePath, tempUserParameterList);
        }
    }

    public static void generatePrivacyBudget(String dirPath, String privacyBudgetFileNameForPersonalized, final List<Integer> userTypeIDList,
                                             final List<Integer> timeStampList, final List<Double> privacyBudgetList,
                                             final List<Double> candidateSortedBudgetList /*升序排列*/, Random random){
//        Double privacyUpperBound = ConfigureUtils.getPrivacyBudgetUpperBound();
        Double remainBackwardPrivacyUpperBound = ConfigureUtils.getBackwardPrivacyBudgetUpperBoundDifference();
        //todo: 这里把backward privacy设置为最大
//        Double remainBackwardPrivacyLowerBound = ConfigureUtils.getBackwardPrivacyBudgetLowerBoundDifference();
        Double remainBackwardPrivacyLowerBound = remainBackwardPrivacyUpperBound;
        File tempDir;
        String tempDirPath;
        List<Double> tempBudgetList;
        List<Double> tempRatioList;
        Integer chosenTypeSize;
        for (Double budget : privacyBudgetList) {
            tempDir = new File(dirPath, "budget_"+String.valueOf(budget).replace(".","-"));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            // 这里按照平均比例分配, 并且分配的budget不小于给定的budget
            tempBudgetList = new ArrayList<>();
            tempRatioList = new ArrayList<>();
            int lowerIndex = candidateSortedBudgetList.indexOf(budget);
            if (lowerIndex < 0) {
                System.out.println("Warning: budget generation for DiscreteParameterGenerator:generatePrivacyBudget does not find given budget in the candidate list!");
            }
            chosenTypeSize = candidateSortedBudgetList.size() - lowerIndex;
            for (int i = lowerIndex; i < candidateSortedBudgetList.size(); ++i) {
                tempBudgetList.add(candidateSortedBudgetList.get(i));
                tempRatioList.add(1.0/chosenTypeSize);
            }
            tempDirPath = tempDir.getAbsolutePath();
            generateFixedPrivacyBudgetForAllUsers(tempDirPath, privacyBudgetFileNameForPersonalized, userTypeIDList, tempBudgetList, tempRatioList);
//            generatePrivacyBudgetForAllUsersWithTimeStamps(tempDirPath, privacyBudgetFileNameForPersonalized, timeStampList, tempBudgetList, remainBackwardPrivacyLowerBound, remainBackwardPrivacyUpperBound, random);
        }
    }

    public static void generateWindowSize(String dirPath, String windowSizeFileNameForPersonalized, final List<Integer> userIDList, final List<Integer> timeStampList, final List<Integer> windowSizeList, final List<Integer> candidateSortedWindowSizeList /*升序排列*/, Random random) {
//        Integer windowSizeLowerBound = ConfigureUtils.getWindowSizeLowerBound();
        Integer backwardWindowSizeLowerBound = ConfigureUtils.getWindowSizeLowerBound();
        File tempDir;
        String tempDirPath;
        List<Integer> tempWindowSizeList;
        List<Double> tempRatioList;
        Integer chosenTypeSize;
        for (Integer windowSize : windowSizeList) {
            tempDir = new File(dirPath, "w_size_"+String.valueOf(windowSize));
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            // 这里按照平均比例分配, 并且分配的budget不小于给定的budget
            tempWindowSizeList = new ArrayList<>();
            tempRatioList = new ArrayList<>();
            int upperIndex = candidateSortedWindowSizeList.indexOf(windowSize);
            if (upperIndex < 0) {
                System.out.println("Warning: budget generation for DiscreteParameterGenerator:generateWindowSize does not find given windowSize in the candidate list!");
            }
            chosenTypeSize = upperIndex + 1;
            for (int i = 0; i <= upperIndex; ++i) {
                tempWindowSizeList.add(candidateSortedWindowSizeList.get(i));
                tempRatioList.add(1.0/chosenTypeSize);
            }
            tempDirPath = tempDir.getAbsolutePath();
            generateFixedWindowSizeForAllUsers(tempDirPath, windowSizeFileNameForPersonalized, userIDList, tempWindowSizeList, tempRatioList);
//            generateWindowSizeForAllUsersWithTimeStamps(tempDirPath, windowSizeFileNameForPersonalized, timeStampList, tempWindowSizeList, backwardWindowSizeLowerBound, random);
        }
    }


    public static void generateParametersForDataset(String datasetBasicPath,
                                                    String privacyBudgetConfigVarianceName,
                                                    String windowSizeConfigVarianceName,
                                                    Integer userSize,
                                                    String basicParameterGenerationDirectoryName,
                                                    String privacyBudgetFileNameForPerson,
                                                    Random random) {
//        String privacyBudgetDirName = "1.privacy_budget";
//        String windowSizeDirName = "2.window_size";

        String datasetParameterBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetBasicPath, basicParameterGenerationDirectoryName);
        // 从xml参数文件中获取 budget
        List<Double> privacyBudgetList = ConfigureUtils.getIndependentPrivacyBudgetList(privacyBudgetConfigVarianceName);
        Double defaultPrivacyBudget = ConfigureUtils.getIndependentSinglePrivacyBudget(privacyBudgetConfigVarianceName);
        // 从xml参数文件中 windowSize
        List<Integer> windowSizeList = ConfigureUtils.getIndependentWindowSizeList(windowSizeConfigVarianceName);
        Integer defaultWindowSize = ConfigureUtils.getIndependentSingleWindowSize(windowSizeConfigVarianceName);


        List<Double> candidatePrivacyBudgetList = ConfigureUtils.getGenerationPrivacyBudgetList()[0];

        List<Integer> candidateWindowSizeList = ConfigureUtils.getGenerationWindowSizeList()[0];
        /* for test */ List<Integer> datasetUserTypeIDList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, 4);
        generatePrivacyBudgetAndWindowSize(datasetParameterBasicPath, privacyBudgetFileNameForPerson, userSize,
                privacyBudgetList, defaultPrivacyBudget, candidatePrivacyBudgetList,
                windowSizeList, defaultWindowSize, candidateWindowSizeList,
                random);
//        generatePrivacyBudget(StringUtil.join(ConstantValues.FILE_SPLIT, datasetParameterBasicPath, privacyBudgetDirName), privacyBudgetFileNameForPersonalized, datasetUserTypeIDList, datasetTimeStampList, privacyBudgetList, candidatePrivacyBudgetList, random);
//        generateWindowSize(StringUtil.join(ConstantValues.FILE_SPLIT, datasetParameterBasicPath, windowSizeDirName), windowSizeFileNameForPersonalized, datasetUserTypeIDList, datasetTimeStampList, windowSizeList, candidateWindowSizeList, random);
    }

    @Deprecated
    public static void generateParametersForTrajectory(String privacyBudgetConfigVarianceName,
                                                       String windowSizeConfigVarianceName,
                                                       String userTypeIDFileName,
                                                       String basicParameterGenerationDirectoryName,
                                                       String privacyBudgetFileNameForPersonalized,
                                                       String windowSizeFileNameForPersonalized,
                                                       Random random) {
        /**
         *
         */
        String privacyBudgetDirName = "1.privacy_budget";
        String windowSizeDirName = "2.window_size";
        String trajectoryParameterBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, basicParameterGenerationDirectoryName);

        List<Double> privacyBudgetList = ConfigureUtils.getIndependentPrivacyBudgetList(privacyBudgetConfigVarianceName);
        List<Integer> windowSizeList = ConfigureUtils.getIndependentWindowSizeList(windowSizeConfigVarianceName);

        List<Integer> trajectoryUserTypeIDList = PreprocessRunUtils.getUserTypeIDList(Constant.trajectoriesFilePath, userTypeIDFileName);
        List<Integer> trajectoryTimeStampList = PreprocessRunUtils.getTimeStampList(Constant.trajectoriesFilePath);
        List<Double> candidatePrivacyBudgetList = ConfigureUtils.getGenerationPrivacyBudgetList()[0];
        List<Integer> candidateWindowSizeList = ConfigureUtils.getGenerationWindowSizeList()[0];
        generatePrivacyBudget(StringUtil.join(ConstantValues.FILE_SPLIT, trajectoryParameterBasicPath, privacyBudgetDirName), privacyBudgetFileNameForPersonalized, trajectoryUserTypeIDList, trajectoryTimeStampList, privacyBudgetList, candidatePrivacyBudgetList, random);
        generateWindowSize(StringUtil.join(ConstantValues.FILE_SPLIT, trajectoryParameterBasicPath, windowSizeDirName), windowSizeFileNameForPersonalized, trajectoryUserTypeIDList, trajectoryTimeStampList, windowSizeList, candidateWindowSizeList, random);
    }

    @Deprecated
    public static void generateParametersForCheckIn(String privacyBudgetConfigVarianceName, String windowSizeConfigVarianceName, String userTypeIDFileName, String basicParameterGenerationDirectoryName, String privacyBudgetFileNameForPersonalized, String windowSizeFileNameForPersonalized, Random random) {

        String privacyBudgetDirName = "1.privacy_budget";
        String windowSizeDirName = "2.window_size";
        String checkInParameterBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, basicParameterGenerationDirectoryName);
        List<Double> privacyBudgetList = ConfigureUtils.getIndependentPrivacyBudgetList(privacyBudgetConfigVarianceName);
        List<Integer> windowSizeList = ConfigureUtils.getIndependentWindowSizeList(windowSizeConfigVarianceName);
        List<Integer> checkInUserTypeIDList = PreprocessRunUtils.getUserTypeIDList(Constant.checkInFilePath, userTypeIDFileName);
        List<Integer> checkInTimeStampList = PreprocessRunUtils.getTimeStampList(Constant.checkInFilePath);
        List<Double> candidatePrivacyBudgetList = ConfigureUtils.getGenerationPrivacyBudgetList()[0];
        List<Integer> candidateWindowSizeList = ConfigureUtils.getGenerationWindowSizeList()[0];
        generatePrivacyBudget(StringUtil.join(ConstantValues.FILE_SPLIT, checkInParameterBasicPath, privacyBudgetDirName), privacyBudgetFileNameForPersonalized, checkInUserTypeIDList, checkInTimeStampList, privacyBudgetList, candidatePrivacyBudgetList, random);
        generateWindowSize(StringUtil.join(ConstantValues.FILE_SPLIT, checkInParameterBasicPath, windowSizeDirName), windowSizeFileNameForPersonalized, checkInUserTypeIDList, checkInTimeStampList, windowSizeList, candidateWindowSizeList, random);
    }

    public static void main0(String[] args) {
        String dirPath = args[0];
        System.out.println(dirPath);
        File file = new File(dirPath);
//        File[] files = file.listFiles(new TxtFilter());
        File[] files = file.listFiles();
        System.out.println(files.length);
        MyPrint.showSplitLine("*", 50);
        for (File file1 : files) {
            System.out.println(file1.getName());
        }
    }

}
