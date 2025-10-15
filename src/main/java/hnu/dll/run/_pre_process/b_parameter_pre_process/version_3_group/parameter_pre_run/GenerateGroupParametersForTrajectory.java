package hnu.dll.run._pre_process.b_parameter_pre_process.version_3_group.parameter_pre_run;

import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.DiscreteParameterGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;

import java.util.Random;

public class GenerateGroupParametersForTrajectory {
    public static void generateParameters(Random random) {
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";
        String userTypeIDFileName = "userTypeID.txt";
        String basicParameterGenerationDirectoryName = "group_generated_parameters";
        String privacyBudgetFileNameForPersonalized = "typePrivacyBudgetFile.txt";
        String windowSizeFileNameForPersonalized = "typeWindowSizeFile.txt";
        DiscreteParameterGenerator.generateParametersForDataset(Constant.trajectoriesFilePath, privacyBudgetConfigVarianceName, windowSizeConfigVarianceName, userTypeIDFileName, basicParameterGenerationDirectoryName, privacyBudgetFileNameForPersonalized, windowSizeFileNameForPersonalized, random);

    }




    public static void main(String[] args) {
//        CatchSignal catchSignal = new CatchSignal();
//        catchSignal.startCatch();
        UserGroupGenerator.generateUserToIndex(Constant.trajectoriesFilePath);
        generateParameters(Constant.randomArray[0]);

    }
}
