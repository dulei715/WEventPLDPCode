package hnu.dll.schemes.compare_scheme._1_non_personalized.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.compare_scheme._1_non_personalized.LPMechanism;
import hnu.dll.special_tools.FOUtils;

import java.util.*;

public class LDPPopulationDistribution extends LPMechanism {
    public LDPPopulationDistribution(Set<String> dataTypeSet, Double privacyBudget, Integer windowSize, Integer userSize, Random random) {
        super(dataTypeSet, privacyBudget, windowSize, userSize, random);
    }

    // M_{r,t}
    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {

        Integer remainingPublicationUserSize = this.userSize / 2 - this.publicationSubMechanismHistoryQueue.getReverseSizeSum(this.windowSize - 1);
        Integer publicationSamplingSize = remainingPublicationUserSize / 2;

        Double error = FOUtils.getGPRRError(this.privacyBudget, publicationSamplingSize, this.domainSize);

        Map<Integer, Double> normalizedEstimation;

        Boolean flag;

        if (dissimilarity > error) {
            flag = true;
            Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
            List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);

            this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
            normalizedEstimation = MechanismUtils.gRR(this.privacyBudget, samplingDataIndexList, this.domainSize, this.random);
            this.lastReleaseEstimation = normalizedEstimation;
            this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);


        } else {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
            this.publicationSubMechanismHistoryQueue.offer(new HashSet());
        }

        // recycle
        Set samplingRecycle = this.samplingSubMechanismHistoryQueue.getFirst();
        Set publicationRecycle = this.publicationSubMechanismHistoryQueue.getFirst();
        if (currentTime >= windowSize) {
            this.candidateUserIndexSet.addAll(samplingRecycle);
            if (publicationRecycle != null) {
                this.candidateUserIndexSet.addAll(publicationRecycle);
            }
        }

        return new CombinePair<>(flag, normalizedEstimation);
    }

    @Override
    public String getSimpleName() {
        return "LPD";
    }
}
