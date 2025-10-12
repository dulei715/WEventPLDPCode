package hnu.dll.structure.stream_data;

import java.util.ArrayList;
import java.util.List;

public class StreamBudgetData {
    protected Integer timeSlot;
    protected List<Double> budgetList;

    public StreamBudgetData(Integer timeSlot, List<Double> budgetList) {
        this.timeSlot = timeSlot;
        this.budgetList = budgetList;
    }

    public StreamBudgetData(Integer timeSlot, Double budget) {
        this.timeSlot = timeSlot;
        setBudget(budget);
    }

    public Integer getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Integer timeSlot) {
        this.timeSlot = timeSlot;
    }

    public List<Double> getBudgetList() {
        return budgetList;
    }

    public void setBudgetList(List<Double> budgetList) {
        this.budgetList = budgetList;
    }

    public Double getBudget() {
        return this.budgetList.get(0);
    }
    public void setBudget(Double budget) {
        this.budgetList = new ArrayList<>();
        this.budgetList.add(budget);
    }
}
