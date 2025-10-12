package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.FrequencyOracle;
import cn.edu.dll.struct.pair.BasicPair;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public class PFOUtils {
    /**
     * 1. 根据不同的隐私预算集合和取值域计算GRR机制下参数Q的集合
     * @param privacyBudgetSet
     * @param valueDomainSize
     * @return
     */
    public static Map<Double, Double> getGeneralRandomResponseParameterQ(Set<Double> privacyBudgetSet, Integer valueDomainSize) {
        Map<Double, Double> qMap = new TreeMap<>();
        for (Double budget : privacyBudgetSet) {
            qMap.put(budget, 1.0 / (Math.exp(budget) + valueDomainSize - 1));
        }
        return qMap;
    }

    /**
     * 2. 根据不同的隐私预算集合和取值域计算GRR机制下参数P的集合
     * @param qMap
     * @return
     */
    public static Map<Double, Double> getGeneralRandomResponseParameterP(Map<Double, Double> qMap) {
        Map<Double, Double> pMap = new TreeMap<>();
        Double budget;
        for (Map.Entry<Double, Double> entry : qMap.entrySet()) {
            budget = entry.getKey();
            pMap.put(budget, Math.exp(budget) * entry.getValue());
        }
        return pMap;
    }



    /**
     * 3. 获取GRR的方差
     * 第一个是获取近似方差，即，假设f_i很小可以忽略
     * 第二个是获取平均方差，即，假设f_i = 1/d，这个用来计算聚合权重alpha的
     * @param epsilon
     * @param userSize
     * @param domainSize
     * @return
     */
//    public static Double getGeneralizedRandomResponseApproximateVariance(Double epsilon, Integer userSize, Integer domainSize) {
//        Double ePowEpsilon = Math.exp(epsilon);
//        return userSize * (ePowEpsilon + domainSize - 2) / Math.pow(ePowEpsilon - 1, 2);
//    }
    public static Double getGeneralizedRandomResponseApproximateFrequencyVariance(Double epsilon, Integer userSize, Integer domainSize) {
        Double ePowEpsilon = Math.exp(epsilon);
        return (ePowEpsilon + domainSize - 2) / Math.pow(ePowEpsilon - 1, 2) / userSize;
    }
    @Deprecated
    public static Double getGeneralizedRandomResponseAverageVariance(Double epsilon, Integer userSize, Integer domainSize) {
        Double ePowEpsilon = Math.exp(epsilon);
        Double q = 1 / (ePowEpsilon + domainSize - 1);
        Double p = q * ePowEpsilon;
        return q * (1 - q) / Math.pow(p - q, 2) / userSize + (1 - p - q) / (p - q) / userSize / domainSize;
    }
    private static BasicPair<Double, Double> getLambdaMuParameters(Double epsilon, Integer userSize, Integer domainSize) {
        Double ePowEpsilon = Math.exp(epsilon);
        Double q = 1 / (ePowEpsilon + domainSize - 1);
        Double p = q * ePowEpsilon;
        return new BasicPair<>(q * (1 - q) / Math.pow(p - q, 2) / userSize, (1 - p - q) / (p - q) / userSize);
    }

    /**
     * 4.1 计算单类epsilon_k中的GRR方差的两个参数
     * 对于任意一个f_j，方差是 q_k(1-q_k)/(n_k(p_k-q_k)^2) + (1-p_k-q_k)/(n_k(p_k-q_k))f_j
     * lamda = q_k(1-q_k)/(n_k(p_k-q_k)^2)
     * mu = (1-p_k-q_k)/(n_k(p_k-q_k))
     * @return
     */
    public static Map<Double, BasicPair<Double, Double>> getLambdaMuPairMap(Map<Double, Integer> distinctBudgetCountMap, Integer domainSize) {
        Double epsilon;
        Integer groupUserSize;
        BasicPair<Double, Double> tempPair;
        Map<Double, BasicPair<Double, Double>> result = new TreeMap<>();
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            epsilon = entry.getKey();
            groupUserSize = entry.getValue();
            tempPair = getLambdaMuParameters(epsilon, groupUserSize, domainSize);
            result.put(epsilon, tempPair);
        }
        return result;
    }

    /**
     * 4.2 获取个性化聚合参数（alpha）
     * 这里g是z的g
     * @param distinctBudgetCountMap
     * @param domainSize
     * @return
     */
    public static Map<Double, Double> getAggregationWeightMap(Map<Double, Integer> distinctBudgetCountMap, Integer domainSize) {
        TreeMap<Double, Double> distinctVarianceReciprocalMap = new TreeMap<>();
        Double epsilon, varianceReciprocal;
        Integer userSize;
        Double totalVarianceReciprocal = 0D;
        Map<Double, Double> aggregationWeightMap;
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            epsilon = entry.getKey();
            userSize = distinctBudgetCountMap.get(epsilon);
            varianceReciprocal = 1.0 / getGeneralizedRandomResponseAverageVariance(epsilon, userSize, domainSize);
            distinctVarianceReciprocalMap.put(epsilon, varianceReciprocal);
            totalVarianceReciprocal += varianceReciprocal;
        }
        aggregationWeightMap = new TreeMap<>();
        for (Map.Entry<Double, Double> entry : distinctVarianceReciprocalMap.entrySet()) {
            epsilon = entry.getKey();
            varianceReciprocal = entry.getValue();
            aggregationWeightMap.put(epsilon, varianceReciprocal / totalVarianceReciprocal);
        }
        return aggregationWeightMap;
    }

    public static Map<Double, Map<Integer, Double>> getAggregation(Map<Double, Integer> distinctBudgetMap, Map<Double, List<Integer>> obfuscatedMap, Integer domainSize) {
        Double epsilon;
        List<Integer> obfuscatedReportList;
        Map<Double, Map<Integer, Double>> result = new TreeMap<>();
        Map<Integer, Integer> personalziedCountMap;
        Map<Integer, Double> personalizedStatisticMap;
        List<Integer> dataIndexList = BasicArrayUtil.getIncreaseIntegerNumberList(0, 1, domainSize - 1);
        for (Map.Entry<Double, List<Integer>> entry : obfuscatedMap.entrySet()) {
            epsilon = entry.getKey();
            obfuscatedReportList = entry.getValue();
            personalziedCountMap = BasicArrayUtil.getUniqueListWithCountList(obfuscatedReportList, dataIndexList);
            personalizedStatisticMap = BasicUtils.getStatisticByCount(personalziedCountMap);
            result.put(epsilon, personalizedStatisticMap);
        }
        return result;
    }


    /**
     * 5. 获取估计值
     * @param obfuscatedEstimationMap
     * @param distinctPMap
     * @param distinctQMap
     * @param aggregationWeightMap
     * @return
     */
    public static Map<Integer, Double> getEstimation(Map<Double, Map<Integer, Double>> obfuscatedEstimationMap, Map<Double, Double> distinctPMap, Map<Double, Double> distinctQMap, Map<Double, Double> aggregationWeightMap) {
        Double tempQ, tempP;
        Double epsilon, tempStatistic, alreadyStatistic, tempWeighted;
        Integer valueIndex;
        Map<Integer, Double> groupStatisticMap;
        Map<Integer, Double> result = new TreeMap<>();

        for (Map.Entry<Double, Map<Integer, Double>> entry : obfuscatedEstimationMap.entrySet()) {
            epsilon = entry.getKey();
            groupStatisticMap = entry.getValue();
            tempWeighted = aggregationWeightMap.get(epsilon);
            tempP = distinctPMap.get(epsilon);
            tempQ = distinctQMap.get(epsilon);
            for (Map.Entry<Integer, Double> innerEntry : groupStatisticMap.entrySet()) {
                valueIndex = innerEntry.getKey();
                tempStatistic = innerEntry.getValue();
                alreadyStatistic = result.getOrDefault(valueIndex, 0D);
                alreadyStatistic += tempWeighted * (tempStatistic - tempQ) / (tempP - tempQ);
                result.put(valueIndex, alreadyStatistic);
            }
        }
        return result;
    }

    /**
     * 6.1. 获取每个取值的方差之和
     * 第一个函数根据具体的用户估计信息计算精确的方差和（用于dis计算和error计算）, 其第一个参数是已经抽样后的用户分组结果
     * 第二个函数根据用户分组占比计算近似的方差和 （用于optimal sampling selection计算），其一个参数是所有用户的分组结果，第二个参数是对应的抽样用户占比
     * @param domainSize
     * @return
     */
    public static Double getPLDPVarianceSumBySpecificUsers(Map<Double, Integer> distinctBudgetCountMap, Integer domainSize) {
        Double lambda, mu, totalVariance = 0D;
        BasicPair<Double, Double> tempPair;
        Double epsilon;
        Integer userSize;
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            epsilon = entry.getKey();
            userSize = entry.getValue();
            tempPair = getLambdaMuParameters(epsilon, userSize, domainSize);
            lambda = tempPair.getKey();
            mu = tempPair.getValue();
            totalVariance += 1.0 / (domainSize * lambda + mu);
        }
        totalVariance = 1.0 / totalVariance;
        return totalVariance;
    }

    public static Double getPLDPVarianceSumByGroupUserRatio(Integer totalUserSize, Map<Double, Double> groupRatioMap, Integer domainSize) {
        Double lambda, mu, totalVariance = 0D;
        BasicPair<Double, Double> tempPair;
        Double epsilon, userRatio;
        Integer userSize;
        for (Map.Entry<Double, Double> entry : groupRatioMap.entrySet()) {
            epsilon = entry.getKey();
            userRatio = entry.getValue();
            userSize = (int) Math.round(totalUserSize * userRatio);
            tempPair = getLambdaMuParameters(epsilon, userSize, domainSize);
            lambda = tempPair.getKey();
            mu = tempPair.getValue();
            totalVariance += 1.0 / (domainSize * lambda + mu);
        }
        totalVariance = 1.0 / totalVariance;
        return totalVariance;
    }

    /**
     * 6.2 获取采样误差
     * @param userSize
     * @param sampleSize
     * @return
     */
    public static Double getSamplingVariance(Integer userSize, Integer sampleSize) {
        return (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
    }

    /**
     * 6.3. 计算Error
     * @param userSize
     * @param sampleSize
     * @param domainSize
     * @return
     */
    public static Double getGPRRErrorBySpecificUsers(Map<Double, Integer> distinctBudgetSamplingCountMap, Integer userSize, Integer sampleSize, Integer domainSize) {
        // 假设重扰动不影响sampling带来的误差。那么传入的userSize是重扰动前的userSize
        Double sampleVariance = getSamplingVariance(userSize, sampleSize);
        Double pldpVariance = getPLDPVarianceSumBySpecificUsers(distinctBudgetSamplingCountMap, domainSize);
        return (sampleVariance + pldpVariance) / domainSize;
    }
    public static Double getGPRRErrorByGroupUserRatio(Map<Double, Double> distinctBudgetRatioMap, Integer userSize, Integer sampleSize, Integer domainSize) {
        // 该方法只用于optimal population selection，这里假设不考虑重扰动。那么传入的userSize就是整体的userSize
        Double sampleVariance = getSamplingVariance(userSize, sampleSize);
        Double pldpVariance = getPLDPVarianceSumByGroupUserRatio(sampleSize, distinctBudgetRatioMap, domainSize);
        return (sampleVariance + pldpVariance) / domainSize;
    }

    /**
     * 7. 计算dissimilarity
     * @param estimationList
     * @param lastEstimationList
     * @param pldpVarianceSum
     * @return
     */
    public static Double getDissimilarity(List<Double> estimationList, List<Double> lastEstimationList, Double pldpVarianceSum) {
        int size = estimationList.size();
        Double differSum = 0D;
        for (int i = 0; i < size; i++) {
            differSum += Math.pow(estimationList.get(i) - lastEstimationList.get(i), 2);
        }
        return (differSum - pldpVarianceSum) / size;
    }

    /**
     * 8. 扰动
     * @param originalDataMap
     * @return
     */
    public static Map<Double, List<Integer>> perturb(Map<Double, List<Integer>> originalDataMap, Integer domainSize, Random random) {
        Double epsilon;
        List<Integer> originalDataList;
        List<Integer> obfuscatedDataList;
        FrequencyOracle<Integer, Integer> frequencyOracle;
        Integer obfuscatedData;
        Map<Double, List<Integer>> result = new TreeMap<>();
        for (Map.Entry<Double, List<Integer>> entry : originalDataMap.entrySet()) {
            epsilon = entry.getKey();
            originalDataList = entry.getValue();
            obfuscatedDataList = new ArrayList<>(originalDataList.size());
//            frequencyOracle = this.distinctFrequencyOracleMap.get(epsilon);
            for (Integer data : originalDataList) {
//                obfuscatedData = frequencyOracle.perturb(data);
                obfuscatedData = FOUtils.gRRPerturb(epsilon, data, domainSize, random);
                obfuscatedDataList.add(obfuscatedData);
            }
            result.put(epsilon, obfuscatedDataList);
        }
        return result;
    }


    /**
     * 9. 计算重扰动
     * @param perturbedDataMap
     * @return
     */
    public static CombinePair<Map<Double, List<Integer>>, Integer> rePerturb(TreeMap<Double, List<Integer>> perturbedDataMap, Integer domainSize, Random random) {
        Integer totalSize = 0;
        // 保证 epsilon 从小到大排列
        List<Double> epsilonSortedList = new ArrayList<>(perturbedDataMap.keySet());
        Integer epsilonListSize = epsilonSortedList.size();
//        FrequencyOracle<Integer, Integer> smallFO, largeFO;
        Double smallEpsilon, largeEpsilon, smallP, smallQ, largeP, largeQ;
        BasicPair<Double, Double> tempPair;
        Double alpha;
        Integer rePerturbIndex;
        List<Integer> currentObfuscatedList, tempObfuscatedList;
        TreeMap<Double, List<Integer>> enhancedMap = new TreeMap<>();
        List<Integer> enhancedIndexList;
        for (int i = 0; i < epsilonListSize; ++i) {
            enhancedIndexList = new ArrayList<>();
            smallEpsilon = epsilonSortedList.get(i);
            smallP = FOUtils.getGRRP(smallEpsilon, domainSize);
            smallQ = FOUtils.getGRRQ(smallEpsilon, domainSize);
            currentObfuscatedList = perturbedDataMap.get(epsilonSortedList.get(i));
            enhancedIndexList.addAll(currentObfuscatedList);
            for (int j = i + 1; j < epsilonListSize; ++j) {
                largeEpsilon = epsilonSortedList.get(j);
                largeP = FOUtils.getGRRP(largeEpsilon, domainSize);
                largeQ = FOUtils.getGRRQ(largeEpsilon, domainSize);
                // todo:这里只实现了GRR的enhancement
                tempPair = PerturbUtils.getGRRRePerturbParameters(smallP, smallQ, largeP, largeQ, domainSize);
                alpha = tempPair.getKey();
                tempObfuscatedList = perturbedDataMap.get(epsilonSortedList.get(j));
                for (Integer tempIndex : tempObfuscatedList) {
                    rePerturbIndex = FOUtils.gRRPerturb(alpha, tempIndex, domainSize, random);
                    enhancedIndexList.add(rePerturbIndex);
                }

            }
            enhancedMap.put(smallEpsilon, enhancedIndexList);
            totalSize += enhancedIndexList.size();
        }
        return new CombinePair<>(enhancedMap, totalSize);
    }






    public static void main(String[] args) throws InstantiationException, IllegalAccessException {

    }
}
