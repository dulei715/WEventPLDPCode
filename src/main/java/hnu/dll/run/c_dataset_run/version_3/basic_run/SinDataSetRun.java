package hnu.dll.run.c_dataset_run.version_3.basic_run;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run.c_dataset_run.version_5.version_utils.DatasetSegmentRunUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class SinDataSetRun {
//    public static void runSin() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.sinFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.basicDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
//    public static void runSinSerially() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        String basicPath = Constant.sinFilePath;
//        String dataTypeFileName = "status.txt";
//        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
//        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.seriallyDatasetRun(basicPath, dataTypeFileName, singleBatchSize);
//    }
    public static void runSinContainingSimpleLDP(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String basicPath = Constant.sinFilePath;
        String dataTypeFileName = "status.txt";
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.enhancedDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }

}
