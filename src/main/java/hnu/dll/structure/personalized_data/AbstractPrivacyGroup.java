package hnu.dll.structure.personalized_data;

import java.util.Objects;

public abstract class AbstractPrivacyGroup {

    public static final int StrictlyPrivatelyDominating = 1;
    public static final int StrictlyPrivatelyDominated = -1;
    public static final int NotPrivatelyDominated = -2;
    public static final int PrivatelyDominatingEqual = 0;

    protected Integer windowSize;
    protected Double epsilon;
    public abstract Integer getUserSize();

    public AbstractPrivacyGroup(Integer windowSize, Double epsilon) {
        this.windowSize = windowSize;
        this.epsilon = epsilon;
    }

    public int getPrivacyDominatingStatus(AbstractPrivacyGroup otherGroup) {
        if (Objects.equals(this.windowSize, otherGroup.windowSize) && Objects.equals(this.epsilon, otherGroup.epsilon)) {
            return PrivatelyDominatingEqual;
        }
        if (this.windowSize >= otherGroup.windowSize && this.epsilon <= otherGroup.epsilon) {
            return StrictlyPrivatelyDominating;
        } else if (this.windowSize <= otherGroup.windowSize && this.epsilon >= otherGroup.epsilon) {
            return StrictlyPrivatelyDominated;
        }
        return NotPrivatelyDominated;
    }


    public Integer getWindowSize() {
        return windowSize;
    }

    public Double getEpsilon() {
        return epsilon;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractPrivacyGroup that = (AbstractPrivacyGroup) o;
        return Objects.equals(windowSize, that.windowSize) && Objects.equals(epsilon, that.epsilon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowSize, epsilon);
    }
}
