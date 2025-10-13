package hnu.dll.run.d_total_run._3_poster_process_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import ecnu.dll._config.Constant;
import ecnu.dll.utils.run.CombineForEachRound;
import ecnu.dll.utils.run.RepeatUtils;

public class TrajectoryPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.trajectoriesFilePath;
        String finalResultDirName = "1.trajectory_result";

        String basicOutputFileString = "../1.result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);
        String roundPattern = "round_%d";
        String outputDir = "group_output";
        String extractDir = "extract_result";

        int roundSize = 10;

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
