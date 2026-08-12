package tool_test;

import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.structure.matrix_data.BlockedRiskMatrixStructure;
import hnu.dll.structure.matrix_data.RiskMatrixStructure;
import hnu.dll.structure.matrix_data.SimpleRiskMatrixStructure;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void getPositiveDefiniteEigenvalueClippingTest() {
        /*
         * 对称正定矩阵 [[2, 1], [1, 2]]
         * 特征值: λ₁ = 3, λ₂ = 1
         * 裁剪下界 = 2 → λ₂ 从 1 裁剪为 2
         * 期望: V * diag(3, 2) * Vᵀ = [[2.5, 0.5], [0.5, 2.5]]
         */
        double[][] rawData = {{2, 1}, {1, 2}};
        Double lowerBound = 2.0;

        // ========== 场景1: BlockedRiskMatrixStructure (CurrentStatus) ==========
        SimpleMatrix dominatingMatrix = new SimpleMatrix(rawData);
        Double targetVariance = 0.5;
        BlockedRiskMatrixStructure blockedInput = new BlockedRiskMatrixStructure(targetVariance, dominatingMatrix);

        System.out.println(blockedInput);

        RiskMatrixStructure result1 = ProposedSchemeUtils.getPositiveDefiniteEigenvalueClipping(
                blockedInput, lowerBound, ProposedSchemeUtils.CurrentStatus
        );

        assertTrue("场景1应返回BlockedRiskMatrixStructure", result1 instanceof BlockedRiskMatrixStructure);
        BlockedRiskMatrixStructure blockedResult = (BlockedRiskMatrixStructure) result1;

        assertEquals(targetVariance, blockedResult.getTargetVariance(), 1e-10);

        SimpleMatrix correctedDom = blockedResult.getDominatingVarianceMatrix();
        assertEquals(2.5, correctedDom.get(0, 0), 1e-8);
        assertEquals(0.5, correctedDom.get(0, 1), 1e-8);
        assertEquals(0.5, correctedDom.get(1, 0), 1e-8);
        assertEquals(2.5, correctedDom.get(1, 1), 1e-8);

        // 所有特征值 ≥ lowerBound
        for (int i = 0; i < correctedDom.numRows(); i++) {
            double ev = correctedDom.eig().getEigenvalue(i).getReal();
            assertTrue("特征值 " + ev + " < " + lowerBound, ev >= lowerBound - 1e-8);
        }

        // ========== 场景2: SimpleRiskMatrixStructure (GeneralStatus) ==========
        SimpleMatrix riskMatrix = new SimpleMatrix(rawData);
        SimpleRiskMatrixStructure simpleInput = new SimpleRiskMatrixStructure(riskMatrix);

        RiskMatrixStructure result2 = ProposedSchemeUtils.getPositiveDefiniteEigenvalueClipping(
                simpleInput, lowerBound, ProposedSchemeUtils.GeneralStatus
        );

        assertTrue("场景2应返回SimpleRiskMatrixStructure", result2 instanceof SimpleRiskMatrixStructure);
        SimpleRiskMatrixStructure simpleResult = (SimpleRiskMatrixStructure) result2;

        SimpleMatrix correctedMatrix = simpleResult.getRiskMatrix();
        assertEquals(2.5, correctedMatrix.get(0, 0), 1e-8);
        assertEquals(0.5, correctedMatrix.get(0, 1), 1e-8);
        assertEquals(0.5, correctedMatrix.get(1, 0), 1e-8);
        assertEquals(2.5, correctedMatrix.get(1, 1), 1e-8);

        for (int i = 0; i < correctedMatrix.numRows(); i++) {
            double ev = correctedMatrix.eig().getEigenvalue(i).getReal();
            assertTrue("特征值 " + ev + " < " + lowerBound, ev >= lowerBound - 1e-8);
        }
    }

    @Test
    public void getPositiveDefiniteEigenvalueClippingNoClippingNeededTest() {
        // 下界 0.5 < min(λ₁, λ₂)=1，无需裁剪，矩阵应保持不变
        SimpleMatrix dominatingMatrix = new SimpleMatrix(new double[][]{{2, 1}, {1, 2}});
        BlockedRiskMatrixStructure blockedInput = new BlockedRiskMatrixStructure(0.5, dominatingMatrix);

        RiskMatrixStructure result = ProposedSchemeUtils.getPositiveDefiniteEigenvalueClipping(
                blockedInput, 0.5, ProposedSchemeUtils.CurrentStatus
        );

        BlockedRiskMatrixStructure blockedResult = (BlockedRiskMatrixStructure) result;
        SimpleMatrix correctedDom = blockedResult.getDominatingVarianceMatrix();

        assertEquals(2.0, correctedDom.get(0, 0), 1e-8);
        assertEquals(1.0, correctedDom.get(0, 1), 1e-8);
        assertEquals(1.0, correctedDom.get(1, 0), 1e-8);
        assertEquals(2.0, correctedDom.get(1, 1), 1e-8);
    }
}
