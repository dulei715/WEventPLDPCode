package hnu.dll.structure.personalized_data;

public class PrivacyGroupTriple extends AbstractPrivacyGroup{
    protected Integer userSize;

    public PrivacyGroupTriple(Integer windowSize, Double epsilon, Integer userSize) {
        super(windowSize, epsilon);
        this.userSize = userSize;
    }

    @Override
    public Integer getUserSize() {
        return this.userSize;
    }
}
