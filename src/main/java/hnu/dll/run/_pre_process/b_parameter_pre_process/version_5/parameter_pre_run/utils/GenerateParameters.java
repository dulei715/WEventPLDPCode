package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils;

import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.DiscreteParameterGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;

import java.util.Random;

public class GenerateParameters {
    public static void generate(String basicDataPath, Integer userSize, Random random) {
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";
//        String basicParameterGenerationDirectoryName = "group_generated_parameters";
//        String personalizedParameterFileName = "personalizedParameterFile.txt";

        UserGroupGenerator.generateUserToIndex(basicDataPath);
        DiscreteParameterGenerator.generateParametersForDataset(basicDataPath,
                privacyBudgetConfigVarianceName,
                windowSizeConfigVarianceName,
                userSize,
                Constant.GroupParameterDirectoryName,
                Constant.PersonalizedParameterFileName,
                random);

    }
}
