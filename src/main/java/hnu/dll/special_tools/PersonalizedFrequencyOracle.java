package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.FrequencyOracle;
import cn.edu.dll.struct.pair.CombinePair;

import java.lang.reflect.Constructor;
import java.util.*;

@Deprecated
public abstract class PersonalizedFrequencyOracle<T extends FrequencyOracle<Integer, Integer>> {
    protected Integer domainSize;
//    protected Integer totalUserSize;
    // 统计不同budget的频率
    protected Map<Double, Integer> distinctBudgetCountMap;
    protected Map<Double, FrequencyOracle<Integer, Integer>> distinctFrequencyOracleMap;
    protected Map<Double, Double> distinctQMap;
    protected Map<Double, Double> distinctPMap;
//    protected TreeMap<Double, Double> distinctVarianceMap;
    protected Map<Double, Double> aggregationWeightMap;

    Random random = new Random();

    protected void initializeFrequencyOracleMap(Class<T> frequencyOracleClass) {
        Set<Double> distinctBudgetSet = this.distinctBudgetCountMap.keySet();
        this.distinctFrequencyOracleMap = new TreeMap<>();
        Constructor<T> constructor;
        FrequencyOracle<Integer, Integer> tempFO;
        try {
            constructor = frequencyOracleClass.getConstructor(Double.class, Integer.class);
            for (Double budget : distinctBudgetSet) {
                tempFO = constructor.newInstance(budget, this.domainSize);
                this.distinctFrequencyOracleMap.put(budget, tempFO);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void initializeAggregationWeightMap() {
        TreeMap<Double, Double> distinctVarianceMap = new TreeMap<>();
        Double epsilon, variance;
        Integer userSize;
        Double totalVariance = 0D;
        FrequencyOracle<Integer, Integer> frequencyOracle;
        for (Map.Entry<Double, FrequencyOracle<Integer, Integer>> entry : this.distinctFrequencyOracleMap.entrySet()) {
            epsilon = entry.getKey();
            frequencyOracle = entry.getValue();
            userSize = this.distinctBudgetCountMap.get(epsilon);
            variance = frequencyOracle.getApproximateVariance(userSize);
            distinctVarianceMap.put(epsilon, variance);
            totalVariance += variance;
        }
        this.aggregationWeightMap = new TreeMap<>();
        for (Map.Entry<Double, Double> entry : distinctVarianceMap.entrySet()) {
            epsilon = entry.getKey();
            variance = entry.getValue();
            this.aggregationWeightMap.put(epsilon, variance / totalVariance);
        }
    }

    public PersonalizedFrequencyOracle(Integer domainSize, Map<Double, Integer> distinctBudgetCountMap, Class<T> frequencyOracleClass) {
        this.domainSize = domainSize;
//        this.totalUserSize = 0;
        this.distinctBudgetCountMap = distinctBudgetCountMap;
//        for (Integer count : this.distinctBudgetCountMap.values()) {
//            this.totalUserSize += count;
//        }
        initializeFrequencyOracleMap(frequencyOracleClass);
        initializeAggregationWeightMap();
    }

    public Integer getDomainSize() {
        return domainSize;
    }

    public Map<Double, Integer> getDistinctBudgetCountMap() {
        return distinctBudgetCountMap;
    }

    public Map<Double, FrequencyOracle<Integer, Integer>> getDistinctFrequencyOracleMap() {
        return distinctFrequencyOracleMap;
    }

    public Map<Double, Double> getDistinctQMap() {
        return distinctQMap;
    }

    public Map<Double, Double> getDistinctPMap() {
        return distinctPMap;
    }

    public Map<Double, Double> getAggregationWeightMap() {
        return aggregationWeightMap;
    }

    public Random getRandom() {
        return random;
    }

    public Map<Double, List<Integer>> perturb(Map<Double, List<Integer>> originalDataMap) {
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
            frequencyOracle = this.distinctFrequencyOracleMap.get(epsilon);
            for (Integer data : originalDataList) {
                obfuscatedData = frequencyOracle.perturb(data);
                obfuscatedDataList.add(obfuscatedData);
            }
            result.put(epsilon, obfuscatedDataList);
        }
        return result;
    }




    public CombinePair<Map<Double, List<Integer>>, Integer> rePerturb(TreeMap<Double, List<Integer>> perturbedDataMap) {
        return PFOUtils.rePerturb(perturbedDataMap, domainSize, random);
    }

    public List<Double> aggregate(Map<Double, List<Integer>> obfuscatedReportData, Integer totalUserSize) {
        Double epsilon, tempWeight, frequency;
        Integer domainIndex, tempCount;
        List<Integer> obfuscatedReportDataList;
        FrequencyOracle<Integer, Integer> frequencyOracle;
        List<Double> domainFrequencyList = BasicArrayUtil.getInitializedList(0D, domainSize);
        Map<Integer, Integer> aggregateMap;
        for (Map.Entry<Double, List<Integer>> entry : obfuscatedReportData.entrySet()) {
            epsilon = entry.getKey();
            obfuscatedReportDataList = entry.getValue();
            tempWeight = this.aggregationWeightMap.get(epsilon);
            frequencyOracle = this.distinctFrequencyOracleMap.get(epsilon);
            aggregateMap = frequencyOracle.aggregate(obfuscatedReportDataList);
            for (Map.Entry<Integer, Integer> innerEntry : aggregateMap.entrySet()) {
                domainIndex = innerEntry.getKey();
                tempCount = innerEntry.getValue();
                frequency = domainFrequencyList.get(domainIndex);
                frequency += tempCount * tempWeight;
                domainFrequencyList.set(domainIndex, frequency);
            }
        }
        for (int i = 0; i < domainFrequencyList.size(); ++i) {
            domainFrequencyList.set(i, domainFrequencyList.get(i) / totalUserSize);
        }
        return domainFrequencyList;
    }



    public List<Double> getEstimation(List<Double> obfucatedEstimationList, Integer totalUserSize) {
        Double paramA = 0D, paramB = 0D;
        Double tempG;
        Double tempQ, tempP;
        Double tempWeight;
        Set<Double> distinctEpsilonSet = this.distinctBudgetCountMap.keySet();
        List<Double> result = new ArrayList<>(distinctEpsilonSet.size());
        for (Double epsilon : distinctEpsilonSet) {
            tempG = this.distinctBudgetCountMap.get(epsilon) * 1.0 / totalUserSize;
            tempQ = this.distinctQMap.get(epsilon);
            tempP = this.distinctPMap.get(epsilon);
            tempWeight = this.aggregationWeightMap.get(epsilon);
            paramA += tempWeight * tempG * tempQ;
            paramB += tempWeight * tempG * (tempP - tempQ);
        }
        for (Double tempObfuscatedEstimation : obfucatedEstimationList) {
            result.add((tempObfuscatedEstimation-paramA) / paramB);
        }
        return result;
    }

    protected abstract void initializeQPMapByBudgetList();
    public abstract Double getPLDPVarianceSum(Integer userSize);
//    public abstract Double getPLDPVarianceSumStar();
    public abstract Double getError(Integer userSize, Integer sampleSize);
}
