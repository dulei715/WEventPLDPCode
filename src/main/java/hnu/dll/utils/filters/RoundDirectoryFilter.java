package hnu.dll.utils.filters;

import java.io.File;
import java.io.FileFilter;

public class RoundDirectoryFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory() && file.getName().startsWith("round");
    }
}
