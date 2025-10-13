package hnu.dll.schemes.main_scheme.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme.EnhancedPLPMechanism;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.AbsorptionLastInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class PLDPPopulationAbsorptionPlus extends EnhancedPLPMechanism {
    protected AbsorptionLastInfo lastInfo;
    public PLDPPopulationAbsorptionPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
        this.lastInfo = new AbsorptionLastInfo(-1, 0);
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        Boolean flag;
        Map<Integer, Double> normalizedEstimation;
        Integer optimalSamplingSize = this.getOptimalSamplingSize();
        Integer lastTimeSlot = this.lastInfo.getTimeSlot();
        Integer lastPublicationPopulationConsumption = this.lastInfo.getPopulationSize();
        Integer nullifiedLength = lastPublicationPopulationConsumption / optimalSamplingSize - 1;
        if (this.currentTime - lastTimeSlot <= nullifiedLength) {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
        } else {
            Integer absorbedLength = this.currentTime - lastTimeSlot - nullifiedLength;
            Integer publicationSamplingSize = optimalSamplingSize * Math.min(absorbedLength, this.optimalWindowSize);
            Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
            // 获取当前 privacy budget 列表
            List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.newPrivacyBudgetList, samplingUserIndexSetForPublication);
            // 获取当前 data 列表
            List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);
            Map<Double, Integer> groupCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingPrivacyBudgetList);

            Double error = PFOUtils.getGPRRErrorBySpecificUsers(groupCountMap, this.userSize, publicationSamplingSize, this.domainSize);
            if (dissimilarity > error) {
                flag = true;
                this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
                normalizedEstimation = MechanismUtils.enhancedGPRR(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random).getKey();
                this.lastReleaseEstimation = normalizedEstimation;
                this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);
                this.lastInfo.update(this.currentTime, publicationSamplingSize);
            } else {
                flag = false;
                normalizedEstimation = this.lastReleaseEstimation;
            }
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
        return "PLPA+";
    }
}
