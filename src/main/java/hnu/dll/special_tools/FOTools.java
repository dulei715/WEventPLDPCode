package hnu.dll.special_tools;

import cn.edu.dll.basic.RandomUtil;

import java.util.Random;

public class FOTools {

    public static Double getGRRQ(Double epsilon, Integer domainSize) {
        Double expEpsilon = Math.exp(epsilon);
        Double q = 1 / (expEpsilon + domainSize - 1);
        return q;
    }

    public static Double getGRRP(Double epsilon, Integer domainSize) {
        Double expEpsilon = Math.exp(epsilon);
        Double p = expEpsilon / (expEpsilon + domainSize - 1);
        return p;
    }

    public static Integer gRRPerturb(Double epsilon, Integer realDataIndex, Integer domainSize, Random random) {
        Double p = getGRRP(epsilon, domainSize);
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
