package hnu.dll.run.c_dataset_run.utils;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.execute.CopyUtils;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import cn.edu.dll.filter.file_filter.TxtFilter;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.io.write.CSVWrite;
import cn.edu.dll.signal.CatchSignal;
import cn.edu.dll.struct.bean_structs.BeanInterface;
import ecnu.dll._config.Constant;
import ecnu.dll.dataset.utils.CSVReadEnhanced;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class PostProcessUtils {
//    public static void combineResultBefore(String dirPath) {
//        File dirFile = new File(dirPath);
//        File[] files = dirFile.listFiles(new TxtFilter());
//        List<List<ResultBeanBefore>> dataList = new ArrayList<>();
//        List<ResultBeanBefore> tempList;
//        BeanInterface<ResultBeanBefore> beanBeanInterface = new ResultBeanBefore();
//        String title = CSVReadEnhanced.readDataTitle(files[0].getAbsolutePath());
//        List<String> dataStringList;
//        for (File file : files) {
//            dataStringList = CSVReadEnhanced.readDataLinesWithoutTitle(file.getAbsolutePath());
//            tempList = new ArrayList<>();
//            for (String str : dataStringList) {
//                tempList.add(ResultBeanBefore.toBean(str));
//            }
//            dataList.add(tempList);
//        }
//        List<ResultBeanBefore> resultList = new ArrayList<>();
//        ResultBeanBefore resultBean, tempBean;
//        int innerSize = dataList.get(0).size();
//        for (int i = 0; i < innerSize; i++) {
//            resultBean = ResultBeanBefore.getInitializedBean(dataList.get(0).get(i));
//            for (List<ResultBeanBefore> innerList : dataList) {
//                tempBean = innerList.get(i);
//                resultBean.setbRE(resultBean.getbRE() + tempBean.getbRE());
//            }
//            resultList.add(resultBean);
//        }
//        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, dirPath, "combine", "combine.txt");
//        BasicWrite basicWrite = new BasicWrite(",");
//        basicWrite.startWriting(outputPath);
//        basicWrite.writeOneLine(title);
//        for (ResultBeanBefore bean : resultList) {
//            basicWrite.writeOneLine(bean.toCSVString());
//        }
//        basicWrite.endWriting();
//    }
    public static void combineResult(String dirPath) {
        File dirFile = new File(dirPath);
        File[] files = dirFile.listFiles(new TxtFilter());
        List<List<ResultPartBean>> dataList = new ArrayList<>();
        List<ResultPartBean> tempList;
        BeanInterface<ResultPartBean> beanBeanInterface = new ResultPartBean();
        String title = CSVReadEnhanced.readDataTitle(files[0].getAbsolutePath());
        List<String> dataStringList;
        for (File file : files) {
            // here
            dataStringList = CSVReadEnhanced.readDataLinesWithoutTitle(file.getAbsolutePath());
            if (dataStringList.isEmpty()) {
                continue;
            }
            tempList = new ArrayList<>();
            for (String str : dataStringList) {
                tempList.add(ResultPartBean.toBean(str));
            }
            dataList.add(tempList);
        }
        List<ResultPartBean> resultList = new ArrayList<>();
        ResultPartBean resultPartBean, tempBean;
        int innerSize = dataList.get(0).size();
        for (int i = 0; i < innerSize; i++) {
            resultPartBean = ResultPartBean.getInitializedBean(dataList.get(0).get(i));
            for (List<ResultPartBean> innerList : dataList) {
                tempBean = innerList.get(i);
                resultPartBean.setBatchSize(resultPartBean.getBatchSize()+ tempBean.getBatchSize());
                resultPartBean.setbRE(resultPartBean.getbRE() + tempBean.getbRE());
                resultPartBean.setTimeCost(resultPartBean.getTimeCost() + tempBean.getTimeCost());
                resultPartBean.setbJSD(resultPartBean.getbJSD() + tempBean.getbJSD());
            }
            resultPartBean.setmRE(resultPartBean.getbRE() / resultPartBean.getBatchSize());
            resultPartBean.setmJSD(resultPartBean.getbJSD() / resultPartBean.getBatchSize());
            resultList.add(resultPartBean);
        }
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, dirPath, "combine", "combine.txt");
        BasicWrite basicWrite = new BasicWrite(",");
        basicWrite.startWriting(outputPath);
        basicWrite.writeOneLine(title + ",MRE" + ",MJSD");
        for (ResultPartBean bean : resultList) {
            basicWrite.writeOneLine(bean.toCSVString());
        }
        basicWrite.endWriting();
    }

    public static void combineAndExtractCombineResult(String inputDir, String outputDir) {
        File dirInputFile = new File(inputDir);
        File dirOutputFile = new File(outputDir);
        FileFilter directoryFileFilter = new DirectoryFileFilter();
        if (!dirOutputFile.exists()) {
            dirOutputFile.mkdirs();
        }
        File tempParametersChangeOutputFile, tempOutputFile, tempInputFile;
        String tempSegmentOutputName;
        File[] dirFiles = dirInputFile.listFiles(directoryFileFilter);
        File[] segmentInputDirs;
        for (File parametersChangeInputFile : dirFiles) {
            tempParametersChangeOutputFile = new File(dirOutputFile, parametersChangeInputFile.getName());
            if (!tempParametersChangeOutputFile.exists()) {
                tempParametersChangeOutputFile.mkdirs();
            }
            segmentInputDirs = parametersChangeInputFile.listFiles(directoryFileFilter);
            for (File segmentInputDir : segmentInputDirs) {
                // here
                combineResult(segmentInputDir.getAbsolutePath());
                tempSegmentOutputName = segmentInputDir.getName();
                tempOutputFile = new File(tempParametersChangeOutputFile, tempSegmentOutputName+".txt");
                tempInputFile = new File(segmentInputDir, StringUtil.join(ConstantValues.FILE_SPLIT, "combine", "combine.txt"));
                CopyUtils.fileCopyWithTransferTo(tempInputFile.getAbsolutePath(), tempOutputFile.getAbsolutePath());
            }
        }
    }

    private static void furtherCombineFiles(File tempOutputFile, File[] segmentInputFiles) {
        File segmentInputFile;
        BeanInterface<ResultBean> beanBeanInterface = new ResultBean();
        List<List<ResultBean>> dataList = new ArrayList<>();
        List<ResultBean> tempBeanList, resultList;
        String title = CSVReadEnhanced.readDataTitle(segmentInputFiles[0].getAbsolutePath());
        for (int i = 0; i < segmentInputFiles.length; i++) {
            segmentInputFile = segmentInputFiles[i];
            tempBeanList = CSVReadEnhanced.readDataToBeanList(segmentInputFile.getAbsolutePath(), beanBeanInterface);
            dataList.add(tempBeanList);
        }
        ResultBean resultBean, tempBean;
        int innerSize = dataList.get(0).size();
        resultList = new ArrayList<>();
        for (int i = 0; i < innerSize; i++) {
            resultBean = ResultBean.getInitializedBean(dataList.get(0).get(i));
            for (List<ResultBean> innerList : dataList) {
                tempBean = innerList.get(i);
                resultBean.setBatchSize(resultBean.getBatchSize()+ tempBean.getBatchSize());
                resultBean.setTimeCost(resultBean.getTimeCost() + tempBean.getTimeCost());
                resultBean.setBre(resultBean.getBre() + tempBean.getBre());
                resultBean.setMre(resultBean.getMre() + tempBean.getMre()*tempBean.getBatchSize());
                resultBean.setBjsd(resultBean.getBjsd() + tempBean.getBjsd());
                resultBean.setMjsd(resultBean.getMjsd() + tempBean.getMjsd() * tempBean.getBatchSize());
            }
            resultBean.setMre(resultBean.getMre() / resultBean.getBatchSize());
            resultBean.setMjsd(resultBean.getMjsd() / resultBean.getBatchSize());
            resultList.add(resultBean);
        }
        String outputPath = tempOutputFile.getAbsolutePath();
        CSVWrite csvWrite = new CSVWrite();
        csvWrite.startWriting(outputPath);
        csvWrite.writeOneLine(title);
        csvWrite.writeBeanList(resultList);
        csvWrite.endWriting();
    }

    public static void furtherCombine(String inputDir, String outputDir) {
        File dirInputFile = new File(inputDir);
        File dirOutputFile = new File(outputDir);
        File dirInnerOutputFile;
        FileFilter directoryFileFilter = new DirectoryFileFilter();
        FileFilter txtFilter = new TxtFilter();

        if (!dirOutputFile.exists()) {
            dirOutputFile.mkdirs();
        }


        File tempParametersChangeOutputFile, tempOutputFile, tempInputFile;
        File[] datasetDirFile = dirInputFile.listFiles(directoryFileFilter);
        File[] segmentInputFiles;
        for (File datasetFile : datasetDirFile) {
            dirInnerOutputFile = new File(dirOutputFile, datasetFile.getName());
            if (!dirInnerOutputFile.exists()) {
                dirInnerOutputFile.mkdirs();
            }
//            dirFiles = datasetFile.listFiles(directoryFileFilter);
            segmentInputFiles = datasetFile.listFiles(txtFilter);
            tempOutputFile = new File(dirInnerOutputFile, "result.txt");
            furtherCombineFiles(tempOutputFile, segmentInputFiles);
        }

    }


    public static void main1(String[] args) {
//        String inputPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "test");
//        combineResultBefore(inputPath);
    }

    public static void main0(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();
//        String inputDirPath = StringUtil.join(combineDir);
//        String inputDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "output");
        String inputDirPath = args[0];
        System.out.println(inputDirPath);
        File dirFile = new File(inputDirPath);
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            combineResult(file.getAbsolutePath());
        }
    }

    public static void main(String[] args) {
        String inputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "result");
        String outputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "..", "result_combine");
        combineResult(inputDir);
//        furtherCombine(inputDir, outputDir);
    }
}
