package hnu.dll.run.c_dataset_run.version_3.basic_run;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run.c_dataset_run.version_5.version_utils.DatasetSegmentRunUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class TLNSDataSetRun {
//    public static void runTLNS() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.tlnsFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.basicDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
//    public static void runTLNSSerially() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.tlnsFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.seriallyDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
    public static void runTLNSContainingSimpleLDP(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String basicPath = Constant.TLNSFilePath;
        String dataTypeFileName = "status.txt";
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.enhancedDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }

//    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        CatchSignal catchSignal = new CatchSignal();
//        catchSignal.startCatch();
//
//        String datasetPath = Constant.tlnsFilePath;
//        String finalResultDirName = "3.tlns_result";
//
//        runTLNS();
//
//        // 4. 后处理
//        String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
//        String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "extract_result");
//        String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
//        PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//        PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);
//    }
}
