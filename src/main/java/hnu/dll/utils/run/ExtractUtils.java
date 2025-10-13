package hnu.dll.utils.run;

import cn.edu.dll.basic.StringUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.execute.CopyUtils;
import cn.edu.dll.filter.file_filter.DirectoryFileFilter;
import cn.edu.dll.filter.file_filter.TxtFilter;
import ecnu.dll.utils.FormatFileName;

import java.io.File;

public class ExtractUtils {
    public static void extractParameters(String inputDir, String outputDir, int size) {
        File inputDirFile = new File(inputDir);
        File outputDirFile = new File(outputDir);
        //1.privacy_budget / 2.window_size
        File[] innerDirFiles = inputDirFile.listFiles(new DirectoryFileFilter());
        String fileName, strNum;
        String tempOutputPath;
        for (File innerDirFile : innerDirFiles) {
            // budget_0-2 / window_size_20
            File[] subInnerFiles = innerDirFile.listFiles(new DirectoryFileFilter());
            String innerOutputBasicPath = StringUtil.join(ConstantValues.FILE_SPLIT, outputDirFile, innerDirFile.getName());
            for (File dirFile : subInnerFiles) {
                String subOutputDirPath = StringUtil.join(ConstantValues.FILE_SPLIT, innerOutputBasicPath, dirFile.getName());
                File subOutputDirFile = new File(subOutputDirPath);
                if (!subOutputDirFile.exists()) {
                    subOutputDirFile.mkdirs();
                }
                File[] txtFiles = dirFile.listFiles(new TxtFilter());
                for (File file : txtFiles) {
                    fileName = file.getName();
                    if (fileName.startsWith("timestamp")) {
                        strNum = FormatFileName.extractNumString(fileName, "_", ".");
                        if (Integer.valueOf(strNum) >= size) {
                            continue;
                        }
                    }
                    tempOutputPath = StringUtil.join(ConstantValues.FILE_SPLIT, subOutputDirPath, file.getName());
                    CopyUtils.fileCopyWithTransferTo(file.getAbsolutePath(), tempOutputPath);
                }

            }
        }
    }

    public static void main(String[] args) {
        Integer size = Integer.valueOf(args[2]);
        extractParameters(args[0], args[1], size);
//        Integer size = 100;
//        String inputDir =  "E:\\dataset\\3.dynamicWEvent\\0_dataset\\TLNS\\group_generated_parameters";
//        String outputDir = "E:\\dataset\\3.dynamicWEvent\\0_dataset\\TLNS\\extract_parameters";
//        extractParameters(inputDir, outputDir, size);
    }
}
