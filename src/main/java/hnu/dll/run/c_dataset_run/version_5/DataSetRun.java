package hnu.dll.run.c_dataset_run.version_5;

import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run.c_dataset_run.version_5.version_utils.DatasetSegmentRunUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class DataSetRun {
    public static void runDataSet(String basicPath, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.basicDatasetRun(basicPath,
                Constant.GroupParameterDirectoryName, Constant.PersonalizedParameterFileName,
                singleBatchSize, random);
    }

    /**
     * 串行执行（单线程），用于测量 TimeCost。
     * 并行版本同时起 9 个线程，会打满 CPU 并共享 Random，导致 wall-clock 时间被拉长且不可复现，
     * 因此论文中的时间开销应当用本方法跑出来的结果。
     */
    public static void runDataSetSerially(String basicPath, String dataTypeFileName, Integer userSize, Random random) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.seriallyDatasetRun(basicPath, dataTypeFileName, singleBatchSize, userSize, random);
    }
}
