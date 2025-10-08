package hnu.dll.structure;

import java.util.List;
import java.util.Objects;

public class BudgetGroupData implements Comparable<BudgetGroupData> {
    private Double privacyBudget;
    // data index list
    private List<Integer> obfuscatedDataIndexList;

    public BudgetGroupData(Double privacyBudget, List<Integer> obfuscatedDataIndexList) {
        this.privacyBudget = privacyBudget;
        this.obfuscatedDataIndexList = obfuscatedDataIndexList;
    }

    public Double getPrivacyBudget() {
        return privacyBudget;
    }

    public List<Integer> getObfuscatedDataIndexList() {
        return obfuscatedDataIndexList;
    }

    public void combine(List<Integer> indexList) {
        this.obfuscatedDataIndexList.addAll(indexList);
    }


    @Override
    public int compareTo(BudgetGroupData budgetGroupData) {
        return this.privacyBudget.compareTo(budgetGroupData.privacyBudget);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetGroupData that = (BudgetGroupData) o;
        return Objects.equals(privacyBudget, that.privacyBudget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(privacyBudget);
    }
}
