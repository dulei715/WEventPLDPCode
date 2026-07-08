package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;
import cn.edu.dll.statistic.StatisticTool;
import hnu.dll.special_tools.PFOUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MechanismExampleTest {
    @Test
    public void pLDPPlusTest() {
        List<Double> piList = Arrays.asList(0.4, 0.35, 0.25);
        List<List<Double>> disNoiseStatisticList = Arrays.asList(
                Arrays.asList(0.5, 0.2, 0.2, 0.1),
                Arrays.asList(0.48, 0.22, 0.19, 0.11),
                Arrays.asList(0.3, 0.35, 0.2, 0.15)
        );
        List<Double> disVarianceList = Arrays.asList(0.008, 0.012, 0.016);

        List<Double> weightedStatisticList = StatisticTool.getWeightedStatisticList(disNoiseStatisticList, piList);
        MyPrint.showList(weightedStatisticList);

        MyPrint.showSplitLine("*", 150);

        List<Double> lastStatistic = Arrays.asList(0.25, 0.35, 0.25, 0.15);
//        Double varianceSum = BasicArrayUtil.getDoubleSum(disVarianceList);
        List<Double> piPowerValueList = BasicArrayUtil.toPowerValue(piList, 2);
        Double varianceSum = BasicArrayUtil.getLinearCombination(disVarianceList, piPowerValueList);
        Double dissimilarity = PFOUtils.getDissimilarity(weightedStatisticList, lastStatistic, varianceSum);
        System.out.println(dissimilarity);
    }
}
