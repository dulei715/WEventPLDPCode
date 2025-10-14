package hnu.dll.run.d_total_run._4_added_run._3_3_poster_process_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.utils.run.CombineForEachRound;
import hnu.dll.utils.run.RepeatUtils;

public class CheckInContainingLDPPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.checkInFilePath;
        String finalResultDirName = "2.check_in_containing_ldp_result";

        String basicOutputFileString = "../4.result_containing_ldp";
        String roundPattern = "round_%d_containing_ldp";
        String outputDir = "group_output_containing_ldp";
        String extractDir = "extract_containing_ldp_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

//        int roundSize = 10;
//        int roundSize = 2;
//        int roundSize = 4;

        String configDatasetFileHandleName = "checkIn";
        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));


        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleMainRound(datasetPath, basicOutputPath, roundSize);
    }
}
