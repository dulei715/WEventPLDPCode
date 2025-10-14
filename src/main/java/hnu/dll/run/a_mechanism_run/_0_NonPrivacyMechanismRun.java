package hnu.dll.run.a_mechanism_run;

import cn.edu.dll.result.ExperimentResult;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes.compare_scheme._0_non_privacy.NonPrivacyMechanism;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class _0_NonPrivacyMechanismRun {

    public static CombinePair<ExperimentResult, List<Map<Integer, Double>>> runBatch(NonPrivacyMechanism scheme, Integer batchID, List<List<Integer>> batchDataList) {
//        NonPrivacyMechanism scheme = new NonPrivacyMechanism(dataType);
        ExperimentResult experimentResult = new ExperimentResult();
        int timeBatchSize = batchDataList.size();
        long startTime, endTime, timeCost;
        List<Map<Integer, Double>> publicationList = new ArrayList<>(timeBatchSize);
        startTime = System.currentTimeMillis();
        CombinePair<Boolean, Map<Integer, Double>> reportDataPair;
        for (int i = 0; i < timeBatchSize; i++) {
            reportDataPair = scheme.updateNextPublicationResult(batchDataList.get(i));
            publicationList.add(reportDataPair.getValue());
        }
        endTime = System.currentTimeMillis();
        timeCost = endTime - startTime;
        experimentResult.addPair(Constant.MechanismName, scheme.getSimpleName());
        experimentResult.addPair(Constant.BatchName, String.valueOf(batchID));
        experimentResult.addPair(Constant.BatchRealSize, String.valueOf(timeBatchSize));
        experimentResult.addPair(Constant.TimeCost, String.valueOf(timeCost));
        experimentResult.addPair(Constant.PrivacyBudget, String.valueOf(0.0));
        experimentResult.addPair(Constant.WindowSize, String.valueOf(0));
        experimentResult.addPair(Constant.BRE, String.valueOf(0));
        experimentResult.addPair(Constant.BJSD, String.valueOf(0));
        return new CombinePair<>(experimentResult, publicationList);
    }

}
