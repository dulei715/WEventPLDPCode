package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
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
import hnu.dll.run.utils.io.UserParameterIOUtils;
import hnu.dll.run.utils.structs.UserParameter;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPAbsorption;
import hnu.dll.schemes.compare_scheme._2_our_baseline._1_1_direct_scheme.impl.DirectPLPDistribution;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPAbsorption;
import hnu.dll.schemes.main_scheme._1_simple_scheme.impl.BasicPLPDistribution;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.impl.PLPAPlus;
import hnu.dll.utils.BasicUtils;
import hnu.dll.utils.file.FileUtils;
import hnu.dll.utils.filters.NumberTxtFilter;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MechanismOnSinDatasetTest {

    public Random random;
    public String finalResultDirName;

    public String basicPath;
//    public String dataTypeFileName;
    public Map<String, String> locationToStrMap;
    public Set<String> dataType;
    public List<Integer> domainIndexList;


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
        basicPath = Constant.SinFilePath;
        finalResultDirName = "4.sin_result";
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
        domainIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, dataType.size() - 1);

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

        BasicPLPDistribution ablateRPDPlus = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
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

        BasicPLPDistribution ablateRPDPlus = new BasicPLPDistribution(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
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

        BasicPLPAbsorption ablateRPAPlus = new BasicPLPAbsorption(dataType, personalizedPrivacyBudgetList, personalizedWindowSizeList, Constant.PopulationLowerBound, random);
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
