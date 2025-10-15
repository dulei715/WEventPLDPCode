package basic_test;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.io.print.MyPrint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BasicTest {
    @Test
    public void fun1() {
        List<Integer> data = BasicArrayUtil.getInitializedList(0, 10);
        data.set(2, 3);
        MyPrint.showList(data);
    }
}
