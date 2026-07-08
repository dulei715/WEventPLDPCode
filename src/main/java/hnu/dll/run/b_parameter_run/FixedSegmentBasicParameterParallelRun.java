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
import hnu.dll.run.utils.structs.UserParameter;
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
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_2_union_enhanced_scheme.impl.UnionPLDPPopulationAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_2_union_enhanced_scheme.impl.UnionPLDPPopulationDistribution;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class FixedSegmentBasicParameterParallelRun implements Runnable {
    /**
     * 1. 文件路径相关信息
     */
    private String basicPath;
    private Map<String, String> locationToMappedStrMap;
    private Set<String> dataType; // dataType 定义为map之后的Str集合

    /**
     * 2. batch 相关信息
     */
    private Integer singleBatchSize;
    private Integer batchID = 0;

    /**
     * 3. 用户参数相关信息
     */
    private Double defaultPrivacyBudget;
    private Integer defaultWindowSize;
    private List<UserParameter> userParameterList;
    private Map<Integer, Integer> userToIndexMap;
//    private List<Double> personalizedPrivacyBudgetList;
//    private List<Integer> personalziedWindowSizeList;
//    private Integer userSize;

    /**
     * 4. 数据段相关信息
     */
    private File[] timeStampDataFiles;
    private int startFileIndex;
    private int endFileIndex;
    private Integer segmentID;

    /**
     * 5. 随机工具
     */
    private Random random;

    /**
     * 6. 控制线程同步的计数器
     */
    private CountDownLatch latch;
    private CountDownLatch innerLatch;


    private Map<String, Mechanism> mechanismMap;



    public FixedSegmentBasicParameterParallelRun(String basicPath, final Map<String, String> locationToMappedStrMap,
                                                 Integer singleBatchSize,
                                                 Double defaultPrivacyBudget, Integer defaultWindowSize, List<UserParameter> userParameterList, Map<Integer, Integer> userToIndexMap,
                                                 File[] timeStampDataFiles, int startFileIndex, int endFileIndex, Integer segmentID,
                                                 Random random,
                                                 CountDownLatch latch, CountDownLatch innerLatch) {
        this.basicPath = basicPath;
        this.locationToMappedStrMap = locationToMappedStrMap;
        this.dataType = new HashSet<>(locationToMappedStrMap.values());
        this.singleBatchSize = singleBatchSize;
        this.defaultPrivacyBudget = defaultPrivacyBudget;
        this.defaultWindowSize = defaultWindowSize;
        this.userParameterList = userParameterList;
        this.userToIndexMap = userToIndexMap;
        this.timeStampDataFiles = timeStampDataFiles;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.segmentID = segmentID;
        this.random = random;
        this.latch = latch;
        this.innerLatch = innerLatch;
        initialize();
    }

    private void initialize() {
//        dataType = DatasetParameterUtils.getDataTypeSet(this.basicPath, this.dataTypeFileName);
        this.mechanismMap = new TreeMap<>();

        /**
         * 0. NonPrivacySchemes
         */
        NonPrivacyMechanism nonPrivacyScheme = new NonPrivacyMechanism(dataType);
        this.mechanismMap.put(Constant.NonPrivacySchemeName, nonPrivacyScheme);

        /**
         * 1. NonPersonalizedSchemes
         */
        Integer userSize = userParameterList.size();
        LDPPopulationDistribution lPD = new LDPPopulationDistribution(dataType, defaultPrivacyBudget, defaultWindowSize, userSize, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.LPDSchemeName, lPD);
        LDPPopulationAbsorption lPA = new LDPPopulationAbsorption(dataType, defaultPrivacyBudget, defaultWindowSize, userSize, random);
        this.mechanismMap.put(Constant.LPASchemeName, lPA);


        /**
         * 2. BaselineSchemes
         */
        List<Double> personalizedPrivacyBudgetList = UserParameter.extractPrivacyBudgetList(userParameterList);
        List<Integer> personalizedWindowSizeList = UserParameter.extractWindowSizeList(userParameterList);
        DirectPLPDistribution baselinePLPD = new DirectPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.DirectPLPDSchemeName, baselinePLPD);
        DirectPLPAbsorption baselinePLPA = new DirectPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        this.mechanismMap.put(Constant.DirectPLPASchemeName, baselinePLPA);

        /**
         * 3. AblationSchemes
         */
//        AblateOPSDistributionPlus ablateOPSD = new AblateOPSDistributionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
//        this.mechanismMap.put(Constant.AblateOPSPLPDSchemeName, ablateOPSD);
//        AblateOPSAbsorptionPlus ablateOPSA = new AblateOPSAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
//        this.mechanismMap.put(Constant.AblateOPSPLPASchemeName, ablateOPSA);
        BasicPLPDistribution ablateRPD = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.BasicPLPDSchemeName, ablateRPD);
        BasicPLPAbsorption ablateRPA = new BasicPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        this.mechanismMap.put(Constant.BasicPLPASchemeName, ablateRPA);

        /**
         * 4. EnhancedSchemes
         */
        UnionPLDPPopulationDistribution enhancedPLPD = new UnionPLDPPopulationDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
        this.mechanismMap.put(Constant.UnionPLPDSchemeName, enhancedPLPD);
        UnionPLDPPopulationAbsorption enhancedPLPA = new UnionPLDPPopulationAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        this.mechanismMap.put(Constant.UnionPLPASchemeName, enhancedPLPA);

    }


    public List<ExperimentResult> runSegmentBatch() {

        List<Integer> dataList;
        File file;
        List<List<Integer>> batchDataList = new ArrayList<>();
        List<ExperimentResult> experimentResultList = new ArrayList<>();
        ExperimentResult tempResult;


        String parameterRelativeFileName = ParameterGroupInitializeUtils.toPathName(defaultPrivacyBudget, defaultWindowSize);

        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT,basicPath, Constant.GroupOutputDirName, parameterRelativeFileName, "segment_"+segmentID);
        File basicOutputFile = new File(basicOutputPathDir);
        if (!basicOutputFile.exists()) {
            basicOutputFile.mkdirs();
        }
        ExperimentResultWrite experimentResultWrite = new ExperimentResultWrite();
        String outputFilePath;
        for (int i = startFileIndex; i <= endFileIndex; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), userToIndexMap, locationToMappedStrMap);

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
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((DirectPLPDistribution)mechanismMap.get(Constant.DirectPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((DirectPLPAbsorption)mechanismMap.get(Constant.DirectPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


//                tempResult = _3_1_PersonalizedWEventLDPAblateOPSMechanismRun.runBatch((AblateOPSDistributionPlus)mechanismMap.get(Constant.AblateOPSPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
//                experimentResultList.add(tempResult);
//
//                tempResult = _3_1_PersonalizedWEventLDPAblateOPSMechanismRun.runBatch((AblateOPSAbsorptionPlus)mechanismMap.get(Constant.AblateOPSPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
//                experimentResultList.add(tempResult);

                tempResult = _3_2_PersonalizedWEventLDPAblateRePerturbMechanismRun.runBatch((BasicPLPDistribution)mechanismMap.get(Constant.BasicPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _3_2_PersonalizedWEventLDPAblateRePerturbMechanismRun.runBatch((BasicPLPAbsorption)mechanismMap.get(Constant.BasicPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);



                tempResult = _4_PersonalizedWEventLDPEnhancedMechanismRun.runBatch((UnionPLDPPopulationDistribution)mechanismMap.get(Constant.UnionPLPDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _4_PersonalizedWEventLDPEnhancedMechanismRun.runBatch((UnionPLDPPopulationAbsorption)mechanismMap.get(Constant.UnionPLPASchemeName), batchID, batchDataList, rawPublicationBatchList);
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
