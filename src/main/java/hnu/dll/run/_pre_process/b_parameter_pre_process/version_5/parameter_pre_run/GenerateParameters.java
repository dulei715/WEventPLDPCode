package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.utils.GroupShiftUtils;
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
            // 取值域大小 d 必须跟随当前实验：
            //   - 维度可扩展性实验（-Dexp.tag=fixed -Dexp.domain=d）用传入的 d；
            //   - 主实验用 DomainSize 的 default 标签（真实数据集仍会把大量位置压成 5 组）。
            // 若这里固定读 default 标签（=5），则 d > 5 的实验会被 getLocationToIndex
            // 随机压缩回 5 组，导致 d=5/10/25/50 的结果完全相同。
            Integer domainSize = ConfigureUtils.getIndependentSingleDomainSize(ConfigureUtils.getExperimentTag());
            if (domainSize == null) {
                domainSize = (Integer) (ConfigureUtils.getIndependentData("DomainSize", "default", "default").getValue());
            }
            LocationGroupGenerator.generateLocationToIndex(basicDataPath, locationFileName, domainSize, random);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (GroupShiftUtils.isEnabled()) {
            DiscreteParameterGenerator.generateGroupShiftParameters(
                    StringUtil.join(ConstantValues.FILE_SPLIT, basicDataPath, Constant.GroupParameterDirectoryName),
                    Constant.PersonalizedParameterFileName,
                    userSize);
            GroupShiftUtils.writeGroupShiftInfo(basicDataPath, userSize);
            return;
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
