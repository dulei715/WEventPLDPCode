package hnu.dll.schemes.compare_scheme._3_ablation.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.compare_scheme._3_ablation.AblateOPSMechanism;
import hnu.dll.special_tools.PFOUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AblateOPSDistributionPlus extends AblateOPSMechanism {
    public AblateOPSDistributionPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {

        Integer remainingPublicationUserSize = this.userSize / 2 - this.publicationSubMechanismHistoryQueue.getReverseSizeSum(this.chosenWindowSize - 1);
        Integer publicationSamplingSize = remainingPublicationUserSize / 2;
        Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
        // 获取当前 privacy budget 列表
        List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.originalPrivacyBudgetList, samplingUserIndexSetForPublication);
        // 获取当前 data 列表
        List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);
        Map<Double, Integer> groupCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingPrivacyBudgetList);

        Double error = PFOUtils.getGPRRErrorBySpecificUsers(groupCountMap, this.userSize, publicationSamplingSize, this.domainSize);

        Map<Integer, Double> normalizedEstimation;

        Boolean flag;

        if (dissimilarity > error) {
            flag = true;
            this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
            normalizedEstimation = MechanismUtils.enhancedGPRR(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random).getKey();
            this.lastReleaseEstimation = normalizedEstimation;
            this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);
        } else {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
        }

        // recycle
        Set samplingRecycle = this.samplingSubMechanismHistoryQueue.getFirst();
        Set publicationRecycle = this.publicationSubMechanismHistoryQueue.getFirst();
        this.candidateUserIndexSet.addAll(samplingRecycle);
        this.candidateUserIndexSet.addAll(publicationRecycle);

        return new CombinePair<>(flag, normalizedEstimation);
    }

    @Override
    public String getSimpleName() {
        return "AblateOPS-PLPD+";
    }
}
