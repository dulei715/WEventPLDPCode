package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function;

import cn.edu.dll.basic.NumberUtil;

import java.util.ArrayList;
import java.util.List;

public class LogFunction implements DataGenerationFunction<Double>{
    private Integer currentTime = 0;
    private Double parameterA;
    private Double parameterB;
    private int precision = 2;

    public LogFunction(Double parameterA, Double parameterB) {
        this.parameterA = parameterA;
        this.parameterB = parameterB;
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
        return parameterA;
    }

    @Override
    public Double getCurrentValue() {
        Double tempValue = this.parameterA*1.0/(1+Math.exp(-parameterB*currentTime));
        return NumberUtil.roundFormat(tempValue, this.precision);
    }

    @Override
    public void reset() {
        this.currentTime = 0;
    }

    @Override
    public String getName() {
        return "log";
    }
}
