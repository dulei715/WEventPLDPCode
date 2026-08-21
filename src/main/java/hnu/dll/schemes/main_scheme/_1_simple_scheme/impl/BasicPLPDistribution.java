package hnu.dll.schemes.main_scheme._1_simple_scheme.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.schemes.main_scheme._1_simple_scheme.BasicPLPMechanism;
import hnu.dll.special_tools.PFOUtils;

import java.util.*;

public class BasicPLPDistribution extends BasicPLPMechanism {
    protected Integer populationSizeLowerBound;
    public BasicPLPDistribution(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Integer populationSizeLowerBound, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
        this.populationSizeLowerBound = populationSizeLowerBound;
    }

    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {

        Integer remainingPublicationUserSize = this.userSize / 2 - this.publicationSubMechanismHistoryQueue.getReverseSizeSum(this.recycleWindowSize - 1);
        Integer publicationSamplingSize = remainingPublicationUserSize / 2;
        Set<Integer> samplingUserIndexSetForPublication = RandomUtil.extractRandomElementWithoutRepeatFromSet(this.candidateUserIndexSet, publicationSamplingSize, random);
        // 获取当前 privacy budget 列表
        List<Double> samplingPrivacyBudgetList = BasicArrayUtil.extractSubListInGivenIndexCollection(this.newPrivacyBudgetList, samplingUserIndexSetForPublication);
        // 获取当前 data 列表
        List<Integer> samplingDataIndexList = BasicArrayUtil.extractSubListInGivenIndexCollection(nextDataIndexList, samplingUserIndexSetForPublication);
        Map<Double, Integer> groupCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingPrivacyBudgetList);

        Double error = PFOUtils.getGPRRErrorBySpecificUsers(groupCountMap, this.userSize, publicationSamplingSize, this.domainSize);

        Map<Integer, Double> normalizedEstimation;

        Boolean flag;

        if (dissimilarity > error && publicationSamplingSize >= this.populationSizeLowerBound) {
            flag = true;
            this.candidateUserIndexSet.removeAll(samplingUserIndexSetForPublication);
            normalizedEstimation = MechanismUtils.gPRR(samplingPrivacyBudgetList, samplingDataIndexList, this.domainSize, this.random).getKey();
            this.lastReleaseEstimation = normalizedEstimation;
            this.publicationSubMechanismHistoryQueue.offer(samplingUserIndexSetForPublication);
        } else {
            flag = false;
            normalizedEstimation = this.lastReleaseEstimation;
            this.publicationSubMechanismHistoryQueue.offer(new HashSet());
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
//        return "AblateRP-PLPD+";
//    }
    public String getSimpleName() {
        return Constant.BasicPLPDSchemeName;
    }
}
