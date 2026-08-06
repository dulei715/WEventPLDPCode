package tool_test;

import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ProposedSchemeUtilsTest {
    @Test
    public void gaOPSTest() {
        List<PrivacyGroupTriple> privacyGroupTripleList = new ArrayList<>();
        privacyGroupTripleList.add(new PrivacyGroupTriple(5, 0.4, 100));
        privacyGroupTripleList.add(new PrivacyGroupTriple(3, 0.2, 200));
        privacyGroupTripleList.add(new PrivacyGroupTriple(3, 0.2, 100));

        GroupAwareOptimalSelectionStruct resultStruct = ProposedSchemeUtils.groupAwareOptimalPopulationSelection(privacyGroupTripleList, 1.0, 5);
//        System.out.println(resultStruct);
        System.out.println(resultStruct.getOptimalErrorList());
        System.out.println(resultStruct.getOptimalSamplingSizeList());
        System.out.println(resultStruct.getOptimalEffectivePrivacyBudgetList());
    }
}
