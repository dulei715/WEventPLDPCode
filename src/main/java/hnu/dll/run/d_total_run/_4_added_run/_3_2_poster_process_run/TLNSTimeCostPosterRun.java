package hnu.dll.run.d_total_run._4_added_run._3_2_poster_process_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.utils.run.CombineForEachRound;
import hnu.dll.utils.run.RepeatUtils;

public class TLNSTimeCostPosterRun {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.TLNSFilePath;
        String finalResultDirName = "3.tlns_time_cost_result";

        String basicOutputFileString = "../3.result_time_cost";
        String roundPattern = "round_%d_time_cost";
        String outputDir = "group_output_time_cost";
        String extractDir = "extract_time_cost_result";
        String basicOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, basicOutputFileString);

//        int roundSize = 10;
        String configDatasetFileHandleName = "tlns";
        int roundSize = Integer.parseInt(ConfigureUtils.getFileHandleInfo(configDatasetFileHandleName, "combineRound"));


        // 3. 后处理
        CombineForEachRound.combineAllRound(datasetPath, finalResultDirName, roundSize, roundPattern, outputDir, extractDir);
        // 4. 合并每轮
        RepeatUtils.combineMultipleSerialRound(datasetPath, basicOutputPath, roundSize);
    }
}
