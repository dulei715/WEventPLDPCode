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
import java.util.TreeMap;

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


    // from here-----------
    @Test
    public void getCorrectedWeightSingleGroupTest() {
        List<PrivacyGroupTriple> privacyGroupList = new ArrayList<>();
        privacyGroupList.add(new PrivacyGroupTriple(5, 0.4, 100));

        List<TreeMap<String, Double>> groupEstimateList = new ArrayList<>();
        TreeMap<String, Double> estimate = new TreeMap<>();
        estimate.put("a", 0.6);
        estimate.put("b", 0.4);
        groupEstimateList.add(estimate);

        List<Double> varianceList = new ArrayList<>();
        varianceList.add(0.1);

        List<Double> finalVarianceList = new ArrayList<>();
        finalVarianceList.add(0.1);

        Double lowerBound = 0.01;

        List<SimpleMatrix> result = ProposedSchemeUtils.getCorrectedWeight(
                privacyGroupList, groupEstimateList, varianceList, finalVarianceList, lowerBound
        );

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).numRows());
        assertEquals(1, result.get(0).numCols());
        assertEquals(1.0, result.get(0).get(0, 0), 1e-8);
    }

    @Test
    public void getCorrectedWeightMultipleGroupsTest() {
        /*
         * 构造 3 个隐私组 (w, ε, userSize):
         *   PG0 = (5, 0.3, 100), PG1 = (3, 0.5, 100), PG2 = (2, 0.8, 100)
         *
         * 按 PrivacyGroupComparator 排序后顺序: PG2(w=2), PG1(w=3), PG0(w=5)
         *
         * constructDominatingGroupsAsIndex 的结果 (source PG_r 需满足 w_r <= w_H && ε_r >= ε_H):
         *   PG0(idx=0): source = [PG2(idx=2), PG1(idx=1)]  — 两者 w 更小且 ε 更大
         *   PG1(idx=1): source = [PG2(idx=2)]              — PG2 的 w 更小且 ε 更大
         *   PG2(idx=2): source = []                        — 无更小的 w
         */
        List<PrivacyGroupTriple> privacyGroupList = new ArrayList<>();
        privacyGroupList.add(new PrivacyGroupTriple(5, 0.3, 100));
        privacyGroupList.add(new PrivacyGroupTriple(3, 0.5, 100));
        privacyGroupList.add(new PrivacyGroupTriple(2, 0.8, 100));

        List<TreeMap<String, Double>> groupEstimateList = new ArrayList<>();

        TreeMap<String, Double> est0 = new TreeMap<>();
        est0.put("a", 0.5);
        est0.put("b", 0.5);
        groupEstimateList.add(est0);

        TreeMap<String, Double> est1 = new TreeMap<>();
        est1.put("a", 0.7);
        est1.put("b", 0.3);
        groupEstimateList.add(est1);

        TreeMap<String, Double> est2 = new TreeMap<>();
        est2.put("a", 0.3);
        est2.put("b", 0.7);
        groupEstimateList.add(est2);

        List<Double> varianceList = new ArrayList<>();
        varianceList.add(0.1);
        varianceList.add(0.15);
        varianceList.add(0.2);

        List<Double> finalVarianceList = new ArrayList<>();
        finalVarianceList.add(0.12);
        finalVarianceList.add(0.18);
        finalVarianceList.add(0.25);

        Double lowerBound = 0.1;

        List<SimpleMatrix> result = ProposedSchemeUtils.getCorrectedWeight(
                privacyGroupList, groupEstimateList, varianceList, finalVarianceList, lowerBound
        );

        int size = 3;
        assertEquals(size, result.size());

        for (int i = 0; i < size; i++) {
            SimpleMatrix weightVec = result.get(i);
            assertEquals(size, weightVec.numRows());
            assertEquals(1, weightVec.numCols());

            double weightSum = 0;
            for (int j = 0; j < size; j++) {
                assertTrue("权重不应为负数: w[" + i + "][" + j + "]=" + weightVec.get(j, 0),
                        weightVec.get(j, 0) >= -1e-10);
                weightSum += weightVec.get(j, 0);
            }
            assertEquals("权重向量元素之和应为1", 1.0, weightSum, 1e-6);
        }

        // PG2(idx=2) 无 dominating source → 权重全部集中在自身
        assertEquals(0.0, result.get(2).get(0, 0), 1e-8);
        assertEquals(0.0, result.get(2).get(1, 0), 1e-8);
        assertEquals(1.0, result.get(2).get(2, 0), 1e-8);

        // PG1(idx=1): source = [PG2(idx=2)] → 只有 index 1 和 2 有权重
        assertEquals(0.0, result.get(1).get(0, 0), 1e-8);
        assertTrue("PG1 自身权重应 > 0", result.get(1).get(1, 0) > 1e-10);
        assertTrue("PG1 的 source(PG2) 权重应 > 0", result.get(1).get(2, 0) > 1e-10);

        // PG0(idx=0): source = [PG2(idx=2), PG1(idx=1)] → 三个位置都应有正权重
        assertTrue("PG0 自身权重应 > 0", result.get(0).get(0, 0) > 1e-10);
        assertTrue("PG0 的 source(PG1) 权重应 > 0", result.get(0).get(1, 0) > 1e-10);
        assertTrue("PG0 的 source(PG2) 权重应 > 0", result.get(0).get(2, 0) > 1e-10);

        System.out.println("PG0 weights: " + result.get(0));
        System.out.println("PG1 weights: " + result.get(1));
        System.out.println("PG2 weights: " + result.get(2));
    }


}
