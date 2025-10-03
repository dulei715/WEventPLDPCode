package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.differential_privacy.ldp.frequency_oracle.foImp.GeneralizedRandomizedResponse;
import cn.edu.dll.io.print.MyPrint;
import hnu.dll.special_tools.MechanismUtils;
import hnu.dll.special_tools.PersonalizedFrequencyOracle;
import hnu.dll.special_tools.impl.GeneralizedPersonalizedRandomResponse;
import hnu.dll.structure.OptimalSelectionStruct;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLOutput;
import java.util.*;

public class MechanismTest {
    public List<Integer> windowSizeList;
    public List<Double> budgetList;
    public List<Integer> samplingSizeList;

    public Map<Double, Integer> budgetCountMap;
    public Map<Integer, Integer> windowSizeCountMap;

    Integer domainSize;

    Integer userSize;

    public PersonalizedFrequencyOracle<GeneralizedRandomizedResponse> pfo;

    public void initializeParameters() {
        this.domainSize = 5;
        budgetCountMap = BasicArrayUtil.getUniqueListWithCountList(this.budgetList);
        windowSizeCountMap = BasicArrayUtil.getUniqueListWithCountList(this.windowSizeList);
        this.pfo = new GeneralizedPersonalizedRandomResponse(domainSize, new TreeMap<>(budgetCountMap), GeneralizedRandomizedResponse.class);
        samplingSizeList = new ArrayList<>(userSize);
        for (Integer windowSize : windowSizeList) {
            samplingSizeList.add((int)Math.floor(userSize * 1.0 / (2 * windowSize)));
        }
    }

    @Before
    public void init() {
        this.windowSizeList = new ArrayList<>();
        this.budgetList = new ArrayList<>();
        this.samplingSizeList = new ArrayList<>();
        int userRepeatSize = 125;
        this.userSize = userRepeatSize * 16;
        for (int i = 0; i < userRepeatSize; ++i) {
            this.windowSizeList.add(4);
            this.windowSizeList.add(2);
            this.windowSizeList.add(3);
            this.windowSizeList.add(3);
            this.windowSizeList.add(1);
            this.windowSizeList.add(2);
            this.windowSizeList.add(4);
            this.windowSizeList.add(2);
            this.windowSizeList.add(3);
            this.windowSizeList.add(1);
            this.windowSizeList.add(2);
            this.windowSizeList.add(4);
            this.windowSizeList.add(2);
            this.windowSizeList.add(2);
            this.windowSizeList.add(3);
            this.windowSizeList.add(4);

            this.budgetList.add(0.2);
            this.budgetList.add(0.4);
            this.budgetList.add(0.6);
            this.budgetList.add(0.2);
            this.budgetList.add(0.2);
            this.budgetList.add(0.8);
            this.budgetList.add(0.6);
            this.budgetList.add(0.2);
            this.budgetList.add(0.4);
            this.budgetList.add(0.6);
            this.budgetList.add(0.4);
            this.budgetList.add(0.2);
            this.budgetList.add(0.6);
            this.budgetList.add(0.8);
            this.budgetList.add(0.6);
            this.budgetList.add(0.2);
        }

        initializeParameters();

    }

    @Test
    public void budgetCountTest() {
        MyPrint.showMap(budgetCountMap);
    }
    @Test
    public void windowSizeCountTest() {
        MyPrint.showMap(windowSizeCountMap);
    }
    @Test
    public void parametersTest() {
        TreeMap<Double, Double> distinctQMap = this.pfo.getDistinctQMap();
        TreeMap<Double, Double> distinctPMap = this.pfo.getDistinctPMap();
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
    public void windowSizeStatisticTest() {
        Integer totalCount = 0;
        Map<Integer, Double> windowSizeStatisticMap = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : windowSizeCountMap.entrySet()) {
            totalCount += entry.getValue();
        }
        for (Map.Entry<Integer, Integer> entry : windowSizeCountMap.entrySet()) {
            windowSizeStatisticMap.put(entry.getKey(), entry.getValue() * 1.0 / totalCount);
        }
        MyPrint.showMap(windowSizeStatisticMap);
    }

    @Test
    public void samplingSizeTest() {
        MyPrint.showList(samplingSizeList);
        MyPrint.showSplitLine("*", 150);
        LinkedHashMap<Integer, Integer> samplingSizeCountMap = BasicArrayUtil.getUniqueListWithCountList(samplingSizeList);
        MyPrint.showMap(samplingSizeCountMap);
    }

    @Test
    public void optimalSamplingSizeTest() {
        OptimalSelectionStruct optimalSelectionStruct = MechanismUtils.optimalPopulationSelection(this.samplingSizeList, this.budgetList, domainSize);
        System.out.println(optimalSelectionStruct);
    }


}
