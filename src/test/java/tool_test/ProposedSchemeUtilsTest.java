package tool_test;

import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.structure.matrix_data.BlockedRiskMatrixStructure;
import hnu.dll.structure.matrix_data.SimpleRiskMatrixStructure;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import org.ejml.simple.SimpleMatrix;
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

    @Test
    public void RiskMatrixStructure() {
        Double variance = 0.4;
        SimpleMatrix riskMatrix = new SimpleMatrix(2, 2);
        riskMatrix.set(0, 0, 1.0);
        riskMatrix.set(0, 1, 2.0);
        riskMatrix.set(1, 0, 3.0);
        riskMatrix.set(1, 1, 4.0);
        System.out.println(riskMatrix);
        BlockedRiskMatrixStructure structA = new BlockedRiskMatrixStructure(variance, riskMatrix);
        System.out.println(structA);

        SimpleRiskMatrixStructure structB = new SimpleRiskMatrixStructure(riskMatrix);
        System.out.println(structB);
    }
}
