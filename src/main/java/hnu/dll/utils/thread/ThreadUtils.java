package hnu.dll.utils.thread;

import cn.edu.dll.io.print.MyPrint;

import java.util.ArrayList;
import java.util.List;

public class ThreadUtils {
    public static List<Integer> getThreadStartIndexList(int threadSizeUpperBound, Integer originalStartIndex, Integer dataLength) {
        int unitSize = (int) Math.ceil(dataLength * 1.0 / threadSizeUpperBound);
        List<Integer> result = new ArrayList<>();
        int endIndex = originalStartIndex + dataLength - 1;
        for (int i = originalStartIndex; i <= endIndex; i += unitSize) {
            result.add(i);
        }
        return result;
    }

    public static void main(String[] args) {
        List<Integer> threadStartList = getThreadStartIndexList(2, 2, 5);
        MyPrint.showList(threadStartList);
    }
}
