package hnu.dll.run.d_total_run._2_main_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run.LogDatasetPreprocessRun;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_pre_run.GenerateGroupParametersForLog;

public class LogMainRun {
    public static void main(String[] args) throws Exception {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.logFilePath;
        String finalResultDirName = "5.log_result";

        // 1. dataset 生成
        LogDatasetPreprocessRun.generateDataset();

        // 2. parameter 生成
        UserGroupGenerator.generateUserToIndex(Constant.logFilePath);
//        UserGroupGenerator.generateUserIDType(Constant.logFilePath);
//        UserGroupGenerator.generateUserToType(Constant.logFilePath);
        GenerateGroupParametersForLog.generateParameters();

        // 3. 执行
//        LogDataSetRun.runLog();

//        // 4. 后处理
//        String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
//        String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "extract_result");
//        String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
//        PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//        PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);

    }
}
