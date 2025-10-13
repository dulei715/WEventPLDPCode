package hnu.dll.run.d_total_run._3_poster_process_run.utils;

import ecnu.dll.utils.run.RepeatUtils;

public class CombineMultipleMainRound {
    public static void main(String[] args) {
        String inputDir = args[0];
        String outputDir = args[1];
        RepeatUtils.combineMultipleMainRound(inputDir, outputDir, 10);
    }
}
