package hnu.dll.structure.personalized_data;

import java.util.Map;

public class PrivacyGroupStatus<K> {
    protected Map<K, Double> dissimilarityEstimate;
    protected Double dissimilarityVariance;
    protected Map<K, Double> publicationEstimate;
    protected Double publicationVariance;
}
