package hnu.dll.run.b_parameter_run.version_3.utils;

import cn.edu.dll.io.read.BasicRead;
import hnu.dll._config.Constant;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;

import java.util.*;

public class ParameterInitializeUtils {
    public static Map<String, Mechanism> getInitializedSchemeMap(Set<String> dataType, Integer userSize,
                                                                 Double staticPrivacyBudget, Integer staticWindowSize,
                                                                 List<Double> personalizedPrivacyBudgetList, List<Integer> personalziedWindowSizeList) {
        Map<String, Mechanism> resultMap = new HashMap<>();

        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);
        resultMap.put(Constant.NonPrivacySchemeName, nonPrivacyMechanism);

        BudgetDistribution budgetDistributionMechanism = new BudgetDistribution(dataType, staticPrivacyBudget, staticWindowSize);
        BudgetAbsorption budgetAbsorptionMechanism = new BudgetAbsorption(dataType, staticPrivacyBudget, staticWindowSize);
        resultMap.put(Constant.LBDSchemeName, budgetDistributionMechanism);
        resultMap.put(Constant.LBASchemeName, budgetAbsorptionMechanism);

        PersonalizedBudgetDistribution personalizedBudgetDistributionScheme = new PersonalizedBudgetDistribution(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList);
        PersonalizedBudgetAbsorption personalizedBudgetAbsorptionScheme = new PersonalizedBudgetAbsorption(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList);
        resultMap.put(Constant.PLPDBasicSchemeName, personalizedBudgetDistributionScheme);
        resultMap.put(Constant.PLPABasicSchemeName, personalizedBudgetAbsorptionScheme);

        DynamicPersonalizedBudgetDistribution dynamicPersonalizedBudgetDistribution = new DynamicPersonalizedBudgetDistribution(dataType, userSize);
        DynamicPersonalizedBudgetAbsorption dynamicPersonalizedBudgetAbsorption = new DynamicPersonalizedBudgetAbsorption(dataType, userSize);
        resultMap.put(Constant.EnhancedPBDSchemeName, dynamicPersonalizedBudgetDistribution);
        resultMap.put(Constant.EnhancedPBASchemeName, dynamicPersonalizedBudgetAbsorption);

        return resultMap;
    }

    public static String toPathName(Double privacyBudget) {
        String valueStr = String.valueOf(privacyBudget).replace(".", "-");
        return "budget_".concat(valueStr);
    }
    public static String toPathName(Integer windowSize) {
        String valueStr = String.valueOf(windowSize);
        return "w_size_".concat(valueStr);
    }

    public static List<Double> getPrivacyBudgetFromFile(String filePath) {
        List<Double> privacyBudgetList = new ArrayList<>();
        List<String> dataStrList;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        dataStrList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String[] splitStr;
        for (String str : dataStrList) {
            splitStr = basicRead.split(str);
            privacyBudgetList.add(Double.valueOf(splitStr[1]));
        }
        return privacyBudgetList;
    }
    public static List<Double>[] getBackForwardPrivacyBudgetFromFile(String filePath) {
        List<Double> remainBackwardPrivacyBudgetList = new ArrayList<>();
        List<Double> forwardPrivacyBudgetList = new ArrayList<>();
        List<String> dataStrList;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        dataStrList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String[] splitStr;
        for (String str : dataStrList) {
            splitStr = basicRead.split(str);
            remainBackwardPrivacyBudgetList.add(Double.valueOf(splitStr[1]));
            forwardPrivacyBudgetList.add(Double.valueOf(splitStr[2]));
        }
        return new List[]{remainBackwardPrivacyBudgetList, forwardPrivacyBudgetList};
    }

    /**
     * 下标：
     * 0 : backward
     * 1 : forward
     * @param filePath
     * @return
     */
    public static List<Integer> getWindowSizeFromFile(String filePath) {
        List<Integer> windowSizeList = new ArrayList<>();
        List<String> dataStrList;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        dataStrList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String[] splitStr;
        for (String str : dataStrList) {
            splitStr = basicRead.split(str);
            windowSizeList.add(Integer.valueOf(splitStr[1]));
        }
        return windowSizeList;
    }
    public static List<Integer>[] getBackForwardWindowSizeFromFile(String filePath) {
        List<Integer> backwardWindowSizeList = new ArrayList<>();
        List<Integer> forwardWindowSizeList = new ArrayList<>();
        List<String> dataStrList;
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(filePath);
        dataStrList = basicRead.readAllWithoutLineNumberRecordInFile();
        basicRead.endReading();
        String[] splitStr;
        for (String str : dataStrList) {
            splitStr = basicRead.split(str);
            backwardWindowSizeList.add(Integer.valueOf(splitStr[1]));
            forwardWindowSizeList.add(Integer.valueOf(splitStr[2]));
        }
        return new List[]{backwardWindowSizeList, forwardWindowSizeList};
    }

    public static Integer getUserSize(String userFilePath) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(userFilePath);
        List<String> data = basicRead.readAllWithoutLineNumberRecordInFile();
        return data.size();
    }

}
