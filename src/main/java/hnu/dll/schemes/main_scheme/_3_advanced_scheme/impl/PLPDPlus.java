package hnu.dll.schemes.main_scheme._3_advanced_scheme.impl;

import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.schemes.main_scheme._3_advanced_scheme.AdvancedPLPMechanism;

import java.util.*;

public class PLPDPlus extends AdvancedPLPMechanism {
    public PLPDPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
    }


    @Override
    protected CombinePair<Boolean, Map<Integer, Double>> reportingSubMechanism(List<Integer> nextDataIndexList, Double dissimilarity) {
        double tempRecyclePeriod;
        int tempGroupInnerSize, tempOptimalSamplingSize;
        List<Integer> groupOptimalSamplingSizeList = super.groupAwareOptimalSelectionStruct.getOptimalSamplingSizeList();
        for (int i = 0; i < super.groupSize; i++) {
            tempGroupInnerSize = super.privacyGroupList.get(i).getUserSize();
            tempOptimalSamplingSize = groupOptimalSamplingSizeList.get(i);
            tempRecyclePeriod = Math.max(1, tempGroupInnerSize / tempOptimalSamplingSize);  // 这里用了整数除法截断性质实现向下取整

        }
        return new CombinePair<>(true, null);
    }

    @Override
    public String getSimpleName() {
        return "PLPD+";
    }
}
