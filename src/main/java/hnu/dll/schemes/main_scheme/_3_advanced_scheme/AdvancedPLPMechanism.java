package hnu.dll.schemes.main_scheme._3_advanced_scheme;

import hnu.dll.structure.HistoryPopulationQueue;
import hnu.dll.structure.selection_struct.OptimalSelectionStruct;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AdvancedPLPMechanism {
    protected Random random;

    protected int currentTime;
    protected List<Double> originalPrivacyBudgetList;
    protected List<Double> newPrivacyBudgetList;
    protected List<Integer> windowSizeList;
    protected List<Integer> domainIndexList;
    protected Integer domainSize;
    protected Integer userSize;

    protected OptimalSelectionStruct optimalSelectionStruct;
    protected Integer optimalWindowSize;

    protected Set<Integer> candidateUserIndexSet;

    protected Map<Integer, Double> lastReleaseEstimation;

    protected HistoryPopulationQueue samplingSubMechanismHistoryQueue;
    protected HistoryPopulationQueue publicationSubMechanismHistoryQueue;

    protected List<Double> distinctBudgetList;
    protected List<Integer> distinctWindowSizeList;
}
