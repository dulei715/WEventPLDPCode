package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.utils.ParameterGroupUtils;
import hnu.dll.utils.io.ListReadUtils;
import hnu.dll.utils.io.ListWriteUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LocationGroupGenerator {
    public static void generateLocationToIndex(String basicPath, Integer indexSize, Random random) {
        String locationNameInputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "cell.txt");
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", Constant.LocationToIndexFileName);
        List<String> locationToIndexData = ParameterGroupUtils.getLocationToIndex(locationNameInputPath, indexSize, random);
        ListWriteUtils.writeList(outputPath, locationToIndexData, ",");
    }

    public static Map<String, String> getLocationToMappedStrMap(String basicPath) {
        Map<String, String> resultMap = new HashMap<>();
        String elementSplit = ",";
        String inputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", Constant.LocationToIndexFileName);
        List<String> strList = ListReadUtils.readAllDataList(inputPath, elementSplit);
        String[] splitStr;
        for (String str : strList) {
            splitStr = str.split(elementSplit);
            resultMap.put(splitStr[0], splitStr[1]);
        }
        return resultMap;
    }
}
