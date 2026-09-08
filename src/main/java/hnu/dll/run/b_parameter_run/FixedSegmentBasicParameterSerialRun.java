package hnu.dll.run.b_parameter_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.ExperimentResultWrite;
import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.LocationGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run.a_mechanism_run.*;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import hnu.dll.run.utils.structs.UserParameter;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationAbsorption;
import hnu.dll.schemes.compare_scheme._1_non_personalized.impl.LDPPopulationDistribution;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPDistribution;
import hnu.dll.schemes.main_scheme._1_simple_scheme.BasicPLPMechanism;
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

import java.io.File;
import java.util.*;

public class FixedSegmentBasicParameterSerialRun {
    private String basicPath;
    private String dataTypeFileName;
    private Integer singleBatchSize;
    private Integer batchID = 0;
    private Double defaultPrivacyBudget;
    private Integer defaultWindowSize;
    private List<UserParameter> userParameterList;
    private File[] timeStampDataFiles;
    private int startFileIndex;
    private int endFileIndex;
    private Integer segmentID;

    private Random random;
    private Integer userSize;

    private Map<String, Mechanism> mechanismMap;
    private Set<String> dataType;
    private Map<Integer, Integer> userToIndexMap;
    private Map<String, String> locationToMappedStrMap;
    private String dynamicPrivacyBudgetBasicPath;
    private String dynamicWindowSizeBasicPath;
    private String userToTypeFilePath;


    public FixedSegmentBasicParameterSerialRun(String basicPath, String dataTypeFileName, Integer singleBatchSize,
                                               Double defaultPrivacyBudget, Integer defaultWindowSize, List<UserParameter> userParameterList, Integer userSize,
                                               File[] timeStampDataFiles, int startFileIndex, Random random, int endFileIndex,
                                               Integer segmentID) {
        this.basicPath = basicPath;
        this.dataTypeFileName = dataTypeFileName;
        this.singleBatchSize = singleBatchSize;
        this.defaultPrivacyBudget = defaultPrivacyBudget;
        this.defaultWindowSize = defaultWindowSize;
        this.userParameterList = userParameterList;
        this.timeStampDataFiles = timeStampDataFiles;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.segmentID = segmentID;
        this.random = random;
        this.userSize = userSize;
        initialize();
    }

    private void initialize() {
        this.userToIndexMap = UserGroupGenerator.getUserToIndexMap(basicPath);
        this.locationToMappedStrMap = LocationGroupGenerator.getLocationToMappedStrMap(basicPath);
        this.dataType = new HashSet<>(this.locationToMappedStrMap.values());
        userToTypeFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user_to_type.txt");

        this.mechanismMap = new TreeMap<>();
        /**
         * 0. NonPrivacySchemes
         */
        NonPrivacyMechanism nonPrivacyScheme = new NonPrivacyMechanism(dataType);
        this.mechanismMap.put(Constant.NonPrivacySchemeName, nonPrivacyScheme);

        /**
         * 1. NonPersonalizedSchemes
         */
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, defaultPrivacyBudget, defaultWindowSize, userSize, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.LPDSchemeName, lPD);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, defaultPrivacyBudget, defaultWindowSize, userSize, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.LPASchemeName, lPA);



        // todo:下面暂时置空
        List<Double> personalizedPrivacyBudgetList = UserParameter.extractPrivacyBudgetList(userParameterList);
        List<Integer> personalizedWindowSizeList = UserParameter.extractWindowSizeList(userParameterList);

        /**
         * 2. DirectSchemes
         */
            DirectPLPDistribution directPLPD = new DirectPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.DirectPLPDSchemeName, directPLPD);
        DirectPLPAbsorption directPLPA = new DirectPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.DirectPLPASchemeName, directPLPA);


        /**
         * 3. BasicSchemes
         */
        BasicPLPMechanism basicPLPD = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.BasicPLPDSchemeName, basicPLPD);
        BasicPLPMechanism basicPLPA = new BasicPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.BasicPLPASchemeName, basicPLPA);


        /**
         * 4. AblationSchemes
         */
        AblationPLPMechanismContainingGAOPSOnly ablationPLPDContainingGAOPSOnly = new PLPDContainingGAOPSOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.PLPDAblateContainingGAOPSchemeName, ablationPLPDContainingGAOPSOnly);
        AblationPLPMechanismContainingGAOPSOnly ablationPLPAContainingGAOPSOnly = new PLPAContainingGAOPSOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, this.random);
        this.mechanismMap.put(Constant.PLPAAblateContainingGAOPSchemeName, ablationPLPAContainingGAOPSOnly);

        AblationPLPMechanismContainingBAPUEOnly ablationPLPDContainingBAPUEOnlyModelPre = new PLPDContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, this.random);
        this.mechanismMap.put(Constant.PLPDAblateContainingBAPUESchemeNamePre, ablationPLPDContainingBAPUEOnlyModelPre);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPDContainingBAPUEOnlyModelPost = new PLPDContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, this.random);
        this.mechanismMap.put(Constant.PLPDAblateContainingBAPUESchemeNamePost, ablationPLPDContainingBAPUEOnlyModelPost);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPAContainingBAPUEOnlyModelPre = new PLPAContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, this.random);
        this.mechanismMap.put(Constant.PLPAAblateContainingBAPUESchemeNamePre, ablationPLPAContainingBAPUEOnlyModelPre);
        AblationPLPMechanismContainingBAPUEOnly ablationPLPAContainingBAPUEOnlyModelPost = new PLPAContainingBAPUEOnly(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, this.random);
        this.mechanismMap.put(Constant.PLPAAblateContainingBAPUESchemeNamePost, ablationPLPAContainingBAPUEOnlyModelPost);

        /**
         * 5. EnhancedSchemes
         */
        AdvancedPLPMechanism advancedPLPDPre = new PLPDPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, this.random);
        this.mechanismMap.put(Constant.AdvancedPLPDSchemeNamePre, advancedPLPDPre);
        AdvancedPLPMechanism advancedPLPDPost = new PLPDPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, this.random);
        this.mechanismMap.put(Constant.AdvancedPLPDSchemeNamePost, advancedPLPDPost);
        AdvancedPLPMechanism advancedPLPAPre = new PLPAPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPre, this.random);
        this.mechanismMap.put(Constant.AdvancedPLPASchemeNamePre, advancedPLPAPre);
        AdvancedPLPMechanism advancedPLPAPost = new PLPAPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, AdvancedPLPMechanism.ModelPost, this.random);
        this.mechanismMap.put(Constant.AdvancedPLPASchemeNamePost, advancedPLPAPost);


    }


    public List<ExperimentResult> runSegmentBatch() {

//        File dirFile = new File(basicPath, "runInput");
//        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        List<Integer> dataList;
        File file;
        List<List<Integer>> batchDataList = new ArrayList<>();
        List<ExperimentResult> experimentResultList = new ArrayList<>();
        ExperimentResult tempResult;

        String tempDataPath;
        List[] dynamicPrivacyListArray, dynamicWindowSizeListArray;

        List<List<Double>> remainBackwardPrivacyBudgetListBatchList = new ArrayList<>(), forwardPrivacyBudgetListBatchList = new ArrayList<>();
        List<List<Integer>> backwardWindowSizeListBatchList = new ArrayList<>(), forwardWindowSizeListBatchList = new ArrayList<>();

        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_output_time_cost", "p_"+String.valueOf(defaultPrivacyBudget).replace(".","-")+"_w_"+ defaultWindowSize, "segment_"+segmentID);
        File basicOutputFile = new File(basicOutputPathDir);
        if (!basicOutputFile.exists()) {
            basicOutputFile.mkdirs();
        }
        ExperimentResultWrite experimentResultWrite = new ExperimentResultWrite();
        String outputFilePath;
        for (int i = startFileIndex; i <= endFileIndex; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), this.userToIndexMap, this.locationToMappedStrMap);
            batchDataList.add(dataList);



            if ((i+1) % singleBatchSize == 0 || i == timeStampDataFiles.length - 1) {
                NonPrivacyMechanism nonPrivacyMechanism = (NonPrivacyMechanism) this.mechanismMap.get(Constant.NonPrivacySchemeName);
                CombinePair<ExperimentResult, List<Map<Integer, Double>>> nonPrivacySchemeResultPair = _0_NonPrivacyMechanismRun.runBatch(nonPrivacyMechanism, batchID, batchDataList);

                List<Map<Integer, Double>> rawPublicationBatchList = nonPrivacySchemeResultPair.getValue();
                tempResult = nonPrivacySchemeResultPair.getKey();
                experimentResultList.add(tempResult);
                // 执行各种机制
                /**
                 * 1. Non personalized Mechanisms
                 */
                tempResult = _1_NonPersonalizedWEventLDPMechanismRun.runBatch((LDPPopulationDistribution)mechanismMap.get(Constant.LPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _1_NonPersonalizedWEventLDPMechanismRun.runBatch((LDPPopulationAbsorption)mechanismMap.get(Constant.LPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                /**
                 * 2.  direct mechanisms
                 */
                tempResult = _2_PersonalizedWEventLDPDirectMechanismRun.runBatch((DirectPLPDistribution)mechanismMap.get(Constant.DirectPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _2_PersonalizedWEventLDPDirectMechanismRun.runBatch((DirectPLPAbsorption)mechanismMap.get(Constant.DirectPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                /**
                 * 3. basic mechanisms
                 */
                tempResult = _3_PersonalizedWEventLDPBasicMechanismRun.runBatch((BasicPLPDistribution)mechanismMap.get(Constant.BasicPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _3_PersonalizedWEventLDPBasicMechanismRun.runBatch((BasicPLPAbsorption)mechanismMap.get(Constant.BasicPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                /**
                 * 4. ablation mechanisms
                 */
                tempResult = _4_1_PersonalizedWEventLDPAblateContainingOPSMechanismRun.runBatch((AblationPLPMechanismContainingGAOPSOnly)mechanismMap.get(Constant.PLPDAblateContainingGAOPSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_1_PersonalizedWEventLDPAblateContainingOPSMechanismRun.runBatch((AblationPLPMechanismContainingGAOPSOnly)mechanismMap.get(Constant.PLPAAblateContainingGAOPSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _4_2_PersonalizedWEventLDPAblateContainingBAPUEMechanismRun.runBatch((AblationPLPMechanismContainingBAPUEOnly)mechanismMap.get(Constant.PLPDAblateContainingBAPUESchemeNamePre), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_2_PersonalizedWEventLDPAblateContainingBAPUEMechanismRun.runBatch((AblationPLPMechanismContainingBAPUEOnly)mechanismMap.get(Constant.PLPDAblateContainingBAPUESchemeNamePost), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_2_PersonalizedWEventLDPAblateContainingBAPUEMechanismRun.runBatch((AblationPLPMechanismContainingBAPUEOnly)mechanismMap.get(Constant.PLPAAblateContainingBAPUESchemeNamePre), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_2_PersonalizedWEventLDPAblateContainingBAPUEMechanismRun.runBatch((AblationPLPMechanismContainingBAPUEOnly)mechanismMap.get(Constant.PLPAAblateContainingBAPUESchemeNamePost), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                /**
                 * 5. enhanced mechanisms
                 */
                tempResult = _5_PersonalizedWEventLDPAdvancedMechanismRun.runBatch((AdvancedPLPMechanism)mechanismMap.get(Constant.AdvancedPLPDSchemeNamePre), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _5_PersonalizedWEventLDPAdvancedMechanismRun.runBatch((AdvancedPLPMechanism)mechanismMap.get(Constant.AdvancedPLPDSchemeNamePost), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _5_PersonalizedWEventLDPAdvancedMechanismRun.runBatch((AdvancedPLPMechanism)mechanismMap.get(Constant.AdvancedPLPASchemeNamePre), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _5_PersonalizedWEventLDPAdvancedMechanismRun.runBatch((AdvancedPLPMechanism)mechanismMap.get(Constant.AdvancedPLPASchemeNamePost), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                // write result
                outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicOutputPathDir, "batch_"+batchID+".txt");
                experimentResultWrite.startWriting(outputFilePath);
                experimentResultWrite.write(experimentResultList);
                experimentResultWrite.endWriting();
                System.out.printf("Finish Writing result in batch %d in thread %d in segment %d\n", batchID, Thread.currentThread().getId(), segmentID);

                ++batchID;
                batchDataList.clear();
                experimentResultList.clear();
            }
        }

        return experimentResultList;
    }


    public static void main(String[] args) {
//        String basicPath = Constant.checkInFilePath;
//        String dataTypeFileName = "country.txt";
//        Integer singleBatchSize = 2;
//        Double privacyBudget = 0.5;
//        Integer windowSize = 10;
//        Runnable runnable = new FixedSegmentParameterRun(basicPath, dataTypeFileName, singleBatchSize, privacyBudget, windowSize);
//        Thread thread = new Thread(runnable);
//        thread.start();
    }

    public static void main1(String[] args) {
        System.out.println("Start Writing...");
        String basicPath = Constant.CheckInFilePath;
        Double privacyBudget = 0.5;
        Integer windowSize = 10;
        Integer batchID = 1;
        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "output", "p_"+String.valueOf(privacyBudget).replace(".","-")+"_w_"+windowSize);
        File basicOutputFile = new File(basicOutputPathDir);
        if (!basicOutputFile.exists()) {
            basicOutputFile.mkdirs();
        }
        ExperimentResultWrite experimentResultWrite = new ExperimentResultWrite();
        String outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicOutputPathDir, "batch_"+batchID+".txt");
        experimentResultWrite.startWriting(outputFilePath);
    }
}
