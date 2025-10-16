package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run;

import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_pre_run.utils.GenerateParameters;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;

@Deprecated
public class GenerateGroupParametersForTrajectory {





    public static void main(String[] args) {
//        CatchSignal catchSignal = new CatchSignal();
//        catchSignal.startCatch();
        Integer userSize = ParameterGroupInitializeUtils.getUserSize(UserGroupGenerator.getUserAbsolutePath(Constant.TrajectoriesFilePath));

        GenerateParameters.generate(Constant.TrajectoriesFilePath, Constant.TrajectoryLocationFileName, userSize, Constant.randomArray[0]);

    }
}
