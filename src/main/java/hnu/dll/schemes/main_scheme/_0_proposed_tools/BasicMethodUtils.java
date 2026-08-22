package hnu.dll.schemes.main_scheme._0_proposed_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import hnu.dll._config.Constant;
import hnu.dll.structure.personalized_data.AbstractPrivacyGroup;
import hnu.dll.utils.comparator.PrivacyGroupComparator;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class BasicMethodUtils {


    public static Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> constructDominatingGroups(final List<? extends AbstractPrivacyGroup> groupList) {
        Map<AbstractPrivacyGroup, List<AbstractPrivacyGroup>> result = new HashMap<>();

        // 拷贝一份，避免改变原 groupList 的顺序
        List<AbstractPrivacyGroup> sortedGroups = new ArrayList<>(groupList);
        sortedGroups.sort(Constant.privacyGroupComparator);
        /*
         * 已经扫描过的 group 按 epsilon 保存。
         * 因为已经按照 w 升序扫描，
         * seen 中的 group 自动满足 w_r <= w_H。
         */
        TreeMap<Double, List<AbstractPrivacyGroup>> seen =  new TreeMap<>();
        for (AbstractPrivacyGroup targetGroup : sortedGroups) {
            List<AbstractPrivacyGroup> sourceGroups = new ArrayList<>();
            double epsilonH = targetGroup.getEpsilon();
            /*
             * 获取所有 epsilon_r >= epsilon_H 的已扫描 group。
             */
            NavigableMap<Double, List<AbstractPrivacyGroup>> candidates = seen.tailMap(epsilonH, true);
            for (List<AbstractPrivacyGroup> groups : candidates.values()) {
                sourceGroups.addAll(groups);
            }
            result.put(targetGroup, sourceGroups);
            // 将当前 group 加入 seen
            List<AbstractPrivacyGroup> sameEpsilonGroups = seen.get(epsilonH);
            if (sameEpsilonGroups == null) {
                sameEpsilonGroups = new ArrayList<>();
                seen.put(epsilonH, sameEpsilonGroups);
            }
            sameEpsilonGroups.add(targetGroup);
        }
        return result;
    }


    public static Map<Integer, List<Integer>> constructDominatingGroupsAsIndex(final List<? extends AbstractPrivacyGroup> groupList) {
        Map<Integer, List<Integer>> result = new HashMap<>();

        // 拷贝一份，避免改变原 groupList 的顺序
        TreeMap<AbstractPrivacyGroup, Integer> sortedGroupIndexMap = new TreeMap<>(Constant.privacyGroupComparator);
        for (int i = 0; i < groupList.size(); i++) {
            sortedGroupIndexMap.put(groupList.get(i), i);
        }
        /*
         * 已经扫描过的 group 按 epsilon 保存。
         * 因为已经按照 w 升序扫描，
         * seen 中的 group 自动满足 w_r <= w_H。
         */
        TreeMap<Double, List<Integer>> seen =  new TreeMap<>();
        for (Map.Entry<AbstractPrivacyGroup, Integer> entry : sortedGroupIndexMap.entrySet()) {
            AbstractPrivacyGroup targetGroup = entry.getKey();
            Integer targetGroupIndex = entry.getValue();
            List<Integer> sourceGroupIndexList = new ArrayList<>();
            double epsilonH = targetGroup.getEpsilon();
            /*
             * 获取所有 epsilon_r >= epsilon_H 的已扫描 group。
             */
            NavigableMap<Double, List<Integer>> candidates = seen.tailMap(epsilonH, true);
            for (List<Integer> groupIndexes : candidates.values()) {
                sourceGroupIndexList.addAll(groupIndexes);
            }
            result.put(targetGroupIndex, sourceGroupIndexList);
            // 将当前 group 加入 seen
            List<Integer> sameEpsilonGroups = seen.get(epsilonH);
            if (sameEpsilonGroups == null) {
                sameEpsilonGroups = new ArrayList<>();
                seen.put(epsilonH, sameEpsilonGroups);
            }
            sameEpsilonGroups.add(targetGroupIndex);
        }
        return result;
    }

    public static List<Double> getGammaParameterList(final List<Double> piList, final Map<Integer, List<Integer>> indexSourceMap, final List<SimpleMatrix> lambdaList) {
        int groupSize = piList.size();
        Double[] resultArray = new Double[groupSize];
        Arrays.fill(resultArray, 0D);
        int tempTargetIndex;
        List<Integer> tempSourceIndexList;
        double tempTargetPi;
        SimpleMatrix tempLambda;
        for (Map.Entry<Integer, List<Integer>> entry : indexSourceMap.entrySet()) {
            tempTargetIndex = entry.getKey();
            tempTargetPi = piList.get(tempTargetIndex);
            tempLambda = lambdaList.get(tempTargetIndex);
            resultArray[tempTargetIndex] += tempTargetPi * tempLambda.get(0, 0);
            tempSourceIndexList = entry.getValue();
            for (int i = 0; i < tempSourceIndexList.size(); i++) {
                int tempSourceIndex = tempSourceIndexList.get(i);
                resultArray[tempSourceIndex] += tempTargetPi * tempLambda.get(i + 1, 0);
            }
        }
        return Arrays.asList(resultArray);
    }

}
