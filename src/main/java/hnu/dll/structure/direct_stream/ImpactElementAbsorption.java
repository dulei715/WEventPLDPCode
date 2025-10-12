package hnu.dll.structure.direct_stream;

public class ImpactElementAbsorption extends ImpactElement{

    // 用于记录M_2中的budget使用情况
    protected Double publicationBudgetUsage;
    protected Double publicationBudgetRemain;
    protected Double publicationRightBorder;
    public ImpactElementAbsorption(Integer timeSlot, Double totalPrivacyBudget, Integer windowSize) {
        super(timeSlot, totalPrivacyBudget, windowSize);
        this.publicationBudgetUsage = 0D;
        this.publicationRightBorder = timeSlot - 1D;
        this.publicationBudgetRemain = totalPrivacyBudget / 2;
    }

    public Double getPublicationBudgetUsage() {
        return publicationBudgetUsage;
    }
    public void updatePublicationBudgetUsage(Double budget) {
        this.publicationBudgetUsage += budget;
        this.publicationBudgetRemain -= budget;
        this.publicationRightBorder = Math.ceil(this.publicationBudgetUsage / (this.totalPrivacyBudget / (2*this.windowSize))) + this.timeSlot - 1;
    }
    public Double getPublicationRightBorder() {
        return this.publicationRightBorder;
    }

    public Double getPublicationBudgetRemain() {
        return publicationBudgetRemain;
    }
}
