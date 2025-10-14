package hnu.dll.run2.utils.io;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.io.write.BasicWrite;
import hnu.dll.run2.utils.structs.UserParameter;

import java.util.ArrayList;
import java.util.List;

public class UserParameterWriteUtils {
    public static void writeUserParameters(String filePath, List<UserParameter> userParameterList) {
        List<String> parameterStringList = new ArrayList<>(userParameterList.size());
        String tempString;
        for (UserParameter userParameter : userParameterList) {
            tempString = StringUtil.join(",", userParameter.getUserID(), userParameter.getPrivacyBudget(), userParameter.getWindowSize());
            parameterStringList.add(tempString);
        }
        BasicWrite basicWrite = new BasicWrite();
        basicWrite.startWriting(filePath);
        basicWrite.writeStringListWithoutSize(parameterStringList);
        basicWrite.endWriting();
    }

    public static void main(String[] args) {

    }
}
