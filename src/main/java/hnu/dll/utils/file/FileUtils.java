package hnu.dll.utils.file;

import hnu.dll._config.Constant;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

public class FileUtils {
    public static File[] listFilesByFileName(File dirFile, FileFilter fileFilter) {
        File[] files = dirFile.listFiles(fileFilter);
        Arrays.sort(files, Comparator.comparing(File::getName));
        return files;
    }

}
