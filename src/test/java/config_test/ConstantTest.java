package config_test;

import hnu.dll._config.Constant;
import org.junit.Test;

public class ConstantTest {
    @Test
    public void pathTest(){
        System.out.println(Constant.projectPath);
        System.out.println(Constant.basicDatasetPath);
    }
}
