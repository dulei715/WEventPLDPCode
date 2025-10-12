package hnu.dll.schemes._scheme_utils;

import java.util.ArrayList;
import java.util.List;

public class BasicSchemeUtils {
    public static List<Integer> getSamplingSizeList(List<Integer> windowSizeList) {
        Integer userSize = windowSizeList.size();
        List<Integer> samplingSizeList = new ArrayList<>(userSize);
        for (Integer windowSize : windowSizeList) {
            // Integer除法的特性保证其向下取整
            samplingSizeList.add(userSize / (2 * windowSize));
        }
        return samplingSizeList;
    }
}
