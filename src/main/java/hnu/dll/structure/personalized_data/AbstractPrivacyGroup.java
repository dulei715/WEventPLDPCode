package hnu.dll.structure.personalized_data;

public abstract class AbstractPrivacyGroup {

    public static final int StrictlyPrivatelyDominating = 1;
    public static final int StrictlyPrivatelyDominated = -1;
    public static final int NotPrivatelyDominated = -2;
    public static final int PrivatelyDominatingEqual = 0;

    protected Integer windowSize;
    protected Double epsilon;
    protected abstract Integer getUserSize();

    public int isPrivacyDominated(AbstractPrivacyGroup otherGroup) {

    }
}
