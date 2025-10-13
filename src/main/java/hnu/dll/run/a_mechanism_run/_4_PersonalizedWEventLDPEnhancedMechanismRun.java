package hnu.dll.run.a_mechanism_run;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.metric.Measurement;
import hnu.dll.schemes.main_scheme.EnhancedPLPMechanism;
import hnu.dll.utils.BasicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class _4_PersonalizedWEventLDPEnhancedMechanismRun {
    /**
     * dataList 的外层List代表timestamp，内层list代表user
     */

    public static ExperimentResult runBatch(EnhancedPLPMechanism scheme, Integer batchID, List<List<Integer>> batchDataList, List<Map<Integer, Double>> rawPublicationBatchList) {
        List<Double> privacyBudgetList = scheme.getDistinctBudgetList();
        List<Integer> windowSizeList = scheme.getDistinctWindowSizeList();
        List<Integer> domainIndexList = scheme.getDomainIndexList();
        ExperimentResult experimentResult = new ExperimentResult();
        int timeBatchSize = batchDataList.size();
        List<Double> rawPublicationData;
        List<Double> publicationData;
        long startTime, endTime, timeCost;
        double batchTotalNormSquare = 0;
        double batchTotalDivergence = 0;
        CombinePair<Boolean, Map<Integer, Double>> estimationPair;

        List<Map<Integer, Double>> publicationList = new ArrayList<>(timeBatchSize);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < timeBatchSize; i++) {
            estimationPair = scheme.updateNextPublicationResult(batchDataList.get(i));
            publicationList.add(estimationPair.getValue());
        }
        endTime = System.currentTimeMillis();
        timeCost = endTime - startTime;
        experimentResult.addPair(Constant.MechanismName, scheme.getSimpleName());
        experimentResult.addPair(Constant.BatchName, String.valueOf(batchID));
        experimentResult.addPair(Constant.BatchRealSize, String.valueOf(timeBatchSize));
        experimentResult.addPair(Constant.TimeCost, String.valueOf(timeCost));
        experimentResult.addPair(Constant.PrivacyBudget, String.valueOf(BasicArrayUtil.getDoubleMinValue(privacyBudgetList)));
        int maximalWindowSizeValue = BasicArrayUtil.getIntegerMaxValue(windowSizeList);
        experimentResult.addPair(Constant.WindowSize, String.valueOf(maximalWindowSizeValue));
        for (int i = 0; i < timeBatchSize; i++) {
            rawPublicationData = BasicUtils.toSortedListByKeys(rawPublicationBatchList.get(i), domainIndexList, 0D);
            publicationData = BasicUtils.toSortedListByKeys(publicationList.get(i), domainIndexList, 0D);
            batchTotalNormSquare += Measurement.get2NormSquareError(rawPublicationData, publicationData);
            batchTotalDivergence += Measurement.getJSDivergence(rawPublicationData, publicationData);
        }
        experimentResult.addPair(Constant.BRE, String.valueOf(batchTotalNormSquare));
        experimentResult.addPair(Constant.BJSD, String.valueOf(batchTotalDivergence));
        return experimentResult;
    }
}
