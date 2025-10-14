package hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import hnu.dll.run._pre_process.b_parameter_pre_process.version_5.parameter_generator.utils.UserGroupUtils;
import hnu.dll.utils.io.ListWriteUtils;

import java.util.List;

public class UserGroupGenerator {

    public static void generateUserToIndex(String basicPath) {
        String userIDInputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user.txt");
        String outputPath = StringUtil.join(ConstantValues.FILE_SPLIT, basicPath, "basic_info", "user_to_Index.txt");
        List<String> userToIndexData = UserGroupUtils.getUserToIndex(userIDInputPath);
        ListWriteUtils.writeList(outputPath, userToIndexData, ",");
    }



}
