package hnu.dll.special_tools;

import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.struct.BasicPair;

import java.util.Random;

public class PerturbUtils {


    public static BasicPair<Double, Double> getOUERePerturbParameters(Double smallP, Double smallQ, Double largeP, Double largeQ) {
        Double beta = (largeP * smallQ - smallP * largeQ) / (largeP - largeQ);
        Double alpha = (smallP - smallQ) / (largeP - largeQ) + beta;
        return new BasicPair<>(alpha, beta);
    }

    public static BasicPair<Double, Double> getGRRRePerturbParameters(Double smallP, Double smallQ, Double largeP, Double largeQ, Integer domainSize) {
        Double paramA = domainSize * largeP - 1;
        Double paramB = largeP - smallP;
        Double beta = paramB / paramA;
        Double alpha = 1 - beta;
        return new BasicPair<>(alpha, beta);
    }




}
