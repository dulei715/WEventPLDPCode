package hnu.dll.structure.selection_struct;

import java.util.List;

public class GroupAwareOptimalSelectionStruct {
    private List<Double> optimalErrorList;
    private List<Integer> optimalSamplingSizeList;
    private List<Integer> effectiveRecycleSizeList;
    private List<Double> optimalEffectivePrivacyBudgetList;


    public GroupAwareOptimalSelectionStruct(List<Double> optimalErrorList, List<Integer> optimalSamplingSizeList, List<Integer> effectiveRecycleSizeList, List<Double> optimalEffectivePrivacyBudgetList) {
        this.optimalErrorList = optimalErrorList;
        this.optimalSamplingSizeList = optimalSamplingSizeList;
        this.optimalEffectivePrivacyBudgetList = optimalEffectivePrivacyBudgetList;
        this.effectiveRecycleSizeList = effectiveRecycleSizeList;
    }

    public List<Double> getOptimalErrorList() {
        return optimalErrorList;
    }

    public List<Integer> getOptimalSamplingSizeList() {
        return optimalSamplingSizeList;
    }

    public List<Double> getOptimalEffectivePrivacyBudgetList() {
        return optimalEffectivePrivacyBudgetList;
    }

    public List<Integer> getEffectiveRecycleSizeList() {
        return effectiveRecycleSizeList;
    }

    @Override
    public String toString() {
        return "GroupAwareOptimalSelectionStruct{" +
                "optimalErrorList=" + optimalErrorList +
                ", optimalSamplingSizeList=" + optimalSamplingSizeList +
                ", effectiveRecycleSizeList=" + effectiveRecycleSizeList +
                ", optimalEffectivePrivacyBudgetList=" + optimalEffectivePrivacyBudgetList +
                '}';
    }

    public Integer getGroupSize() {
        return optimalErrorList.size();
    }
}
