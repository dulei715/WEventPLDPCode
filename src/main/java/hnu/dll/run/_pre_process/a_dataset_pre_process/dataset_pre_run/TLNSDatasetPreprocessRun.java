package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function.TLNSFunction;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.SyntheticGenerationUtils;

public class TLNSDatasetPreprocessRun {
    private static void generateProbabilityParameters() {
        // 生成20000个user，1个位置，10000个timestamp
        // 保证所有user的统计满足某个函数的分布
        Double p0 = 0.5;
        Double gaussAverage = 0D;
        Double gaussStandardVariance = 0.0025;
        String datasetName = "tlns";
        TLNSFunction TLNSFunction = new TLNSFunction(p0, gaussAverage, gaussStandardVariance);
        int timeStampSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "timeStampSize"));
        SyntheticGenerationUtils.generateProbability(TLNSFunction, timeStampSize, true);

    }

    public static void generateDataset() {
        String datasetName = "tlns";
        String datasetPath = Constant.tlnsFilePath;
        String positionFileName = "status.txt";

        // 1. 生成时刻文件 time_stamp.txt
        Integer timeStampSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "timeStampSize"));
        SyntheticGenerationUtils.generateTimeStamp(datasetPath, timeStampSize);

        // 2. 生成每个时刻的概率参数到 basic_info/probability.txt
        generateProbabilityParameters();

        // 3. 生成userID.txt
        Integer userSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "userSize"));
        SyntheticGenerationUtils.generateUserID(datasetPath, userSize);

        // 4. 生成位置 status.txt
        Integer statusSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "locationTypeSize"));
        SyntheticGenerationUtils.generatePositionType(datasetPath, positionFileName, statusSize);

        // 5. 生成输入数据到 runInput 文件夹下
        SyntheticGenerationUtils.generateRunInputData(datasetPath, positionFileName);


    }

    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        generateDataset();
    }
}
