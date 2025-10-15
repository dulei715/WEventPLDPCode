package hnu.dll.run.b_parameter_run.utils;

import cn.edu.dll.io.read.BasicRead;
import hnu.dll._config.Constant;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationAbsorption;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationDistribution;
import hnu.dll.schemes.compare_scheme._2_our_baseline.impl.BaselinePLPAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline.impl.BaselinePLPDistribution;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateOPSAbsorptionPlus;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateOPSDistributionPlus;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateRePerturbAbsorptionPlus;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateRePerturbDistributionPlus;
import hnu.dll.utils.io.ListReadUtils;

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
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, staticPrivacyBudget, staticWindowSize, userSize, random);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, staticPrivacyBudget, staticWindowSize, userSize, random);
        resultMap.put(Constant.LPDSchemeName, lPD);
        resultMap.put(Constant.LPASchemeName, lPA);


        BaselinePLPDistribution basePLPD = new BaselinePLPDistribution(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        BaselinePLPAbsorption basePLPA = new BaselinePLPAbsorption(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        resultMap.put(Constant.BasePLPDSchemeName, basePLPD);
        resultMap.put(Constant.BasePLPASchemeName, basePLPA);

        AblateOPSDistributionPlus AblateOPSPLPD = new AblateOPSDistributionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        AblateOPSAbsorptionPlus AblateOPSPLPA = new AblateOPSAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        AblateRePerturbDistributionPlus AblateRPPLPD = new AblateRePerturbDistributionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        AblateRePerturbAbsorptionPlus AblateRPPLPA = new AblateRePerturbAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalziedWindowSizeList, random);
        resultMap.put(Constant.AblateRPPLPDSchemeName, AblateRPPLPD);
        resultMap.put(Constant.AblateRPPLPASchemeName, AblateRPPLPA);

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
