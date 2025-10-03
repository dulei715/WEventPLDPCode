package hnu.dll.special_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.FrequencyOracle;
import cn.edu.dll.struct.BasicPair;
import cn.edu.dll.struct.CombinePair;

import java.lang.reflect.Constructor;
import java.util.*;


public abstract class PersonalizedFrequencyOracle<T extends FrequencyOracle<Integer, Integer>> {
    protected Integer domainSize;
//    protected Integer totalUserSize;
    // 统计不同budget的频率
    protected TreeMap<Double, Integer> distinctBudgetCountMap;
    protected TreeMap<Double, FrequencyOracle<Integer, Integer>> distinctFrequencyOracleMap;
    protected TreeMap<Double, Double> distinctQMap;
    protected TreeMap<Double, Double> distinctPMap;
//    protected TreeMap<Double, Double> distinctVarianceMap;
    protected TreeMap<Double, Double> aggregationWeightMap;

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

    public PersonalizedFrequencyOracle(Integer domainSize, TreeMap<Double, Integer> distinctBudgetCountMap, Class<T> frequencyOracleClass) {
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

    public TreeMap<Double, Integer> getDistinctBudgetCountMap() {
        return distinctBudgetCountMap;
    }

    public TreeMap<Double, FrequencyOracle<Integer, Integer>> getDistinctFrequencyOracleMap() {
        return distinctFrequencyOracleMap;
    }

    public TreeMap<Double, Double> getDistinctQMap() {
        return distinctQMap;
    }

    public TreeMap<Double, Double> getDistinctPMap() {
        return distinctPMap;
    }

    public TreeMap<Double, Double> getAggregationWeightMap() {
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
        Integer totalSize = 0;
        // 保证 epsilon 从小到大排列
        List<Double> epsilonSortedList = new ArrayList<>(perturbedDataMap.keySet());
        Integer epsilonListSize = epsilonSortedList.size();
        FrequencyOracle<Integer, Integer> smallFO, largeFO;
        Double smallEpsilon, largeEpsilon, smallP, smallQ, largeP, largeQ;
        BasicPair<Double, Double> tempPair;
        Double alpha;
        Integer rePerturbIndex;
        List<Integer> currentObfuscatedList;
        TreeMap<Double, List<Integer>> enhancedMap = new TreeMap<>();
        List<Integer> enhancedIndexList;
        for (int i = 0; i < epsilonListSize; ++i) {
            enhancedIndexList = new ArrayList<>();
            smallEpsilon = epsilonSortedList.get(i);
            smallFO = this.distinctFrequencyOracleMap.get(smallEpsilon);
            smallP = smallFO.getP();
//            smallQ = smallFO.getQ();
            currentObfuscatedList = perturbedDataMap.get(epsilonSortedList.get(i));
            enhancedIndexList.addAll(currentObfuscatedList);
            for (int j = i + 1; j < epsilonListSize; ++j) {
                largeEpsilon = epsilonSortedList.get(j);
                largeFO = this.distinctFrequencyOracleMap.get(largeEpsilon);
                largeP = largeFO.getP();
                largeQ = largeFO.getQ();
                // todo:这里只实现了GRR的enhancement
                tempPair = PerturbUtils.getGRRRePerturbParameters(smallP, smallP, largeP, largeQ, domainSize);
                alpha = tempPair.getKey();
//                beta = tempPair.getValue();
                rePerturbIndex = PerturbUtils.grrPerturb(domainSize, i, alpha, random);
                enhancedIndexList.add(rePerturbIndex);
            }
            enhancedMap.put(smallEpsilon, enhancedIndexList);
            totalSize += enhancedIndexList.size();
        }
        return new CombinePair<>(enhancedMap, totalSize);
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

    public abstract void initializeQPMapByBudgetList();
    public abstract Double getPLDPVarianceSum(Integer userSize);
//    public abstract Double getPLDPVarianceSumStar();
    public abstract Double getError(Integer userSize, Integer sampleSize);
}
