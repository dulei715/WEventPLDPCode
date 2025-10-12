package hnu.dll.structure.direct_stream;

public class ImpactElement implements Comparable<ImpactElement> {
    protected Integer timeSlot;
    protected Double totalPrivacyBudget;
    protected Integer windowSize;

    public ImpactElement(Integer timeSlot, Double totalPrivacyBudget, Integer windowSize) {
        this.timeSlot = timeSlot;
        this.totalPrivacyBudget = totalPrivacyBudget;
        this.windowSize = windowSize;
    }

    public Integer getTimeSlot() {
        return timeSlot;
    }

    public Double getTotalPrivacyBudget() {
        return totalPrivacyBudget;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    @Override
    public int compareTo(ImpactElement element) {
        return this.timeSlot - element.timeSlot;
    }
}
