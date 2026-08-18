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
        Map<CombinePair<Integer, Double>, Set<Integer>> groupUserMap = new HashMap<>();
        for (int i = 0; i < userSize; i++) {
            CombinePair<Integer, Double> groupKey = new CombinePair<>(windowSizeList.get(i), epsilonList.get(i));
            groupUserMap.computeIfAbsent(groupKey, k -> new HashSet<>()).add(i);
        }
        List<SimplePrivacyGroup> resultList = new ArrayList<>(groupUserMap.size());

        for (Map.Entry<CombinePair<Integer, Double>, Set<Integer>> entry : groupUserMap.entrySet()) {
            CombinePair<Integer, Double> entryKey = entry.getKey();
            resultList.add(new SimplePrivacyGroup(entryKey.getKey(), entryKey.getValue(), entry.getValue()));
        }
        return resultList;
    }
}
