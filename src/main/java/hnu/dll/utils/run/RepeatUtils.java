package hnu.dll.utils.run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import cn.edu.dll.io.write.CSVWrite;
import cn.edu.dll.struct.bean_structs.BeanInterface;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.ConfigureUtils;
import hnu.dll._config.Constant;
import hnu.dll.dataset.utils.CSVReadEnhanced;
import hnu.dll.run.c_dataset_run.utils.ResultBean;
import hnu.dll.utils.filters.RoundDirectoryFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepeatUtils {

    private static final String[] nameStringArray = new String[]{
            "NP",
            "LPD", "LPA",
            "PLPD", "PLPA",
            "AOPS-PLPD+", "AOPS-PLPA+",
            "ARP-PLPD+", "ARP-PLPA+",
            "PLPD+", "PLPA+"
    };
    private static final String[] nameStringArrayOnlyForTimeCost = new String[]{
            "NP",
            "LPD", "LPA",
            "PLPD", "PLPA",
            "AOPS-PLPD+", "AOPS-PLPA+",
            "ARP-PLPD+", "ARP-PLPA+",
            "PLPD+", "PLPA+"
    };

    /**
     * 将每轮最终结果合并取平均值
     * @param outputMethodDirFile
     * @param inputMethodDirFileList
     * @param parameterSet
     */
    private static void combineMainProcess(File outputMethodDirFile, List<File> inputMethodDirFileList, Set<String> parameterSet) {
        List<ResultBean> combineBeanList = null, updateBeanList;
        ResultBean tempBean;
        BeanInterface<ResultBean> modelBean = new ResultBean();
        BasicPair<Double, Integer> paramsPair;
        String inputFilePath, outputFilePath, title;
        CSVWrite csvWrite = new CSVWrite();
        File parentFile;
        FileFilter directoryFileFilter = new DirectoryFileFilter();
        for (String parameterFileDir : parameterSet) {
            paramsPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(parameterFileDir);
            title = CSVReadEnhanced.readDataTitle(inputMethodDirFileList.get(0).listFiles(directoryFileFilter)[0].getAbsolutePath()+ConstantValues.FILE_SPLIT+"result.txt");
//            System.out.println(title);
            combineBeanList = new ArrayList<>();
            for (String beanName : nameStringArray) {
                tempBean = ResultBean.getInitializedBean(beanName, paramsPair.getKey(), paramsPair.getValue());
                combineBeanList.add(tempBean);
            }
            for (File inputMethodDir : inputMethodDirFileList) {
                inputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, inputMethodDir, parameterFileDir, "result.txt");
                updateBeanList = CSVReadEnhanced.readDataToBeanList(inputFilePath, modelBean);
                update(combineBeanList, updateBeanList);
            }
            average(combineBeanList, inputMethodDirFileList.size());
            parentFile = new File(outputMethodDirFile, parameterFileDir);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, parentFile.getAbsolutePath(), "result.txt");
            csvWrite.startWriting(outputFilePath);
            csvWrite.writeOneLine(title);
            csvWrite.writeBeanList(combineBeanList);
            csvWrite.endWriting();
        }

    }
    private static void combineInternalProcess(File outputMethodDirFile, List<File> inputMethodDirFileList, Set<String> parameterSet) {
        List<ResultBean> combineBeanList = null, updateBeanList;
        ResultBean tempBean;
        BeanInterface<ResultBean> modelBean = new ResultBean();
        BasicPair<Double, Integer> paramsPair;
        String inputFilePath, outputFilePath, title;
        CSVWrite csvWrite = new CSVWrite();
        File parentFile;
        FileFilter directoryFileFilter = new DirectoryFileFilter();
        Double[] twoFixedPrivacyBudget = ConfigureUtils.getTwoFixedPrivacyBudget();
        Integer[] twoFixedWindowSize = ConfigureUtils.getTwoFixedWindowSize();
        for (String parameterFileDir : parameterSet) {
//            paramsPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(parameterFileDir);
            title = CSVReadEnhanced.readDataTitle(inputMethodDirFileList.get(0).listFiles(directoryFileFilter)[0].getAbsolutePath()+ConstantValues.FILE_SPLIT+"result.txt");
//            System.out.println(title);
            combineBeanList = new ArrayList<>();
            for (String beanName : nameStringArray) {
                tempBean = ResultBean.getInitializedBean(beanName, twoFixedPrivacyBudget[1], twoFixedWindowSize[1]);
                combineBeanList.add(tempBean);
            }
            for (File inputMethodDir : inputMethodDirFileList) {
                inputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, inputMethodDir, parameterFileDir, "result.txt");
                updateBeanList = CSVReadEnhanced.readDataToBeanList(inputFilePath, modelBean);
                update(combineBeanList, updateBeanList);
            }
            average(combineBeanList, inputMethodDirFileList.size());
            parentFile = new File(outputMethodDirFile, parameterFileDir);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, parentFile.getAbsolutePath(), "result.txt");
            csvWrite.startWriting(outputFilePath);
            csvWrite.writeOneLine(title);
            csvWrite.writeBeanList(combineBeanList);
            csvWrite.endWriting();
        }

    }

    private static void combineSerialProcess(File outputMethodDirFile, List<File> inputMethodDirFileList, Set<String> parameterSet) {
        // todo: 还未修改成关于串行的运行结果的合并(目前和combineMainProcess除了nameStringArrayOnlyForTimeCost外完全相同)
        List<ResultBean> combineBeanList = null, updateBeanList;
        ResultBean tempBean;
        BeanInterface<ResultBean> modelBean = new ResultBean();
        BasicPair<Double, Integer> paramsPair;
        String inputFilePath, outputFilePath, title;
        CSVWrite csvWrite = new CSVWrite();
        File parentFile;
        FileFilter directoryFileFilter = new DirectoryFileFilter();
        for (String parameterFileDir : parameterSet) {
            paramsPair = ParameterUtils.extractBudgetWindowSizeParametersAccordingFileDirName(parameterFileDir);
            title = CSVReadEnhanced.readDataTitle(inputMethodDirFileList.get(0).listFiles(directoryFileFilter)[0].getAbsolutePath()+ConstantValues.FILE_SPLIT+"result.txt");
//            System.out.println(title);
            combineBeanList = new ArrayList<>();
            for (String beanName : nameStringArrayOnlyForTimeCost) {
                tempBean = ResultBean.getInitializedBean(beanName, paramsPair.getKey(), paramsPair.getValue());
                combineBeanList.add(tempBean);
            }
            for (File inputMethodDir : inputMethodDirFileList) {
                inputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, inputMethodDir, parameterFileDir, "result.txt");
                updateBeanList = CSVReadEnhanced.readDataToBeanList(inputFilePath, modelBean);
                update(combineBeanList, updateBeanList);
            }
            average(combineBeanList, inputMethodDirFileList.size());
            parentFile = new File(outputMethodDirFile, parameterFileDir);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            outputFilePath = StringUtil.join(ConstantValues.FILE_SPLIT, parentFile.getAbsolutePath(), "result.txt");
            csvWrite.startWriting(outputFilePath);
            csvWrite.writeOneLine(title);
            csvWrite.writeBeanList(combineBeanList);
            csvWrite.endWriting();
        }

    }

    private static void update(List<ResultBean> combineBeanList, List<ResultBean> updateBeanList) {
        ResultBean combineBean, updateBean;
        for (int i = 0; i < combineBeanList.size(); i++) {
            combineBean = combineBeanList.get(i);
            updateBean = updateBeanList.get(i);
            combineBean.setBatchSize(combineBean.getBatchSize() + updateBean.getBatchSize());
            combineBean.setTimeCost(combineBean.getTimeCost() + updateBean.getTimeCost());
            combineBean.setBre(combineBean.getBre() + updateBean.getBre());
            combineBean.setBjsd(combineBean.getBjsd() + updateBean.getBjsd());
            combineBean.setMre(combineBean.getMre()+ updateBean.getMre());
            combineBean.setMjsd(combineBean.getMjsd() + updateBean.getMjsd());
        }
    }

    private static void average(List<ResultBean> combineBeanList, int size) {
        for (ResultBean bean : combineBeanList) {
            bean.setBatchSize(bean.getBatchSize()/size);
            bean.setTimeCost(bean.getTimeCost()/size);
            bean.setBre(bean.getBre()/size);
            bean.setBjsd(bean.getBjsd()/size);
            bean.setMre(bean.getMre()/size);
            bean.setMjsd(bean.getMjsd()/size);
        }
    }

    private static File fillRoundAndParameterInfoAndGetOutputMethodDirFile(String outputDir, File inputDirFile, FileFilter roundDirectoryFileFilter, File outputDirFile, Set<String> outputParamsFileNameSet, List<File> datasetRoundList, int roundSize) {
        File outputMethodDirFile;
        File[] roundDirs = inputDirFile.listFiles(roundDirectoryFileFilter);

        File tempInputMethodDirFile = OtherUtils.getSubDatasetNameFile(roundDirs[0]);
        String methodName = tempInputMethodDirFile.getName();
        outputMethodDirFile = new File(outputDir, methodName);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }
        FileFilter directoryFilter = new DirectoryFileFilter();

        if (!outputMethodDirFile.exists()) {
            outputMethodDirFile.mkdirs();
        }
        File[] paramDirFileArray = tempInputMethodDirFile.listFiles(directoryFilter);
        for (File paramFile : paramDirFileArray) {
            outputParamsFileNameSet.add(paramFile.getName());
        }
        for (int i = 0; i < roundDirs.length; ++i) {
            if (i >= roundSize) {
                break;
            }
            File roundDir = roundDirs[i];
            File methodDirFile =  OtherUtils.getSubDatasetNameFile(roundDir);
            roundDir.listFiles(directoryFilter);
            datasetRoundList.add(methodDirFile);
        }
        return outputMethodDirFile;
    }

    /**
     * input dir:
     *      ${input_dir}/round_i/k.${dataset_name}/param_dir/result.txt
     * output dir:
     *      ${output_dir}/k.${dataset_name}/param_dir/result.txt
     * @param inputDir
     * @param outputDir
     */
    public static void combineMultipleMainRound(String inputDir, String outputDir, int roundSize) {
        FileFilter roundDirectoryFileFilter = new RoundDirectoryFilter();
        File inputDirFile = new File(inputDir);
        File outputDirFile = new File(outputDir);
        File outputMethodDirFile;
        List<File> datasetRoundList = new ArrayList<>();
        Set<String> outputParamsFileNameSet = new HashSet<>();
        outputMethodDirFile = fillRoundAndParameterInfoAndGetOutputMethodDirFile(outputDir, inputDirFile, roundDirectoryFileFilter, outputDirFile, outputParamsFileNameSet, datasetRoundList, roundSize);

        combineMainProcess(outputMethodDirFile, datasetRoundList, outputParamsFileNameSet);
    }
    public static void combineMultipleInternalRound(String inputDir, String outputDir, int roundSize) {
        FileFilter roundDirectoryFileFilter = new RoundDirectoryFilter();
        File inputDirFile = new File(inputDir);
        File outputDirFile = new File(outputDir);
        File outputMethodDirFile;
        List<File> datasetRoundList = new ArrayList<>();
        Set<String> outputParamsFileNameSet = new HashSet<>();
        outputMethodDirFile = fillRoundAndParameterInfoAndGetOutputMethodDirFile(outputDir, inputDirFile, roundDirectoryFileFilter, outputDirFile, outputParamsFileNameSet, datasetRoundList, roundSize);

        combineInternalProcess(outputMethodDirFile, datasetRoundList, outputParamsFileNameSet);
    }

    public static void combineMultipleSerialRound(String inputDir, String outputDir, int roundSize) {
        FileFilter roundDirectoryFileFilter = new RoundDirectoryFilter();
        File inputDirFile = new File(inputDir);
        File outputDirFile = new File(outputDir);
        File outputMethodDirFile;
        List<File> datasetRoundList = new ArrayList<>();
        Set<String> outputParamsFileNameSet = new HashSet<>();
        outputMethodDirFile = fillRoundAndParameterInfoAndGetOutputMethodDirFile(outputDir, inputDirFile, roundDirectoryFileFilter, outputDirFile, outputParamsFileNameSet, datasetRoundList, roundSize);

        combineSerialProcess(outputMethodDirFile, datasetRoundList, outputParamsFileNameSet);
    }

    public static void main(String[] args) {
//        String inputDir = args[0];
//        String outputDir = args[1];
        String inputDir = Constant.TrajectoriesFilePath;
        String outputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "1.result");
        String roundSizeStr = ConfigureUtils.getFileHandleInfo("trajectories", "combineRound");
        combineMultipleMainRound(inputDir, outputDir, Integer.parseInt(roundSizeStr));
//        String inputDir = Constant.tlnsFilePath;
//        String outputDir = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.basicDatasetPath, "1.result_internal");
//        combineMultipleInternalRound(inputDir, outputDir);
    }
}
