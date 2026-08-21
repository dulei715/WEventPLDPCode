package hnu.dll.schemes.main_scheme._3_advanced_scheme.impl;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll._config.Constant;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class PLPDPlus extends AdvancedPLPMechanism {

    protected GroupAwareOptimalSelectionStruct pubGroupAwareOptimalSelectionStruct;

    public PLPDPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
    }


    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        int tempRecyclePeriod;
        int tempGroupInnerSize, tempOptimalSamplingSize, remainingPopulationSize, potentialPublicationUserSize;
        List<Integer> groupOptimalSamplingSizeList = super.disGroupAwareOptimalSelectionStruct.getOptimalSamplingSizeList();
        List<Integer> windowSizeList = super.disGroupAwareOptimalSelectionStruct.getWindowSizeList();
        List<PrivacyGroupTriple> publicationPrivacyGroupTripleList = new ArrayList<>(super.groupSize);
        for (int i = 0; i < super.groupSize; i++) {
            tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
            tempOptimalSamplingSize = groupOptimalSamplingSizeList.get(i);
            tempRecyclePeriod = Math.max(1, tempGroupInnerSize / tempOptimalSamplingSize);  // 这里用了整数除法截断性质实现向下取整
            remainingPopulationSize = tempGroupInnerSize / 2 - super.publicationSubMechanismHistoryQueueList.get(i).getReverseSizeSum(windowSizeList.get(i) - 1);
            potentialPublicationUserSize = remainingPopulationSize / 2;     // 这里用了整数除法截断性质实现向下取整
            publicationPrivacyGroupTripleList.add(new PrivacyGroupTriple(windowSizeList.get(i), super.distinctBudgetList.get(i), potentialPublicationUserSize));
        }
        this.pubGroupAwareOptimalSelectionStruct = ProposedSchemeUtils.groupAwareOptimalPopulationSelection(publicationPrivacyGroupTripleList, 1.0, super.domainSize);
        List<Double> optimalErrorList = this.pubGroupAwareOptimalSelectionStruct.getOptimalErrorList();
        List<Double> prePublicationVarianceList = BasicArrayUtil.getLinearTransformToList(optimalErrorList, super.domainSize, 0);
        List<SimpleMatrix> preCorrectedWeightList = ProposedSchemeUtils.getCorrectedWeight(super.sourceMap, super.currentDisEstimationList, super.currentDisUnbiasedVarianceList, prePublicationVarianceList, Constant.EigenvaluesLowerBound);

        return new CombinePair<>(true, null);
    }

    @Override
    public String getSimpleName() {
        return "PLPD+";
    }
}
