package hnu.dll.structure.selection_struct;

import java.util.List;

public class GroupAwareOptimalSelectionStruct {
    private List<Double> optimalErrorList;
    private List<Integer> optimalSamplingSizeList;
    private List<Double> optimalEffectivePrivacyBudgetList;

    public GroupAwareOptimalSelectionStruct(List<Double> optimalErrorList, List<Integer> optimalSamplingSizeList, List<Double> optimalEffectivePrivacyBudgetList) {
        this.optimalErrorList = optimalErrorList;
        this.optimalSamplingSizeList = optimalSamplingSizeList;
        this.optimalEffectivePrivacyBudgetList = optimalEffectivePrivacyBudgetList;
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

    @Override
    public String toString() {
        return "GroupAwareOptimalSelectionStruct{" +
                "optimalErrorList=" + optimalErrorList +
                ", optimalSamplingSizeList=" + optimalSamplingSizeList +
                ", optimalEffectivePrivacyBudgetList=" + optimalEffectivePrivacyBudgetList +
                '}';
    }
}
