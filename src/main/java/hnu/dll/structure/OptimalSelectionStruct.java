package hnu.dll.structure;

import java.util.List;

public class OptimalSelectionStruct {
    private Integer optimalSamplingSize;
    private Double error;
    private List<Double> newPrivacyBudgetList;
    private Integer optimalSamplingSizeIndex;

    public OptimalSelectionStruct(Integer optimalSamplingSize, Double error, List<Double> newPrivacyBudgetList, Integer optimalSamplingSizeIndex) {
        this.optimalSamplingSize = optimalSamplingSize;
        this.error = error;
        this.newPrivacyBudgetList = newPrivacyBudgetList;
        this.optimalSamplingSizeIndex = optimalSamplingSizeIndex;
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

    public Integer getOptimalSamplingSizeIndex() {
        return optimalSamplingSizeIndex;
    }

    @Override
    public String toString() {
        return "OptimalSelectionStruct{" +
                "optimalSamplingSize=" + optimalSamplingSize +
                ", error=" + error +
                ", newPrivacyBudgetList=" + newPrivacyBudgetList +
                ", optimalSamplingSizeIndex=" + optimalSamplingSizeIndex +
                '}';
    }
}
