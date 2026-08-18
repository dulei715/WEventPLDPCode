package hnu.dll.structure.selection_struct;

import java.util.List;

public class GroupAwareOptimalSelectionStruct {
    private List<Double> optimalErrorList;
    private List<Integer> optimalSamplingSizeList;
    private List<Double> optimalEffectivePrivacyBudgetList;
    // 可由samplingSizeList计算出
    private List<Integer> windowSizeList;


    public GroupAwareOptimalSelectionStruct(List<Double> optimalErrorList, List<Integer> optimalSamplingSizeList, List<Double> optimalEffectivePrivacyBudgetList, List<Integer> windowSizeList) {
        this.optimalErrorList = optimalErrorList;
        this.optimalSamplingSizeList = optimalSamplingSizeList;
        this.optimalEffectivePrivacyBudgetList = optimalEffectivePrivacyBudgetList;
        this.windowSizeList = windowSizeList;
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

    public List<Integer> getWindowSizeList() {
        return windowSizeList;
    }

    @Override
    public String toString() {
        return "GroupAwareOptimalSelectionStruct{" +
                "optimalErrorList=" + optimalErrorList +
                ", optimalSamplingSizeList=" + optimalSamplingSizeList +
                ", optimalEffectivePrivacyBudgetList=" + optimalEffectivePrivacyBudgetList +
                ", windowSizeList=" + windowSizeList +
                '}';
    }

    public Integer getGroupSize() {
        return optimalErrorList.size();
    }
}
