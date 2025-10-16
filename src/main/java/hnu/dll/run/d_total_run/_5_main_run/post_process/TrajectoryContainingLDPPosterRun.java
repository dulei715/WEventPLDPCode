package hnu.dll.run.d_total_run._5_main_run.post_process;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.utils.run.CombineForEachRound;
import hnu.dll.utils.run.RepeatUtils;

public class TrajectoryContainingLDPPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.trajectoriesFilePath;
        String finalResultDirName = "1.trajectory_result";

        String basicOutputFileString = "../4.result";
        String roundPattern = "round_%d_containing_ldp";
        String outputDir = Constant.GroupOutputDirName; // "group_output"
        String extractDir = "extract_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

//        int roundSize = 10;
//        int roundSize = 2;
//        int roundSize = 4;
        String configDatasetFileHandleName = "trajectories";
        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));


//        for (int i = 1; i <= roundSize; i++) {
//            // 3. 后处理
//            String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, String.format(roundFormat, i), rawDirName);
//            String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, String.format(roundFormat, i), extractDirName);
//            String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, String.format(roundFormat, i), finalResultDirName);
//            PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//            PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);
//        }

        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleMainRound(datasetPath, basicOutputPath, roundSize);
    }
}
