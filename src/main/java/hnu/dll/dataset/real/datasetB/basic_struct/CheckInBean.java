package hnu.dll.dataset.real.datasetB.basic_struct;


import hnu.dll.dataset.real.datasetB.spetial_tools.CheckInStringTool;

import java.util.Date;

public class CheckInBean {
    private Integer userID;
    private String venueID;
    private Long utcTime;
    private Integer timezoneOffset;

    public CheckInBean(Integer userID, String venueID, Long utcTime, Integer timezoneOffset) {
        this.userID = userID;
        this.venueID = venueID;
        this.utcTime = utcTime;
        this.timezoneOffset = timezoneOffset;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getVenueID() {
        return venueID;
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }

    public Long getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(Long utcTime) {
        this.utcTime = utcTime;
    }

    public Integer getTimezoneOffset() {
        return timezoneOffset;
    }

    public void setTimezoneOffset(Integer timezoneOffset) {
        this.timezoneOffset = timezoneOffset;
    }

    public static CheckInBean toBean(String[] parameters) {
        Integer userID = Integer.valueOf(parameters[0]);
        String venueID = parameters[1];
        Long utcTime = CheckInStringTool.toTimestamp(parameters[2]);
        Integer timezoneOffset = Integer.valueOf(parameters[3]);
        return new CheckInBean(userID, venueID, utcTime, timezoneOffset);
    }

    public Date getUTCDate() {
        return new Date(this.utcTime);
    }

    @Override
    public String toString() {
        return "CheckInBean{" +
                "userID=" + userID +
                ", venueID='" + venueID + '\'' +
                ", utcTime='" + utcTime + '\'' +
                ", timezoneOffset=" + timezoneOffset +
                '}';
    }

}
