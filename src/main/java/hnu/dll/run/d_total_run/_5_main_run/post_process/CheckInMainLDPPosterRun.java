package hnu.dll.run.d_total_run._5_main_run.post_process;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.utils.run.CombineForEachRound;
import hnu.dll.utils.run.RepeatUtils;

public class CheckInMainLDPPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.CheckInFilePath;
        String finalResultDirName = "2.checkIn_result";
        String configDatasetFileHandleName = "checkIn";

        String basicOutputFileString = "../1.main_ldp_result";
        String roundPattern = "round_%d_main_ldp";
        String outputDir = Constant.GroupOutputDirName; // "group_output"
        String extractDir = "extract_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));

        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleMainRound(datasetPath, basicOutputPath, roundSize);
    }
}
