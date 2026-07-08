package hnu.dll.run.b_parameter_run.utils;

import cn.edu.dll.io.read.BasicRead;
import hnu.dll._config.Constant;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationAbsorption;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationDistribution;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPDistribution;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateOPSAbsorptionPlus;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateOPSDistributionPlus;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPAbsorption;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPDistribution;

import java.util.*;

public class ParameterGroupInitializeUtils {
    public static Map<String, Mechanism> getInitializedSchemeMap(Set<String> dataType, Integer userSize,
                                                                 Double staticPrivacyBudget, Integer staticWindowSize,
                                                                 List<Double> personalizedPrivacyBudgetList, List<Integer> personalziedWindowSizeList,
                                                                 Random random) {
        Map<String, Mechanism> resultMap = new HashMap<>();

        // 0. non private scheme
        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);
        resultMap.put(Constant.NonPrivacySchemeName, nonPrivacyMechanism);

        // 1. non personalized schemes
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, staticPrivacyBudget, staticWindowSize, userSize, Constant.PopulationLowerBound, random);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, staticPrivacyBudget, staticWindowSize, userSize, random);
        resultMap.put(Constant.LPDSchemeName, lPD);
        resultMap.put(Constant.LPASchemeName, lPA);


        DirectPLPDistribution basePLPD = new DirectPLPDistribution(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, Constant.PopulationLowerBound, random);
        DirectPLPAbsorption basePLPA = new DirectPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        resultMap.put(Constant.DirectPLPDSchemeName, basePLPD);
        resultMap.put(Constant.DirectPLPASchemeName, basePLPA);

        AblateOPSDistributionPlus AblateOPSPLPD = new AblateOPSDistributionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, Constant.PopulationLowerBound, random);
        AblateOPSAbsorptionPlus AblateOPSPLPA = new AblateOPSAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        BasicPLPDistribution AblateRPPLPD = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, Constant.PopulationLowerBound, random);
        BasicPLPAbsorption AblateRPPLPA = new BasicPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        resultMap.put(Constant.BasicPLPDSchemeName, AblateRPPLPD);
        resultMap.put(Constant.BasicPLPASchemeName, AblateRPPLPA);

        return resultMap;
    }

    public static String toPathName(Double privacyBudget) {
        String valueStr = String.valueOf(privacyBudget).replace(".", "-");
        return "p_".concat(valueStr);
    }
    public static String toPathName(Integer windowSize) {
        String valueStr = String.valueOf(windowSize);
        return "w_".concat(valueStr);
    }

    public static String toPathName(Double privacyBudget, Integer windowSize) {
        String budgetPathName = toPathName(privacyBudget);
        String windowSizePathName = toPathName(windowSize);
        return budgetPathName.concat("_").concat(windowSizePathName);
    }

    public static Integer getUserSize(String userFilePath) {
        BasicRead basicRead = new BasicRead(",");
        basicRead.startReading(userFilePath);
        List<String> data = basicRead.readAllWithoutLineNumberRecordInFile();
        return data.size();
    }

}
