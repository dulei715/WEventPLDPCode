package hnu.dll.structure;

public class AbsorptionLastInfo {
    private Integer timeSlot;
    private Integer populationSize;

    public AbsorptionLastInfo(Integer timeSlot, Integer populationSize) {
        this.timeSlot = timeSlot;
        this.populationSize = populationSize;
    }

    public Integer getTimeSlot() {
        return timeSlot;
    }

    public Integer getPopulationSize() {
        return populationSize;
    }

    public void update(Integer timeSlot, Integer populationSize) {
        this.timeSlot = timeSlot;
        this.populationSize = populationSize;
    }

    @Override
    public String toString() {
        return "AbsorptionLastInfo{" +
                "timeSlot=" + timeSlot +
                ", populationSize=" + populationSize +
                '}';
    }
}
