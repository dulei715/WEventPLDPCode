package hnu.dll.schemes.main_scheme._1_simple_scheme.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._1_simple_scheme.BasicPLPMechanism;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.AbsorptionLastInfo;
import hnu.dll.utils.BasicUtils;

import java.util.*;

public class BasicPLPAbsorption extends BasicPLPMechanism {
    protected Integer populationSizeLowerBound;
    protected AbsorptionLastInfo lastInfo;
    public BasicPLPAbsorption(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Integer populationSizeLowerBound, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
        this.lastInfo = new AbsorptionLastInfo(-1, 0);
        this.populationSizeLowerBound = populationSizeLowerBound;
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        Boolean flag;
        Map<Integer, Double> normalizedEstimation, rawEstimation;
        Integer optimalSamplingSize = this.getOptimalSamplingSize();
        Integer lastTimeSlot = this.lastInfo.getTimeSlot();
        Integer lastPublicationPopulationConsumption = this.lastInfo.getPopulationSize();
        Integer nullifiedLength = lastPublicationPopulationConsumption / optimalSamplingSize - 1;
        if (this.currentTime - lastTimeSlot <= nullifiedLength) {
            flag = false;
            rawEstimation = this.lastRawReleaseEstimation;
            normalizedEstimation = this.lastReleaseEstimation;
            this.publicationSubMechanismHistoryQueue.offer(new HashSet());
        } else {
            Integer absorbedLength = this.currentTime - lastTimeSlot - nullifiedLength;
            Integer publicationSamplingSize = optimalSamplingSize * Math.min(absorbedLength, this.recycleWindowSize);
            Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
            // 获取当前 privacy budget 列表
            List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.newPrivacyBudgetList, samplingUserIndexSetForPublication);
            // 获取当前 data 列表
            List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);
            Map<Double, Integer> groupCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingPrivacyBudgetList);

            Double error = PFOUtils.getGPRRErrorBySpecificUsers(groupCountMap, this.userSize, publicationSamplingSize, this.domainSize);
            if (dissimilarity > error && publicationSamplingSize >= this.populationSizeLowerBound) {
                flag = true;
                this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
                rawEstimation = MechanismUtils.gPRRRaw(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random).getKey();
                this.lastRawReleaseEstimation = rawEstimation;
                normalizedEstimation = BasicUtils.normalizeValues(rawEstimation);
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
        if (this.currentTime >= this.recycleWindowSize) {
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
//    public String getSimpleName() {
//        return "AblateRP-PLPA+";
//    }
    public String getSimpleName() {
        return Constant.BasicPLPASchemeName;
    }
}
