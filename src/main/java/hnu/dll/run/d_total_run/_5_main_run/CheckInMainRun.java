package hnu.dll.run.d_total_run._5_main_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils.GenerateParameters;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.version_5.DataSetRun;

import java.util.Random;

public class CheckInMainRun {
    public static void main(String[] args) throws Exception {

        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.CheckInFilePath;
        String locationFileName = Constant.CheckInLocationFileName;

        Integer randomIndex = Integer.valueOf(args[0]);
//        Integer randomIndex = 0;
        Random random = Constant.randomArray[randomIndex];

        Integer userSize = ParameterGroupInitializeUtils.getUserSize(UserGroupGenerator.getUserAbsolutePath(datasetPath));
        // 1. parameter 生成
        GenerateParameters.generate(datasetPath, locationFileName, userSize, random);

        // 上面步骤执行完才能执行这步
        // 2. 执行
        DataSetRun.runDataSet(datasetPath, random);

    }
}
