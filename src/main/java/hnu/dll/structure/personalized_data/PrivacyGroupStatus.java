package hnu.dll.structure.personalized_data;

import cn.edu.dll.map.MapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrivacyGroupStatus<K> {
    protected Map<K, Double> dissimilarityEstimate;
    protected Double dissimilarityVariance;
    protected Map<K, Double> publicationEstimate;
    protected Double publicationVariance;

    public PrivacyGroupStatus(Map<K, Double> dissimilarityEstimate, Double dissimilarityVariance, Map<K, Double> publicationEstimate, Double publicationVariance) {
        this.dissimilarityEstimate = dissimilarityEstimate;
        this.dissimilarityVariance = dissimilarityVariance;
        this.publicationEstimate = publicationEstimate;
        this.publicationVariance = publicationVariance;
    }

    public Map<K, Double> getDissimilarityEstimate() {
        return dissimilarityEstimate;
    }

    public Double getDissimilarityVariance() {
        return dissimilarityVariance;
    }

    public Map<K, Double> getPublicationEstimate() {
        return publicationEstimate;
    }

    public Double getPublicationVariance() {
        return publicationVariance;
    }

    @Override
    public String toString() {
        return "PrivacyGroupStatus{" +
                "dissimilarityEstimate=" + dissimilarityEstimate +
                ", dissimilarityVariance=" + dissimilarityVariance +
                ", publicationEstimate=" + publicationEstimate +
                ", publicationVariance=" + publicationVariance +
                '}';
    }

    public static <K> List<PrivacyGroupStatus<K>> getIntializedPrivacyGroupStatusList(List<K> domainList, int listSize) {
        List<PrivacyGroupStatus<K>> resultList = new ArrayList<>(listSize);
        for (int i = 0; i < listSize; i++) {
            resultList.add(new PrivacyGroupStatus<K>(MapUtils.getInitializedMap(domainList, 0D), 0D, MapUtils.getInitializedMap(domainList, 0D), 0D));
        }
        return resultList;
    }
}
