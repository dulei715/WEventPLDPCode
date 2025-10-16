package hnu.dll.run.d_total_run._5_main_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run.SinDatasetPreprocessRun;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run.TLNSDatasetPreprocessRun;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils.GenerateParameters;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.version_5.DataSetRun;

import java.util.Random;

public class SinMainRun {
    public static void main(String[] args) throws Exception {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.SinFilePath;
        String fileName = Constant.SinLocationFileName;

        Integer randomIndex = Integer.valueOf(args[0]);
//        Integer randomIndex = 0;
        Random random = Constant.randomArray[randomIndex];


        // 1. dataset 生成
        SinDatasetPreprocessRun.generateDataset();

        // 2. parameter 生成
        Integer userSize = ParameterGroupInitializeUtils.getUserSize(UserGroupGenerator.getUserAbsolutePath(datasetPath));
        GenerateParameters.generate(datasetPath, fileName, userSize, random);

        // 3. 执行
        DataSetRun.runDataSet(datasetPath, random);

//        // 4. 后处理
//        String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
//        String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "extract_result");
//        String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
//        PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//        PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);
    }
}
