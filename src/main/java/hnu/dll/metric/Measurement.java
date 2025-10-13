package hnu.dll.metric;

import cn.edu.dll.basic.BasicArrayUtil;
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

    public static Double getJSDivergence(List<Double> rawData, List<Double> sanitizedData) {
        int size = rawData.size();
        if (sanitizedData.size() != size) {
            throw new RuntimeException("The lengths of rawData and sanitizedData are not equal!");
        }
        Double jsSum = 0D;
        for (int i = 0; i < size; i++) {
            jsSum += StatisticTool.getJSStatisticDivergence(rawData, sanitizedData);
        }
        return jsSum;
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
