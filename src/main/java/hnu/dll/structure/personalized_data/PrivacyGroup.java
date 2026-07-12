package hnu.dll.structure.personalized_data;

import java.util.Objects;
import java.util.Set;

public class PrivacyGroup {
    protected Integer windowSize;
    protected Double epsilon;
    protected Set<Integer> userIDSet;

    public PrivacyGroup(Integer windowSize, Double epsilon, Set<Integer> userIDSet) {
        this.windowSize = windowSize;
        this.epsilon = epsilon;
        this.userIDSet = userIDSet;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public Double getEpsilon() {
        return epsilon;
    }

    public Set<Integer> getUserIDSet() {
        return userIDSet;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PrivacyGroup that = (PrivacyGroup) o;
        return Objects.equals(windowSize, that.windowSize) && Objects.equals(epsilon, that.epsilon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowSize, epsilon);
    }
}
