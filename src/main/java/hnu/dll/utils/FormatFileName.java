package hnu.dll.utils;


import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.filter.file_filter.TxtFilter;
import hnu.dll._config.Constant;

import java.io.File;

public class FormatFileName {

    /**
     * 仅支持非负数
     */
    public static String getFormatNumberString(Integer value, Integer begin, Integer size) {
        Integer mostValue = begin + size - 1;
        int digitNumber = 0;
        for(; mostValue > 0; mostValue /= 10, ++digitNumber);
        return String.format("%0"+digitNumber+"d", value);
    }

    public static String extractNumString(String originalName, String leftSplit, String rightSplit) {
        int left = originalName.lastIndexOf(leftSplit);
        int right = originalName.lastIndexOf(rightSplit);
        return originalName.substring(left+1, right);
    }

    public static String formatFileName(String originalName, String leftSplit, String rightSplit, int digitNumber) {
        int leftIndex;
        if ("".equals(leftSplit)) {
            leftIndex = -1;
        } else {
            leftIndex = originalName.lastIndexOf(leftSplit);
        }
        int rightIndex = originalName.lastIndexOf(rightSplit);
        String numString = originalName.substring(leftIndex+1, rightIndex);
        Long number = Long.valueOf(numString);
        numString = String.format("%0"+digitNumber+"d", number);
        return originalName.substring(0, leftIndex+1).concat(numString).concat(originalName.substring(rightIndex));
    }


    public static void main(String[] args) {
//        String directoryPath = args[0];
//        String leftSplit = args[1];
//        String rightSplit = args[2];
//        String directoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.checkInFilePath, "runInput");
        String directoryPath = StringUtil.join(ConstantValues.FILE_SPLIT, Constant.trajectoriesFilePath, "runInput");
        String leftSplit = "_";
        String rightSplit = ".";
        File directoryFile = new File(directoryPath);
        File[] files = directoryFile.listFiles(new TxtFilter());
        // 假设从文件编号从0开始
        int digitNum = 0, fileQuantity = files.length - 1;
        for (; fileQuantity > 0; ++digitNum){
            fileQuantity /= 10;
        } // fileQuantity不会是0
        String tempOldName, tempNewName;
        File tempNewFile;
        for (File file : files) {
            tempOldName = file.getName();
            tempNewName = formatFileName(tempOldName, leftSplit, rightSplit, digitNum);
            tempNewFile = new File(file.getParentFile(), tempNewName);
            file.renameTo(tempNewFile);
        }
    }

    public static void main2(String[] args) {
        String result = getFormatNumberString(12, 5, 100);
        System.out.println(result);
    }

    public static void main3(String[] args) {
        String originalName = "time_090.txt";
        String result = extractNumString(originalName, "_", ".");
        System.out.println(result);

    }
}
