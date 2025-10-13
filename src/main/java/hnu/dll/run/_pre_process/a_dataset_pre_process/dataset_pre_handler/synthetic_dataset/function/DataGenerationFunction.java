package hnu.dll.run._pre_process.a_dataset_pre_process.dataset_pre_handler.synthetic_dataset.function;

import java.util.List;

public interface DataGenerationFunction<T> {
    List<T> nextProbability(int size);
    List<T> nextProbability();
    T getInitializedValue();
    T getCurrentValue();
    void reset();
    String getName();
}
