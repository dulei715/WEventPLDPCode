package hnu.dll.run.d_total_run._4_added_run._2_3_containing_ldp_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_pre_run.GenerateGroupParametersForCheckIn;
import hnu.dll.run.c_dataset_run.version_3.basic_run.CheckInDataSetRun;

public class CheckInContainingLDPRun {
    public static void main(String[] args) throws Exception {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        String datasetPath = Constant.checkInFilePath;
        String finalResultDirName = "2.checkIn_containing_ldp_result";


        // 1. parameter 生成
        UserGroupGenerator.generateUserIDType(Constant.checkInFilePath);
        UserGroupGenerator.generateUserToType(Constant.checkInFilePath);
        GenerateGroupParametersForCheckIn.generateParameters();

        // 2. 执行
        CheckInDataSetRun.runCheckInContainingSimpleLDP();

//        // 3. 后处理
//        String rawDataDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "group_output");
//        String extractResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, "extract_result");
//        String finalResultDir = StringUtil.join(ConstantValues.FILE_SPLIT, datasetPath, finalResultDirName);
//        PostProcessUtils.combineAndExtractCombineResult(rawDataDir, extractResultDir);
//        PostProcessUtils.furtherCombine(extractResultDir, finalResultDir);

    }
}
