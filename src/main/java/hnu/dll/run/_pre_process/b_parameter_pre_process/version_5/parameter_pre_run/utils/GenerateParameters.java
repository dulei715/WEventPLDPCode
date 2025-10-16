package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils;

import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.DiscreteParameterGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.LocationGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;

import java.util.Random;

public class GenerateParameters {
    public static void generate(String basicDataPath, String locationFileName, Integer userSize, Random random) {
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";

        UserGroupGenerator.generateUserToIndex(basicDataPath);

        try {
            Integer domainSize = (Integer) (ConfigureUtils.getIndependentData("DomainSize", "default", "default").getValue());
            LocationGroupGenerator.generateLocationToIndex(basicDataPath, locationFileName, domainSize, random);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DiscreteParameterGenerator.generateParametersForDataset(basicDataPath,
                privacyBudgetConfigVarianceName,
                windowSizeConfigVarianceName,
                userSize,
                Constant.GroupParameterDirectoryName,
                Constant.PersonalizedParameterFileName,
                random);

    }
}
