package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.struct.BasicPair;
import hnu.dll.special_tools.MechanismUtils;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.junit.Test;

import java.util.*;

public class WindowSizeTest {
    public static final Double[] BudgetCandidate = new Double[]{
            0.2, 0.4, 0.6, 0.8
    };
    @Test
    public void samplingSizeTest() {
        Random random = new Random(2);
        Integer domainSize = 5;
        List<Integer> windowSizeList = new ArrayList<>();
        Integer userSize = 2000;
        List<Double> budgetList = new ArrayList<>();
        Integer tempInteger;
        for (int i = 0; i < userSize; ++i) {
            tempInteger = random.nextInt(4) + 1;
            if (tempInteger == 4) {
                tempInteger = random.nextDouble() < 0.01 ? 4 : 3;
            }
//            else if (tempInteger == 1) {
//                tempInteger = RandomUtil.isChosen(0.01) ? 1 : 2;
//            }
            windowSizeList.add(tempInteger);
            tempInteger = random.nextInt(4);
            if (tempInteger == 0) {
                tempInteger = random.nextDouble() < 0.5 ? 0 : 1;
            }
            budgetList.add(BudgetCandidate[tempInteger]);
        }
        List<Integer> samplingSizeList = new ArrayList<>(userSize);
        for (Integer windowSize : windowSizeList) {
            samplingSizeList.add((int)Math.floor(userSize * 1.0 / (2 * windowSize)));
        }
        LinkedHashMap<Integer, Integer> countList = BasicArrayUtil.getUniqueListWithCountList(samplingSizeList);
        LinkedHashMap<Double, Integer> budgetCountList = BasicArrayUtil.getUniqueListWithCountList(budgetList);
        MyPrint.showMap(countList);
        MyPrint.showSplitLine("*", 120);
        MyPrint.showMap(budgetCountList);
        MyPrint.showSplitLine("*", 120);
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(samplingSizeList, budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
        MyPrint.showSplitLine("*", 120);

        Map<BasicPair<Integer, Double>, Integer> pairCount = BasicUtils.countUniquePair(windowSizeList, budgetList);
        MyPrint.showMap(pairCount);

    }
}
