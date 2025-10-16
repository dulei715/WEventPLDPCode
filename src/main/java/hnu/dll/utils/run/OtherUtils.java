package hnu.dll.utils.run;

import cn.edu.dll.basic.NumberUtil;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import hnu.dll._config.Constant;

import java.io.File;

public class OtherUtils {
    public static File getSubDatasetNameFile(File superFile) {
        File[] dirFiles = superFile.listFiles(new DirectoryFileFilter());
        String fileName;
        for (File dirFile : dirFiles) {
            fileName = dirFile.getName();
            String[] strArrA = fileName.split("_");
            String[] strArrB = fileName.split("\\.");
            if (strArrA.length > 1 && "result".equals(fileName.split("_")[strArrA.length-1]) && strArrB.length > 1 && NumberUtil.isNumber(strArrB[0])) {
                return dirFile;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        File file = new File(Constant.SinFilePath);
        File subDatasetNameFile = getSubDatasetNameFile(file);
        System.out.println(subDatasetNameFile.getName());
    }
}
