package hnu.dll.dataset.real.datasetB.basic_struct;

public class POIBean {
    private String venueID;
    private Double latitude;
    private Double longitude;
    private String venueCategoryName;
    private String countryCode;

    public POIBean(String venueID, Double latitude, Double longitude, String venueCategoryName, String countryCode) {
        this.venueID = venueID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.venueCategoryName = venueCategoryName;
        this.countryCode = countryCode;
    }

    public String getVenueID() {
        return venueID;
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getVenueCategoryName() {
        return venueCategoryName;
    }

    public void setVenueCategoryName(String venueCategoryName) {
        this.venueCategoryName = venueCategoryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public static POIBean toBean(String[] parameters) {
        String venueID = parameters[0];
        Double latitude = Double.valueOf(parameters[1]);
        Double longitude = Double.valueOf(parameters[2]);
        String venueCategoryName = parameters[3];
        String countryCode = parameters[4];
        return new POIBean(venueID, latitude, longitude, venueCategoryName, countryCode);
    }

    @Override
    public String toString() {
        return "POIBean{" +
                "venueID='" + venueID + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", venueCategoryName='" + venueCategoryName + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}
