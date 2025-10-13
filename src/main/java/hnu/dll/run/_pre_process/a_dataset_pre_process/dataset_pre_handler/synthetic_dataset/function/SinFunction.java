package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function;


import cn.edu.dll.basic.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class SinFunction implements DataGenerationFunction<Double> {
    private Integer currentTime = 0;
    private Double parameterA;
    private Double parameterOmega;
    private Double parameterH;
    private int precision = 2;

    public SinFunction(Double parameterA, Double parameterOmega, Double parameterH) {
        this.parameterA = parameterA;
        this.parameterOmega = parameterOmega;
        this.parameterH = parameterH;
    }


    @Override
    public List<Double> nextProbability(int size) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ++currentTime;
            result.add(getCurrentValue());
        }
        return result;
    }

    @Override
    public List<Double> nextProbability() {
        return nextProbability(1);
    }

    @Override
    public Double getInitializedValue() {
        return parameterH;
    }

    @Override
    public Double getCurrentValue() {
        Double tempValue = parameterA * Math.sin(parameterOmega * currentTime) + parameterH;
        return NumberUtil.roundFormat(tempValue, this.precision);
    }

    @Override
    public void reset() {
        this.currentTime = 0;
    }

    @Override
    public String getName() {
        return "sin";
    }
}
