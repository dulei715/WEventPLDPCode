package hnu.dll.schemes.main_scheme._0_proposed_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.matrix.VectorUtil;
import hnu.dll.special_tools.FOUtils;
import hnu.dll.special_tools.PFOUtils;
import hnu.dll.structure.matrix_data.RiskMatrixStructure;
import hnu.dll.structure.matrix_data.SimpleRiskMatrixStructure;
import hnu.dll.structure.personalized_data.AbstractPrivacyGroup;
import hnu.dll.structure.selection_struct.GroupAwareOptimalSelectionStruct;
import hnu.dll.structure.selection_struct.OptimalSelectionStruct;
import hnu.dll.structure.matrix_data.BlockedRiskMatrixStructure;
import hnu.dll.structure.personalized_data.PrivacyGroupTriple;
import org.ejml.simple.SimpleEVD;
import org.ejml.simple.SimpleMatrix;

import java.util.*;

public class ProposedSchemeUtils {
    /**
     * 整体选择最优 sampling size
     * 对应论文的 Algorithm 1
     * @param samplingSizeRequirementList
     * @param privacyBudgetList
     * @param domainSize
     * @return
     */

    public static final boolean CurrentStatus = false;
    public static final boolean GeneralStatus = true;

    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, Integer domainSize) {
        Map<Integer, Integer> uniqueSamplingSizeMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithFirstAppearanceIndex(samplingSizeRequirementList));
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer originalSamplingSize;
        Double originalBudget;
        LinkedHashMap<Double, Double> newUniqueBudgetStatisticMap;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null, finalSamplingSizeIndex = null;
        for (Integer uniqueSamplingSize : uniqueSamplingSizeSet) {
            tempBudgetList = new ArrayList<>();
            for (int i = 0; i < userSize; ++i) {
                originalSamplingSize = samplingSizeRequirementList.get(i);
                originalBudget = privacyBudgetList.get(i);
                if (originalSamplingSize < uniqueSamplingSize) {
//                    tempBudgetList.add(originalBudget * originalSamplingSize / uniqueSamplingSize);
                    tempBudgetList.add(originalBudget / Math.ceil(uniqueSamplingSize * 1.0 / originalSamplingSize));
                } else {
                    tempBudgetList.add(originalBudget);
                }
            }

            newUniqueBudgetStatisticMap = BasicArrayUtil.getUniqueListWithStatisticList(tempBudgetList);
            tempError = PFOUtils.getGPRRErrorByGroupUserRatio(newUniqueBudgetStatisticMap, userSize, uniqueSamplingSize, domainSize);
            //todo: for test
//            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
//            MyPrint.showList(tempBudgetList, "; ");
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalBudgetList = tempBudgetList;
                finalSamplingSizeIndex = uniqueSamplingSizeMap.get(finalSamplingSize);
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalError, finalBudgetList, finalSamplingSizeIndex);
    }

    protected static Set<Integer> getCandidateSamplingSizeSet(Integer windowSize, Integer samplingSize, Integer samplingSizeLowerBound) {
        Set<Integer> candidateSamplingSizeSet = new TreeSet<>();
        int i = 1;
        Integer tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
        for(; i < windowSize && tempCandidateSamplingSize < samplingSizeLowerBound; i++) {
            tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
        }


        for (; i < windowSize && tempCandidateSamplingSize <= samplingSize; i++) {
            candidateSamplingSizeSet.add(tempCandidateSamplingSize);
            tempCandidateSamplingSize = (int)Math.floor((i+1)*samplingSize*1.0/windowSize);
        }
        return candidateSamplingSizeSet;
    }

    /**
     * 对应论文的 Algorithm 3
     * @param privacyGroupTripleList
     * @param samplingRatio
     * @param domainSize
     * @return
     */
    public static GroupAwareOptimalSelectionStruct groupAwareOptimalPopulationSelection(List<PrivacyGroupTriple> privacyGroupTripleList, Double samplingRatio, Integer domainSize) {
        List<Double> errorList = new ArrayList<>();
        List<Integer> optimalSamplingSizeList = new ArrayList<>();
        List<Double> effectivePrivacyBudgetList = new ArrayList<>();
        int tempWindowSize;
        Integer tempMaximumSamplingSize, repeatSize, tempOptimalSamplingSize = null;
        Double tempPrivacyBudget, tempEffectiveBudget, optimalEffectiveBudget = null, tempError, tempCurrentError;
        Set<Integer> tempCandidateSamplingSizeSet;
        for (PrivacyGroupTriple privacyGroupTriple : privacyGroupTripleList) {
            tempMaximumSamplingSize = (int)Math.ceil(privacyGroupTriple.getUserSize() * samplingRatio);
            tempWindowSize = privacyGroupTriple.getWindowSize();
            tempPrivacyBudget = privacyGroupTriple.getEpsilon();
            tempCandidateSamplingSizeSet = getCandidateSamplingSizeSet(tempWindowSize, tempMaximumSamplingSize, 1);
            tempCurrentError = Double.MAX_VALUE;
            for (Integer candidateSamplingSize : tempCandidateSamplingSizeSet) {
                repeatSize = (int) Math.ceil(candidateSamplingSize * tempWindowSize * 1.0 / tempMaximumSamplingSize);
                tempEffectiveBudget = tempPrivacyBudget / repeatSize;
                tempError = FOUtils.getGPRRErrorBySamplingSize(tempEffectiveBudget, tempMaximumSamplingSize, candidateSamplingSize, domainSize);
                if (tempError < tempCurrentError) {
                    tempCurrentError = tempError;
                    tempOptimalSamplingSize = candidateSamplingSize;
                    optimalEffectiveBudget = tempEffectiveBudget;
                }
            }
            errorList.add(tempCurrentError);
            optimalSamplingSizeList.add(tempOptimalSamplingSize);
            effectivePrivacyBudgetList.add(optimalEffectiveBudget);
        }
        return new GroupAwareOptimalSelectionStruct(errorList, optimalSamplingSizeList, effectivePrivacyBudgetList);
    }


    /**
     * 对应论文的 Algorithm 4
     * @param empiricalRiskMatrix
     * @param eigenvalueLowerBound
     * @param status
     * @return
     */
    public static RiskMatrixStructure getPositiveDefiniteEigenvalueClipping(RiskMatrixStructure empiricalRiskMatrix, Double eigenvalueLowerBound, boolean status) {
        int matrixDimSize;
        SimpleEVD<SimpleMatrix> empiricalEig;
        SimpleMatrix correctedEigenvalueMatrix;
        Double targetVariance = null;
        RiskMatrixStructure resultMatrix;
        if (status == CurrentStatus) {
            BlockedRiskMatrixStructure blockedRiskMatrix = (BlockedRiskMatrixStructure) empiricalRiskMatrix;
            targetVariance = blockedRiskMatrix.getTargetVariance();
            SimpleMatrix empiricalVarianceMatrix = blockedRiskMatrix.getDominatingVarianceMatrix();
            matrixDimSize = empiricalVarianceMatrix.numRows();
            empiricalEig = empiricalVarianceMatrix.eig();
        } else {
            matrixDimSize = empiricalRiskMatrix.getRiskMatrix().numRows();
            empiricalEig = empiricalRiskMatrix.getRiskMatrix().eig();
        }

        correctedEigenvalueMatrix = new SimpleMatrix(matrixDimSize, matrixDimSize);
        SimpleMatrix eigenvectorMatrix = new SimpleMatrix(matrixDimSize, matrixDimSize);
        for (int i = 0; i < matrixDimSize; i++) {
            double eigenvalue = empiricalEig.getEigenvalue(i).getReal();
            SimpleMatrix eigenvector = empiricalEig.getEigenVector(i);
            eigenvectorMatrix.insertIntoThis(0, i, eigenvector);
            correctedEigenvalueMatrix.set(i, i, Math.max(eigenvalue, eigenvalueLowerBound));
        }
        SimpleMatrix correctedVarianceMatrix = eigenvectorMatrix.mult(correctedEigenvalueMatrix).mult(eigenvectorMatrix.transpose());
        if (status == CurrentStatus) {
            resultMatrix = new BlockedRiskMatrixStructure(targetVariance, correctedVarianceMatrix);
        } else {
            resultMatrix = new SimpleRiskMatrixStructure(correctedVarianceMatrix);
        }
        return resultMatrix;
    }


    public static <T extends Comparable<T>> List<SimpleMatrix> getCorrectedWeight(final List<? extends AbstractPrivacyGroup> privacyGroupList, List<TreeMap<T, Double>> groupEstimateList, List<Double> varianceList, List<Double> finalVarianceList, Double lowerBound) {
        List<SimpleMatrix> resultList;
        int size = groupEstimateList.size();
        Map<Integer, List<Integer>> indexSourceMap = BasicMethodUtils.constructDominatingGroupsAsIndex(privacyGroupList);
//        TreeMap<T, Double> targetEstimate;
        SimpleMatrix targetEstimateVector, differA, differB;
        Double targetVariance;
        for (int i = 0; i < size; i++) {
            targetEstimateVector = VectorUtil.toColumnVector(groupEstimateList.get(i).values());
            targetVariance = varianceList.get(i);
            List<Integer> sourceList = indexSourceMap.get(i);
            int dominatingSize = sourceList.size();
            SimpleMatrix tempDominatingMatrix = new SimpleMatrix(dominatingSize, dominatingSize);
            int sourceAIndex, sourceBIndex;
            double tempValue, sourceAVariance, sourceAVarianceFinal;
//            TreeMap<T, Double> sourceAEstimate, sourceBEstimate;
            SimpleMatrix sourceAEstimateVector, sourceBEstimateVector;
            for (int j = 0; j < dominatingSize; j++) {
                sourceAIndex = sourceList.get(j);
                sourceAVariance = varianceList.get(sourceAIndex);
                sourceAVarianceFinal = finalVarianceList.get(sourceAIndex);
                sourceAEstimateVector = VectorUtil.toColumnVector(groupEstimateList.get(sourceAIndex).values());
                differA = sourceAEstimateVector.minus(targetEstimateVector);
                for (int k = j; k < dominatingSize; k++) {
                    sourceBIndex = sourceList.get(k);
                    sourceBEstimateVector = VectorUtil.toColumnVector(groupEstimateList.get(sourceBIndex).values());
                    differB = sourceBEstimateVector.minus(targetEstimateVector);
                    tempValue = differA.dot(differB) - targetVariance + (sourceAVarianceFinal - sourceAVariance) * (j==k?1:0);
                    tempDominatingMatrix.set(j, k, tempValue);
                    tempDominatingMatrix.set(k, j, tempValue);
                }
            }
            // todo: 分解
        }
        return null;
    }



























}
