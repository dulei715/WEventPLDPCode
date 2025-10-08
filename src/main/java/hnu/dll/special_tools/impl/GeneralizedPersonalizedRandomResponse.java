package hnu.dll.special_tools.impl;

import cn.edu.dll.differential_privacy.ldp.frequency_oracle.foImp.GeneralizedRandomizedResponse;
import hnu.dll.special_tools.PersonalizedFrequencyOracle;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Deprecated
public class GeneralizedPersonalizedRandomResponse extends PersonalizedFrequencyOracle<GeneralizedRandomizedResponse> {
    public GeneralizedPersonalizedRandomResponse(Integer domainSize, Map<Double, Integer> distinctBudgetCountMap, Class<GeneralizedRandomizedResponse> frequencyOracleClass) {
        super(domainSize, distinctBudgetCountMap, frequencyOracleClass);
        initializeQPMapByBudgetList();
    }

    @Override
    protected void initializeQPMapByBudgetList() {
        Double budget, q, p;
        this.distinctQMap = new TreeMap<>();
        this.distinctPMap = new TreeMap<>();
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            budget = entry.getKey();
            q = 1 / (Math.exp(budget) + this.domainSize - 1);
            p = q * Math.exp(budget);
            this.distinctQMap.put(budget, q);
            this.distinctPMap.put(budget, p);
        }
    }



    @Override
    public Double getPLDPVarianceSum(Integer userSize) {
        Double paramA = 0D, paramB = 0D, paramC = 0D;
        Double tempG, budget, personalizedWeight, result;
        Double tempQ, tempP;
        for (Map.Entry<Double, Integer> entry : distinctBudgetCountMap.entrySet()) {
            tempG = entry.getValue() * 1.0 / userSize;
            budget = entry.getKey();
            tempQ = this.distinctQMap.get(budget);
            tempP = this.distinctPMap.get(budget);
            personalizedWeight = this.aggregationWeightMap.get(budget);
            paramA += personalizedWeight * personalizedWeight * tempG * (1 - tempQ) * tempQ;
            paramB += personalizedWeight * tempG * (tempP - tempQ);
            paramC += personalizedWeight * personalizedWeight * tempG * (1 - tempP - tempQ) * (tempP - tempQ);
        }
        paramB = userSize * paramB * paramB;
        result = (domainSize * paramA + paramC) / paramB;
        return result;
    }

//    @Override
//    public Double getPLDPVarianceSumStar() {
//        return null;
//    }

    @Override
    public Double getError(Integer userSize, Integer sampleSize) {
        Double sampleVariance = (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
        Double pldpVariance = getPLDPVarianceSum(sampleSize);
        return (sampleVariance + pldpVariance) / this.domainSize;
    }



}
