package hnu.dll.run.c_dataset_run.version_3.basic_run;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run.c_dataset_run.version_5.version_utils.DatasetSegmentRunUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
@Deprecated
public class TrajectoryDataSetRunBefore {



    public static void runTrajectory(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String basicPath = Constant.trajectoriesFilePath;
        String dataTypeFileName = "cell.txt";
//        Integer singleBatchSize = 2;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
//        DatasetSegmentRunUtils.basicDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }

    public static void runTrajectorySerially(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String basicPath = Constant.trajectoriesFilePath;
        String dataTypeFileName = "cell.txt";
//        Integer singleBatchSize = 2;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.seriallyDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }
    public static void runTrajectoryContainingSimpleLDP(Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        String basicPath = Constant.trajectoriesFilePath;
        String dataTypeFileName = "cell.txt";
//        Integer singleBatchSize = 2;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.enhancedDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }

//    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
//        CatchSignal catchSignal = new CatchSignal();
//        catchSignal.startCatch();
//        runTrajectory();
//    }
}
