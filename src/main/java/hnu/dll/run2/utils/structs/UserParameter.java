package hnu.dll.run2.utils.structs;

public class UserParameter {
    private Integer userID;
    private Double privacyBudget;
    private Integer windowSize;

    public UserParameter(Integer userID, Double privacyBudget, Integer windowSize) {
        this.userID = userID;
        this.privacyBudget = privacyBudget;
        this.windowSize = windowSize;
    }

    public Integer getUserID() {
        return userID;
    }

    public Double getPrivacyBudget() {
        return privacyBudget;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    @Override
    public String toString() {
        return "UserParameter{" +
                "userID=" + userID +
                ", privacyBudget=" + privacyBudget +
                ", windowSize=" + windowSize +
                '}';
    }
}
