package hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_pre_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.DiscreteParameterGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;

public class GenerateGroupParametersForCheckIn {
    public static void generateParameters() {
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";
        String userTypeIDFileName = "userTypeID.txt";
        String basicParameterGenerationDirectoryName = "group_generated_parameters";
        String userPrivacyFileNameForPersonalized = "typePrivacyBudgetFile.txt";
        String windowSizeFileNameForPersonalized = "typeWindowSizeFile.txt";
//        DiscreteParameterGenerator.generateParametersForCheckIn(privacyBudgetConfigVarianceName, windowSizeConfigVarianceName, userTypeIDFileName, basicParameterGenerationDirectoryName, userPrivacyFileNameForPersonalized, windowSizeFileNameForPersonalized);

    }
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        UserGroupGenerator.generateUserToIndex(Constant.checkInFilePath);
//        UserGroupGenerator.generateUserIDType(Constant.checkInFilePath);
//        UserGroupGenerator.generateUserToType(Constant.checkInFilePath);
        generateParameters();
    }
}
