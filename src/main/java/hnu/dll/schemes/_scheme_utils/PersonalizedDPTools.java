package hnu.dll.schemes._scheme_utils;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.differential_privacy.noise.LaplaceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PersonalizedDPTools {
    public static List<Integer> sampleIndex(List<Double> epsilonList, Double threshold) {
        List<Integer> result = new ArrayList<Integer>();
        Double epsilon, probability;
        for (int index = 0; index < epsilonList.size(); index++) {
            epsilon = epsilonList.get(index);
            if (epsilon >= threshold) {
                result.add(index);
            } else {
                probability = (Math.exp(epsilon) - 1) / (Math.exp(threshold) - 1);
                if (RandomUtil.isChosen(probability)) {
                    result.add(index);
                }
            }
        }
        return result;
    }


    public static TreeMap<String, Double> getNoiseCount(TreeMap<String, Integer> data, Double privacyBudget) {
        String tempTypeName;
        Integer tempCount;
        Double tempNoiseCount;
        TreeMap<String, Double> result = new TreeMap<String, Double>();
        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            tempTypeName = entry.getKey();
            tempCount = entry.getValue();
            tempNoiseCount = tempCount + LaplaceUtils.getLaplaceNoise(1, privacyBudget);
            result.put(tempTypeName, tempNoiseCount);
        }
        return result;
    }

}
