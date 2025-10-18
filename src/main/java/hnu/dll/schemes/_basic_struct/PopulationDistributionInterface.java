package hnu.dll.schemes._basic_struct;

/**
 * 任何的population distribution方法都要设置最小可抽样人数，不然会严重影响准确度
 */
public interface PopulationDistributionInterface {
    Integer getPopulationSizeLowerBound();
}
