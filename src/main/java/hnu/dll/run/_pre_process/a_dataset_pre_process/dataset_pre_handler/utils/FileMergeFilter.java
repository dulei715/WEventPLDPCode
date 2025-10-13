package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils;


import java.io.File;
import java.io.FileFilter;

public class FileMergeFilter implements FileFilter {
    private Long fileNumber = null;

    public FileMergeFilter(Long fileNumber) {
        this.fileNumber = fileNumber;
    }

    @Override
    public boolean accept(File file) {
        String fileName = file.getName();
        if (!fileName.endsWith(".txt")) {
            return false;
        }
        String fileNumberString = fileName.split("_")[1].split("\\.")[0];
        Long fileNumber = Long.valueOf(fileNumberString);
        if (fileNumber < this.fileNumber) {
            return false;
        }
        return true;
    }
}
