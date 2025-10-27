package important_test;

import cn.edu.dll.basic.BasicArrayUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IntroductionTest {
    public static Double getGRRStandardApproximateVariance(Double epsilon, Integer windowSize, Integer userSize, Integer domainSize) {
        Double ePower = Math.exp(epsilon / windowSize);
        return (domainSize - 2 + ePower) * userSize / Math.pow(ePower - 1, 2);
    }
    public static Double getGRRStandardError(Integer domainSize, Double epsilon, Integer windowSize, Integer userSize) {
        Double variance = getGRRStandardApproximateVariance(epsilon, windowSize, userSize, domainSize);
        return Math.sqrt(variance / userSize);
    }
    public static Double getGRRStandardError(Integer domainSize, Double epsilon, List<Integer> windowSizeList, List<Integer> userSizeList) {
        int size = windowSizeList.size();
        if (size != userSizeList.size()) {
            throw new RuntimeException("The sizes are not equal!");
        }
        Double totalVariance = 0D;
        Integer totalUserSize = 0, tempUserSize;
        for (int i = 0; i < size; i++) {
            tempUserSize = userSizeList.get(i);
            totalUserSize += tempUserSize;
            totalVariance += getGRRStandardApproximateVariance(epsilon, windowSizeList.get(i), tempUserSize, domainSize);
        }
        return Math.sqrt(totalVariance / totalUserSize);
    }
    @Test
    public void fun1() {
        Integer domainSize = 5;
        Integer userSize = 100;
        Double epsilon = 1.0;
        Integer windowSize = 8;
        Double standardError = getGRRStandardError(domainSize, epsilon, windowSize, userSize);
        System.out.println(standardError);
//        System.out.println(standardError / Math.sqrt(userSize));
    }
    @Test
    public void fun2() {
        Integer domainSize = 5;
        Double epsilon = 1.0;
        List<Integer> windowSizeList = Arrays.asList(4, 8);
        List<Integer> userSizeList = Arrays.asList(98, 2);
        Integer totalUserSize = BasicArrayUtil.getSum(userSizeList.toArray(new Integer[0]));
        Double standardError = getGRRStandardError(domainSize, epsilon, windowSizeList, userSizeList);
        System.out.println(totalUserSize);
        System.out.println(standardError);
//        System.out.println(standardError / Math.sqrt(totalUserSize));
    }
}
