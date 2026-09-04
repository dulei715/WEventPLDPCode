package tool_test;

import hnu.dll.schemes.main_scheme._0_proposed_tools.BasicMethodUtils;
import hnu.dll.schemes.main_scheme._0_proposed_tools.ProposedSchemeUtils;
import hnu.dll.structure.matrix_data.BlockedRiskMatrixStructure;
import hnu.dll.structure.matrix_data.RiskMatrixStructure;
import hnu.dll.structure.matrix_data.SimpleRiskMatrixStructure;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import org.ejml.simple.SimpleMatrix;
import org.junit.Test;

import java.util.*;

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
        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
        indexSourceMap.put(0, Collections.emptyList());

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
                indexSourceMap, groupEstimateList, varianceList, finalVarianceList, lowerBound
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
         * constructDominatingGroupsAsIndex 的结果 (source PG_r 需满足 w_r <= w_H && ε_r >= ε_H):
         *   PG0(idx=0): source = [PG2(idx=2), PG1(idx=1)]  — 两者 w 更小且 ε 更大
         *   PG1(idx=1): source = [PG2(idx=2)]              — PG2 的 w 更小且 ε 更大
         *   PG2(idx=2): source = []                        — 无更小的 w
         *
         * 返回的权重向量维度 = 1 + dominatingSize:
         *   PG0: 3×1 (target + 2 sources), 顺序: [PG0, PG2, PG1]
         *   PG1: 2×1 (target + 1 source),  顺序: [PG1, PG2]
         *   PG2: 1×1 (target only),        顺序: [PG2]
         */
        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
        indexSourceMap.put(0, Arrays.asList(2, 1));
        indexSourceMap.put(1, Collections.singletonList(2));
        indexSourceMap.put(2, Collections.emptyList());

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
                indexSourceMap, groupEstimateList, varianceList, finalVarianceList, lowerBound
        );

        int size = 3;
        assertEquals(size, result.size());

        // PG0(idx=0): source = [2, 1] → 权重向量长度 = 3
        SimpleMatrix w0 = result.get(0);
        assertEquals(3, w0.numRows());
        assertEquals(1, w0.numCols());
        double sum0 = 0;
        for (int j = 0; j < w0.numRows(); j++) {
            assertTrue("PG0 权重不应为负数: w[" + j + "]=" + w0.get(j, 0), w0.get(j, 0) >= -1e-10);
            sum0 += w0.get(j, 0);
        }
        assertEquals("PG0 权重之和应为1", 1.0, sum0, 1e-6);
        assertTrue("PG0 自身权重应 > 0", w0.get(0, 0) > 1e-10);
        assertTrue("PG0 的 source(PG2) 权重应 > 0", w0.get(1, 0) > 1e-10);
        assertTrue("PG0 的 source(PG1) 权重应 > 0", w0.get(2, 0) > 1e-10);

        // PG1(idx=1): source = [2] → 权重向量长度 = 2
        SimpleMatrix w1 = result.get(1);
        assertEquals(2, w1.numRows());
        assertEquals(1, w1.numCols());
        double sum1 = 0;
        for (int j = 0; j < w1.numRows(); j++) {
            assertTrue("PG1 权重不应为负数: w[" + j + "]=" + w1.get(j, 0), w1.get(j, 0) >= -1e-10);
            sum1 += w1.get(j, 0);
        }
        assertEquals("PG1 权重之和应为1", 1.0, sum1, 1e-6);
        assertTrue("PG1 自身权重应 > 0", w1.get(0, 0) > 1e-10);
        assertTrue("PG1 的 source(PG2) 权重应 > 0", w1.get(1, 0) > 1e-10);

        // PG2(idx=2): source = [] → 权重向量长度 = 1, 权重全部集中在自身
        SimpleMatrix w2 = result.get(2);
        assertEquals(1, w2.numRows());
        assertEquals(1, w2.numCols());
        assertEquals(1.0, w2.get(0, 0), 1e-8);

        System.out.println("PG0 weights (3×1): " + w2);
        System.out.println("PG0 weights (3×1): " + w0);
        System.out.println("PG1 weights (2×1): " + w1);
        System.out.println("PG2 weights (1×1): " + w2);
    }

    @Test
    public void partialAbsorptionCorrectedWeightTest() {
        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
        indexSourceMap.put(0, Collections.singletonList(1));
        indexSourceMap.put(1, Collections.emptyList());

        TreeMap<String, Double> estimate0 = new TreeMap<>();
        estimate0.put("a", 0.8);
        estimate0.put("b", 0.2);
        TreeMap<String, Double> estimate1 = new TreeMap<>();
        estimate1.put("a", 0.4);
        estimate1.put("b", 0.6);
        List<TreeMap<String, Double>> mixedPublicationEstimateList = Arrays.asList(estimate0, estimate1);

        TreeMap<String, Double> disEstimate0 = new TreeMap<>();
        disEstimate0.put("a", 0.5);
        disEstimate0.put("b", 0.5);
        TreeMap<String, Double> disEstimate1 = new TreeMap<>();
        disEstimate1.put("a", 0.4);
        disEstimate1.put("b", 0.6);
        List<TreeMap<String, Double>> disEstimateList = Arrays.asList(disEstimate0, disEstimate1);

        List<SimpleMatrix> weightList = ProposedSchemeUtils.getPartialAbsorptionCorrectedWeight(
                indexSourceMap, mixedPublicationEstimateList, disEstimateList,
                Arrays.asList(0.2, 0.1), Arrays.asList(0.2, 0.15),
                Arrays.asList(0.1, 0.1), Collections.singleton(0), 0.01);

        assertEquals(2, weightList.size());
        assertEquals(2, weightList.get(0).numRows());
        assertEquals(1, weightList.get(1).numRows());
        assertEquals(1.0, weightList.get(0).elementSum(), 1e-6);
        assertEquals(1.0, weightList.get(1).elementSum(), 1e-6);
    }

    @Test
    public void partialAbsorptionBAPUEErrorTest() {
        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
        indexSourceMap.put(0, Collections.emptyList());
        indexSourceMap.put(1, Collections.emptyList());
        List<SimpleMatrix> lambdaList = Arrays.asList(
                new SimpleMatrix(1, 1, true, new double[]{1.0}),
                new SimpleMatrix(1, 1, true, new double[]{1.0}));

        List<Double> pList =  Arrays.asList(0.5, 0.5);
        List<Double> gammaList = BasicMethodUtils.getGammaParameterList(pList, indexSourceMap, lambdaList);

        Double error = ProposedSchemeUtils.getPartialAbsorptionBAPUEError(
                pList, gammaList,
                Arrays.asList(Arrays.asList(0.6, 0.4), Arrays.asList(1.0, 0.0)),
                Arrays.asList(0.2, 0.3),
                Arrays.asList(Arrays.asList(0.6, 0.4), Arrays.asList(0.0, 1.0)),
                Arrays.asList(0.1, 0.2), Collections.singleton(1), 2);

        assertEquals(0.25, error, 1e-8);
    }

    // new test
//    @Test
//    public void biasedAwarePersonalizedUtilityEnhancementManualTest() {
//        /*
//         * 3 个组, domain = {"a", "b"}
//         * indexSourceMap:
//         *   group 0: sources = [1, 2]  → weight 向量长度 = 3 (target + 2 sources)
//         *   group 1: sources = [2]     → weight 向量长度 = 2 (target + 1 source)
//         *   group 2: sources = []      → weight 向量长度 = 1 (target only)
//         *
//         * 手工指定权重，验证线性组合结果:
//         *   result[0] = 0.5*est0 + 0.3*est1 + 0.2*est2
//         *   result[1] = 0.6*est1 + 0.4*est2
//         *   result[2] = 1.0*est2
//         */
//        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
//        indexSourceMap.put(0, Arrays.asList(1, 2));
//        indexSourceMap.put(1, Collections.singletonList(2));
//        indexSourceMap.put(2, Collections.emptyList());
//
//        List<TreeMap<String, Double>> groupEstimateList = new ArrayList<>();
//        TreeMap<String, Double> est0 = new TreeMap<>();
//        est0.put("a", 0.5); est0.put("b", 0.5);
//        groupEstimateList.add(est0);
//
//        TreeMap<String, Double> est1 = new TreeMap<>();
//        est1.put("a", 0.7); est1.put("b", 0.3);
//        groupEstimateList.add(est1);
//
//        TreeMap<String, Double> est2 = new TreeMap<>();
//        est2.put("a", 0.3); est2.put("b", 0.7);
//        groupEstimateList.add(est2);
//
//        List<SimpleMatrix> correctedWeightList = new ArrayList<>();
//        correctedWeightList.add(new SimpleMatrix(3, 1, true, new double[]{0.5, 0.3, 0.2}));
//        correctedWeightList.add(new SimpleMatrix(2, 1, true, new double[]{0.6, 0.4}));
//        correctedWeightList.add(new SimpleMatrix(1, 1, true, new double[]{1.0}));
//
//        List<TreeMap<String, Double>> result = ProposedSchemeUtils.biasedAwarePersonalizedUtilityEnhancement(
//                indexSourceMap, groupEstimateList, correctedWeightList
//        );
//
//        assertEquals(3, result.size());
//
//        // result[0] = 0.5*est0 + 0.3*est1 + 0.2*est2
//        assertEquals(0.52, result.get(0).get("a"), 1e-8);
//        assertEquals(0.48, result.get(0).get("b"), 1e-8);
//
//        // result[1] = 0.6*est1 + 0.4*est2
//        assertEquals(0.54, result.get(1).get("a"), 1e-8);
//        assertEquals(0.46, result.get(1).get("b"), 1e-8);
//
//        // result[2] = 1.0*est2
//        assertEquals(0.3, result.get(2).get("a"), 1e-8);
//        assertEquals(0.7, result.get(2).get("b"), 1e-8);
//
//        System.out.println("result[0]: " + result.get(0));
//        System.out.println("result[1]: " + result.get(1));
//        System.out.println("result[2]: " + result.get(2));
//    }

//    @Test
//    public void biasedAwarePersonalizedUtilityEnhancementIntegrationTest() {
//        /*
//         * 串联 getCorrectedWeight → biasedAwarePersonalizedUtilityEnhancement
//         * 验证:
//         *   1. 每个结果 map 的值非负（权重非负 + 估计非负）
//         *   2. 每个结果 map 的值之和 ≈ 1（权重和为 1 + 估计和为 1）
//         *   3. 无 dominating source 的组，结果等于其自身估计
//         */
//        List<PrivacyGroupTriple> privacyGroupList = new ArrayList<>();
//        privacyGroupList.add(new PrivacyGroupTriple(5, 0.3, 100));
//        privacyGroupList.add(new PrivacyGroupTriple(3, 0.5, 100));
//        privacyGroupList.add(new PrivacyGroupTriple(2, 0.8, 100));
//
//        List<TreeMap<String, Double>> groupEstimateList = new ArrayList<>();
//        TreeMap<String, Double> est0 = new TreeMap<>();
//        est0.put("a", 0.5); est0.put("b", 0.5);
//        groupEstimateList.add(est0);
//
//        TreeMap<String, Double> est1 = new TreeMap<>();
//        est1.put("a", 0.7); est1.put("b", 0.3);
//        groupEstimateList.add(est1);
//
//        TreeMap<String, Double> est2 = new TreeMap<>();
//        est2.put("a", 0.3); est2.put("b", 0.7);
//        groupEstimateList.add(est2);
//
//        List<Double> varianceList = Arrays.asList(0.1, 0.15, 0.2);
//        List<Double> finalVarianceList = Arrays.asList(0.12, 0.18, 0.25);
//        Double lowerBound = 0.1;
//
//        Map<Integer, List<Integer>> indexSourceMap = new HashMap<>();
//        indexSourceMap.put(0, Arrays.asList(2, 1));
//        indexSourceMap.put(1, Collections.singletonList(2));
//        indexSourceMap.put(2, Collections.emptyList());
//
//        List<SimpleMatrix> correctedWeightList = ProposedSchemeUtils.getCorrectedWeight(
//                indexSourceMap, groupEstimateList, varianceList, finalVarianceList, lowerBound
//        );
//
//        List<TreeMap<String, Double>> result = ProposedSchemeUtils.biasedAwarePersonalizedUtilityEnhancement(
//                indexSourceMap, groupEstimateList, correctedWeightList
//        );
//
//        assertEquals(3, result.size());
//
//        for (int i = 0; i < result.size(); i++) {
//            TreeMap<String, Double> estimate = result.get(i);
//            double sum = 0;
//            for (Double value : estimate.values()) {
//                assertTrue("result[" + i + "] 的值不应为负数: " + value, value >= -1e-10);
//                sum += value;
//            }
//            assertEquals("result[" + i + "] 的值之和应为1", 1.0, sum, 1e-6);
//        }
//
//        // PG2(idx=2) 无 dominating source → 权重 = [1.0] → 结果 = est2
//        assertEquals(0.3, result.get(2).get("a"), 1e-8);
//        assertEquals(0.7, result.get(2).get("b"), 1e-8);
//
//        for (int i = 0; i < result.size(); i++) {
//            System.out.println("enhanced result[" + i + "]: " + result.get(i));
//        }
//    }






}
