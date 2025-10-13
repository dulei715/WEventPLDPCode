package hnu.dll.utils.run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll.run.c_dataset_run.utils.PostProcessUtils;

public class CombineForEachRound {
    public static void combineAllRound(String datasetBasicPath, String finalResultDirName, int roundSize, String roundPattern, String outputDir, String extractDir) {
//        String roundPattern = "round_%d";
//        String outputDir = "group_output";
//        String extractDir = "extract_result";
        for (int i = 1; i <= roundSize; i++) {
            String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetBasicPath, String.format(roundPattern, i));
            String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, outputDir);
            String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, extractDir);
            String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
            PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
            PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);
        }

    }
}
