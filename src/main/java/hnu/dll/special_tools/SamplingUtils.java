package hnu.dll.special_tools;

public class SamplingUtils {
    /**
     * 获取采样误差
     * @param userSize
     * @param sampleSize
     * @return
     */
    public static Double getSamplingVariance(Integer userSize, Integer sampleSize) {
        return (userSize - sampleSize) * 1.0 / (sampleSize * (userSize - 1));
    }
}
