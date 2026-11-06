package hnu.dll.schemes._basic_struct;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes the latest group-level estimates for shift-specific evaluation.
 */
public interface GroupEstimationProvider {
    List<Map<Integer, Double>> getLastGroupEstimationList();

    List<Double> getGroupProportionList();

    List<Set<Integer>> getGroupUserIndexSetList();
}
