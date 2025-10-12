package example_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.RandomUtil;
import hnu.dll.schemes.main_scheme.impl.PLDPPopulationDistributionPlus;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class MechanismTotalTest {
    public Random random;
    public Integer totalUserSize;
    public Set<String> dataType;
    public List<Integer> windowSizeList;
    public List<Double> privacyBudgetList;

    @Before
    public void before() {
        this.random = new Random(0);
        this.totalUserSize = 2000;
        this.dataType = new HashSet<>(Arrays.asList("A", "B", "C", "D", "E"));
        this.windowSizeList = RandomUtil.getRandomIntegerList(1, 5, this.totalUserSize, this.random);
        this.privacyBudgetList = RandomUtil.getRandomDoubleList(0.5, 2.0, this.totalUserSize, this.random);

    }

    @Test
    public void pLPDTest() {
        PLDPPopulationDistributionPlus pLPDPlus = new PLDPPopulationDistributionPlus(this.dataType, this.privacyBudgetList, this.windowSizeList, this.random);
        pLPDPlus.initializeOptimalParameters();
    }

}
