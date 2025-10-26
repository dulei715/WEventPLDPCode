package important_test;

import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.bean_structs.BeanInterface;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.Constant;
import hnu.dll.dataset.utils.CSVReadEnhanced;
import hnu.dll.run.c_dataset_run.utils.ResultBean;
import hnu.dll.utils.run.ParameterUtils;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class ResultTestBasic {

    public static List<ResultBean> searchBeanByName(List<ResultBean> data, String name) {
        List<ResultBean> resultList = new ArrayList<>();
        for (ResultBean bean : data) {
            if (bean.getName().equals(name)) {
                resultList.add(bean);
            }
        }
        return resultList;
    }

    public static TreeMap<Double, List<ResultBean>> getResultBeanListMapByBudget(File[] fileDirFile) {
        BeanInterface<ResultBean> bean = new ResultBean();
        String dirName;
        TreeMap<Double, List<ResultBean>> resultMap = new TreeMap<>();
        List<ResultBean> tempResult;
        File resultFile;
        Double tempEpsilon;
        for (File dirFile : fileDirFile) {
            dirName = dirFile.getName();
            tempEpsilon = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(dirName).getKey();
            resultFile = new File(dirFile, "result.txt");
            tempResult = CSVReadEnhanced.readDataToBeanList(resultFile.getAbsolutePath(), bean);
            resultMap.put(tempEpsilon, tempResult);
        }
        return resultMap;
    }

    public static TreeMap<Integer, List<ResultBean>> getResultBeanListMapByWindowSize(File[] fileDirFile) {
        BeanInterface<ResultBean> bean = new ResultBean();
        String dirName;
        TreeMap<Integer, List<ResultBean>> resultMap = new TreeMap<>();
        List<ResultBean> tempResult;
        File resultFile;
        Integer tempWindowSize;
        for (File dirFile : fileDirFile) {
            dirName = dirFile.getName();
            tempWindowSize = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(dirName).getValue();
            resultFile = new File(dirFile, "result.txt");
            tempResult = CSVReadEnhanced.readDataToBeanList(resultFile.getAbsolutePath(), bean);
            resultMap.put(tempWindowSize, tempResult);
        }
        return resultMap;
    }

    @Test
    public void resultEpsilonMapTest() {
        String datasetOrderName = "1.trajectory_containing_ldp_result";
//        String datasetOrderName = "2.check_in_containing_ldp_result";
//        String datasetOrderName = "3.tlns_containing_ldp_result";
//        String datasetOrderName = "4.sin_containing_ldp_result";
//        String datasetOrderName = "5.log_containing_ldp_result";
        String originalMethodName = "BD";
        String improveMethodName = "PBD";
        String furtherImproveMethodName = "PDBD";
//        String originalMethodName = "BA";
//        String improveMethodName = "PBA";
//        String furtherImproveMethodName = "PDBA";
        Integer defaultWindowSize = 120;
        boolean whetherLog = false;
//        boolean whetherLog = true;
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "4.result_containing_ldp", datasetOrderName);
        File file = new File(datasetPath);
        File[] totalDirFileArray = file.listFiles(new DirectoryFileFilter());
        List<File> dirFileList;
        TreeMap<Double, File> budgetFileMap = new TreeMap<>();
        String innerDirName;
        BasicPair<Double, Integer> tempPair;
        Double tempBudget;
        Integer tempWindowSize;
        for (File innerDir : totalDirFileArray) {
            innerDirName = innerDir.getName();
            tempPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(innerDirName);
            tempBudget = tempPair.getKey();
            tempWindowSize = tempPair.getValue();
            if (tempWindowSize.equals(defaultWindowSize)) {
                budgetFileMap.put(tempBudget, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(budgetFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
        TreeMap<Double, List<ResultBean>> result = getResultBeanListMapByBudget(dirFileArray);
        MyPrint.showMap(result, ConstantValues.LINE_SPLIT);
    }
    @Test
    public void resultWindowSizeMapTest() {
        String datasetOrderName = "1.trajectory_containing_ldp_result";
//        String datasetOrderName = "2.check_in_containing_ldp_result";
//        String datasetOrderName = "3.tlns_containing_ldp_result";
//        String datasetOrderName = "4.sin_containing_ldp_result";
//        String datasetOrderName = "5.log_containing_ldp_result";
        String originalMethodName = "BD";
        String improveMethodName = "PBD";
        String furtherImproveMethodName = "PDBD";
//        String originalMethodName = "BA";
//        String improveMethodName = "PBA";
//        String furtherImproveMethodName = "PDBA";
//        Integer defaultWindowSize = 120;
        Double defaultEpsilon = 0.6;
        boolean whetherLog = false;
//        boolean whetherLog = true;
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "4.result_containing_ldp", datasetOrderName);
        File file = new File(datasetPath);
        File[] totalDirFileArray = file.listFiles(new DirectoryFileFilter());
        List<File> dirFileList;
        TreeMap<Integer, File> windowSizeFileMap = new TreeMap<>();
        String innerDirName;
        BasicPair<Double, Integer> tempPair;
        Double tempBudget;
        Integer tempWindowSize;
        for (File innerDir : totalDirFileArray) {
            innerDirName = innerDir.getName();
            tempPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(innerDirName);
            tempBudget = tempPair.getKey();
            tempWindowSize = tempPair.getValue();
            if (tempBudget.equals(defaultEpsilon)) {
                windowSizeFileMap.put(tempWindowSize, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(windowSizeFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
//        TreeMap<Double, List<ResultBean>> result = getResultBeanListMapByBudget(dirFileArray);
        TreeMap<Integer, List<ResultBean>> result = getResultBeanListMapByWindowSize(dirFileArray);
        MyPrint.showMap(result, ConstantValues.LINE_SPLIT);
    }


    public static final String[] MechanismNameArray = new String[] {
            "BD", "BA", "PLBU", "PBD", "PBA", "PDBD", "PDBA"
    };
    public static final Map<String, Integer> PositionMap = new HashMap<String, Integer>();
    static {
        PositionMap.put("BD", 0);
        PositionMap.put("BA", 1);
        PositionMap.put("PLBU", 2);
        PositionMap.put("PBD", 3);
        PositionMap.put("PBA", 4);
        PositionMap.put("PDBD", 5);
        PositionMap.put("PDBA", 6);
    }



    @Test
    public void showContainingLDPBudgetResult() {
//        String datasetOrderName = "1.trajectory_containing_ldp_result";
//        String datasetOrderName = "2.check_in_containing_ldp_result";
//        String datasetOrderName = "3.tlns_containing_ldp_result";
//        String datasetOrderName = "4.sin_containing_ldp_result";
        String datasetOrderName = "5.log_containing_ldp_result";
        Integer defaultWindowSize = 120;
        boolean whetherLog = false;
//        boolean whetherLog = true;
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "4.result_containing_ldp", datasetOrderName);
        File file = new File(datasetPath);
        File[] totalDirFileArray = file.listFiles(new DirectoryFileFilter());
        List<File> dirFileList;
        TreeMap<Double, File> budgetFileMap = new TreeMap<>();
        String innerDirName;
        BasicPair<Double, Integer> tempPair;
        Double tempBudget;
        Integer tempWindowSize;
        for (File innerDir : totalDirFileArray) {
            innerDirName = innerDir.getName();
            tempPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(innerDirName);
            tempBudget = tempPair.getKey();
            tempWindowSize = tempPair.getValue();
            if (tempWindowSize.equals(defaultWindowSize)) {
                budgetFileMap.put(tempBudget, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(budgetFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
        TreeMap<Double, List<ResultBean>> resultBeanListMapByBudget = getResultBeanListMapByBudget(dirFileArray);
//        MyPrint.showMap(resultBeanListMapByBudget, ConstantValues.LINE_SPLIT);
        List<List<Double>> resulList = new ArrayList<>();
        for (int i = 0; i < PositionMap.size(); i++) {
            resulList.add(new ArrayList<>());
        }
        Double tempMetric;
        String tempMechanismName;
        List<ResultBean> tempValue;
        List<Double> metricList;
        Integer tempPosition;
        for (Map.Entry<Double, List<ResultBean>> entry : resultBeanListMapByBudget.entrySet()) {
//            tempKey = entry.getKey();
            tempValue = entry.getValue();
            for (ResultBean tempBean : tempValue) {
                tempMechanismName = tempBean.getName();
                tempPosition = PositionMap.get(tempMechanismName);
                if (tempPosition == null) {
                    continue;
                }
                metricList = resulList.get(tempPosition);
//                if (metricList == null) {
//                    metricList = new ArrayList<>();
//                    resulList.add(tempPosition, metricList);
//                }

                tempMetric = tempBean.getMre();
                if (whetherLog) {
                    tempMetric = Math.log(tempMetric);
                }
                tempMetric = BasicCalculation.getPrecisionValue(tempMetric, 4);
                metricList.add(tempMetric);
            }
        }
        for (int i = 0; i < MechanismNameArray.length; i++) {
            System.out.print(MechanismNameArray[i] + ": ");
            MyPrint.showList(resulList.get(i), "\t");
        }
    }

    @Test
    public void showContainingLDPWindowSizeResult() {
//        String datasetOrderName = "1.trajectory_containing_ldp_result";
//        String datasetOrderName = "2.check_in_containing_ldp_result";
//        String datasetOrderName = "3.tlns_containing_ldp_result";
//        String datasetOrderName = "4.sin_containing_ldp_result";
        String datasetOrderName = "5.log_containing_ldp_result";
        Double defaultBudget = 0.6;
        boolean whetherLog = false;
//        boolean whetherLog = true;
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "4.result_containing_ldp", datasetOrderName);
        File file = new File(datasetPath);
        File[] totalDirFileArray = file.listFiles(new DirectoryFileFilter());
        List<File> dirFileList;
        TreeMap<Integer, File> windowSizeFileMap = new TreeMap<>();
        String innerDirName;
        BasicPair<Double, Integer> tempPair;
        Double tempBudget;
        Integer tempWindowSize;
        for (File innerDir : totalDirFileArray) {
            innerDirName = innerDir.getName();
            tempPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(innerDirName);
            tempBudget = tempPair.getKey();
            tempWindowSize = tempPair.getValue();
            if (tempBudget.equals(defaultBudget)) {
                windowSizeFileMap.put(tempWindowSize, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(windowSizeFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
        TreeMap<Integer, List<ResultBean>> resultBeanListMapByWindowSize = getResultBeanListMapByWindowSize(dirFileArray);
//        MyPrint.showMap(resultBeanListMapByBudget, ConstantValues.LINE_SPLIT);
        List<List<Double>> resulList = new ArrayList<>();
        for (int i = 0; i < PositionMap.size(); i++) {
            resulList.add(new ArrayList<>());
        }
        Double tempMetric;
        String tempMechanismName;
        List<ResultBean> tempValue;
        List<Double> metricList;
        Integer tempPosition;
        for (Map.Entry<Integer, List<ResultBean>> entry : resultBeanListMapByWindowSize.entrySet()) {
//            tempKey = entry.getKey();
            tempValue = entry.getValue();
            for (ResultBean tempBean : tempValue) {
                tempMechanismName = tempBean.getName();
                tempPosition = PositionMap.get(tempMechanismName);
                if (tempPosition == null) {
                    continue;
                }
                metricList = resulList.get(tempPosition);
//                if (metricList == null) {
//                    metricList = new ArrayList<>();
//                    resulList.add(tempPosition, metricList);
//                }

                tempMetric = tempBean.getMre();
                if (whetherLog) {
                    tempMetric = Math.log(tempMetric);
                }
                tempMetric = BasicCalculation.getPrecisionValue(tempMetric, 4);
                metricList.add(tempMetric);
            }
        }
        for (int i = 0; i < MechanismNameArray.length; i++) {
            System.out.print(MechanismNameArray[i] + ": ");
            MyPrint.showList(resulList.get(i), "\t");
        }
    }



}
