package hnu.dll.schemes.compare_scheme._1_non_personalized.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.compare_scheme._1_non_personalized.LPMechanism;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.structure.AbsorptionLastInfo;

import java.util.*;

public class LDPPopulationAbsorption extends LPMechanism {
    protected AbsorptionLastInfo lastInfo;
    public LDPPopulationAbsorption(Set<String> dataTypeSet, Double privacyBudget, Integer windowSize, Integer userSize, Random random) {
        super(dataTypeSet, privacyBudget, windowSize, userSize, random);
        this.lastInfo = new AbsorptionLastInfo(-1, 0);
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        Boolean flag;
        Map<Integer, Double> normalizedEstimation;
        Integer lastTimeSlot = this.lastInfo.getTimeSlot();
        Integer lastPublicationPopulationConsumption = this.lastInfo.getPopulationSize();
        Integer nullifiedLength = lastPublicationPopulationConsumption / this.samplingSize - 1;
        if (this.currentTime - lastTimeSlot <= nullifiedLength) {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
            this.publicationSubMechanismHistoryQueue.offer(new HashSet());
        } else {
            Integer absorbedLength = this.currentTime - lastTimeSlot - nullifiedLength;
            Integer publicationSamplingSize = this.samplingSize * Math.min(absorbedLength, this.windowSize);

            Double error = FOUtils.getGRRError(this.privacyBudget, publicationSamplingSize, this.domainSize);
            if (dissimilarity > error) {
                flag = true;
                Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
                // 获取当前 data 列表
                List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);

                this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
                normalizedEstimation = MechanismUtils.gRR(this.privacyBudget, samplingDataIndexList, this.domainSize, this.random);
                this.lastReleaseEstimation = normalizedEstimation;
                this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);
                this.lastInfo.update(this.currentTime, publicationSamplingSize);
            } else {
                flag = false;
                normalizedEstimation = this.lastReleaseEstimation;
                this.publicationSubMechanismHistoryQueue.offer(new HashSet());
            }
        }

        // recycle
        if (currentTime >= windowSize) {
            Set samplingRecycle = this.samplingSubMechanismHistoryQueue.getFirst();
            Set publicationRecycle = this.publicationSubMechanismHistoryQueue.getFirst();
            this.candidateUserIndexSet.addAll(samplingRecycle);
            if (publicationRecycle != null) {
                this.candidateUserIndexSet.addAll(publicationRecycle);
            }
        }

        return new CombinePair<>(flag, normalizedEstimation);

    }

    @Override
    public String getSimpleName() {
        return "LPA";
    }
}
