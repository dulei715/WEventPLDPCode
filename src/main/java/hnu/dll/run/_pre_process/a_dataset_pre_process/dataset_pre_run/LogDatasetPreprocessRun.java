package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function.LogFunction;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.ProbabilityDistributionUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.SyntheticGenerationUtils;

public class LogDatasetPreprocessRun {
    public static void generateProbabilityParameters() {
//        Double parameterA = 0.25;
        Double parameterA = 0.7;
//        Double parameterB = 0.01;
        Double parameterB = 0.02;
        String datasetName = "log";
        LogFunction logFunction = new LogFunction(parameterA, parameterB);
        int timeStampSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "timeStampSize"));
        SyntheticGenerationUtils.generateProbability(logFunction, timeStampSize, true);

    }

    public static void generateDataset() {
        String datasetName = "log";
        String datasetPath = Constant.LogFilePath;
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
        // 取值域大小 d 优先取 -Dexp.tag 对应标签下的 DomainSize；未配置时回退到 fileHandle 的 locationTypeSize
        Integer statusSize = ConfigureUtils.getIndependentSingleDomainSize(ConfigureUtils.getExperimentTag());
        if (statusSize == null) {
            statusSize = Integer.valueOf(ConfigureUtils.getFileHandleInfo(datasetName, "locationTypeSize"));
        }
        SyntheticGenerationUtils.generatePositionType(datasetPath, positionFileName, statusSize);

        // 5. 配置 Zipf 指数（读 parameter_config.xml 的 zipfAlpha；未配置或为 0 时其余状态均分）
        ProbabilityDistributionUtils.configureZipfAlpha();

        // 6. 生成输入数据到 runInput 文件夹下
        SyntheticGenerationUtils.generateRunInputData(datasetPath, positionFileName);


    }

    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        generateDataset();
    }
}
