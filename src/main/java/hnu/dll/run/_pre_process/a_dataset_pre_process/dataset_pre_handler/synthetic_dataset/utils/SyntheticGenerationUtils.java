package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.BasicWrite;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function.DataGenerationFunction;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.sub_thread.runInputGeneratorThread;
import hnu.dll.utils.io.ListReadUtils;
import hnu.dll.utils.io.ListWriteUtils;
import hnu.dll.utils.thread.ThreadUtils;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SyntheticGenerationUtils {
    public static void generateProbability(DataGenerationFunction<Double> function, Integer probabilitySize, Boolean containInitialValue) {
        String basicFileName  = ConfigureUtils.getDatasetFileName(function.getName());
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicFileName, "basic_info", "probability.txt");
        List<Double> probabilityList = function.nextProbability(probabilitySize);
        BasicWrite basicWrite = new BasicWrite();
        basicWrite.startWriting(outputPath);
        basicWrite.writeOneLine("0,".concat(String.valueOf(function.getInitializedValue())));
        int i = 0;
        for (; i < probabilitySize - 1; i++) {
//            System.out.println(probabilityList.get(i));
            basicWrite.writeOneLine(String.valueOf(i+1).concat(",").concat(String.valueOf(probabilityList.get(i))));
        }
        if (!containInitialValue) {
            basicWrite.writeOneLine(String.valueOf(i+1).concat(",").concat(String.valueOf(probabilityList.get(i))));
        }
        basicWrite.endWriting();
    }

    public static void generateUserID(String datasetPath, int size) {
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "user.txt");
        List<Integer> userList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, size-1);
        ListWriteUtils.writeList(outputPath, userList, ",");
    }
    public static void generatePositionType(String datasetPath, String outputFileName, int positionSize) {
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", outputFileName);
        List<Integer> userList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, positionSize-1);
        ListWriteUtils.writeList(outputPath, userList, ",");
    }

    public static void generateTimeStamp(String datasetPath, int timeStampSize) {
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "time_stamp.txt");
        List<Integer> timestampList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, timeStampSize-1);
        ListWriteUtils.writeList(outputPath, timestampList, ",");
    }

    public static void generateRunInputData(String datasetPath, String positionFileName) {
        String probabilityListPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "probability.txt");
        String positionPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", positionFileName);
        String userIDPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "basic_info", "user.txt");
        String basicOutputDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "runInput");
//        List<String> timeStampStringList = ListReadUtils.readAllDataList(timeStampPath, ",");
        List<String> probabilityStrList = ListReadUtils.readAllDataList(probabilityListPath, ",");
        List<String> positionStringList = ListReadUtils.readAllDataList(positionPath, ",");
        List<String> userIDList = ListReadUtils.readAllDataList(userIDPath, ",");
        Integer threadSize = 10;
        Integer totalTimeStampSize = probabilityStrList.size();
        List<Integer> threadStartIndexList = ThreadUtils.getThreadStartIndexList(threadSize, 0, totalTimeStampSize);
        Integer startIndex, endIndex;
        Thread tempTread;
        Runnable tempRunnable;
        CountDownLatch latch = new CountDownLatch(threadSize);
        for (int i = 0; i < threadStartIndexList.size(); i++) {
            startIndex = threadStartIndexList.get(i);
            if (i < threadStartIndexList.size() - 1) {
                endIndex = threadStartIndexList.get(i+1) - 1;
            } else {
                endIndex = totalTimeStampSize - 1;
            }
            tempRunnable = new runInputGeneratorThread(startIndex, endIndex, basicOutputDir, probabilityStrList, userIDList, positionStringList, latch);
            tempTread = new Thread(tempRunnable);
            tempTread.start();
            System.out.println("Start thread " + tempTread.getName() + " with id " + tempTread.getId());
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }



}
