package hnu.dll.structure.stream_data;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class StreamNoiseCountData {
    protected Integer timeSlot;
    protected TreeMap<String, Double> dataMap;

    public StreamNoiseCountData(Integer timeSlot, TreeMap<String, Double> dataMap) {
        this.timeSlot = timeSlot;
        this.dataMap = dataMap;
    }

    public StreamNoiseCountData(Integer timeSlot, Set<String> dataType) {
        this.timeSlot = timeSlot;
        this.dataMap = new TreeMap<>();
        for (String keyName : dataType) {
            this.dataMap.put(keyName, 0D);
        }
    }


    public Integer getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Integer timeSlot) {
        this.timeSlot = timeSlot;
    }

    public TreeMap<String, Double> getDataMap() {
        return dataMap;
    }

    public void setDataMap(TreeMap<String, Double> dataMap) {
        this.dataMap = dataMap;
    }

    public void addEntry(String key, Double value) {
        this.dataMap.put(key, value);
    }
}
