package hnu.dll.dataset.real.datasetB.basic_struct;

public class CityBean {
    private String cityName;
    private Double latitude;
    private Double longitude;
    private String countryCode;
    private String countryName;
    private String cityType;

    public CityBean(String cityName, Double latitude, Double longitude, String countryCode, String countryName, String cityType) {
        this.cityName = cityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.cityType = cityType;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCityType() {
        return cityType;
    }

    public void setCityType(String cityType) {
        this.cityType = cityType;
    }

    public static CityBean toBean(String[] parameters) {
        String cityName = parameters[0];
        Double latitude = Double.valueOf(parameters[1]);
        Double longitude = Double.valueOf(parameters[2]);
        String countryCode = parameters[3];
        String countryName = parameters[4];
        String cityType = parameters[5];
        return new CityBean(cityName, latitude, longitude, countryCode, countryName, cityType);
    }

    @Override
    public String toString() {
        return "CityBean{" +
                "cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", cityType='" + cityType + '\'' +
                '}';
    }
}
