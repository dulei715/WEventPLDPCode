package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.pair.CombinePair;
import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.LocationGroupGenerator;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.UserGroupGenerator;
import hnu.dll.run.b_parameter_run.utils.ParameterGroupInitializeUtils;
import hnu.dll.run.c_dataset_run.utils.DatasetParameterUtils;
import hnu.dll.run2.utils.io.UserParameterIOUtils;
import hnu.dll.run2.utils.structs.UserParameter;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateRePerturbAbsorptionPlus;
import hnu.dll.schemes.compare_scheme._3_ablation.impl.AblateRePerturbDistributionPlus;
import hnu.dll.utils.file.FileUtils;
import hnu.dll.utils.filters.NumberTxtFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MechanismOnRealDatasetTest {

    public Random random;
    public String finalResultDirName;

    public String basicPath;
//    public String dataTypeFileName;
    public Map<String, String> locationToStrMap;
    public Set<String> dataType;


    public CombineTriple<String, Integer, List<Integer>> independentBatchUnitData;
    public Integer singleBatchSize;
    public CombineTriple<String, Integer, List<Integer>> independentSegmentUnitData;
    public Integer segmentUnitSize;

    public List<Double> budgetChangeList;
    public List<Integer> windowSizeChangeList;
    public Double budgetDefault;
    public Integer windowSizeDefault;

    public Map<Integer, Integer> userToIndexMap;

    public File[] timeStampDataFiles;

    public List<UserParameter> userParameterList;
    public List<Double> personalizedPrivacyBudgetList;
    public List<Integer> personalizedWindowSizeList;




    // Test Trajectory
    @Before
    public void before() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        basicPath = Constant.TrajectoriesFilePath;
        finalResultDirName = "1.trajectory_result";
        Integer randomIndex = 0;
        random = Constant.randomArray[randomIndex];

//        dataTypeFileName = "cell.txt";

        independentBatchUnitData = ConfigureUtils.getIndependentData("BatchUnitSize", "default", "default");
        singleBatchSize = independentBatchUnitData.getValue();


        // basic message
        budgetChangeList = ConfigureUtils.getIndependentPrivacyBudgetList("default");
        windowSizeChangeList = ConfigureUtils.getIndependentWindowSizeList("default");
        budgetDefault = ConfigureUtils.getIndependentSinglePrivacyBudget("default");
        windowSizeDefault = ConfigureUtils.getIndependentSingleWindowSize("default");

        userToIndexMap = UserGroupGenerator.getUserToIndexMap(basicPath);


        File dirFile = new File(basicPath, "runInput");
        timeStampDataFiles = FileUtils.listFilesByFileName(dirFile, new NumberTxtFilter());
        int totalFileSize = timeStampDataFiles.length;

//        dataType = DatasetParameterUtils.getDataTypeSet(basicPath, dataTypeFileName);
        locationToStrMap = LocationGroupGenerator.getLocationToMappedStrMap(basicPath);
        dataType = new HashSet<>(locationToStrMap.values());

        independentSegmentUnitData = ConfigureUtils.getIndependentData("SegmentUnitSize", "default", "default");
        segmentUnitSize = independentSegmentUnitData.getValue();


//        Integer startIndex, endIndex;
//        Integer segmentID = 0;
//        Integer segmentSize = (int) Math.ceil(totalFileSize * 1.0 / segmentUnitSize);

        String parameterFileName = ParameterGroupInitializeUtils.toPathName(budgetDefault, windowSizeDefault);
        String inputDataFileName = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, Constant.GroupParameterDirectoryName, parameterFileName, Constant.PersonalizedParameterFileName);
        userParameterList = UserParameterIOUtils.readUserParameters(inputDataFileName);

        personalizedPrivacyBudgetList = UserParameter.extractPrivacyBudgetList(userParameterList);
        personalizedWindowSizeList = UserParameter.extractWindowSizeList(userParameterList);


    }


    @Test
    public void basicInfoTest() {
        System.out.println(basicPath);
        System.out.println(independentBatchUnitData);
        MyPrint.showList(budgetChangeList);
        MyPrint.showList(windowSizeChangeList);
        System.out.println(budgetDefault);
        System.out.println(windowSizeDefault);
//        MyPrint.showMap(userToIndexMap);
        MyPrint.showSplitLine("*", 150);
        System.out.println(independentSegmentUnitData);
        System.out.println("User size: " + this.userParameterList.size());;
        MyPrint.showSplitLine("*", 150);
        MyPrint.showArray(timeStampDataFiles);
    }

    @Test
    public void mechanismParameterTest() {
        Integer timeSlotSize = timeStampDataFiles.length;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        AblateRePerturbDistributionPlus ablateRPDPlus = new AblateRePerturbDistributionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        Map<Integer, Integer> realData = null;
        for (int i = 0; i < timeSlotSize; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), userToIndexMap, locationToStrMap);
            realData = BasicArrayUtil.getUniqueListWithCountList(dataList);
            System.out.println("userSize: " + dataList.size());
            MyPrint.showMap(realData, "; ");
//            MyPrint.showMap(realData);
            System.out.println(realData.size());
            MyPrint.showSplitLine("*", 150);
            if (i == 10) {
                break;
            }
        }
        System.out.println(realData.size());
        MyPrint.showCollection(new TreeSet(dataType), "; ");
        System.out.println(dataType.size());
        MyPrint.showSplitLine("*", 150);
    }

    @Test
    public void mechanismTest() {
        Integer timeSlotSize = timeStampDataFiles.length;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        AblateRePerturbDistributionPlus ablateRPDPlus = new AblateRePerturbDistributionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        Map<Integer, Integer> realData = null;
        for (int i = 0; i < timeSlotSize; i++) {
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), userToIndexMap, locationToStrMap);

            CombinePair<Boolean, Map<Integer, Double>> booleanMapCombinePair = ablateRPDPlus.updateNextPublicationResult(dataList);
            System.out.println(booleanMapCombinePair.getKey());
            MyPrint.showMap(booleanMapCombinePair.getValue(), "; ");

            MyPrint.showSplitLine("*", 150);
//            if (i == 10) {
//                break;
//            }
        }
    }

    @Test
    public void mechanismTest2() {
        Integer timeSlotSize = timeStampDataFiles.length;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        AblateRePerturbAbsorptionPlus ablateRPAPlus = new AblateRePerturbAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        Map<Integer, Integer> realData = null;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println(i);
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), userToIndexMap, locationToStrMap);

            CombinePair<Boolean, Map<Integer, Double>> booleanMapCombinePair = ablateRPAPlus.updateNextPublicationResult(dataList);
            System.out.println(booleanMapCombinePair.getKey());
            MyPrint.showMap(booleanMapCombinePair.getValue(), "; ");

            MyPrint.showSplitLine("*", 150);
//            if (i == 10) {
//                break;
//            }
        }
    }

    public void mechanismTest0() {
        Integer timeSlotSize = timeStampDataFiles.length;
        File file;
        List<Integer> dataList;

        System.out.println(dataType.size());

        AblateRePerturbAbsorptionPlus ablateRPAPlus = new AblateRePerturbAbsorptionPlus(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, random);
        Map<Integer, Integer> realData = null;
        for (int i = 0; i < timeSlotSize; i++) {
            System.out.println(i);
            file = timeStampDataFiles[i];
            dataList = DatasetParameterUtils.getDataMappedToIndex(file.getAbsolutePath(), new ArrayList<>(dataType), userToIndexMap, locationToStrMap);

            CombinePair<Boolean, Map<Integer, Double>> booleanMapCombinePair = ablateRPAPlus.updateNextPublicationResult(dataList);
            System.out.println(booleanMapCombinePair.getKey());
            MyPrint.showMap(booleanMapCombinePair.getValue(), "; ");

            MyPrint.showSplitLine("*", 150);
//            if (i == 10) {
//                break;
//            }
        }
    }

}
