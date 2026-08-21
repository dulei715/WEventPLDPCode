package hnu.dll.structure.selection_struct;

import java.util.List;

public class OptimalSelectionStruct {
    private Integer optimalSamplingSize;
    private Integer populationRecycleWindowSize;
    private Double error;
    private List<Double> newPrivacyBudgetList;
    private Integer optimalSamplingSizeIndex;

    public OptimalSelectionStruct(Integer optimalSamplingSize, Integer populationRecycleWindowSize, Double error, List<Double> newPrivacyBudgetList, Integer optimalSamplingSizeIndex) {
        this.optimalSamplingSize = optimalSamplingSize;
        this.populationRecycleWindowSize = populationRecycleWindowSize;
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

    public Integer getPopulationRecycleWindowSize() {
        return populationRecycleWindowSize;
    }

    @Override
    public String toString() {
        return "OptimalSelectionStruct{" +
                "optimalSamplingSize=" + optimalSamplingSize +
                ", populationRecycleWindowSize=" + populationRecycleWindowSize +
                ", error=" + error +
                ", newPrivacyBudgetList=" + newPrivacyBudgetList +
                ", optimalSamplingSizeIndex=" + optimalSamplingSizeIndex +
                '}';
    }
}
