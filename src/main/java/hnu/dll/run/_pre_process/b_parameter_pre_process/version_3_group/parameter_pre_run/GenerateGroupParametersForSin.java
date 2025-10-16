package hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_pre_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;

public class GenerateGroupParametersForSin {
    public static void generateParameters() {
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";
        String userTypeIDFileName = "userTypeID.txt";
        String basicParameterGenerationDirectoryName = "group_generated_parameters";
        String userPrivacyFileNameForPersonalized = "typePrivacyBudgetFile.txt";
        String windowSizeFileNameForPersonalized = "typeWindowSizeFile.txt";
//        DiscreteParameterGenerator.generateParametersForDataset(Constant.sinFilePath, privacyBudgetConfigVarianceName, windowSizeConfigVarianceName, userTypeIDFileName, basicParameterGenerationDirectoryName, userPrivacyFileNameForPersonalized, windowSizeFileNameForPersonalized);

    }
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        // 1. 生成  user_to_index.txt 到 basic_info中
        UserGroupGenerator.generateUserToIndex(Constant.SinFilePath);
//        UserGroupGenerator.generateUserIDType(Constant.sinFilePath);
//        UserGroupGenerator.generateUserToType(Constant.sinFilePath);
        // 2. 生成实验参数到 group_generated_parameters
        generateParameters();
    }
}
