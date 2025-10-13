package hnu.dll.run.d_total_run._4_added_run._3_3_poster_process_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import ecnu.dll._config.ConfigureUtils;
import ecnu.dll._config.Constant;
import ecnu.dll.utils.run.CombineForEachRound;
import ecnu.dll.utils.run.RepeatUtils;

public class TLNSContainingLDPPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.tlnsFilePath;
        String finalResultDirName = "3.tlns_containing_ldp_result";

        String basicOutputFileString = "../4.result_containing_ldp";
        String roundPattern = "round_%d_containing_ldp";
        String outputDir = "group_output_containing_ldp";
        String extractDir = "extract_containing_ldp_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

//        int roundSize = 10;
//        int roundSize = 4;

        String configDatasetFileHandleName = "tlns";
        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));

        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleMainRound(datasetPath, basicOutputPath, roundSize);
    }
}
