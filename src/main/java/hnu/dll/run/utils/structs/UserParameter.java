package hnu.dll.run.utils.structs;

import java.util.ArrayList;
import java.util.List;

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

    public static UserParameter toBean(String data, String splitTag) {
        String[] splitData = data.split(splitTag);
        Integer userID = Integer.valueOf(splitData[0]);
        Double budget = Double.valueOf(splitData[1]);
        Integer windowSize = Integer.valueOf(splitData[2]);
        return new UserParameter(userID, budget, windowSize);
    }

    public static List<UserParameter> toBeanList(List<String> dataList, String splitTag) {
        List<UserParameter> resultList = new ArrayList<>(dataList.size());
        for (String str : dataList) {
            resultList.add(toBean(str, splitTag));
        }
        return resultList;
    }

    public static List<Double> extractPrivacyBudgetList(List<UserParameter> data) {
        List<Double> result = new ArrayList<>(data.size());
        for (UserParameter datum : data) {
            result.add(datum.privacyBudget);
        }
        return result;
    }
    public static List<Integer> extractWindowSizeList(List<UserParameter> data) {
        List<Integer> result = new ArrayList<>(data.size());
        for (UserParameter datum : data) {
            result.add(datum.windowSize);
        }
        return result;
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
