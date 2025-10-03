package hnu.dll.structure;

import java.util.List;

public class OptimalSelectionStruct {
    private Integer optimalSamplingSize;
    private Double error;
    private List<Double> newPrivacyBudgetList;

    public OptimalSelectionStruct(Integer optimalSamplingSize, Double error, List<Double> newPrivacyBudgetList) {
        this.optimalSamplingSize = optimalSamplingSize;
        this.error = error;
        this.newPrivacyBudgetList = newPrivacyBudgetList;
    }

    public Integer getOptimalSamplingSize() {
        return optimalSamplingSize;
    }

    public Double getError() {
        return error;
    }

    public List<Double> getNewPrivacyBudgetList() {
        return newPrivacyBudgetList;
    }

    @Override
    public String toString() {
        return "OptimalSelectionStruct{" +
                "optimalSamplingSize=" + optimalSamplingSize +
                ", error=" + error +
                ", newPrivacyBudgetList=" + newPrivacyBudgetList +
                '}';
    }
}
