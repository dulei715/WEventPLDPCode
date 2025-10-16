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

//        String basicPath = Constant.trajectoriesFilePath;
//        String dataTypeFileName = "cell.txt";
//        Integer singleBatchSize = 2;
        CombineTriple<String, Integer, List<Integer>> independentData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        Integer singleBatchSize = independentData.getValue();
        DatasetSegmentRunUtils.basicDatasetRun(basicPath,
                Constant.GroupParameterDirectoryName, Constant.PersonalizedParameterFileName,
                singleBatchSize, random);
    }
}
