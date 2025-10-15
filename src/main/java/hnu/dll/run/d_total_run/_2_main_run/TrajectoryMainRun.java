package hnu.dll.run.d_total_run._2_main_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils.GenerateParameters;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.version_5.TrajectoryDataSetRun;

import java.util.Random;

public class TrajectoryMainRun {
    public static void main(String[] args) throws Exception {

        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.trajectoriesFilePath;
        String finalResultDirName = "1.trajectory_result";

//        Integer randomIndex = Integer.valueOf(args[0]);
        Integer randomIndex = 0;
        Random random = Constant.randomArray[randomIndex];

        Integer userSize = ParameterGroupInitializeUtils.getUserSize(UserGroupGenerator.getUserAbsolutePath(Constant.trajectoriesFilePath));
        // 1. parameter 生成
//        UserGroupGenerator.generateUserToIndex(Constant.trajectoriesFilePath);
//        UserGroupGenerator.generateUserIDType(Constant.trajectoriesFilePath);
//        UserGroupGenerator.generateUserToType(Constant.trajectoriesFilePath);
        GenerateParameters.generate(Constant.trajectoriesFilePath, userSize, random);

        // 上面步骤执行完才能执行这步
        // 2. 执行
        TrajectoryDataSetRun.runTrajectory(random);

//        // 3. 后处理
//        String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
//        String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "extract_result");
//        String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
//        PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//        PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);
    }
}
