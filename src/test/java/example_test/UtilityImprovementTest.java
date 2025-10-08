package example_test;

import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.BasicPair;
import hnu.dll.special_tools.PerturbUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class UtilityImprovementTest {

    public List<Double> epsilonList = new ArrayList<>();

    @Before
    public void initialize() {
        epsilonList.add(0.2);
        epsilonList.add(0.3);
        epsilonList.add(0.5);
        epsilonList.add(0.6);
        epsilonList.add(0.8);

    }


    @Test
    public void alphaBetaTest() {


        Integer domainSize = 2;

        List<Double> pList = new ArrayList<>(), qList = new ArrayList<>();
        Double tempQ, tempP;
        for (Double epsilon : epsilonList) {
            tempQ = 1.0 / (Math.exp(epsilon) + domainSize - 1);
            tempP = tempQ * Math.exp(epsilon);
            pList.add(tempP);
            qList.add(tempQ);
        }

        Double smallP = pList.get(2), smallQ = qList.get(2);
        Double largeP = pList.get(3), largeQ = qList.get(3);
        BasicPair<Double, Double> result = PerturbUtils.getGRRRePerturbParameters(smallP, smallQ, largeP, largeQ, domainSize);
        System.out.println("p_3 = " + smallP);
        System.out.println("q_3 = " + smallQ);
        System.out.println("p_4 = " + largeP);
        System.out.println("q_4 = " + largeQ);
        System.out.println("alpha_{4,3} = " + result.getKey());
        System.out.println("beta_{4,3} = " + result.getValue());

        largeP = pList.get(4);
        largeQ = qList.get(4);

        MyPrint.showSplitLine("*", 150);

        result = PerturbUtils.getGRRRePerturbParameters(smallP, smallQ, largeP, largeQ, domainSize);
        System.out.println("p_3 = " + smallP);
        System.out.println("q_3 = " + smallQ);
        System.out.println("p_5 = " + largeP);
        System.out.println("q_5 = " + largeQ);
        System.out.println("alpha_{5,3} = " + result.getKey());
        System.out.println("beta_{5,3} = " + result.getValue());

    }

    @Test
    public void optimalPopulationTest() {

    }

}
