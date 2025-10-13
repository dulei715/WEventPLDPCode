package hnu.dll.run.c_dataset_run.version_3.basic_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll.run.c_dataset_run.utils.PostProcessUtils;

import java.io.File;

public class PostProcessRun {
    public static void combineGroupResult(String datasetPath) {
//        CatchSignal catchSignal = new CatchSignal();
//        catchSignal.startCatch();
        String inputDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
        File dirFile = new File(inputDirPath);
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            File[] innerFiles = file.listFiles();
            for (File innerFile : innerFiles) {
                if (!innerFile.isDirectory()) {
                    continue;
                }
                PostProcessUtils.combineResult(innerFile.getAbsolutePath());
            }
        }
    }

    public static void extractData(String datasetPath, String dirFileName) {
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, dirFileName);
        String inputDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
        PostProcessUtils.combineAndExtractCombineResult(inputDirPath, outputPath);
    }

    public static void main(String[] args) {
//        combineGroupResult(args[0]);
        extractData(args[0], args[1]);
    }
}
