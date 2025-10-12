package hnu.dll.dataset;

import java.util.Random;

public class DataSetHandler {
    public static Double[] getLinearNormalSequence(Double startValue, Double noiseStandardVariance, int size) {
        Double[] result = new Double[size];
        Random random = new Random();
        result[0] = startValue + random.nextGaussian()*noiseStandardVariance;
        for (int i = 1; i < size; i++) {
            result[i] = result[i-1] + random.nextGaussian()*noiseStandardVariance;
        }
        return result;
    }
    public static Double[] getSinSequence(Double amplitude, Double angularVelocity, Double initialY, int size) {
        Double[] result = new Double[size];
        for (int i = 0; i < size; i++) {
            result[i] = amplitude * Math.sin(angularVelocity * (i+1))+initialY;
        }
        return result;
    }
    public static Double[] getLogSequence(Double valueA, Double valueB, int size) {
        Double[] result = new Double[size];
        for (int i = 0; i < size; i++) {
            result[i] = valueA / (1 + Math.exp(-valueB * (i+1)));
        }
        return result;
    }
}
