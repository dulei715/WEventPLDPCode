package hnu.dll.run.a_mechanism_run;

import cn.edu.dll.collection.ListUtils;
import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.statistic.StatisticTool;
import ecnu.dll._config.Constant;
import ecnu.dll.schemes.main_scheme.a_optimal_fixed_window_size.ldp.PersonalizedLDPEventMechanism;
import ecnu.dll.struts.stream_data.StreamCountData;
import ecnu.dll.struts.stream_data.StreamDataElement;
import ecnu.dll.struts.stream_data.StreamNoiseCountData;

import java.util.ArrayList;
import java.util.List;

public class _4_LDPPersonalizedEventMechanismRun {
    /**
     * dataList 的外层List代表timestamp，内层list代表user
     */
    public static ExperimentResult runBatch(PersonalizedLDPEventMechanism scheme, Integer batchID, List<List<StreamDataElement<Boolean>>> batchDataList, List<StreamCountData> rawPublicationBatchList) {
        List<Double> privacyBudgetList = scheme.getPrivacyBudgetList();
        List<Integer> windowSizeList = scheme.getWindowSizeList();
        ExperimentResult experimentResult = new ExperimentResult();
        int timeBatchSize = batchDataList.size();
        StreamCountData rawPublicationData;
        StreamNoiseCountData publicationData;
        long startTime, endTime, timeCost;
        double batchTotalVarianceStatistic = 0;
        double batchTotalDivergence = 0;
        List<StreamNoiseCountData> publicationList = new ArrayList<>(timeBatchSize);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < timeBatchSize; i++) {
            scheme.updateNextPublicationResult(batchDataList.get(i));
            publicationList.add(scheme.getReleaseNoiseCountMap());
        }
        endTime = System.currentTimeMillis();
        timeCost = endTime - startTime;
        experimentResult.addPair(Constant.MechanismName, scheme.getSimpleName());
        experimentResult.addPair(Constant.BatchName, String.valueOf(batchID));
        experimentResult.addPair(Constant.BatchRealSize, String.valueOf(timeBatchSize));
        experimentResult.addPair(Constant.TimeCost, String.valueOf(timeCost));
        experimentResult.addPair(Constant.PrivacyBudget, String.valueOf(ListUtils.getMinimalValue(privacyBudgetList)));
        int maximalWindowSizeValue = ListUtils.getMaximalValue(windowSizeList, 1);
        experimentResult.addPair(Constant.WindowSize, String.valueOf(maximalWindowSizeValue));
        for (int i = 0; i < timeBatchSize; i++) {
            rawPublicationData = rawPublicationBatchList.get(i);
            publicationData = publicationList.get(i);
            batchTotalVarianceStatistic += StatisticTool.getVariance(rawPublicationData.getDataMap(), publicationData.getDataMap());
            batchTotalDivergence += StatisticTool.getJSDivergence(rawPublicationData.getDataMap(), publicationData.getDataMap());
        }
//        batchTotalVarianceStatistic /= timeUpperBound;
        experimentResult.addPair(Constant.BRE, String.valueOf(batchTotalVarianceStatistic));
        experimentResult.addPair(Constant.BJSD, String.valueOf(batchTotalDivergence));
        return experimentResult;
    }

}
