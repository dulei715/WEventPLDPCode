package important_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultCombineTest {
    @Test
    public void realDatasetCombine() {
        // 前两个是epsilon变化的两个real dataset的平均值，后两个是w变化的两个real dataset的平均值。后面类似
        List<Double> improveListOfRealDatasetForAbsorption = Arrays.asList(
                0.12163122582976924, 0.09384977497228493, 0.08473951096240959, 0.07475373930455685
        );
        List<Double> furtherListOfRealDatasetForAbsorption = Arrays.asList(
                0.5683288494356041, 0.516175884721569, 0.5143026537975617, 0.49319268501376873
        );
        System.out.println("improve real absorption: " + BasicArrayUtil.getDoubleAverage(improveListOfRealDatasetForAbsorption));
        System.out.println("further real absorption: " + BasicArrayUtil.getDoubleAverage(furtherListOfRealDatasetForAbsorption));
    }

    @Test
    public void syntheticDatasetCombine() {
        List<Double> improveListOfSyntheticDatasetForAbsorption = Arrays.asList(
                0.074494982101513, 0.0683067868555052, -0.11823497066241248, 0.04701173404729411, 0.04235377715221609, -0.1238830331938731
        );
        List<Double> furtherListOfSyntheticDatasetForAbsorption = Arrays.asList(
                0.5036608820006657, 0.49586961983211975, 0.276312846267428, 0.46534106109957507, 0.4609226171702076, 0.24358575507366592
        );
        System.out.println("improve real absorption: " + BasicArrayUtil.getDoubleAverage(improveListOfSyntheticDatasetForAbsorption));
        System.out.println("further real absorption: " + BasicArrayUtil.getDoubleAverage(furtherListOfSyntheticDatasetForAbsorption));
    }
}
