package important_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.bean_structs.BeanInterface;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.Constant;
import hnu.dll.dataset.utils.CSVReadEnhanced;
import hnu.dll.run.c_dataset_run.utils.ResultBean;
import hnu.dll.utils.BasicUtils;
import hnu.dll.utils.run.ParameterUtils;
import org.apache.commons.collections4.ListUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ResultTest {

    public static List<ResultBean> searchBeanByName(List<ResultBean> data, String name) {
        List<ResultBean> resultList = new ArrayList<>();
        for (ResultBean bean : data) {
            if (bean.getName().equals(name)) {
                resultList.add(bean);
            }
        }
        return resultList;
    }

    public static List[] getAverageImprovementForBudgetChange(File[] fileDirFile, String furtherImproveMethodName, String improveMethodName, String originalMethodName, boolean whetherLog) {
        BeanInterface<ResultBean> bean = new ResultBean();
        String dirName;
        int dataLength = fileDirFile.length;
        List<Double> epsilonList = new ArrayList<>(dataLength);
        List<Double> improveRatioList = new ArrayList<>(dataLength);
        List<Double> furtherImproveRatioList = new ArrayList<>(dataLength);
        List<ResultBean> tempResult;
        ResultBean improveBean, furtherImproveBean, originalBean;
        File resultFile;
        Double originalValue, improveValue, furtherImproveValue;
        for (File dirFile : fileDirFile) {
            dirName = dirFile.getName();
            epsilonList.add(ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(dirName).getKey());
            resultFile = new File(dirFile, "result.txt");
            tempResult = CSVReadEnhanced.readDataToBeanList(resultFile.getAbsolutePath(), bean);
            improveBean = searchBeanByName(tempResult, improveMethodName).get(0);
            originalBean = searchBeanByName(tempResult, originalMethodName).get(0);
            furtherImproveBean = searchBeanByName(tempResult, furtherImproveMethodName).get(0);
            originalValue = originalBean.getMre();
            improveValue = improveBean.getMre();
            furtherImproveValue = furtherImproveBean.getMre();
            if (whetherLog) {
                improveRatioList.add((Math.log(originalValue) - Math.log(improveValue)) / Math.log(originalValue));
                furtherImproveRatioList.add((Math.log(originalValue) - Math.log(furtherImproveValue)) / Math.log(originalValue));
            } else {
                improveRatioList.add((originalValue - improveValue) / originalValue);
                furtherImproveRatioList.add((originalValue - furtherImproveValue) / originalValue);
            }
        }
        List[] resultList = new List[] {
                epsilonList,
                improveRatioList,
                furtherImproveRatioList
        };
        return resultList;
    }
    public static List[] getAverageImprovementForWindowSizeChange(File[] fileDirFile, String furtherImproveMethodName, String improveMethodName, String originalMethodName, boolean whetherLog) {
        BeanInterface<ResultBean> bean = new ResultBean();
        String dirName;
        int dataLength = fileDirFile.length;
        List<Integer> windowSizeList = new ArrayList<>(dataLength);
        List<Double> improveRatioList = new ArrayList<>(dataLength);
        List<Double> furtherImproveRatioList = new ArrayList<>(dataLength);
        List<ResultBean> tempResult;
        ResultBean improveBean, furtherImproveBean, originalBean;
        File resultFile;
        Double originalValue, improveValue, furtherImproveValue;
        for (File dirFile : fileDirFile) {
            dirName = dirFile.getName();
            windowSizeList.add(ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(dirName).getValue());
            resultFile = new File(dirFile, "result.txt");
            tempResult = CSVReadEnhanced.readDataToBeanList(resultFile.getAbsolutePath(), bean);
            improveBean = searchBeanByName(tempResult, improveMethodName).get(0);
            furtherImproveBean = searchBeanByName(tempResult, furtherImproveMethodName).get(0);
            originalBean = searchBeanByName(tempResult, originalMethodName).get(0);
            originalValue = originalBean.getMre();
            improveValue = improveBean.getMre();
            furtherImproveValue = furtherImproveBean.getMre();
            if (whetherLog) {
                improveRatioList.add((Math.log(originalValue) - Math.log(improveValue)) / Math.log(originalValue));
                furtherImproveRatioList.add((Math.log(originalValue) - Math.log(furtherImproveValue)) / Math.log(originalValue));
            } else {
                improveRatioList.add((originalValue - improveValue) / originalValue);
                furtherImproveRatioList.add((originalValue - furtherImproveValue) / originalValue);
            }
        }
        List[] resultList = new List[] {
                windowSizeList,
                improveRatioList,
                furtherImproveRatioList
        };
        return resultList;
    }

    @Test
    public void testBudgetChangeImprove() {
//        String datasetOrderName = "1.trajectory_result";
//        String datasetOrderName = "2.checkIn_result";
//        String datasetOrderName = "3.tlns_result";
//        String datasetOrderName = "4.sin_result";
        String datasetOrderName = "5.log_result";
//        String originalMethodName = "BD";
//        String improveMethodName = "PBD";
//        String furtherImproveMethodName = "PDBD";
        String originalMethodName = "LPA";
        String improveMethodName = "PLPA";
        String furtherImproveMethodName = "PLPA+";
        Integer defaultWindowSize = 120;
        boolean whetherLog = false;
//        boolean whetherLog = true;
//        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "1.result", datasetOrderName);
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "1.main_ldp_result", datasetOrderName);
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
//                dirFileList.add(innerDir);
                budgetFileMap.put(tempBudget, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(budgetFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
        List[] result = getAverageImprovementForBudgetChange(dirFileArray, furtherImproveMethodName, improveMethodName, originalMethodName, whetherLog);
        MyPrint.showList(result[0]);
        MyPrint.showList(result[1]);
        double sum = BasicArrayUtil.getDoubleSum(result[1]);
        System.out.println(sum / result[1].size());
        double sum2 = BasicArrayUtil.getDoubleSum(result[2]);
        MyPrint.showList(result[2]);
        System.out.println(sum2 / result[2].size());
    }

    @Test
    public void testWindowSizeImprove() {
//        String datasetOrderName = "1.trajectory_result";
//        String datasetOrderName = "2.checkIn_result";
//        String datasetOrderName = "3.tlns_result";
//        String datasetOrderName = "4.sin_result";
        String datasetOrderName = "5.log_result";
//        String originalMethodName = "BD";
//        String improveMethodName = "PBD";
//        String furtherImproveMethodName = "PDBD";
        String originalMethodName = "LPA";
        String improveMethodName = "PLPA";
        String furtherImproveMethodName = "PLPA+";
        Double defaultEpsilon = 1.5;
        boolean whetherLog = false;
//        boolean whetherLog = true;
        String datasetPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "1.main_ldp_result", datasetOrderName);
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
//                dirFileList.add(innerDir);
                windowSizeFileMap.put(tempWindowSize, innerDir);
            }
        }
        dirFileList = new ArrayList<>();
        dirFileList.addAll(windowSizeFileMap.values());
        File[] dirFileArray = dirFileList.toArray(new File[0]);
        List[] result = getAverageImprovementForWindowSizeChange(dirFileArray, furtherImproveMethodName, improveMethodName, originalMethodName, whetherLog);
        MyPrint.showList(result[0]);
        MyPrint.showList(result[1]);
        double sum = BasicArrayUtil.getDoubleSum(result[1]);
        System.out.println(sum / result[1].size());
        double sum2 = BasicArrayUtil.getDoubleSum(result[2]);
        MyPrint.showList(result[2]);
        System.out.println(sum2 / result[2].size());
    }


}
