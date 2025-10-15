package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.io.read.BasicRead;
import hnu.dll._config.Constant;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.utils.UserGroupUtils;
import hnu.dll.utils.io.ListReadUtils;
import hnu.dll.utils.io.ListWriteUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGroupGenerator {

    public static void generateUserToIndex(String basicPath) {
        String userIDInputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user.txt");
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", Constant.UserToIndexFileName);
        List<String> userToIndexData = UserGroupUtils.getUserToIndex(userIDInputPath);
        ListWriteUtils.writeList(outputPath, userToIndexData, ",");
    }

    public static Map<Integer, Integer> getUserToIndexMap(String basicPath) {
        Map<Integer, Integer> resultMap = new HashMap<>();
        String elementSplit = ",";
        String inputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", Constant.UserToIndexFileName);
        List<String> strList = ListReadUtils.readAllDataList(inputPath, elementSplit);
        String keyStr, valueStr;
        String[] splitStr;
        for (String str : strList) {
            splitStr = str.split(elementSplit);
            resultMap.put(Integer.valueOf(splitStr[0]), Integer.valueOf(splitStr[1]));
        }
        return resultMap;
    }

    public static String getUserAbsolutePath(String basicPath) {
        return StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user.txt");
    }



}
