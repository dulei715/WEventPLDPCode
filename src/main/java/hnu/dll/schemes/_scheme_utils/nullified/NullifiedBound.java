package hnu.dll.schemes._scheme_utils.nullified;

import java.util.List;

public abstract class NullifiedBound {
    public static final int AverageType = 0;
    public static final int MinimalType = 1;
    public static final int MaximalType = 2;
    public abstract Double getNullifiedBound(List<Double> nullifiedTimestampList);
}
