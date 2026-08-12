package hnu.dll.utils.comparator;

import hnu.dll.structure.personalized_data.AbstractPrivacyGroup;

import java.util.Comparator;
public class PrivacyGroupComparator implements Comparator<AbstractPrivacyGroup> {
    /*
     * 排序规则：
     * 1. w 从小到大
     * 2. w 相同时，epsilon 从大到小
     *
     * 因为 source PG_r 需要满足：
     * w_r <= w_H && epsilon_r >= epsilon_H
     */
    @Override
    public int compare(AbstractPrivacyGroup groupA, AbstractPrivacyGroup groupB) {
        int cmpW = Integer.compare(groupA.getWindowSize(), groupB.getWindowSize());

        if (cmpW != 0) {
            return cmpW;
        }

        // epsilon 降序
        return Double.compare(groupB.getEpsilon(), groupA.getEpsilon());
    }
}
