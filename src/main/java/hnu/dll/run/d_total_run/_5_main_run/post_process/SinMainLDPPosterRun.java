package hnu.dll.run.d_total_run._5_main_run.post_process;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.utils.run.CombineForEachRound;
import hnu.dll.utils.run.RepeatUtils;

public class SinMainLDPPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.SinFilePath;
        String finalResultDirName = "4.sin_result";
        String configDatasetFileHandleName = "sin";

        String basicOutputFileString = ConfigureUtils.getResultOutputDirName();
        String roundPattern = "round_%d_main_ldp";
        // 并行运行写入 group_output；串行运行为了保留可比较的时间开销，
        // 写入 group_output_time_cost。后处理必须读取与运行模式一致的目录。
        String runMode = System.getProperty("exp.run.mode");
        String outputDir = "serial".equalsIgnoreCase(runMode)
                ? "group_output_time_cost" : Constant.GroupOutputDirName;
        String extractDir = "extract_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));
        String repeatRoundOverride = System.getProperty("exp.repeat.round");
        if (repeatRoundOverride != null && !repeatRoundOverride.trim().isEmpty()) {
            roundSize = Integer.parseInt(repeatRoundOverride.trim());
        }

        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleMainRound(datasetPath, basicOutputPath, roundSize);
    }
}
