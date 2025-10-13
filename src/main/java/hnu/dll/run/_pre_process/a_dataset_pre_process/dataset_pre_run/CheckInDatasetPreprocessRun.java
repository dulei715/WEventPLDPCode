package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.TxtFilter;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.signal.CatchSignal;
import cn.edu.dll.signal.signal_handle.NoTerminalHandler;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.dataset.real.datasetA.basic_struct.TrajectoryTools;
import hnu.dll.dataset.real.datasetB.handled_struct.CheckInSimplifiedBean;
import hnu.dll.dataset.real.datasetB.spetial_tools.CheckInBeanUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.CheckInPreprocessRunUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.PreprocessRunUtils;
import hnu.dll.utils.io.ListReadUtils;
import hnu.dll.utils.io.ListWriteUtils;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.io.File;
import java.util.*;

public class CheckInDatasetPreprocessRun {

    private static final String checkInDataFileName = "dataset_TIST2015_Checkins.txt";
    public static void dataSplit(int unitSize) {
        String dataPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, checkInDataFileName);
        String outputSuperDirectory = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "split");
//        System.out.println(dataPath);
//        System.out.println(outputSuperDirectory);

        File outputDataSuperPathDirectory = new File(outputSuperDirectory);
        if (!outputDataSuperPathDirectory.exists()) {
            outputDataSuperPathDirectory.mkdirs();
        }
        int k = 1;
        BasicRead basicRead = new BasicRead();
        BasicWrite basicWrite = new BasicWrite();
        List<String> recordList;

        basicRead.startReading(dataPath);
        do {
            recordList = basicRead.readGivenLineSize(unitSize);
            basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, outputDataSuperPathDirectory, k+".txt"));
            basicWrite.writeStringListWithoutSize(recordList);
            System.out.println("Finish " + k + ".txt");
            basicWrite.endWriting();
            ++k;
        } while (recordList != null && !recordList.isEmpty() && recordList.size() >= unitSize);

    }

    public static void dataJoin() {
        String outputDataSuperPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "join");
        File outputDataSuperPathDirectory = new File(outputDataSuperPath);
        String splitDirectory = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "split");
        if (!outputDataSuperPathDirectory.exists()) {
            outputDataSuperPathDirectory.mkdirs();
        }
        CheckInBeanUtils.transformSplitFilesToCountry(Constant.checkInFilePath, splitDirectory, outputDataSuperPath);
    }



    @Deprecated
    public static void toExperimentRawDataSimple() {
        int cacheSize = 10;
        int timeStamp = 0;
        String inputDirectoryName = "join";
        String outputDirectoryName = "runInput";
        String inputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, inputDirectoryName);
        String outputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, outputDirectoryName);
        Long startCheckInTimeSlot = 1333476006000L; // 在/Users/admin/MainFiles/5.GitTrans/2.github_code/DynamicWEventCode/src/test/java/important_test/DatasetCheckInTest.testTime()测试中得到
        Long endCheckInTimeSlot = 1379373855000L;
        Long timeInterval = ConfigureUtils.getTimeInterval("checkIn");
        File fileDirectory = new File(inputDirectoryPath);
        File[] inputFiles = fileDirectory.listFiles();
        BasicRead basicRead = new BasicRead(",");
        BasicWrite basicWrite = new BasicWrite(",");
        List<String> tempDataList;

        // 记录第t个时间间隔内每个user对应的country名
        Map<Integer, Map<Integer, BasicPair<Long, String>>> cacheMap = new TreeMap<>();
        Map<Integer, BasicPair<Long, String>> tempLatestUserTimeSlotLocationMap;
        Map<Long, Integer> helpMap = new HashMap<>();
        long cacheLeft= startCheckInTimeSlot;
        long cacheRight;
        long timeSlotRightBound, tempUserTime;
        CheckInSimplifiedBean tempBean;
        while (cacheLeft <= endCheckInTimeSlot) {
            cacheMap.clear();
            helpMap.clear();
            cacheRight = cacheLeft + cacheSize * timeInterval;
            for (long tempTimeSlot = cacheLeft; tempTimeSlot < cacheRight && tempTimeSlot <= endCheckInTimeSlot; tempTimeSlot += timeInterval) {
                cacheMap.put(timeStamp, new TreeMap<>());
                helpMap.put(tempTimeSlot, timeStamp);
                ++timeStamp;
            }
            for (File inputFile : inputFiles) {
                basicRead.startReading(inputFile.getAbsolutePath());
                tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
                basicRead.endReading();
                for (long timeSlotLeftBound = cacheLeft; timeSlotLeftBound < cacheRight && timeSlotLeftBound <= endCheckInTimeSlot; timeSlotLeftBound += timeInterval) {
                    timeSlotRightBound = timeSlotLeftBound + timeInterval;
                    tempLatestUserTimeSlotLocationMap = cacheMap.get(helpMap.get(timeSlotLeftBound));
                    for (String dataString : tempDataList) {
                        tempBean = CheckInSimplifiedBean.toBean(basicRead.split(dataString));
                        tempUserTime = tempBean.getCheckInTimeStamp();
                        if (tempUserTime >= timeSlotLeftBound && tempUserTime < timeSlotRightBound) {
                            PreprocessRunUtils.updateLatestTimeSlotData(tempLatestUserTimeSlotLocationMap, tempBean.getUserID(), tempUserTime, tempBean.getCountryName());
                        }
                    }

                }

            }
            // 遍历cacheMap写文件
            Integer tempTimeStamp, tempUserID;
            String tempOutputTimeStampPath, tempDataLine;
            BasicPair<Long, String> tempTimeLocationPair;
            for (Map.Entry<Integer, Map<Integer, BasicPair<Long, String>>> cacheEntry : cacheMap.entrySet()) {
                tempTimeStamp = cacheEntry.getKey();
                tempLatestUserTimeSlotLocationMap = cacheEntry.getValue();
                tempOutputTimeStampPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputDirectoryPath, String.format("timestamp_%d.txt", tempTimeStamp));
                basicWrite.startWriting(tempOutputTimeStampPath);
                for (Map.Entry<Integer, BasicPair<Long, String>> latestEntry : tempLatestUserTimeSlotLocationMap.entrySet()) {
                    tempUserID = latestEntry.getKey();
                    tempTimeLocationPair = latestEntry.getValue();
                    tempDataLine = String.format("%d,%s,%d", tempUserID, tempTimeLocationPair.getValue(), tempTimeLocationPair.getKey());
                    basicWrite.writeOneLine(tempDataLine);
                }
                basicWrite.endWriting();
            }
            cacheLeft = cacheRight;
        }

    }

    /**
     * 将数据按照时间段先后分在不同的文件里（以时间段号命名）
     */
    public static void shuffleJoinFilesByTimeSlot() {
        int cacheSize = 10;
        int timeStamp = 0;
        String inputDirectoryName = "join";
        String outputDirectoryName = "shuffle_by_time_slot";
        String inputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, inputDirectoryName);
        String outputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, outputDirectoryName);
        Long startCheckInTimeSlot = 1333476006000L; // 在/Users/admin/MainFiles/5.GitTrans/2.github_code/DynamicWEventCode/src/test/java/important_test/DatasetCheckInTest.testTime()测试中得到
        Long endCheckInTimeSlot = 1379373855000L;
        Long timeInterval = ConfigureUtils.getTimeInterval("checkIn");
        File fileDirectory = new File(inputDirectoryPath);
        File[] inputFiles = fileDirectory.listFiles();
        BasicRead basicRead = new BasicRead(",");
        BasicWrite basicWrite = new BasicWrite(",");
        List<String> tempDataList;

        // 记录第t个时间间隔内每个user对应的country名
        Map<Integer, List<String>> cacheMap = new TreeMap<>();
        List<String> tempTimeSlotDataList;
        Map<Long, Integer> helpMap = new HashMap<>();
        long cacheLeft= startCheckInTimeSlot;
        long cacheRight;
        long timeSlotRightBound, tempUserTime;
        CheckInSimplifiedBean tempBean;
//        int tempTimeStamp;
        while (cacheLeft <= endCheckInTimeSlot) {
            cacheMap.clear();
            helpMap.clear();
            cacheRight = cacheLeft + cacheSize * timeInterval;
//            tempLatestUserTimeSlotLocationMap = new HashMap<>();
            for (long tempTimeSlot = cacheLeft; tempTimeSlot < cacheRight && tempTimeSlot <= endCheckInTimeSlot; tempTimeSlot += timeInterval) {
                cacheMap.put(timeStamp, new ArrayList<>());
                helpMap.put(tempTimeSlot, timeStamp);
                ++timeStamp;
            }
            for (File inputFile : inputFiles) {
                basicRead.startReading(inputFile.getAbsolutePath());
                tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
                basicRead.endReading();
                for (long timeSlotLeftBound = cacheLeft; timeSlotLeftBound < cacheRight && timeSlotLeftBound <= endCheckInTimeSlot; timeSlotLeftBound += timeInterval) {
                    timeSlotRightBound = timeSlotLeftBound + timeInterval;
                    tempTimeSlotDataList = cacheMap.get(helpMap.get(timeSlotLeftBound));
                    for (String dataString : tempDataList) {
                        tempBean = CheckInSimplifiedBean.toBean(basicRead.split(dataString));
                        tempUserTime = tempBean.getCheckInTimeStamp();
                        if (tempUserTime >= timeSlotLeftBound && tempUserTime < timeSlotRightBound) {
//                            CheckInPreprocessRunUtils.updateLatestTimeSlotData(tempLatestUserTimeSlotLocationMap, tempBean.getUserID(), tempUserTime, tempBean.getCountryName());
                            tempTimeSlotDataList.add(dataString);
                        }
                    }

                }

            }
            // 遍历cacheMap写文件
            Integer tempTimeStamp, tempUserID;
            String tempOutputTimeStampPath, tempDataLine;
            BasicPair<Long, String> tempTimeLocationPair;
            for (Map.Entry<Integer, List<String>> cacheEntry : cacheMap.entrySet()) {
                tempTimeStamp = cacheEntry.getKey();
                tempTimeSlotDataList = cacheEntry.getValue();
                tempOutputTimeStampPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputDirectoryPath, String.format("timestamp_%05d.txt", tempTimeStamp));
                basicWrite.startWriting(tempOutputTimeStampPath);
                basicWrite.writeStringListWithoutSize(tempTimeSlotDataList);
                basicWrite.endWriting();
            }
            cacheLeft = cacheRight;
        }
    }

    @Deprecated
    public static void toExperimentRawData() {
        int cacheSize = 10;
        int timeStamp = 0;
        String inputDirectoryName = "shuffle_by_time_slot";
        String outputDirectoryName = "runInput";
        String inputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, inputDirectoryName);
        String outputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, outputDirectoryName);
        Long startCheckInTimeSlot = 1333476006000L; // 在/Users/admin/MainFiles/5.GitTrans/2.github_code/DynamicWEventCode/src/test/java/important_test/DatasetCheckInTest.testTime()测试中得到
        Long endCheckInTimeSlot = 1379373855000L;
        Long timeInterval = ConfigureUtils.getTimeInterval("checkIn");
        File fileDirectory = new File(inputDirectoryPath);
        File[] inputFiles = fileDirectory.listFiles();
        BasicRead basicRead = new BasicRead(",");
        BasicWrite basicWrite = new BasicWrite(",");
        List<String> tempDataList;
        Map<Integer, BasicPair<Long, String>> userTimeSlotLocationMap = CheckInPreprocessRunUtils.getInitialUserTimeSlotLocationMap(StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "join"));
        CheckInSimplifiedBean tempBean;
        String tempOutputLineData;
        BasicPair<Long, String> tempBasicPair;

        for (File inputFile : inputFiles) {
            basicRead.startReading(inputFile.getAbsolutePath());
            tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            for (String dataLine : tempDataList) {
                tempBean = CheckInSimplifiedBean.toBean(basicRead.split(dataLine));
                PreprocessRunUtils.updateLatestTimeSlotData(userTimeSlotLocationMap, tempBean.getUserID(), tempBean.getCheckInTimeStamp(), tempBean.getCountryName());
            }
            basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, outputDirectoryPath, inputFile.getName()));
            for (Map.Entry<Integer, BasicPair<Long, String>> entry : userTimeSlotLocationMap.entrySet()) {
                tempBasicPair = entry.getValue();
                tempOutputLineData = String.format("%d,%s,%d", entry.getKey(), tempBasicPair.getValue(), tempBasicPair.getKey());
                basicWrite.writeOneLine(tempOutputLineData);
            }
            basicWrite.endWriting();
        }
    }

    /**
     * 根据每个时间段中的数据依次更新每个用户的位置状态并记录该时间段中最晚的用户状态
     */
    public static void mergeToExperimentRawData(String outputFileName) {
        String path = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "shuffle_by_time_slot");
        File directoryFile = new File(path);
        File[] files = directoryFile.listFiles(new TxtFilter());
//        File[] files = directoryFile.listFiles();
        BasicRead basicRead = new BasicRead(",");
        BasicWrite basicWrite = new BasicWrite(",");
        List<String> tempDataList;
        CheckInSimplifiedBean tempBean;
        Map<Integer, BasicPair<Long, String>> userTimeSlotLocationMap = CheckInPreprocessRunUtils.getInitialUserTimeSlotLocationMap(StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "join"));
        String outputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, outputFileName);

        for (File file : files) {
            basicRead.startReading(file.getAbsolutePath());
            tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            for (String lineString : tempDataList) {
                tempBean = CheckInSimplifiedBean.toBean(basicRead.split(lineString));
                PreprocessRunUtils.updateLatestTimeSlotData(userTimeSlotLocationMap, tempBean.getUserID(), tempBean.getCheckInTimeStamp(), tempBean.getCountryName());
            }
            basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, outputDirectoryPath, file.getName()));
            for (Map.Entry<Integer, BasicPair<Long, String>> entry : userTimeSlotLocationMap.entrySet()) {
                basicWrite.writeOneLine(CheckInPreprocessRunUtils.toSimpleCheckInString(entry));
            }
            basicWrite.endWriting();
        }
    }


    public static void main1(String[] args) {
        int unitSize = 204800;
        dataSplit(unitSize);
//        System.out.println("Hello RealDataSetPreprocessRun");
    }

    public static void main2(String[] args) {
        dataJoin();
    }

    public static void main3(String[] args) {
        String inputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "taxi_log_2008_by_id_filter");
        String outputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "filter_sample");
        Double ratio = Constant.Sample_Ratio_For_Picture;
        int bufferSize = 1000;
        TrajectoryTools.sampleData(inputDir, outputDir, ratio, bufferSize);
    }

    public static void main4(String[] args) {
        SignalHandler handler = new NoTerminalHandler(2);
        try {
            Signal sigTERM = new Signal("TERM");
            Signal sigINT = new Signal("INT");
            Signal.handle(sigTERM, handler);
            Signal.handle(sigINT, handler);

            // 程序主逻辑
            System.out.println("Program is running...");
            shuffleJoinFilesByTimeSlot();
//            Thread.sleep(Integer.MAX_VALUE); // 防止程序立即退出
            System.out.println("Program finished !");
        } catch (Exception e) {
            e.printStackTrace();
//            System.exit(0);
        }
    }

    public static void recordCountryInfo() {
        String countryReadDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "join");

        File dirFile = new File(countryReadDirPath);
        File[] files = dirFile.listFiles(new TxtFilter());
        BasicRead basicRead = new BasicRead(",");
        List<String> tempData;
        TreeSet<String> countrySet = new TreeSet<>();
        CheckInSimplifiedBean bean;
        for (File file : files) {
            basicRead.startReading(file.getAbsolutePath());
            tempData = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            for (String line : tempData) {
                bean = CheckInSimplifiedBean.toBean(basicRead.split(line));
                countrySet.add(bean.getCountryName());
            }
        }
        List<String> countryList = new ArrayList<>(countrySet);
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "basic_info", "country.txt"));
        basicWrite.writeStringListWithoutSize(countryList);
        basicWrite.endWriting();
    }



    public static void recordBasicInformation() {
        recordCountryInfo();
        PreprocessRunUtils.recordUserInfo(Constant.checkInFilePath, "runInput_raw", "user_raw.txt");
        PreprocessRunUtils.recordTimeStampInfo(Constant.checkInFilePath, "runInput_raw");
    }

    @Deprecated
    public static void extractUserBefore() {
        Double ratio = 0.05;
        String basicPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "basic_info");
        String rawDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "user.txt");
        String newRawDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "user_raw.txt");
        List<String> rawData, newData = new ArrayList<>();
        File newRawFile = new File(newRawDataPath);
        if (newRawFile.exists()) {
            System.out.println("为了不覆盖数据，请先删除原有的user_raw.txt");
            return;
        }
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(rawDataPath);
        rawData = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        for (String rawDatum : rawData) {
            if (RandomUtil.isChosen(ratio)) {
                newData.add(rawDatum);
            }
        }
        File rawFile = new File(rawDataPath);

        BasicWrite basicWrite = new BasicWrite(",");
        try {
            rawFile.renameTo(newRawFile);
            basicWrite.startWriting(rawDataPath);
            basicWrite.writeStringListWithoutSize(newData);
            basicWrite.endWriting();
        } catch (Exception e) {
            e.printStackTrace();
            basicWrite.startWriting(rawDataPath);
            basicWrite.writeStringListWithoutSize(rawData);
            basicWrite.endWriting();
            newRawFile.delete();
        }
    }
    public static void extractUser() {
        Double ratio = 0.05;
        String basicPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "basic_info");
        String rawDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "user_raw.txt");
        String extractDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "user.txt");
        List<String> rawData, extractData = new ArrayList<>();
        File extractDataFile = new File(extractDataPath);
        if (extractDataFile.exists()) {
            System.out.println("为了不覆盖数据，请先删除原有的user.txt");
            return;
        }
        rawData = ListReadUtils.readAllDataList(rawDataPath, ",");
        for (String rawDatum : rawData) {
            if (RandomUtil.isChosen(ratio)) {
                extractData.add(rawDatum);
            }
        }
        ListWriteUtils.writeList(extractDataPath, extractData, ",");
    }

    public static void extractUserDataBefore() {
        // 保证basic_info/user.txt是抽取过的user
        String rawDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "runInput");
        String newRawDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "runInput_raw");
        String userIDPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "basic_info", "user.txt");
        File newRawFile = new File(newRawDir);
        File[] rawFileArray;
        if (newRawFile.exists()) {
            System.out.println("为了保证文件不覆盖，请先删除runInput_raw文件夹！");
            return;
        }
        // 获取所有user id 并将其放进集合
        File rawFile = new File(rawDir);
        List<String> tempData, newData;
        BasicWrite basicWrite;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(userIDPath);
        tempData = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        Set<Integer> userIDSet = new HashSet<>();
        for (String idStr : tempData) {
            userIDSet.add(Integer.valueOf(idStr));
        }
        try {
            rawFile.renameTo(newRawFile);
            rawFile.mkdirs();
            rawFileArray = newRawFile.listFiles(new TxtFilter());
            basicWrite = new BasicWrite(",");
            Integer tempUserID;
            for (File file : rawFileArray) {
                basicRead.startReading(file.getAbsolutePath());
                tempData = basicRead.readAllWithoutLineNumberRecordInFile();
                basicRead.endReading();
                newData = new ArrayList<>();
                for (String strLine : tempData) {
                    tempUserID = Integer.valueOf(basicRead.split(strLine)[0]);
                    if (userIDSet.contains(tempUserID)) {
                        newData.add(strLine);
                    }
                }
                basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, rawDir, file.getName()));
                basicWrite.writeStringListWithoutSize(newData);
                basicWrite.endWriting();
            }
        } catch (Exception e) {
            if (!newRawFile.exists() || newRawFile.listFiles(new TxtFilter()).length < 1) {
                return;
            }
            File tempCopyRunInputFile = new File(StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "tempRunInput"+System.currentTimeMillis()+".txt"));
            rawFile.renameTo(tempCopyRunInputFile);
            newRawFile.renameTo(rawFile);
            e.printStackTrace();
        }

    }
    public static void extractUserData() {
        // 保证basic_info/user.txt是抽取过的user
        String rawDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "runInput_raw");
        String extractInputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "runInput");
        String userIDPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "basic_info", "user.txt");
        File extractInputDirFile = new File(extractInputDir);
        File[] rawFileArray;
        if (extractInputDirFile.exists()) {
            System.out.println("为了保证文件不覆盖，请先删除runInput文件夹！");
            return;
        }
        extractInputDirFile.mkdirs();
        // 获取所有user id 并将其放进集合
        File rawDirFile = new File(rawDir);
        List<String> tempDataList, extractData;
        BasicWrite basicWrite;
        tempDataList = ListReadUtils.readAllDataList(userIDPath, ",");
        Set<Integer> userIDSet = new HashSet<>();
        for (String idStr : tempDataList) {
            userIDSet.add(Integer.valueOf(idStr));
        }
        BasicRead basicRead = new BasicRead(",");
        rawFileArray = rawDirFile.listFiles(new TxtFilter());
        basicWrite = new BasicWrite(",");
        Integer tempUserID;
        for (File file : rawFileArray) {
            basicRead.startReading(file.getAbsolutePath());
            tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            extractData = new ArrayList<>();
            for (String strLine : tempDataList) {
                tempUserID = Integer.valueOf(basicRead.split(strLine)[0]);
                if (userIDSet.contains(tempUserID)) {
                    extractData.add(strLine);
                }
            }
            basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, extractInputDir, file.getName()));
            basicWrite.writeStringListWithoutSize(extractData);
            basicWrite.endWriting();
        }

    }

    public static void main0(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        // 程序主逻辑
        System.out.println("Program is running... ...");

        // 1. 将数据分割成多个文件以方便分批读取到内存进行处理
        System.out.println("Start data split...");
        int unitSize = 204800;
        dataSplit(unitSize);

        // 2. 将数据与country文件链接，组合成 (userID,country,timestamp)的形式
        System.out.println("Start join...");
        dataJoin();


        // 3. 将数据按照时间，划分成多个文件
        System.out.println("Start shuffle...");
        shuffleJoinFilesByTimeSlot();

        // 4. 保留每个timestamp的用户状态
        System.out.println("Start merge...");
        mergeToExperimentRawData("runInput_raw");

        // 5. 记录country.txt, user.txt, timestamp.txt三个基本文件到 basic_info/ 目录下
        System.out.println("Start record...");
        recordBasicInformation();
        System.out.println("Program finished !");

    }

    public static void main5(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        recordBasicInformation();
    }

    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        // 6. 抽取 5% 的 user 记录在 user.txt 中
//        extractUser();
        // 7. 根据新的 user.txt 抽取 runInput 中的数据
        extractUserData();
    }

}
