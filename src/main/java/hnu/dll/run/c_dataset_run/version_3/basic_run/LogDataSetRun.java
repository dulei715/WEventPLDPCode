package hnu.dll.run.c_dataset_run.version_3.basic_run;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run.c_dataset_run.version_3.version_utils.DatasetSegmentRunUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class LogDataSetRun {
//    public static void runLog() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.logFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.basicDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
//    public static void runLogSerially() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.logFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.seriallyDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
    public static void runLogContainingSimpleLDP(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String basicPath = Constant.logFilePath;
        String dataTypeFileName = "status.txt";
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.enhancedDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }

}
