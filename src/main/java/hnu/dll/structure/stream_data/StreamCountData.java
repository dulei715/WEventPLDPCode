package hnu.dll.structure.stream_data;

import java.util.List;
import java.util.TreeMap;

public class StreamCountData {
    protected Integer timeSlot;
    protected TreeMap<String, Integer> dataMap;

    public StreamCountData(Integer timeSlot, TreeMap<String, Integer> dataMap) {
        this.timeSlot = timeSlot;
        this.dataMap = dataMap;
    }

    public StreamCountData(Integer timeSlot, List<String> dataType) {
        this.timeSlot = timeSlot;
        this.dataMap = new TreeMap<>();
        for (String keyName : dataType) {
            this.dataMap.put(keyName, 0);
        }
    }


    public Integer getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(Integer timeSlot) {
        this.timeSlot = timeSlot;
    }

    public TreeMap<String, Integer> getDataMap() {
        return dataMap;
    }

    public void setDataMap(TreeMap<String, Integer> dataMap) {
        this.dataMap = dataMap;
    }

    public void addEntry(String key, Integer value) {
        this.dataMap.put(key, value);
    }
}
