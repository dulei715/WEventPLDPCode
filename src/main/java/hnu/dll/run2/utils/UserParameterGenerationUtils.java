package hnu.dll.run2.utils;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.pair.CombineTriple;
import hnu.dll.run2.utils.structs.UserParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserParameterGenerationUtils {
    /**
     * 给定隐私预算和窗口大小范围，为每个user生成参数
     * @param userSize
     * @param budgetCandidateList
     * @param windowSizeCandidateList
     * @param random
     * @return
     */
    public static List<UserParameter> generateUserParameterList(Integer userSize, List<Double> budgetCandidateList, List<Integer> windowSizeCandidateList, Random random) {
        List<UserParameter> result = new ArrayList<>(userSize);
        Integer budgetListSize = budgetCandidateList.size();
        Integer windowSizeListSize = windowSizeCandidateList.size();
        Integer randomIndex;
        Double budget;
        Integer windowSize;
        for (int i = 0; i < userSize; i++) {
            randomIndex = random.nextInt(budgetListSize);
            budget = budgetCandidateList.get(randomIndex);
            randomIndex = random.nextInt(windowSizeListSize);
            windowSize = windowSizeCandidateList.get(randomIndex);
            result.add(new UserParameter(i, budget, windowSize));
        }
        return result;
    }

}
