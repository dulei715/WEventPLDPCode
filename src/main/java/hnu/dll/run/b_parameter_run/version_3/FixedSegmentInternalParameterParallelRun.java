package hnu.dll.run.b_parameter_run.version_3;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.collection.ListUtils;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.ExperimentResultWrite;
import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.struct.pair.PurePair;
import ecnu.dll._config.ConfigureUtils;
import ecnu.dll._config.Constant;
import ecnu.dll.run.a_mechanism_run._0_NonPrivacyMechanismRun;
import ecnu.dll.run.a_mechanism_run._1_WEventMechanismRun;
import ecnu.dll.run.a_mechanism_run._2_PersonalizedEventMechanismRun;
import ecnu.dll.run.b_parameter_run.basic.version_3.utils.ParameterGroupInitializeUtils;
import ecnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import ecnu.dll.run.utils.PatternUtils;
import ecnu.dll.schemes._basic_struct.Mechanism;
import ecnu.dll.schemes.basic_scheme.NonPrivacyMechanism;
import ecnu.dll.schemes.compared_scheme.w_event_dp.BudgetAbsorption;
import ecnu.dll.schemes.compared_scheme.w_event_dp.BudgetDistribution;
import ecnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetAbsorption;
import ecnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetDistribution;
import ecnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetAbsorption;
import ecnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetDistribution;
import ecnu.dll.struts.stream_data.StreamCountData;
import ecnu.dll.struts.stream_data.StreamDataElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class FixedSegmentInternalParameterParallelRun implements Runnable {
    public static final Boolean Change_Two_Privacy_Budget_Status = false;
    public static final Boolean Change_Two_Window_Size_Status = true;

    private String basicPath;
    private String dataTypeFileName;
    private Integer singleBatchSize;
    private Integer batchID = 0;
    private Double[] privacyBudgetCandidateArray;
    private Integer[] windowSizeCandidatetArray;
    private Double userRatio;
    private File[] timeStampDataFiles;
    private int startFileIndex;
    private int endFileIndex;
    private Integer segmentID;
    private boolean changeStatus;

    private Map<String, Mechanism> mechanismMap;
    private List<String> dataType;
    private String dynamicPrivacyBudgetBasicPath;
    private String dynamicWindowSizeBasicPath;
    private String userToTypeFilePath;
    private String endDirName;
    private CountDownLatch latch;
    private CountDownLatch innerLatch;


    public FixedSegmentInternalParameterParallelRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Double userRatio, File[] timeStampDataFiles, int startFileIndex, int endFileIndex, Integer segmentID, Boolean changeStatus, CountDownLatch latch, CountDownLatch innerLatch) {
        this.basicPath = basicPath;
        this.dataTypeFileName = dataTypeFileName;
        this.singleBatchSize = singleBatchSize;
        this.userRatio = userRatio;
        this.timeStampDataFiles = timeStampDataFiles;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.segmentID = segmentID;
        this.changeStatus = changeStatus;
        this.latch = latch;
        this.innerLatch = innerLatch;
        initialize();
    }

    private void initialize() {
        dataType = DatasetParameterUtils.getDataType(this.basicPath, this.dataTypeFileName);
        dynamicPrivacyBudgetBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_generated_parameters", "3.user_ratio", "3.typePrivacyBudget", "typePrivacyBudgetFile.txt");
        dynamicWindowSizeBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_generated_parameters", "3.user_ratio", "4.typeWindowSize", "typeWindowSizeFile.txt");
        userToTypeFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_generated_parameters", "3.user_ratio", "2.user_to_type_ratio", PatternUtils.toRatioFileNamePattern(this.userRatio, ".txt"));

        this.privacyBudgetCandidateArray = ConfigureUtils.getTwoFixedPrivacyBudget();
        this.windowSizeCandidatetArray = ConfigureUtils.getTwoFixedWindowSize();

        Double defaultPrivacyBudget = privacyBudgetCandidateArray[1];
        Integer defaultWindowSize = windowSizeCandidatetArray[1];
        this.mechanismMap = new TreeMap<>();
        NonPrivacyMechanism nonPrivacyScheme = new NonPrivacyMechanism(dataType);
        this.mechanismMap.put(Constant.NonPrivacySchemeName, nonPrivacyScheme);
        BudgetDistribution budgetDistributionScheme = new BudgetDistribution(dataType, defaultPrivacyBudget, defaultWindowSize);
        this.mechanismMap.put(Constant.BudgetDistributionSchemeName, budgetDistributionScheme);
        BudgetAbsorption budgetAbsorption = new BudgetAbsorption(dataType, defaultPrivacyBudget, defaultWindowSize);
        this.mechanismMap.put(Constant.BudgetAbsorptionSchemeName, budgetAbsorption);

        List<Double> privacyBudgetList;
        List<Integer> windowSizeList;
        if (Change_Two_Privacy_Budget_Status.equals(this.changeStatus)) {
            privacyBudgetList = ParameterGroupInitializeUtils.getTypePrivacyBudgetFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicPrivacyBudgetBasicPath), userToTypeFilePath);
            try {
                windowSizeList = ListUtils.generateListWithFixedElement(defaultWindowSize, privacyBudgetList.size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.endDirName = String.format("w_%d", defaultWindowSize);
        } else if (Change_Two_Window_Size_Status.equals(this.changeStatus)) {
            windowSizeList = ParameterGroupInitializeUtils.getTypeWindowSizeFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicWindowSizeBasicPath), userToTypeFilePath);
            try {
                privacyBudgetList = ListUtils.generateListWithFixedElement(defaultPrivacyBudget, windowSizeList.size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            this.endDirName = String.format("p_%s", String.valueOf(defaultPrivacyBudget).replace(".", "-"));
        } else {
            throw new RuntimeException("The change status is wrong!");
        }
        PersonalizedBudgetDistribution personalizedBudgetDistribution = new PersonalizedBudgetDistribution(dataType, privacyBudgetList, windowSizeList);
        this.mechanismMap.put(Constant.PersonalizedBudgetDistributionSchemeName, personalizedBudgetDistribution);
        PersonalizedBudgetAbsorption personalizedBudgetAbsorption = new PersonalizedBudgetAbsorption(dataType, privacyBudgetList, windowSizeList);
        this.mechanismMap.put(Constant.PersonalizedBudgetAbsorptionSchemeName, personalizedBudgetAbsorption);

        Integer userSize = ParameterGroupInitializeUtils.getUserSize(StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user.txt"));
        DynamicPersonalizedBudgetDistribution dynamicPersonalizedBudgetDistribution = new DynamicPersonalizedBudgetDistribution(dataType, userSize);
        this.mechanismMap.put(Constant.DynamicPersonalizedBudgetDistributionSchemeName, dynamicPersonalizedBudgetDistribution);
        DynamicPersonalizedBudgetAbsorption dynamicPersonalizedBudgetAbsorption = new DynamicPersonalizedBudgetAbsorption(dataType, userSize);
        this.mechanismMap.put(Constant.DynamicPersonalizedBudgetAbsorptionSchemeName, dynamicPersonalizedBudgetAbsorption);
    }


    public List<ExperimentResult> runSegmentBatch() {

        List<StreamDataElement<Boolean>> dataList;
        File file;
        List<List<StreamDataElement<Boolean>>> batchDataList = new ArrayList<>();
        List<ExperimentResult> experimentResultList = new ArrayList<>();
        ExperimentResult tempResult;

        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_output_internal", "u_"+String.valueOf(userRatio).replace(".","-")+"_".concat(endDirName), "segment_"+segmentID);
        File basicOutputFile = new File(basicOutputPathDir);
        if (!basicOutputFile.exists()) {
            basicOutputFile.mkdirs();
        }
        ExperimentResultWrite experimentResultWrite = new ExperimentResultWrite();
        String outputFilePath;
        for (int i = startFileIndex; i <= endFileIndex; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getData(file.getAbsolutePath(), dataType);
            batchDataList.add(dataList);

            if ((i+1) % singleBatchSize == 0 || i == timeStampDataFiles.length - 1) {
                NonPrivacyMechanism nonPrivacyMechanism = (NonPrivacyMechanism) this.mechanismMap.get(Constant.NonPrivacySchemeName);
                PurePair<ExperimentResult, List<StreamCountData>> nonPrivacySchemeResultPair = _0_NonPrivacyMechanismRun.runBatch(nonPrivacyMechanism, batchID, batchDataList);
                List<StreamCountData> rawPublicationBatchList = nonPrivacySchemeResultPair.getValue();
                tempResult = nonPrivacySchemeResultPair.getKey();
                experimentResultList.add(tempResult);
                // 执行各种机制
                tempResult = _1_WEventMechanismRun.runBatch((BudgetDistribution)mechanismMap.get(Constant.BudgetDistributionSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _1_WEventMechanismRun.runBatch((BudgetAbsorption)mechanismMap.get(Constant.BudgetAbsorptionSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

                tempResult = _2_PersonalizedEventMechanismRun.runBatch((PersonalizedBudgetDistribution)mechanismMap.get(Constant.PersonalizedBudgetDistributionSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
                tempResult = _2_PersonalizedEventMechanismRun.runBatch((PersonalizedBudgetAbsorption)mechanismMap.get(Constant.PersonalizedBudgetAbsorptionSchemeName), batchID, batchDataList, rawPublicationBatchList);
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
