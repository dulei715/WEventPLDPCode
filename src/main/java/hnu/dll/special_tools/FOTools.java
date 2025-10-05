package hnu.dll.special_tools;

import cn.edu.dll.basic.RandomUtil;

import java.util.Random;

public class FOTools {
    public static Integer perturb(Double epsilon, Integer realDataIndex, Integer domainSize, Random random) {
        Double expEpsilon = Math.exp(epsilon);
        Double p = expEpsilon / (expEpsilon + domainSize - 1);
        Boolean chosen = RandomUtil.isChosen(p, random);
        if (chosen) {
            return realDataIndex;
        }
        int newIndex = random.nextInt(domainSize - 1);
        if (newIndex >= realDataIndex) {
            newIndex += 1;
        }
        return newIndex;
    }
}
