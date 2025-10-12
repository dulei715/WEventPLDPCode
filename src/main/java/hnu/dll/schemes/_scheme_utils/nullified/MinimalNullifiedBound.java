package hnu.dll.schemes._scheme_utils.nullified;

import cn.edu.dll.basic.BasicArrayUtil;

import java.util.List;

public class MinimalNullifiedBound extends NullifiedBound {
    @Override
    public Double getNullifiedBound(List<Double> nullifiedTimestampList) {
        return BasicArrayUtil.getDoubleMinValue(nullifiedTimestampList);
    }
}
