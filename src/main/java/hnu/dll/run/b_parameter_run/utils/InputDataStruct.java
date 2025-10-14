package hnu.dll.run.b_parameter_run.utils;

public class InputDataStruct {
    private Integer userID;
    private String location;
    private Long timeStamp;

    public InputDataStruct(Integer userID, String location, Long timeStamp) {
        this.userID = userID;
        this.location = location;
        this.timeStamp = timeStamp;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public static InputDataStruct toBean(String[] dataArray) {
        Integer userID = Integer.valueOf(dataArray[0]);
        String location = dataArray[1];
        Long timeStamp = Long.valueOf(dataArray[2]);
        return new InputDataStruct(userID, location, timeStamp);
    }
}
