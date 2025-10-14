package hnu.dll.run.b_parameter_run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.ExperimentResultWrite;
import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.run.a_mechanism_run.*;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
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
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationAbsorptionPlus;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationDistributionPlus;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class FixedSegmentBasicParameterParallelRun implements Runnable {
    private String basicPath;
    private String dataTypeFileName;
    private Integer singleBatchSize;
    private Integer batchID = 0;
    private Double privacyBudget;
    private Integer windowSize;
    private File[] timeStampDataFiles;
    private int startFileIndex;
    private int endFileIndex;
    private Integer segmentID;

    private Random random;
    private Integer userSize;

    private Map<String, Mechanism> mechanismMap;
    private Set<String> dataType;
    private String dynamicPrivacyBudgetBasicPath;
    private String dynamicWindowSizeBasicPath;
    private String userToTypeFilePath;
    private CountDownLatch latch;
    private CountDownLatch innerLatch;


    public FixedSegmentBasicParameterParallelRun(String basicPath, String dataTypeFileName,
                                                 Integer singleBatchSize,
                                                 Double privacyBudget, Integer windowSize, Integer userSize,
                                                 File[] timeStampDataFiles,
                                                 int startFileIndex, int endFileIndex, Integer segmentID,
                                                 Random random,
                                                 CountDownLatch latch, CountDownLatch innerLatch) {
        this.basicPath = basicPath;
        this.dataTypeFileName = dataTypeFileName;
        this.singleBatchSize = singleBatchSize;
        this.privacyBudget = privacyBudget;
        this.windowSize = windowSize;
        this.timeStampDataFiles = timeStampDataFiles;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.segmentID = segmentID;
        this.userSize = userSize;
        this.random = random;
        this.latch = latch;
        this.innerLatch = innerLatch;
        initialize();
    }

    private void initialize() {
        dataType = DatasetParameterUtils.getDataTypeSet(this.basicPath, this.dataTypeFileName);
        this.mechanismMap = new TreeMap<>();

        /**
         * 0. NonPrivacySchemes
         */
        NonPrivacyMechanism nonPrivacyScheme = new NonPrivacyMechanism(dataType);
        this.mechanismMap.put(Constant.NonPrivacySchemeName, nonPrivacyScheme);

        /**
         * 1. NonPersonalizedSchemes
         */
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, privacyBudget, windowSize, userSize, random);
        this.mechanismMap.put(Constant.LPDSchemeName, lPD);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, privacyBudget, windowSize, userSize, random);
        this.mechanismMap.put(Constant.LPASchemeName, lPA);



        // todo:下面暂时置空
//        List<Double> privacyBudgetList = ParameterGroupInitializeUtils.getTypePrivacyBudgetFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicPrivacyBudgetBasicPath, "typePrivacyBudgetFile.txt"), userToTypeFilePath);
//        List<Integer> windowSizeList = ParameterGroupInitializeUtils.getTypeWindowSizeFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicWindowSizeBasicPath, "typeWindowSizeFile.txt"), userToTypeFilePath);
        List<Double> privacyBudgetList = null;
        List<Integer> windowSizeList = null;

        /**
         * 2. BaselineSchemes
         */
        BaselinePLPDistribution baselinePLPD = new BaselinePLPDistribution(dataType, privacyBudgetList, windowSizeList, random);
        this.mechanismMap.put(Constant.BasePLPDSchemeName, baselinePLPD);
        BaselinePLPAbsorption baselinePLPA = new BaselinePLPAbsorption(dataType, privacyBudgetList, windowSizeList, random);
        this.mechanismMap.put(Constant.BasePLPASchemeName, baselinePLPA);

        /**
         * AblationSchemes
         */
        AblateOPSDistributionPlus ablateOPSD = new AblateOPSDistributionPlus(dataType, privacyBudgetList, windowSizeList, random);
        this.mechanismMap.put(Constant.AblateOPSPLPDSchemeName, ablateOPSD);
        AblateOPSAbsorptionPlus ablateOPSA = new AblateOPSAbsorptionPlus(dataType, privacyBudgetList, windowSizeList, random);
        this.mechanismMap.put(Constant.AblateOPSPLPASchemeName, ablateOPSA);
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


        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_output_containing_ldp", "p_"+String.valueOf(privacyBudget).replace(".","-")+"_w_"+windowSize, "segment_"+segmentID);
        File basicOutputFile = new File(basicOutputPathDir);
        if (!basicOutputFile.exists()) {
            basicOutputFile.mkdirs();
        }
        ExperimentResultWrite experimentResultWrite = new ExperimentResultWrite();
        String outputFilePath;
        for (int i = startFileIndex; i <= endFileIndex; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getData(file.getAbsolutePath(), new ArrayList<>(dataType));
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
                 * 2. Baseline mechanisms
                 */
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((BaselinePLPDistribution)mechanismMap.get(Constant.BasePLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((BaselinePLPAbsorption)mechanismMap.get(Constant.BasePLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                tempResult = _3_1_PersonalizedWEventLDPAblateOPSMechanismRun.runBatch((AblateOPSDistributionPlus)mechanismMap.get(Constant.AblateOPSPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _3_1_PersonalizedWEventLDPAblateOPSMechanismRun.runBatch((AblateOPSAbsorptionPlus)mechanismMap.get(Constant.AblateOPSPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _3_2_PersonalizedWEventLDPAblateRePerturbMechanismRun.runBatch((AblateRePerturbDistributionPlus)mechanismMap.get(Constant.AblateRPPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _3_2_PersonalizedWEventLDPAblateRePerturbMechanismRun.runBatch((AblateRePerturbAbsorptionPlus)mechanismMap.get(Constant.AblateRPPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);



                tempResult = _4_PersonalizedWEventLDPEnhancedMechanismRun.runBatch((PLDPPopulationDistributionPlus)mechanismMap.get(Constant.EnhancedPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_PersonalizedWEventLDPEnhancedMechanismRun.runBatch((PLDPPopulationAbsorptionPlus)mechanismMap.get(Constant.EnhancedPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
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

    @Override
    public void run() {
        runSegmentBatch();
        this.innerLatch.countDown();
        this.latch.countDown();
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
        String basicPath = Constant.checkInFilePath;
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
