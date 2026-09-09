package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.struct.pair.CombinePair;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.personalized_data.SimplePrivacyGroup;

import java.util.*;

public class AdvancedSchemeUtils {
    public static List<SimplePrivacyGroup> getGroupSamplingSizeList(List<Integer> windowSizeList, List<Double> epsilonList) {
        int userSize = windowSizeList.size();
        if (userSize != epsilonList.size()) {
            throw new IllegalArgumentException("windowSizeList and epsilonList must have the same size!");
        }
        // 注意：CombinePair 未重写 equals/hashCode，不能直接作为 HashMap 的键，
        // 否则每个用户都会被分到独立的组（n_r = 1）。这里用字符串作为分组键。

        Map<String, Set<Integer>> groupUserMap = new HashMap<>();
        for (int i = 0; i < userSize; i++) {
            String groupKey = windowSizeList.get(i) + "#" + epsilonList.get(i);
            groupUserMap.computeIfAbsent(groupKey, k -> new HashSet<>()).add(i);
        }
        List<SimplePrivacyGroup> resultList = new ArrayList<>(groupUserMap.size());
        for (Map.Entry<String, Set<Integer>> entry : groupUserMap.entrySet()) {
            String[] keyParts = entry.getKey().split("#");
            resultList.add(new SimplePrivacyGroup(
                    Integer.valueOf(keyParts[0]), Double.valueOf(keyParts[1]), entry.getValue()));
        }
        return resultList;

//        Map<CombinePair<Integer, Double>, Set<Integer>> groupUserMap = new HashMap<>();
//        for (int i = 0; i < userSize; i++) {
//            CombinePair<Integer, Double> groupKey = new CombinePair<>(windowSizeList.get(i), epsilonList.get(i));
//            groupUserMap.computeIfAbsent(groupKey, k -> new HashSet<>()).add(i);
//        }
//        List<SimplePrivacyGroup> resultList = new ArrayList<>(groupUserMap.size());
//
//        for (Map.Entry<CombinePair<Integer, Double>, Set<Integer>> entry : groupUserMap.entrySet()) {
//            CombinePair<Integer, Double> entryKey = entry.getKey();
//            resultList.add(new SimplePrivacyGroup(entryKey.getKey(), entryKey.getValue(), entry.getValue()));
//        }
//        return resultList;
    }
}
