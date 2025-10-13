package hnu.dll.utils.run;

import cn.edu.dll.struct.pair.BasicPair;

public class ParameterUtils {
    public static BasicPair<Double, Integer> extractBudgetWindowSizeParametersAccordingFileDirName(String dirName) {
        String[] paramStringArray = dirName.split("_");
        Double privacyBudget = Double.valueOf(paramStringArray[1].replace("-", "."));
        Integer windowSize = Integer.valueOf(paramStringArray[3]);
        BasicPair<Double, Integer> result = new BasicPair<>(privacyBudget, windowSize);
        return result;
    }
}
