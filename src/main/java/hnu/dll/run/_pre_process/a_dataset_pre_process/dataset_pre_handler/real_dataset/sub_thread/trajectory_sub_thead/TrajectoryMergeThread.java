package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.real_dataset.sub_thread.trajectory_sub_thead;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.read.BasicRead;
import cn.edu.dll.io.write.BasicWrite;
import cn.edu.dll.struct.pair.BasicPair;
import hnu.dll._config.Constant;
import hnu.dll.dataset.real.datasetA.handled_struct.TrajectorySimplifiedBean;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.FileMergeEnhancedFilter;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.PreprocessRunUtils;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils.TrajectoryPreprocessRunUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TrajectoryMergeThread implements Runnable{
    private Long fileNumberStart;
    private Long fileNumberEnd;
    private String initializedInputDirectoryPath;
    private String inputDirectoryName;
    private String outputDirectoryName;

    public TrajectoryMergeThread(Long fileNumberStart, Long fileNumberEnd, String initializedInputDirectoryPath, String inputDirectoryName, String outputDirectoryName) {
        this.fileNumberStart = fileNumberStart;
        this.fileNumberEnd = fileNumberEnd;
        this.initializedInputDirectoryPath = initializedInputDirectoryPath;
        this.inputDirectoryName = inputDirectoryName;
        this.outputDirectoryName = outputDirectoryName;
    }

    public void mergeToRawData() {
//        String path = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "shuffle_by_time_slot");
        File directoryFile = new File(initializedInputDirectoryPath);
        File[] files = directoryFile.listFiles(new FileMergeEnhancedFilter(fileNumberStart, fileNumberEnd));
        BasicRead basicRead = new BasicRead(",");
        BasicWrite basicWrite = new BasicWrite(",");
        List<String> tempDataList;
        TrajectorySimplifiedBean tempBean;
        Map<Integer, BasicPair<Long, String>> userTimeSlotLocationMap = TrajectoryPreprocessRunUtils.getInitialUserTimeSlotLocationMap(StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, inputDirectoryName));
        String outputDirectoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, outputDirectoryName);

        for (File file : files) {
            basicRead.startReading(file.getAbsolutePath());
            tempDataList = basicRead.readAllWithoutLineNumberRecordInFile();
            basicRead.endReading();
            for (String lineString : tempDataList) {
                tempBean = TrajectorySimplifiedBean.toBean(basicRead.split(lineString));
                PreprocessRunUtils.updateLatestTimeSlotData(userTimeSlotLocationMap, tempBean.getUserID(), tempBean.getTimestamp(), tempBean.getAreaIndex()+"");
            }
            basicWrite.startWriting(StringUtil.join(ConstantValues.FILE_SPLIT, outputDirectoryPath, file.getName()));
            for (Map.Entry<Integer, BasicPair<Long, String>> entry : userTimeSlotLocationMap.entrySet()) {
                basicWrite.writeOneLine(TrajectoryPreprocessRunUtils.toSimpleTrajectoryString(entry));
            }
            basicWrite.endWriting();
        }
    }
    @Override
    public void run() {
        mergeToRawData();
        System.out.println("end thread...");
    }
}
