package example_test;

import hnu.dll.schemes._scheme_utils.MechanismUtils;
import hnu.dll.structure.OptimalSelectionStruct;
import hnu.dll.utils.BasicUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptimaPopulationSelectionTest {
    @Test
    public void test1() {
        Integer domainSize = 2;
//        List<Integer> basicSamplingSizeList = Arrays.asList(3, 5, 9, 8, 9, 5, 6, 5, 8, 9), samplingSizeList = new ArrayList<>();
//        List<Double> basicBudgetList = Arrays.asList(0.1, 0.4, 0.4, 0.1, 0.4, 0.4, 0.8, 0.8, 0.8, 0.4), budgetList = new ArrayList<>();
        List<Integer> samplingSizeList = Arrays.asList(3, 5, 9, 8, 9, 5, 6, 5, 8, 9);
        List<Double> budgetList = Arrays.asList(0.1, 0.4, 0.4, 0.1, 0.4, 0.4, 0.8, 0.8, 0.8, 0.4);
        int repeatSize = 10;
//        for (int i = 0; i < repeatSize; i++) {
//            samplingSizeList.addAll(basicSamplingSizeList);
//            budgetList.addAll(basicBudgetList);
//        }
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(samplingSizeList, budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
    }
}
