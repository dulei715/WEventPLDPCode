package hnu.dll.structure.personalized_data;

import java.util.Objects;
import java.util.Set;

public class PrivacyGroup extends AbstractPrivacyGroup {
    protected Set<Integer> userIDSet;

    public PrivacyGroup(Integer windowSize, Double epsilon, Set<Integer> userIDSet) {
        super(windowSize, epsilon);
        this.userIDSet = userIDSet;
    }



    public Set<Integer> getUserIDSet() {
        return userIDSet;
    }



    @Override
    public Integer getUserSize() {
        return this.userIDSet.size();
    }
}
