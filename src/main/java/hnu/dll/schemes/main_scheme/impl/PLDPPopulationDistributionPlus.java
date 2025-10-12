package hnu.dll.schemes.main_scheme.impl;

import hnu.dll.schemes.main_scheme.PersonalizedEventLocalPrivacyMechanism;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class PLDPPopulationDistributionPlus extends PersonalizedEventLocalPrivacyMechanism {
    public PLDPPopulationDistributionPlus(Set<String> dataTypeSet, List<Double> originalPrivacyBudgetList, List<Integer> windowSizeList, Random random) {
        super(dataTypeSet, originalPrivacyBudgetList, windowSizeList, random);
    }

    @Override
    protected void setCalculationParameters() {

    }

    @Override
    protected void setPublicationParameters() {

    }

    @Override
    public String getSimpleName() {
        return "PLPD+";
    }
}
