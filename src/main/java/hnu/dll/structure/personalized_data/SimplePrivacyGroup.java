package hnu.dll.structure.personalized_data;

import java.util.Set;

public class SimplePrivacyGroup extends AbstractPrivacyGroup {
    // 这里的ID定为所在的List的下标
    protected Set<Integer> userIDSet;

    public SimplePrivacyGroup(Integer windowSize, Double epsilon, Set<Integer> userIDSet) {
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
