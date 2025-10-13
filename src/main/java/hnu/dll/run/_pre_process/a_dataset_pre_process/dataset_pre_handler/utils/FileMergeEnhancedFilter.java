package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.utils;


import java.io.File;
import java.io.FileFilter;

public class FileMergeEnhancedFilter implements FileFilter {
    private Long fileNumberStart = null;
    private Long fileNumberEnd = null;

    public FileMergeEnhancedFilter(Long fileNumberStart, Long fileNumberEnd) {
        this.fileNumberStart = fileNumberStart;
        this.fileNumberEnd = fileNumberEnd;
    }

    @Override
    public boolean accept(File file) {
        String fileName = file.getName();
        if (!fileName.endsWith(".txt")) {
            return false;
        }
        String fileNumberString = fileName.split("_")[1].split("\\.")[0];
        Long fileNumber = Long.valueOf(fileNumberString);
        if (fileNumber < this.fileNumberStart || fileNumber > fileNumberEnd) {
            return false;
        }
        return true;
    }
}
