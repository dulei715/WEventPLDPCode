package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.foImp.GeneralizedRandomizedResponse;
import cn.edu.dll.io.print.MyPrint;
import hnu.dll.special_tools.PersonalizedFrequencyOracle;
import hnu.dll.special_tools.impl.GeneralizedPersonalizedRandomResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
@Deprecated
public class Mechanism2Test {
    public List<Integer> windowSizeList;
    public List<Double> budgetList;

    public Map<Double, Integer> budgetCountMap;
//    public Map<Integer, Integer> windowSizeCountMap;

    Integer domainSize;

    public PersonalizedFrequencyOracle<GeneralizedRandomizedResponse> pfo;

    public void initializeParameters() {
        this.domainSize = 5;
//        budgetCountMap = BasicArrayUtil.getUniqueListWithCountList(this.budgetList);
//        windowSizeCountMap = BasicArrayUtil.getUniqueListWithCountList(this.windowSizeList);


        this.pfo = new GeneralizedPersonalizedRandomResponse(domainSize, new TreeMap<>(budgetCountMap), GeneralizedRandomizedResponse.class);
    }

    @Before
    public void init() {
        this.budgetCountMap = new TreeMap<>();
        this.budgetCountMap.put(0.2, 350);
        this.budgetCountMap.put(0.4, 250);
        this.budgetCountMap.put(0.6, 250);
        this.budgetCountMap.put(0.8, 150);
        initializeParameters();

    }

    @Test
    public void budgetCountTest() {
        MyPrint.showMap(budgetCountMap);
    }
    @Test
    public void parametersTest() {
        Map<Double, Double> distinctQMap = this.pfo.getDistinctQMap();
        Map<Double, Double> distinctPMap = this.pfo.getDistinctPMap();
        MyPrint.showMap(distinctQMap);
        MyPrint.showSplitLine("*", 150);
        MyPrint.showMap(distinctPMap);
    }

    @Test
    public void budgetStatisticTest() {
        Integer totalCount = 0;
        Map<Double, Double> budgetStatisticMap = new TreeMap<>();
        for (Map.Entry<Double, Integer> entry : budgetCountMap.entrySet()) {
            totalCount += entry.getValue();
        }
        for (Map.Entry<Double, Integer> entry : budgetCountMap.entrySet()) {
            budgetStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / totalCount);
        }
        MyPrint.showMap(budgetStatisticMap);
    }

    @Test
    public void optimalSamplingSizeTest() {

    }


}
