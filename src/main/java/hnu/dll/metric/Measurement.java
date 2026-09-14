package hnu.dll.metric;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.constant_values.ConstantValues;
import cn.edu.dll.statistic.StatisticTool;

import java.util.List;

public class Measurement {

    public static Double get2NormSquareError(List<Double> rawData, List<Double> sanitizedData) {
        int size = rawData.size();
        if (sanitizedData.size() != size) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        Double squareSum = 0D;
        for (int i = 0; i < size; i++) {
            squareSum += Math.pow(sanitizedData.get(i) - rawData.get(i), 2);
        }
        return squareSum;
    }
    public static Double getMRE(List<Double> rawData, List<Double> sanitizedData) {
        int size = rawData.size();
        if (sanitizedData.size() != size) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        Double squareSum = 0D;
        for (int i = 0; i < size; i++) {
            if (Math.abs(rawData.get(i)) < ConstantValues.DOUBLE_PRECISION) {
                continue;
            }
            squareSum += Math.abs((rawData.get(i) - sanitizedData.get(i)) / rawData.get(i));
        }
        return squareSum / size;
    }

    public static Double get2NormSquareErrorSum(List<List<Double>> rawDataList, List<List<Double>> sanitizedDataList) {
        int size = rawDataList.size();
        if (sanitizedDataList.size() != size) {
            throw new RuntimeException("The lengths of rawDataList and sanitizedDataList are not equal!");
        }
        Double sum = 0D;
        for (int i = 0; i < size; i++) {
            sum += get2NormSquareError(rawDataList.get(i), sanitizedDataList.get(i));
        }
        return sum;
    }

    /**
     * 计算两个"分布"之间的 JS 散度。
     *
     * 注意：JS 散度是对两个分布整体定义的量（JSD(P||Q) <= ln2），不能按元素逐个计算再求和。
     * 这里原先写成 for 循环累加，但循环体里传的是整个 rawData / sanitizedData 而不是第 i 个分量，
     * 导致结果被放大了 domainSize 倍，JSD 会随 d 虚假膨胀并突破 ln2 的理论上界。
     *
     * @param rawData       真实分布（一次发布的 d 维频率分布）
     * @param sanitizedData 估计分布
     * @return 两个分布的 JS 散度
     */
    public static Double getJSDivergence(List<Double> rawData, List<Double> sanitizedData) {
        if (sanitizedData.size() != rawData.size()) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        return StatisticTool.getJSStatisticDivergence(rawData, sanitizedData);
    }

    public static Double getJSDivergenceErrorSum(List<List<Double>> rawDataList, List<List<Double>> sanitizedDataList) {
        int size = rawDataList.size();
        if (sanitizedDataList.size() != size) {
            throw new RuntimeException("The lengths of rawDataList and sanitizedDataList are not equal!");
        }
        Double sum = 0D;
        for (int i = 0; i < size; i++) {
            sum += getJSDivergence(rawDataList.get(i), sanitizedDataList.get(i));
        }
        return sum;
    }

    public static Double getMeanAbsoluteError(Double[] rawData, Double[] sanitizedData) {
        int size = rawData.length;
        if (sanitizedData.length != size) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += Math.abs(sanitizedData[i] - rawData[i]);
        }
        return sum / size;
    }
    public static Double getMeanRelativeError(Double[] rawData, Double[] sanitizedData) {
        int size = rawData.length;
        if (sanitizedData.length != size) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        Double rawSum = BasicArrayUtil.getSum(rawData);
        double gamma = rawSum * 0.001;
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += Math.abs(sanitizedData[i] - rawData[i]) / Math.max(gamma, rawData[i]);
        }
        return sum / size;
    }

    public static double[] getMeanAbsoluteError(Double[][] rawData, Double[][] sanitizedData) {
        int rowSize = rawData.length;
        int colSize = rawData[0].length;
        if (sanitizedData.length != rowSize || sanitizedData[0].length != colSize) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        double[] sumArray = new double[rowSize];
        double tempSum;
        for (int i = 0; i < rowSize; i++) {
            tempSum = 0;
            for (int j = 0; j < colSize; j++) {
                tempSum += Math.abs(sanitizedData[i][j] - rawData[i][j]);
            }
            sumArray[i] = tempSum / colSize;
        }
        return sumArray;
    }
    public static double[] getMeanRelativeError(Double[][] rawData, Double[][] sanitizedData) {
        int rowSize = rawData.length;
        int colSize = rawData[0].length;
        if (sanitizedData.length != rowSize || sanitizedData[0].length != colSize) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        double[] sumArray = new double[rowSize];
        double tempSum, tempRowSum, tempGamma;
        for (int i = 0; i < rowSize; i++) {
            tempSum = 0;
            tempRowSum = BasicArrayUtil.getSum(rawData[i]);
            tempGamma = tempRowSum * 0.001;
            for (int j = 0; j < colSize; j++) {
                tempSum += Math.abs(sanitizedData[i][j] - rawData[i][j]) / Math.max(tempGamma, rawData[i][j]);
            }
            sumArray[i] = tempSum / colSize;
        }
        return sumArray;
    }

    



}
