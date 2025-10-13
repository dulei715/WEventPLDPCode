package hnu.dll.run.d_total_run._1_initialize_run;

import cn.edu.dll.signal.CatchSignal;
import hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_run.CheckInDatasetPreprocessRun;

public class CheckInInitialize {
    public static void main(String[] args) {
        CatchSignal catchSignal = new CatchSignal();
        catchSignal.startCatch();

        System.out.println("Program is running... ...");

        // 1. 将数据分割成多个文件以方便分批读取到内存进行处理
        System.out.println("Start data split...");
        int unitSize = 204800;
        CheckInDatasetPreprocessRun.dataSplit(unitSize);

        // 2. 将数据与country文件链接，组合成 (userID,country,timestamp)的形式
        System.out.println("Start join...");
        CheckInDatasetPreprocessRun.dataJoin();


        // 3. 将数据按照时间，划分成多个文件
        System.out.println("Start shuffle...");
        CheckInDatasetPreprocessRun.shuffleJoinFilesByTimeSlot();

        // 4. 保留每个timestamp的用户状态
        System.out.println("Start merge...");
        CheckInDatasetPreprocessRun.mergeToExperimentRawData("runInput_raw");

        // 5. 记录country.txt, user_raw.txt, timestamp.txt三个基本文件到 basic_info/ 目录下
        System.out.println("Start record...");
        CheckInDatasetPreprocessRun.recordBasicInformation();
        System.out.println("Program finished !");

        // 6. dataset 抽取
        CheckInDatasetPreprocessRun.extractUser();  // 抽取 5% 的 user 记录在 user.txt
        CheckInDatasetPreprocessRun.extractUserData(); // 根据新的 user.txt 抽取 runInput 中的数据

    }
}
