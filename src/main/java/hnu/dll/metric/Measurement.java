package hnu.dll.metric;

import cn.edu.dll.basic.BasicArrayUtil;

public class Measurement {
    public static Double getMeanAbsoluteError(Double[] rawData, Double[] sanitizedData) {
        int size = rawData.length;
        if (sanitizedData.length != size) {
            throw new RuntimeException("The lengths of raoData and sanitizedData are not equal!");
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
            throw new RuntimeException("The lengths of raoData and sanitizedData are not equal!");
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
            throw new RuntimeException("The lengths of raoData and sanitizedData are not equal!");
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
            throw new RuntimeException("The lengths of raoData and sanitizedData are not equal!");
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
