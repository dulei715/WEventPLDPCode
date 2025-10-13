package hnu.dll.run.b_parameter_run.version_3;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.write.ExperimentResultWrite;
import cn.edu.dll.result.ExperimentResult;
import hnu.dll._config.Constant;
import hnu.dll.run.a_mechanism_run._0_NonPrivacyMechanismRun;
import hnu.dll.run.a_mechanism_run._1_WEventMechanismRun;
import hnu.dll.run.a_mechanism_run._2_PersonalizedWEventLDPBaselineMechanismRun;
import hnu.dll.run.b_parameter_run.basic.version_3.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import hnu.dll.schemes._basic_struct.Mechanism;
import hnu.dll.schemes.basic_scheme.NonPrivacyMechanism;
import hnu.dll.schemes.compared_scheme.w_event_dp.BudgetAbsorption;
import hnu.dll.schemes.compared_scheme.w_event_dp.BudgetDistribution;
import hnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetAbsorption;
import hnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.cdp.impl.PersonalizedBudgetDistribution;
import hnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetAbsorption;
import hnu.dll.schemes.main_scheme.b_dynamic_windown_size.DynamicPersonalizedBudgetDistribution;
import hnu.dll.struts.stream_data.StreamCountData;
import hnu.dll.struts.stream_data.StreamDataElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FixedSegmentBasicParameterSerialRun {
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

    private Map<String, Mechanism> mechanismMap;
    private List<String> dataType;
    private String dynamicPrivacyBudgetBasicPath;
    private String dynamicWindowSizeBasicPath;
    private String userToTypeFilePath;


    public FixedSegmentBasicParameterSerialRun(String basicPath, String dataTypeFileName, Integer singleBatchSize, Double privacyBudget, Integer windowSize, File[] timeStampDataFiles, int startFileIndex, int endFileIndex, Integer segmentID) {
        this.basicPath = basicPath;
        this.dataTypeFileName = dataTypeFileName;
        this.singleBatchSize = singleBatchSize;
        this.privacyBudget = privacyBudget;
        this.windowSize = windowSize;
        this.timeStampDataFiles = timeStampDataFiles;
        this.startFileIndex = startFileIndex;
        this.endFileIndex = endFileIndex;
        this.segmentID = segmentID;
        initialize();
    }

    private void initialize() {
        dataType = DatasetParameterUtils.getDataType(this.basicPath, this.dataTypeFileName);
        dynamicPrivacyBudgetBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_generated_parameters", "1.privacy_budget", ParameterGroupInitializeUtils.toPathName(privacyBudget));
        dynamicWindowSizeBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_generated_parameters", "2.window_size", ParameterGroupInitializeUtils.toPathName(windowSize));
        userToTypeFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user_to_type.txt");

        this.mechanismMap = new TreeMap<>();
        NonPrivacyMechanism nonPrivacyScheme = new NonPrivacyMechanism(dataType);
        this.mechanismMap.put(Constant.NonPrivacySchemeName, nonPrivacyScheme);
        BudgetDistribution budgetDistributionScheme = new BudgetDistribution(dataType, privacyBudget, windowSize);
        this.mechanismMap.put(Constant.LBDSchemeName, budgetDistributionScheme);
        BudgetAbsorption budgetAbsorption = new BudgetAbsorption(dataType, privacyBudget, windowSize);
        this.mechanismMap.put(Constant.LBASchemeName, budgetAbsorption);

        List<Double> privacyBudgetList = ParameterGroupInitializeUtils.getTypePrivacyBudgetFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicPrivacyBudgetBasicPath, "typePrivacyBudgetFile.txt"), userToTypeFilePath);
        List<Integer> windowSizeList = ParameterGroupInitializeUtils.getTypeWindowSizeFromFileAndFill(StringUtil.join(ConstantValues.FILE_SPLIT, dynamicWindowSizeBasicPath, "typeWindowSizeFile.txt"), userToTypeFilePath);
        PersonalizedBudgetDistribution personalizedBudgetDistribution = new PersonalizedBudgetDistribution(dataType, privacyBudgetList, windowSizeList);
        this.mechanismMap.put(Constant.PLPDBasicSchemeName, personalizedBudgetDistribution);
        PersonalizedBudgetAbsorption personalizedBudgetAbsorption = new PersonalizedBudgetAbsorption(dataType, privacyBudgetList, windowSizeList);
        this.mechanismMap.put(Constant.PLPABasicSchemeName, personalizedBudgetAbsorption);

        Integer userSize = ParameterGroupInitializeUtils.getUserSize(StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user.txt"));
        DynamicPersonalizedBudgetDistribution dynamicPersonalizedBudgetDistribution = new DynamicPersonalizedBudgetDistribution(dataType, userSize);
        this.mechanismMap.put(Constant.EnhancedPBDSchemeName, dynamicPersonalizedBudgetDistribution);
        DynamicPersonalizedBudgetAbsorption dynamicPersonalizedBudgetAbsorption = new DynamicPersonalizedBudgetAbsorption(dataType, userSize);
        this.mechanismMap.put(Constant.EnhancedPBASchemeName, dynamicPersonalizedBudgetAbsorption);
    }


    public List<ExperimentResult> runSegmentBatch() {

//        File dirFile = new File(basicPath, "runInput");
//        File[] timeStampDataFiles = dirFile.listFiles(new NumberTxtFilter());
        List<StreamDataElement<Boolean>> dataList;
        File file;
        List<List<StreamDataElement<Boolean>>> batchDataList = new ArrayList<>();
        List<ExperimentResult> experimentResultList = new ArrayList<>();
        ExperimentResult tempResult;

        String tempDataPath;
        List[] dynamicPrivacyListArray, dynamicWindowSizeListArray;

        List<List<Double>> remainBackwardPrivacyBudgetListBatchList = new ArrayList<>(), forwardPrivacyBudgetListBatchList = new ArrayList<>();
        List<List<Integer>> backwardWindowSizeListBatchList = new ArrayList<>(), forwardWindowSizeListBatchList = new ArrayList<>();

        String basicOutputPathDir = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "group_output_time_cost", "p_"+String.valueOf(privacyBudget).replace(".","-")+"_w_"+windowSize, "segment_"+segmentID);
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

            tempDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, dynamicPrivacyBudgetBasicPath, file.getName());
            dynamicPrivacyListArray = ParameterGroupInitializeUtils.getBackForwardPrivacyBudgetFromFile(tempDataPath, userToTypeFilePath);
            remainBackwardPrivacyBudgetListBatchList.add(dynamicPrivacyListArray[0]);
            forwardPrivacyBudgetListBatchList.add(dynamicPrivacyListArray[1]);
            tempDataPath = StringUtil.join(ConstantValues.FILE_SPLIT, dynamicWindowSizeBasicPath, file.getName());
            dynamicWindowSizeListArray = ParameterGroupInitializeUtils.getBackForwardWindowSizeFromFile(tempDataPath, userToTypeFilePath);
            backwardWindowSizeListBatchList.add(dynamicWindowSizeListArray[0]);
            forwardWindowSizeListBatchList.add(dynamicWindowSizeListArray[1]);


            if ((i+1) % singleBatchSize == 0 || i == timeStampDataFiles.length - 1) {
                NonPrivacyMechanism nonPrivacyMechanism = (NonPrivacyMechanism) this.mechanismMap.get(Constant.NonPrivacySchemeName);
                PurePair<ExperimentResult, List<StreamCountData>> nonPrivacySchemeResultPair = _0_NonPrivacyMechanismRun.runBatch(nonPrivacyMechanism, batchID, batchDataList);
                List<StreamCountData> rawPublicationBatchList = nonPrivacySchemeResultPair.getValue();
                tempResult = nonPrivacySchemeResultPair.getKey();
                experimentResultList.add(tempResult);
                // 执行各种机制
//                System.out.println("Start BudgetDistribution...");
                tempResult = _1_WEventMechanismRun.runBatch((BudgetDistribution)mechanismMap.get(Constant.LBDSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

//                System.out.println("Start BudgetAbsorption...");
                tempResult = _1_WEventMechanismRun.runBatch((BudgetAbsorption)mechanismMap.get(Constant.LBASchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);

//                System.out.println("Start PersonalizedBudgetDistribution...");
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((PersonalizedBudgetDistribution)mechanismMap.get(Constant.PLPDBasicSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);
//                System.out.println("Start PersonalizedBudgetAbsorption...");
                tempResult = _2_PersonalizedWEventLDPBaselineMechanismRun.runBatch((PersonalizedBudgetAbsorption)mechanismMap.get(Constant.PLPABasicSchemeName), batchID, batchDataList, rawPublicationBatchList);
                experimentResultList.add(tempResult);


                tempResult = _3_PersonalizedWEventLDPAblateMechanismRun.runBatch((DynamicPersonalizedBudgetDistribution)mechanismMap.get(Constant.EnhancedPBDSchemeName), batchID, batchDataList, rawPublicationBatchList, remainBackwardPrivacyBudgetListBatchList, backwardWindowSizeListBatchList, forwardPrivacyBudgetListBatchList, forwardWindowSizeListBatchList);
                experimentResultList.add(tempResult);

                tempResult = _3_PersonalizedWEventLDPAblateMechanismRun.runBatch((DynamicPersonalizedBudgetAbsorption)mechanismMap.get(Constant.EnhancedPBASchemeName), batchID, batchDataList, rawPublicationBatchList, remainBackwardPrivacyBudgetListBatchList, backwardWindowSizeListBatchList, forwardPrivacyBudgetListBatchList, forwardWindowSizeListBatchList);
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
