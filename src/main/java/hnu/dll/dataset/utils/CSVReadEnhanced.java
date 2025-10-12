package hnu.dll.dataset.utils;

import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.io.read.CSVRead;
import cn.edu.dll.io.write.CSVWrite;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVReadEnhanced extends CSVRead {
    public static List<String> readStringData(String filePath) {
        String historyString, currentString = "";
        BufferedReader bufferedReader = null;
        List<String> elementList = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            historyString = currentString;
            while ((currentString = bufferedReader.readLine()) != null) {
                if (currentString.startsWith(CSVWrite.commonTag) || currentString.equals(historyString)) {
                    continue;
                }
                elementList.add(currentString);
                historyString = currentString;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return elementList;
    }

    public static void main(String[] args) {
        String filePath = "E:\\1.学习\\4.数据集\\3.dataset_for_dynamic_w_event\\0_dataset\\T-drive Taxi Trajectories\\taxi_log_2008_by_id\\1.txt";
        List<String> result = CSVReadEnhanced.readStringData(filePath);
        System.out.println(result.size());
        MyPrint.showList(result, ConstantValues.LINE_SPLIT);
    }
}
