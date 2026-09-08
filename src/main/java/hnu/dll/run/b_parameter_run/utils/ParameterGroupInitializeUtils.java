package hnu.dll.run.b_parameter_run.utils;

import cn.edu.dll.io.read.BasicRead;
import hnu.dll._config.Constant;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationAbsorption;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationDistribution;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPDistribution;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPAbsorption;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPDistribution;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.AblationPLPMechanismContainingBAPUEOnly;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.impl.PLPAContainingBAPUEOnly;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ba_pue_only.impl.PLPDContainingBAPUEOnly;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ga_ops_only.AblationPLPMechanismContainingGAOPSOnly;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ga_ops_only.impl.PLPAContainingGAOPSOnly;
import hnu.dll.schemes.main_scheme._2_ablation_scheme.containing_ga_ops_only.impl.PLPDContainingGAOPSOnly;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.impl.PLPAPlus;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.impl.PLPDPlus;

import java.util.*;

public class ParameterGroupInitializeUtils {
    public static Map<String, Mechanism> getInitializedSchemeMap(Set<String> dataType, Integer userSize,
                                                                 Double staticPrivacyBudget, Integer staticWindowSize,
                                                                 List<Double> personalizedPrivacyBudgetList, List<Integer> personalizedWindowSizeList,
                                                                 Random random) {
        Map<String, Mechanism> resultMap = new HashMap<>();

        // 0. non private scheme
        NonPrivacyMechanism nonPrivacyMechanism = new NonPrivacyMechanism(dataType);
        resultMap.put(Constant.NonPrivacySchemeName, nonPrivacyMechanism);

        // 1. non personalized schemes
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, staticPrivacyBudget, staticWindowSize, userSize, Constant.PopulationLowerBound, random);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, staticPrivacyBudget, staticWindowSize, userSize, Constant.PopulationLowerBound, random);
        resultMap.put(Constant.LPDSchemeName, lPD);
        resultMap.put(Constant.LPASchemeName, lPA);

        // 2. direct personalized schemes
        DirectPLPDistribution directPLPD = new DirectPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        DirectPLPAbsorption directPLPA = new DirectPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        resultMap.put(Constant.DirectPLPDSchemeName, directPLPD);
        resultMap.put(Constant.DirectPLPASchemeName, directPLPA);

        // 3. basic personalized schemes
        BasicPLPDistribution basicPLPD = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        BasicPLPAbsorption basicPLPA = new BasicPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        resultMap.put(Constant.BasicPLPDSchemeName, basicPLPD);
        resultMap.put(Constant.BasicPLPASchemeName, basicPLPA);

        // 4. ablation schemes
        AblationPLPMechanismContainingGAOPSOnly ablationPLPDContainingGAOPSOnly = new PLPDContainingGAOPSOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        resultMap.put(Constant.PLPDAblateContainingGAOPSchemeName, ablationPLPDContainingGAOPSOnly);
        AblationPLPMechanismContainingGAOPSOnly ablationPLPAContainingGAOPSOnly = new PLPAContainingGAOPSOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        resultMap.put(Constant.PLPAAblateContainingGAOPSchemeName, ablationPLPAContainingGAOPSOnly);

        AblationPLPMechanismContainingBAPUEOnly ablationPLPDContainingBAPUEOnlyModelPre = new PLPDContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, random);
        resultMap.put(Constant.PLPDAblateContainingBAPUESchemeNamePre, ablationPLPDContainingBAPUEOnlyModelPre);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPDContainingBAPUEOnlyModelPost = new PLPDContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, random);
        resultMap.put(Constant.PLPDAblateContainingBAPUESchemeNamePost, ablationPLPDContainingBAPUEOnlyModelPost);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPAContainingBAPUEOnlyModelPre = new PLPAContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, random);
        resultMap.put(Constant.PLPAAblateContainingBAPUESchemeNamePre, ablationPLPAContainingBAPUEOnlyModelPre);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPAContainingBAPUEOnlyModelPost = new PLPAContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, random);
        resultMap.put(Constant.PLPAAblateContainingBAPUESchemeNamePost, ablationPLPAContainingBAPUEOnlyModelPost);

        // 5. EnhancedSchemes
        AdvancedPLPMechanism advancedPLPDPre = new PLPDPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, random);
        resultMap.put(Constant.AdvancedPLPDSchemeNamePre, advancedPLPDPre);
        AdvancedPLPMechanism advancedPLPDPost = new PLPDPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, random);
        resultMap.put(Constant.AdvancedPLPDSchemeNamePost, advancedPLPDPost);
        AdvancedPLPMechanism advancedPLPAPre = new PLPAPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, random);
        resultMap.put(Constant.AdvancedPLPASchemeNamePre, advancedPLPAPre);
        AdvancedPLPMechanism advancedPLPAPost = new PLPAPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, random);
        resultMap.put(Constant.AdvancedPLPASchemeNamePost, advancedPLPAPost);

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
