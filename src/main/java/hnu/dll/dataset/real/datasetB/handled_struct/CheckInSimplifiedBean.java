package hnu.dll.dataset.real.datasetB.handled_struct;

public class CheckInSimplifiedBean {
    private Integer userID;
    private String countryName;
    private Long checkInTimeStamp;

    public CheckInSimplifiedBean(Integer userID, String countryName, Long checkInTimeStamp) {
        this.userID = userID;
        this.countryName = countryName;
        this.checkInTimeStamp = checkInTimeStamp;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Long getCheckInTimeStamp() {
        return checkInTimeStamp;
    }

    public void setCheckInTimeStamp(Long checkInTimeStamp) {
        this.checkInTimeStamp = checkInTimeStamp;
    }

    @Override
    public String toString() {
        return "CheckInSimplifiedBean{" +
                "userID=" + userID +
                ", countryName='" + countryName + '\'' +
                ", checkInTimeStamp=" + checkInTimeStamp +
                '}';
    }

    public static CheckInSimplifiedBean toBean(String[] strings) {
        Integer userID = Integer.valueOf(strings[0]);
        String cityName = strings[1];
        Long checkInTimeStamp = Long.valueOf(strings[2]);
        return new CheckInSimplifiedBean(userID, cityName, checkInTimeStamp);
    }
}
