package basic_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.print.MyPrint;
import hnu.dll._config.Constant;
import hnu.dll.metric.Measurement;
import hnu.dll.run.b_parameter_run.utils.InputDataStruct;
import hnu.dll.utils.file.FileUtils;
import hnu.dll.utils.filters.NumberTxtFilter;
import hnu.dll.utils.io.ListReadUtils;
import org.junit.Test;

import java.io.File;
import java.util.*;

public class BasicTest {
    @Test
    public void fun1() {
        List<Integer> data = BasicArrayUtil.getInitializedList(0, 10);
        data.set(2, 3);
        MyPrint.showList(data);
    }

    @Test
    public void fun2() {
        Random random = new Random(0);
        List<Double> dataA = RandomUtil.getRandomDoubleList(0D, 1D, 5, random);
        List<Double> dataB = RandomUtil.getRandomDoubleList(0D, 1D, 5, random);
        MyPrint.showList(dataA, ", ");
        MyPrint.showList(dataB, ", ");
        Double jsDivergence = Measurement.getJSDivergence(dataA, dataB);
        System.out.println(jsDivergence);
        MyPrint.showSplitLine("*", 150);

        Double jsDivergence1 = Measurement.getJSDivergence(dataA, dataA);
        System.out.println(jsDivergence1);
        MyPrint.showSplitLine("*", 150);

        List<Double> dataC = RandomUtil.getRandomDoubleList(0D, 0D, 5, random);
        List<Double> dataD = RandomUtil.getRandomDoubleList(1D, 1.1D, 5, random);
        Double jsDivergence2 = Measurement.getJSDivergence(dataC, dataD);
        System.out.println(jsDivergence2);
    }

    @Test
    public void fun3() {
        String dataDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.TrajectoriesFilePath, "runInput");
//        System.out.println(dataDirPath);
        File fileDir = new File(dataDirPath);
        File[] files = FileUtils.listFilesByFileName(fileDir, new NumberTxtFilter());
        Map<Integer, Integer> countResult = new TreeMap<>();
        for (File file : files) {

            List<String> strList = ListReadUtils.readAllDataList(file.getAbsolutePath(), ",");
            for (String str : strList) {
                InputDataStruct bean = InputDataStruct.toBean(str.split(","));
                String location = bean.getLocation();
                Integer locationInt = Integer.valueOf(location);
                Integer count = countResult.getOrDefault(locationInt, 0);
                ++count;
                countResult.put(locationInt, count);
            }
        }
        MyPrint.showMap(countResult);
        System.out.println(countResult.size());
    }
}
