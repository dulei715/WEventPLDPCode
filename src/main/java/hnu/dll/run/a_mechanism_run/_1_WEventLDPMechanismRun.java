package hnu.dll.run.a_mechanism_run;

import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.statistic.StatisticTool;
import hnu.dll._config.Constant;
import hnu.dll.schemes.compare_scheme._2_our_baseline.BaseLinePLPMechanism;
import hnu.dll.schemes.compared_scheme.w_event_dp.WEventMechanism;
import hnu.dll.struts.stream_data.StreamCountData;
import hnu.dll.struts.stream_data.StreamDataElement;
import hnu.dll.struts.stream_data.StreamNoiseCountData;

import java.util.ArrayList;
import java.util.List;

public class _1_WEventLDPMechanismRun {
    /**
     * dataList 的外层List代表timestamp，内层list代表user
     */

    public static ExperimentResult runBatch(BaseLinePLPMechanism scheme, Integer batchID, List<List<Integer>> batchDataList, List<StreamCountData> rawPublicationBatchList) {
        Double privacyBudget = scheme.getPrivacyBudget();
        Integer windowSize = scheme.getWindowSize();
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
        experimentResult.addPair(Constant.PrivacyBudget, String.valueOf(privacyBudget));
        experimentResult.addPair(Constant.WindowSize, String.valueOf(windowSize));

        for (int i = 0; i < timeBatchSize; i++) {
            rawPublicationData = rawPublicationBatchList.get(i);
            publicationData = publicationList.get(i);
            batchTotalVarianceStatistic += StatisticTool.getVariance(rawPublicationData.getDataMap(), publicationData.getDataMap());
            batchTotalDivergence += StatisticTool.getJSDivergence(rawPublicationData.getDataMap(), publicationData.getDataMap());
        }
        experimentResult.addPair(Constant.BRE, String.valueOf(batchTotalVarianceStatistic));
        experimentResult.addPair(Constant.BJSD, String.valueOf(batchTotalDivergence));
        return experimentResult;
    }

}
