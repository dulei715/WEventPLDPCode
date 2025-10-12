package hnu.dll.dataset.real.datasetA.handled_struct;

public class TrajectoryComplicatedBean {
    private Integer userID;
    private Long timestamp;
    private Double longitude;
    private Double latitude;
    private Integer areaIndex;

    public TrajectoryComplicatedBean(Integer userID, Long timestamp, Double longitude, Double latitude, Integer areaIndex) {
        this.userID = userID;
        this.timestamp = timestamp;
        this.longitude = longitude;
        this.latitude = latitude;
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

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getAreaIndex() {
        return areaIndex;
    }

    public void setAreaIndex(Integer areaIndex) {
        this.areaIndex = areaIndex;
    }

    @Override
    public String toString() {
        return "TrajectoryComplicatedBean{" +
                "userID=" + userID +
                ", timestamp=" + timestamp +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", areaIndex=" + areaIndex +
                '}';
    }

    public static TrajectoryComplicatedBean toBean(String[] stringArray) {
        Integer userID = Integer.valueOf(stringArray[0]);
        Long timeStamp = Long.valueOf(stringArray[1]);
        Double longitude = Double.valueOf(stringArray[2]);
        Double latitude = Double.valueOf(stringArray[3]);
        Integer areaIndex = Integer.valueOf(stringArray[4]);
        return new TrajectoryComplicatedBean(userID, timeStamp, longitude, latitude, areaIndex);
    }
}
