package hnu.dll.dataset.real.datasetA.handled_struct;


public class TrajectorySimplifiedBean {
    private Integer userID;
    private Long timestamp;
    private Integer areaIndex;

    public TrajectorySimplifiedBean(Integer userID, Long timestamp, Integer areaIndex) {
        this.userID = userID;
        this.timestamp = timestamp;
        this.areaIndex = areaIndex;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getAreaIndex() {
        return areaIndex;
    }

    public void setAreaIndex(Integer areaIndex) {
        this.areaIndex = areaIndex;
    }

    @Override
    public String toString() {
        return "TrajectorySimplifiedBean{" +
                "userID=" + userID +
                ", timestamp=" + timestamp +
                ", areaIndex=" + areaIndex +
                '}';
    }
    public static TrajectorySimplifiedBean toBean(String[] stringArray) {
        Integer userID = Integer.valueOf(stringArray[0]);
        Long timeStamp = Long.valueOf(stringArray[1]);
        Integer areaIndex = Integer.valueOf(stringArray[2]);
        return new TrajectorySimplifiedBean(userID, timeStamp, areaIndex);
    }
}
