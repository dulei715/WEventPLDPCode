package hnu.dll.schemes._scheme_utils.nullified;

import cn.edu.dll.basic.BasicArrayUtil;

import java.util.List;

public class AverageNullifiedBound extends NullifiedBound {
    @Override
    public Double getNullifiedBound(List<Double> nullifiedTimestampList) {
        Double sum = BasicArrayUtil.getDoubleSum(nullifiedTimestampList);
        return sum / nullifiedTimestampList.size();
    }
}
