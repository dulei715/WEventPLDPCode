package hnu.dll.run.b_parameter_run.version_3.utils;

import cn.edu.dll.io.read.BasicRead;
import ecnu.dll._config.Constant;
import ecnu.dll.schemes._basic_struct.Mechanism;
import ecnu.dll.schemes.basic_scheme.NonPrivacyMechanism;
import ecnu.dll.schemes.compared_scheme.w_event_dp.BudgetAbsorption;
import ecnu.dll.schemes.compared_scheme.w_event_dp.BudgetDistribution;
import ecnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetAbsorption;
import ecnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetDistribution;
import ecnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetAbsorption;
import ecnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetDistribution;
import ecnu.dll.utils.io.ListReadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterGroupInitializeUtils {
    public static Map<String, Mechanism> getInitializedSchemeMap(List<String> dataType, Integer userSize,
                                                                 Double staticPrivacyBudget, Integer staticWindowSize,
                                                                 List<Double> personalizedPrivacyBudgetList, List<Integer> personalziedWindowSizeList) {
        Map<String, Mechanism> resultMap = new HashMap<>();

        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);
        resultMap.put(Constant.NonPrivacySchemeName, nonPrivacyMechanism);

        BudgetDistribution budgetDistributionMechanism = new BudgetDistribution(dataType, staticPrivacyBudget, staticWindowSize);
        BudgetAbsorption budgetAbsorptionMechanism = new BudgetAbsorption(dataType, staticPrivacyBudget, staticWindowSize);
        resultMap.put(Constant.BudgetDistributionSchemeName, budgetDistributionMechanism);
        resultMap.put(Constant.BudgetAbsorptionSchemeName, budgetAbsorptionMechanism);

        PersonalizedBudgetDistribution personalizedBudgetDistributionScheme = new PersonalizedBudgetDistribution(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList);
        PersonalizedBudgetAbsorption personalizedBudgetAbsorptionScheme = new PersonalizedBudgetAbsorption(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList);
        resultMap.put(Constant.PersonalizedBudgetDistributionSchemeName, personalizedBudgetDistributionScheme);
        resultMap.put(Constant.PersonalizedBudgetAbsorptionSchemeName, personalizedBudgetAbsorptionScheme);

        DynamicPersonalizedBudgetDistribution dynamicPersonalizedBudgetDistribution = new DynamicPersonalizedBudgetDistribution(dataType, userSize);
        DynamicPersonalizedBudgetAbsorption dynamicPersonalizedBudgetAbsorption = new DynamicPersonalizedBudgetAbsorption(dataType, userSize);
        resultMap.put(Constant.DynamicPersonalizedBudgetDistributionSchemeName, dynamicPersonalizedBudgetDistribution);
        resultMap.put(Constant.DynamicPersonalizedBudgetAbsorptionSchemeName, dynamicPersonalizedBudgetAbsorption);

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

    public static List<Double> getTypePrivacyBudgetFromFileAndFill(String typePrivacyBudgetFilePath, String userToTypeFilePath) {
        List<Double> privacyBudgetList = new ArrayList<>();
        List<String> typeValueList, userToTypeList;
        typeValueList = ListReadUtils.readAllDataList(typePrivacyBudgetFilePath, ",");
        userToTypeList = ListReadUtils.readAllDataList(userToTypeFilePath, ",");
        Map<String, String> typeMap = new HashMap<>();
        for (String str : typeValueList) {
            int splitIndex = str.indexOf(",");
            typeMap.put(str.substring(0, splitIndex), str.substring(splitIndex+1));
        }
        String[] splitStr;
        for (String str : userToTypeList) {
            splitStr = str.split(",");
            String typeValue = typeMap.get(splitStr[1]);
            privacyBudgetList.add(Double.valueOf(typeValue));
        }
        return privacyBudgetList;
    }
    public static List<Double>[] getBackForwardPrivacyBudgetFromFile(String typePrivacyBudgetFilePath, String userToTypeFilePath) {
        List<Double> remainBackwardPrivacyBudgetList = new ArrayList<>();
        List<Double> forwardPrivacyBudgetList = new ArrayList<>();
        List<String> userToTypeList = ListReadUtils.readAllDataList(userToTypeFilePath, ",");
        List<String> typeValueList = ListReadUtils.readAllDataList(typePrivacyBudgetFilePath, ",");
        Map<String, String> typeMap = new HashMap<>();
        for (String str : typeValueList) {
            int splitIndex = str.indexOf(",");
            typeMap.put(str.substring(0, splitIndex), str.substring(splitIndex+1));
        }
        String[] splitStr;
        for (String str : userToTypeList) {
            String key = str.split(",")[1];
            splitStr = typeMap.get(key).split(",");
            remainBackwardPrivacyBudgetList.add(Double.valueOf(splitStr[0]));
            forwardPrivacyBudgetList.add(Double.valueOf(splitStr[1]));
        }
        return new List[]{remainBackwardPrivacyBudgetList, forwardPrivacyBudgetList};
    }

    /**
     * 下标：
     * 0 : backward
     * 1 : forward
     * @return
     */
    public static List<Integer> getTypeWindowSizeFromFileAndFill(String typeWindowSizeFilePath, String userToTypeFilePath) {
        List<Integer> windowSizeList = new ArrayList<>();
        List<String> typeValueList, userToTypeList;
        typeValueList = ListReadUtils.readAllDataList(typeWindowSizeFilePath, ",");
        userToTypeList = ListReadUtils.readAllDataList(userToTypeFilePath, ",");
        Map<String, String> typeMap = new HashMap<>();
        for (String str : typeValueList) {
            int splitIndex = str.indexOf(",");
            typeMap.put(str.substring(0, splitIndex), str.substring(splitIndex+1));
        }
        String[] splitStr;
        for (String str : userToTypeList) {
            splitStr = str.split(",");
            String typeValue = typeMap.get(splitStr[1]);
            windowSizeList.add(Integer.valueOf(typeValue));
        }
        return windowSizeList;
    }
    public static List<Integer>[] getBackForwardWindowSizeFromFile(String typeWindowSizeFilePath, String userToTypeFilePath) {
        List<Integer> backwardWindowSizeList = new ArrayList<>();
        List<Integer> forwardWindowSizeList = new ArrayList<>();
        List<String> userToTypeList = ListReadUtils.readAllDataList(userToTypeFilePath, ",");
        List<String> typeValueList = ListReadUtils.readAllDataList(typeWindowSizeFilePath, ",");
        Map<String, String> typeMap = new HashMap<>();
        for (String str : typeValueList) {
            int splitIndex = str.indexOf(",");
            typeMap.put(str.substring(0, splitIndex), str.substring(splitIndex+1));
        }
        String[] splitStr;
        for (String str : userToTypeList) {
            String key = str.split(",")[1];
            splitStr = typeMap.get(key).split(",");
            backwardWindowSizeList.add(Integer.valueOf(splitStr[0]));
            forwardWindowSizeList.add(Integer.valueOf(splitStr[1]));
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
