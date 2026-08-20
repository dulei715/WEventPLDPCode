package FO_test;

import hnu.dll.special_tools.FOUtils;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class FOUtilsTest {
    @Test
    public void getUnbiasedVarianceMonteCarloTest() {
        int userSize = 1000;
        int sampleSize = 100;
        int domainSize = 5;
        double epsilon = 1.0;
        int iterations = 50000;
        Random random = new Random(42);

        double[] trueFreq = {0.3, 0.25, 0.2, 0.15, 0.1};

        List<Integer> populationData = generatePopulationData(trueFreq, userSize, domainSize, random);

        double[] actualFreq = computeFrequency(populationData, domainSize);
        double trueNormSquare = normSquare(actualFreq);

        double theta = (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
        double ldpVarSum = FOUtils.getLDPVarianceSum(epsilon, sampleSize, domainSize);
        double theoreticalVariance = theta * (1 - trueNormSquare) + ldpVarSum;

        double p = FOUtils.getGRRP(epsilon, domainSize);
        double q = FOUtils.getGRRQ(epsilon, domainSize);

        double sumUnbiasedVariance = 0;
        double sumActualMSE = 0;

        List<Integer> allIndices = new ArrayList<>(userSize);
        for (int i = 0; i < userSize; i++) {
            allIndices.add(i);
        }

        for (int iter = 0; iter < iterations; iter++) {
            Collections.shuffle(allIndices, random);

            List<Integer> sampleData = new ArrayList<>(sampleSize);
            for (int i = 0; i < sampleSize; i++) {
                sampleData.add(populationData.get(allIndices.get(i)));
            }

            List<Integer> perturbedData = FOUtils.gRRPerturb(sampleData, epsilon, domainSize, random);
            Map<Integer, Double> aggregation = FOUtils.getAggregation(perturbedData, domainSize);
            Map<Integer, Double> estimation = FOUtils.getEstimation(aggregation, p, q);

            double[] estimateFreq = new double[domainSize];
            double estimateNormSquare = 0;
            for (int k = 0; k < domainSize; k++) {
                estimateFreq[k] = estimation.getOrDefault(k, 0.0);
                estimateNormSquare += estimateFreq[k] * estimateFreq[k];
            }

            sumUnbiasedVariance += FOUtils.getUnbiasedVariance(epsilon, userSize, sampleSize, domainSize, estimateNormSquare);

            double actualMSE = 0;
            for (int k = 0; k < domainSize; k++) {
                actualMSE += Math.pow(estimateFreq[k] - actualFreq[k], 2);
            }
            sumActualMSE += actualMSE;
        }

        double avgUnbiasedVariance = sumUnbiasedVariance / iterations;
        double avgActualMSE = sumActualMSE / iterations;

        System.out.println("=== Non-uniform distribution ===");
        System.out.println("Theoretical V:        " + theoreticalVariance);
        System.out.println("Avg actual MSE:       " + avgActualMSE);
        System.out.println("Avg unbiasedVar:      " + avgUnbiasedVariance);
        System.out.println("Rel diff (MSE vs theory):      " + Math.abs(avgActualMSE - theoreticalVariance) / theoreticalVariance);
        System.out.println("Rel diff (unbiased vs theory): " + Math.abs(avgUnbiasedVariance - theoreticalVariance) / theoreticalVariance);

        assertEquals("Monte Carlo MSE ≈ theoretical V", theoreticalVariance, avgActualMSE, theoreticalVariance * 0.05);
        assertEquals("getUnbiasedVariance ≈ theoretical V", theoreticalVariance, avgUnbiasedVariance, theoreticalVariance * 0.05);
        assertEquals("getUnbiasedVariance ≈ actual MSE", avgActualMSE, avgUnbiasedVariance, Math.abs(avgActualMSE) * 0.05);
    }

    @Test
    public void getUnbiasedVarianceUniformMonteCarloTest() {
        int userSize = 2000;
        int sampleSize = 200;
        int domainSize = 10;
        double epsilon = 0.5;
        int iterations = 50000;
        Random random = new Random(123);

        double[] trueFreq = new double[domainSize];
        Arrays.fill(trueFreq, 1.0 / domainSize);

        List<Integer> populationData = generatePopulationData(trueFreq, userSize, domainSize, random);

        double[] actualFreq = computeFrequency(populationData, domainSize);
        double trueNormSquare = normSquare(actualFreq);

        double theta = (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
        double ldpVarSum = FOUtils.getLDPVarianceSum(epsilon, sampleSize, domainSize);
        double theoreticalVariance = theta * (1 - trueNormSquare) + ldpVarSum;

        double p = FOUtils.getGRRP(epsilon, domainSize);
        double q = FOUtils.getGRRQ(epsilon, domainSize);

        double sumUnbiasedVariance = 0;
        double sumActualMSE = 0;

        List<Integer> allIndices = new ArrayList<>(userSize);
        for (int i = 0; i < userSize; i++) {
            allIndices.add(i);
        }

        for (int iter = 0; iter < iterations; iter++) {
            Collections.shuffle(allIndices, random);

            List<Integer> sampleData = new ArrayList<>(sampleSize);
            for (int i = 0; i < sampleSize; i++) {
                sampleData.add(populationData.get(allIndices.get(i)));
            }

            List<Integer> perturbedData = FOUtils.gRRPerturb(sampleData, epsilon, domainSize, random);
            Map<Integer, Double> aggregation = FOUtils.getAggregation(perturbedData, domainSize);
            Map<Integer, Double> estimation = FOUtils.getEstimation(aggregation, p, q);

            double[] estimateFreq = new double[domainSize];
            double estimateNormSquare = 0;
            for (int k = 0; k < domainSize; k++) {
                estimateFreq[k] = estimation.getOrDefault(k, 0.0);
                estimateNormSquare += estimateFreq[k] * estimateFreq[k];
            }

            sumUnbiasedVariance += FOUtils.getUnbiasedVariance(epsilon, userSize, sampleSize, domainSize, estimateNormSquare);

            double actualMSE = 0;
            for (int k = 0; k < domainSize; k++) {
                actualMSE += Math.pow(estimateFreq[k] - actualFreq[k], 2);
            }
            sumActualMSE += actualMSE;
        }

        double avgUnbiasedVariance = sumUnbiasedVariance / iterations;
        double avgActualMSE = sumActualMSE / iterations;

        System.out.println("=== Uniform distribution ===");
        System.out.println("Theoretical V:        " + theoreticalVariance);
        System.out.println("Avg actual MSE:       " + avgActualMSE);
        System.out.println("Avg unbiasedVar:      " + avgUnbiasedVariance);
        System.out.println("Rel diff (MSE vs theory):      " + Math.abs(avgActualMSE - theoreticalVariance) / theoreticalVariance);
        System.out.println("Rel diff (unbiased vs theory): " + Math.abs(avgUnbiasedVariance - theoreticalVariance) / theoreticalVariance);

        assertEquals("Monte Carlo MSE ≈ theoretical V", theoreticalVariance, avgActualMSE, theoreticalVariance * 0.05);
        assertEquals("getUnbiasedVariance ≈ theoretical V", theoreticalVariance, avgUnbiasedVariance, theoreticalVariance * 0.05);
        assertEquals("getUnbiasedVariance ≈ actual MSE", avgActualMSE, avgUnbiasedVariance, Math.abs(avgActualMSE) * 0.05);
    }

    private List<Integer> generatePopulationData(double[] freq, int userSize, int domainSize, Random random) {
        List<Integer> data = new ArrayList<>(userSize);
        double[] cumulative = new double[domainSize];
        cumulative[0] = freq[0];
        for (int k = 1; k < domainSize; k++) {
            cumulative[k] = cumulative[k - 1] + freq[k];
        }
        for (int i = 0; i < userSize; i++) {
            double r = random.nextDouble();
            for (int k = 0; k < domainSize; k++) {
                if (r < cumulative[k]) {
                    data.add(k);
                    break;
                }
            }
        }
        return data;
    }

    private double[] computeFrequency(List<Integer> data, int domainSize) {
        double[] freq = new double[domainSize];
        for (int d : data) {
            freq[d]++;
        }
        for (int k = 0; k < domainSize; k++) {
            freq[k] /= data.size();
        }
        return freq;
    }

    private double normSquare(double[] arr) {
        double sum = 0;
        for (double v : arr) {
            sum += v * v;
        }
        return sum;
    }
}
