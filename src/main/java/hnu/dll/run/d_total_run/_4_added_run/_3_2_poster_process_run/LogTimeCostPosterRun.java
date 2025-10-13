package hnu.dll.run.d_total_run._4_added_run._3_2_poster_process_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import ecnu.dll._config.ConfigureUtils;
import ecnu.dll._config.Constant;
import ecnu.dll.utils.run.CombineForEachRound;
import ecnu.dll.utils.run.RepeatUtils;

public class LogTimeCostPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.logFilePath;
        String finalResultDirName = "5.log_time_cost_result";

        String basicOutputFileString = "../3.result_time_cost";
        String roundPattern = "round_%d_time_cost";
        String outputDir = "group_output_time_cost";
        String extractDir = "extract_time_cost_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

//        int roundSize = 10;
        String configDatasetFileHandleName = "log";
        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));


        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleSerialRound(datasetPath, basicOutputPath, roundSize);
    }
}
