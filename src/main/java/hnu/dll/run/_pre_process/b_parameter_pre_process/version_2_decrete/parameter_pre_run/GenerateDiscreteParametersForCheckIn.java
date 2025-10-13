package hnu.dll.run._pre_process.b_parameter_pre_process.version_2_decrete.parameter_pre_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_2_decrete.parameter_generator.DiscreteParameterGenerator;

public class GenerateDiscreteParametersForCheckIn {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
        String privacyBudgetConfigVarianceName = "default";
        String windowSizeConfigVarianceName = "default";
        String userTypeIDFileName = "user.txt";  // 这里不对user进行分类
        String basicParameterGenerationDirectoryName = "generated_parameters";
        String userPrivacyFileNameForPersonalized = "userPrivacyBudgetFile.txt";
        String windowSizeFileNameForPersonalized = "userWindowSizeFile.txt";
        DiscreteParameterGenerator.generateParametersForCheckIn(privacyBudgetConfigVarianceName, windowSizeConfigVarianceName, userTypeIDFileName, basicParameterGenerationDirectoryName, userPrivacyFileNameForPersonalized, windowSizeFileNameForPersonalized);
    }
}
