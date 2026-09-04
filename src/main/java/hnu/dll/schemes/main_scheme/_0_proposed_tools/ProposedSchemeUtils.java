package hnu.dll.schemes.main_scheme._0_proposed_tools;

import cn.edu.dll.basic.BasicArrayUtil;
import cn.edu.dll.basic.BasicCalculation;
import cn.edu.dll.map.MapUtils;
import cn.edu.dll.math_calculation.LinearAlgorithmUtils;
import cn.edu.dll.matrix.VectorUtil;
import cn.edu.dll.statistic.StatisticTool;
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

    public static OptimalSelectionStruct optimalPopulationSelection(List<Integer> samplingSizeRequirementList, List<Double> privacyBudgetList, List<Integer> windowSizeList, Double samplingRatio, Integer domainSize) {
        Map<Integer, Integer> uniqueSamplingSizeMap = new TreeMap<>(BasicArrayUtil.getUniqueListWithFirstAppearanceIndex(samplingSizeRequirementList));
        Set<Integer> uniqueSamplingSizeSet = uniqueSamplingSizeMap.keySet();
        int userSize = samplingSizeRequirementList.size();
        List<Double> tempBudgetList, finalBudgetList = null;
        Integer tempWindowSize, finalTau = -1, tempTau;
        Double originalBudget;
        LinkedHashMap<Double, Double> newUniqueBudgetStatisticMap;
        Double tempError, finalError = Double.MAX_VALUE;
        Integer finalSamplingSize = null, finalSamplingSizeIndex = null;
        for (Integer uniqueSamplingSize : uniqueSamplingSizeSet) {
            tempTau = Math.max(1, (int)Math.floor(samplingRatio * userSize / uniqueSamplingSize));
            tempBudgetList = new ArrayList<>();
            for (int i = 0; i < userSize; ++i) {
//                originalSamplingSize = samplingSizeRequirementList.get(i);
                originalBudget = privacyBudgetList.get(i);
//                if (originalSamplingSize < uniqueSamplingSize) {
//                    tempBudgetList.add(originalBudget / Math.ceil(uniqueSamplingSize * 1.0 / originalSamplingSize));
//                } else {
//                    tempBudgetList.add(originalBudget);
//                }
                tempWindowSize = windowSizeList.get(i);
                tempBudgetList.add(originalBudget / Math.ceil(tempWindowSize * 1.0 / tempTau));
            }

            newUniqueBudgetStatisticMap = BasicArrayUtil.getUniqueListWithStatisticList(tempBudgetList);
            tempError = PFOUtils.getGPRRErrorByGroupUserRatio(newUniqueBudgetStatisticMap, userSize, uniqueSamplingSize, domainSize);
            //todo: for test
//            System.out.println("sampling size: " + uniqueSamplingSize + " error: " + tempError);
//            MyPrint.showList(tempBudgetList, "; ");
            if (tempError < finalError) {
                finalError = tempError;
                finalSamplingSize = uniqueSamplingSize;
                finalTau = tempTau;
                finalBudgetList = tempBudgetList;
                finalSamplingSizeIndex = uniqueSamplingSizeMap.get(finalSamplingSize);
            }
        }
        return new OptimalSelectionStruct(finalSamplingSize, finalTau, finalError, finalBudgetList, finalSamplingSizeIndex);
    }

    protected static Set<Integer> getCandidateSamplingSizeSet(Integer windowSize, Integer samplingSize, Integer samplingSizeLowerBound) {
        Set<Integer> result = new TreeSet<>();
//        int i = 1;
//        Integer tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
//        for(; i < windowSize && tempCandidateSamplingSize < samplingSizeLowerBound; i++) {
//            tempCandidateSamplingSize = (int)Math.floor(i*samplingSize*1.0/windowSize);
//        }
//        for (; i <= windowSize && tempCandidateSamplingSize <= samplingSize; i++) {
//            if (tempCandidateSamplingSize >= samplingSizeLowerBound) {
//                result.add(tempCandidateSamplingSize);
//            }
//            tempCandidateSamplingSize = (int)Math.floor((i+1)*samplingSize*1.0/windowSize);
//        }
        for (int omega = 1; omega <= windowSize; omega++) {
            int tempCandidateSamplingSize = (int) Math.floor(omega * samplingSize * 1.0 / windowSize);
            if (tempCandidateSamplingSize >= samplingSizeLowerBound) {
                result.add(tempCandidateSamplingSize);
            }
        }
        return result;
    }

    /**
     * 对应论文的 Algorithm 3
     * @param privacyGroupList
     * @param samplingRatio
     * @param domainSize
     * @return
     */
    public static <T extends AbstractPrivacyGroup> GroupAwareOptimalSelectionStruct groupAwareOptimalPopulationSelection(List<T> privacyGroupList, Double samplingRatio, Integer domainSize) {
        int groupSize = privacyGroupList.size();
        List<Double> errorList = new ArrayList<>(groupSize);
        List<Integer> optimalSamplingSizeList = new ArrayList<>(groupSize);
        List<Double> effectivePrivacyBudgetList = new ArrayList<>(groupSize);
        List<Integer> effectiveWindowSizeList = new ArrayList<>(groupSize);
        int tempWindowSize;
        Integer tempMaximumSamplingSize, repeatSize, tempTau;
        Double tempPrivacyBudget, tempEffectiveBudget, tempError;
        Set<Integer> tempCandidateSamplingSizeSet;
        for (T privacyGroup : privacyGroupList) {
            Integer tempOptimalSamplingSize = null, optimalTau = null;
            Double optimalEffectiveBudget = null;
            double tempCurrentError = Double.MAX_VALUE;

            tempMaximumSamplingSize = (int)Math.floor(privacyGroup.getUserSize() * samplingRatio);
            tempWindowSize = privacyGroup.getWindowSize();
            tempPrivacyBudget = privacyGroup.getEpsilon();
            tempCandidateSamplingSizeSet = getCandidateSamplingSizeSet(tempWindowSize, tempMaximumSamplingSize, 2);
            for (Integer candidateSamplingSize : tempCandidateSamplingSizeSet) {
                tempTau = Math.max(1, tempMaximumSamplingSize / candidateSamplingSize);
//                repeatSize = (int) Math.ceil(candidateSamplingSize * tempWindowSize * 1.0 / tempMaximumSamplingSize);
                repeatSize = (int) Math.ceil(tempWindowSize * 1.0 / tempTau);
                tempEffectiveBudget = tempPrivacyBudget / repeatSize;
                tempError = FOUtils.getGPRRErrorBySamplingSize(tempEffectiveBudget, tempMaximumSamplingSize, candidateSamplingSize, domainSize);
                if (tempError < tempCurrentError) {
                    tempCurrentError = tempError;
                    tempOptimalSamplingSize = candidateSamplingSize;
                    optimalTau = tempTau;
                    optimalEffectiveBudget = tempEffectiveBudget;
                }
            }
            if (tempOptimalSamplingSize == null) {
                throw new IllegalArgumentException(
                        "No feasible GA-OPS sampling size for group: " + privacyGroup
                );
            }
            errorList.add(tempCurrentError);
            optimalSamplingSizeList.add(tempOptimalSamplingSize);
            effectivePrivacyBudgetList.add(optimalEffectiveBudget);
            effectiveWindowSizeList.add(optimalTau);


        }
        return new GroupAwareOptimalSelectionStruct(errorList, optimalSamplingSizeList, effectiveWindowSizeList, effectivePrivacyBudgetList);
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
            if (matrixDimSize == 0) {
                // 空矩阵（无 dominating 组）无需特征值裁剪，直接返回原结构
                return blockedRiskMatrix;
            }
            empiricalEig = empiricalVarianceMatrix.eig();
        } else {
            matrixDimSize = empiricalRiskMatrix.getRiskMatrix().numRows();
            if (matrixDimSize == 0) {
                // 空矩阵无需特征值裁剪，直接返回原结构
                return empiricalRiskMatrix;
            }
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

    /**
     *
     * 对应论文的 Algorithm 5
     * @param indexSourceMap
     * @param groupEstimateList
     * @param varianceList
     * @param finalVarianceList
     * @param lowerBound
     * @return
     * @param <T>
     */
    public static <T extends Comparable<T>> List<SimpleMatrix> getCorrectedWeight(final Map<Integer, List<Integer>> indexSourceMap, List<? extends Map<T, Double>> groupEstimateList, List<Double> varianceList, List<Double> finalVarianceList, Double lowerBound) {
        int size = groupEstimateList.size();
        List<TreeMap<T, Double>> sortedGroupEstimateList = new ArrayList<>(size);
        for (Map<T, Double> groupEstimate : groupEstimateList) {
            sortedGroupEstimateList.add(new TreeMap<>(groupEstimate));
        }
        List<SimpleMatrix> resultList = new ArrayList<>(size);
//        Map<Integer, List<Integer>> indexSourceMap = BasicMethodUtils.constructDominatingGroupsAsIndex(privacyGroupList);
//        TreeMap<T, Double> targetEstimate;
        SimpleMatrix targetEstimateVector, differA, differB;
        Double targetEstimateVariance, targetRiskVariance;
        for (int i = 0; i < size; i++) {
            targetEstimateVector = VectorUtil.toColumnVector(sortedGroupEstimateList.get(i).values());
            targetEstimateVariance = varianceList.get(i);
            targetRiskVariance = finalVarianceList.get(i);
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
                sourceAEstimateVector = VectorUtil.toColumnVector(sortedGroupEstimateList.get(sourceAIndex).values());
                differA = sourceAEstimateVector.minus(targetEstimateVector);
                for (int k = j; k < dominatingSize; k++) {
                    sourceBIndex = sourceList.get(k);
                    sourceBEstimateVector = VectorUtil.toColumnVector(sortedGroupEstimateList.get(sourceBIndex).values());
                    differB = sourceBEstimateVector.minus(targetEstimateVector);
                    tempValue = differA.dot(differB) - targetEstimateVariance + (sourceAVarianceFinal - sourceAVariance) * (j == k ? 1 : 0);
                    tempDominatingMatrix.set(j, k, tempValue);
                    tempDominatingMatrix.set(k, j, tempValue);
                }
            }
            RiskMatrixStructure riskMatrixStructure = new BlockedRiskMatrixStructure(targetRiskVariance, tempDominatingMatrix);
            riskMatrixStructure = getPositiveDefiniteEigenvalueClipping(riskMatrixStructure, lowerBound, ProposedSchemeUtils.CurrentStatus);

            SimpleMatrix simpleMatrix = LinearAlgorithmUtils.solveSimplexConstrain(riskMatrixStructure.getRiskMatrix());
//            int k = 0;
//            // 初始化整个向量元素值为0
//            double[] extendVectorValue = new double[size];
//            extendVectorValue[i] = simpleMatrix.get(k++, 0);
//            for (int j = 0; j < dominatingSize; j++) {
//                extendVectorValue[sourceList.get(j)] = simpleMatrix.get(k++, 0);
//            }
//            resultList.add(new SimpleMatrix(size, 1, true, extendVectorValue));
            resultList.add(simpleMatrix);
        }
        return resultList;
    }

    /**
     * 对应论文的 Algorithm 6
     * @param indexSourceMap
     * @param groupEstimateList
     * @param correctedWeightList
     * @return
     * @param <T>
     */
//    public static <T> List<TreeMap<T, Double>> biasedAwarePersonalizedUtilityEnhancement(final Map<Integer, List<Integer>> indexSourceMap, List<TreeMap<T, Double>> groupEstimateList, List<SimpleMatrix> correctedWeightList) {
//        int size = groupEstimateList.size();
//        List<TreeMap<T, Double>> resultList = new ArrayList<>(size);
//        SimpleMatrix tempWeight;
//        List<Double> tempWeightList;
//        TreeMap<T, Double> tempEstimate;
//        List<TreeMap<T, Double>> extendSourceEstimateList;
//        List<Integer> tempSourceIndexList;
//        for (int i = 0; i < size; i++) {
//            tempWeight = correctedWeightList.get(i);
//            tempSourceIndexList = indexSourceMap.get(i);
//            extendSourceEstimateList = BasicArrayUtil.getElementListInGivenMultipleIndexes(groupEstimateList, Collections.singletonList(i), tempSourceIndexList);
//            tempWeightList = VectorUtil.toList(tempWeight);
//            tempEstimate = StatisticTool.getWeightedStatisticMap(extendSourceEstimateList, tempWeightList);
//            resultList.add(tempEstimate);
//        }
//        return resultList;
//    }
    public static <T> List<Map<T, Double>> biasedAwarePersonalizedUtilityEnhancement(final Map<Integer, List<Integer>> indexSourceMap, List<? extends Map<T, Double>> groupEstimateList, List<SimpleMatrix> correctedWeightList) {
        int size = groupEstimateList.size();
        List<Map<T, Double>> resultList = new ArrayList<>(size);
        SimpleMatrix tempWeight;
        List<Double> tempWeightList;
        Map<T, Double> tempEstimate;
        List<Map<T, Double>> extendSourceEstimateList;
        List<Integer> tempSourceIndexList;
        for (int i = 0; i < size; i++) {
            tempWeight = correctedWeightList.get(i);
            tempSourceIndexList = indexSourceMap.get(i);
            // 顺序与权重向量分量一致: [target, source_1, ..., source_k]
            extendSourceEstimateList = new ArrayList<>();
            extendSourceEstimateList.add(groupEstimateList.get(i));
            for (Integer sourceIndex : tempSourceIndexList) {
                extendSourceEstimateList.add(groupEstimateList.get(sourceIndex));
            }
            tempWeightList = VectorUtil.toList(tempWeight);
            tempEstimate = StatisticTool.getWeightedStatisticMap(extendSourceEstimateList, tempWeightList);
            resultList.add(tempEstimate);
        }
        return resultList;
    }



    public static Double getBAPUEError(final List<Double> piList, final Map<Integer, List<Integer>> indexSourceMap, final List<SimpleMatrix> lambdaList,
                                       final List<Double> unknownEstimateVarianceList, final List<List<Double>> knownEstimateList, final List<Double> knownEstimateVarianceList, final int dimension) {
        List<Double> gammaParameterList = BasicMethodUtils.getGammaParameterList(piList, indexSourceMap, lambdaList);
        double mainPartSum = 0.0, partBSquare, partCSum = 0.0;
        double tempGamma, tempUnknownVariance, tempKnownVariance, tempPi, tempDiffer;
        int groupSize = piList.size();
        List<Double> tempKnownEstimate, partBWeightedList = new ArrayList<>(groupSize);
        for (int i = 0; i < groupSize; i++) {
            tempGamma = gammaParameterList.get(i);
            tempUnknownVariance = unknownEstimateVarianceList.get(i);
            tempPi = piList.get(i);
            tempKnownVariance = knownEstimateVarianceList.get(i);

            mainPartSum += tempGamma * tempGamma * tempUnknownVariance;
            tempDiffer = tempGamma - tempPi;
            partBWeightedList.add(tempDiffer);
            partCSum += tempDiffer * tempDiffer * tempKnownVariance;

        }

        List<Double> partBEstimate = StatisticTool.getWeightedStatisticList(knownEstimateList, partBWeightedList);
        partBSquare = BasicCalculation.get2NormSquare(partBEstimate);
        return 1.0 / dimension * (mainPartSum + Math.max(0, partBSquare - partCSum));

    }

    /**
     * Corresponds to Algorithm 8 (PA-GCW) in the paper.
     *
     * @param indexSourceMap source groups of each target group
     * @param mixedPublicationEstimateList publication candidates; an active group uses its current
     *                                     dissimilarity estimate in PRE and its actual publication
     *                                     estimate in POST, while a reused group uses its historical
     *                                     publication estimate
     * @param currentDissimilarityEstimateList current group-level dissimilarity estimates
     * @param estimationVarianceList variances of the estimates used for distribution-shift estimation
     * @param publicationVarianceList variances of the publication candidates that will be fused
     * @param dissimilarityVarianceList variances of current dissimilarity estimates
     * @param reusedGroupIndexSet reused privacy-group indexes
     * @param lowerBound eigenvalue clipping lower bound
     */
    public static <T extends Comparable<T>> List<SimpleMatrix> getPartialAbsorptionCorrectedWeight(
            final Map<Integer, List<Integer>> indexSourceMap,
            final List<? extends Map<T, Double>> mixedPublicationEstimateList,
            final List<? extends Map<T, Double>> currentDissimilarityEstimateList,
            final List<Double> estimationVarianceList,
            final List<Double> publicationVarianceList,
            final List<Double> dissimilarityVarianceList,
            final Set<Integer> reusedGroupIndexSet,
            final Double lowerBound) {
        int groupSize = mixedPublicationEstimateList.size();
        List<SimpleMatrix> mixedPublicationEstimateVectorList = new ArrayList<>(groupSize);
        List<SimpleMatrix> currentDissimilarityEstimateVectorList = new ArrayList<>(groupSize);
        for (int i = 0; i < groupSize; i++) {
            mixedPublicationEstimateVectorList.add(VectorUtil.toColumnVector(new TreeMap<>(mixedPublicationEstimateList.get(i)).values()));
            currentDissimilarityEstimateVectorList.add(VectorUtil.toColumnVector(new TreeMap<>(currentDissimilarityEstimateList.get(i)).values()));
        }

        List<SimpleMatrix> resultList = new ArrayList<>(groupSize);
        for (int targetIndex = 0; targetIndex < groupSize; targetIndex++) {
            List<Integer> sourceIndexList = indexSourceMap.get(targetIndex);
            SimpleMatrix targetDissimilarityEstimateVector = currentDissimilarityEstimateVectorList.get(targetIndex);
            RiskMatrixStructure riskMatrixStructure;

            if (reusedGroupIndexSet.contains(targetIndex)) {
                List<Integer> candidateIndexList = new ArrayList<>(sourceIndexList.size() + 1);
                candidateIndexList.add(targetIndex);
                candidateIndexList.addAll(sourceIndexList);
                SimpleMatrix empiricalRiskMatrix = new SimpleMatrix(candidateIndexList.size(), candidateIndexList.size());
                for (int row = 0; row < candidateIndexList.size(); row++) {
                    int rowGroupIndex = candidateIndexList.get(row);
                    SimpleMatrix rowDifference = mixedPublicationEstimateVectorList.get(rowGroupIndex).minus(targetDissimilarityEstimateVector);
                    for (int column = row; column < candidateIndexList.size(); column++) {
                        int columnGroupIndex = candidateIndexList.get(column);
                        SimpleMatrix columnDifference = mixedPublicationEstimateVectorList.get(columnGroupIndex).minus(targetDissimilarityEstimateVector);
                        double value = rowDifference.dot(columnDifference) - dissimilarityVarianceList.get(targetIndex)
                                + (publicationVarianceList.get(rowGroupIndex) - estimationVarianceList.get(rowGroupIndex))
                                * (row == column ? 1 : 0);
                        empiricalRiskMatrix.set(row, column, value);
                        empiricalRiskMatrix.set(column, row, value);
                    }
                }
                riskMatrixStructure = new SimpleRiskMatrixStructure(empiricalRiskMatrix);
                riskMatrixStructure = getPositiveDefiniteEigenvalueClipping(riskMatrixStructure, lowerBound, GeneralStatus);
            } else {
                SimpleMatrix empiricalSourceRiskMatrix = new SimpleMatrix(sourceIndexList.size(), sourceIndexList.size());
                for (int row = 0; row < sourceIndexList.size(); row++) {
                    int rowGroupIndex = sourceIndexList.get(row);
                    SimpleMatrix rowDifference = mixedPublicationEstimateVectorList.get(rowGroupIndex).minus(targetDissimilarityEstimateVector);
                    for (int column = row; column < sourceIndexList.size(); column++) {
                        int columnGroupIndex = sourceIndexList.get(column);
                        SimpleMatrix columnDifference = mixedPublicationEstimateVectorList.get(columnGroupIndex).minus(targetDissimilarityEstimateVector);
                        double value = rowDifference.dot(columnDifference) - dissimilarityVarianceList.get(targetIndex)
                                + (publicationVarianceList.get(rowGroupIndex) - estimationVarianceList.get(rowGroupIndex))
                                * (row == column ? 1 : 0);
                        empiricalSourceRiskMatrix.set(row, column, value);
                        empiricalSourceRiskMatrix.set(column, row, value);
                    }
                }
                riskMatrixStructure = new BlockedRiskMatrixStructure(publicationVarianceList.get(targetIndex), empiricalSourceRiskMatrix);
                riskMatrixStructure = getPositiveDefiniteEigenvalueClipping(riskMatrixStructure, lowerBound, CurrentStatus);
            }
            resultList.add(LinearAlgorithmUtils.solveSimplexConstrain(riskMatrixStructure.getRiskMatrix()));
        }
        return resultList;
    }

    /**
     * Calculates the staleness-aware potential reporting error in Equation (12).
     */
//    public static Double getPartialAbsorptionBAPUEError(
//            final List<Double> piList,
//            final Map<Integer, List<Integer>> indexSourceMap,
//            final List<SimpleMatrix> lambdaList,
//            final List<List<Double>> mixedPublicationEstimateList,
//            final List<Double> publicationVarianceList,
//            final List<List<Double>> currentDissimilarityEstimateList,
//            final List<Double> dissimilarityVarianceList,
//            final Set<Integer> reusedGroupIndexSet,
//            final int dimension) {
//        List<Double> gammaParameterList = BasicMethodUtils.getGammaParameterList(piList, indexSourceMap, lambdaList);
//        double[] estimatedBias = new double[dimension];
//        double publicationVarianceSum = 0.0;
//        double biasVarianceCorrection = 0.0;
//
//        for (int groupIndex = 0; groupIndex < piList.size(); groupIndex++) {
//            double gamma = gammaParameterList.get(groupIndex);
//            double pi = piList.get(groupIndex);
//            boolean reused = reusedGroupIndexSet.contains(groupIndex);
//            double currentDissimilarityWeight = reused ? -pi : gamma - pi;
//
//            List<Double> currentDissimilarityEstimate = currentDissimilarityEstimateList.get(groupIndex);
//            for (int domainIndex = 0; domainIndex < dimension; domainIndex++) {
//                estimatedBias[domainIndex] += currentDissimilarityWeight * currentDissimilarityEstimate.get(domainIndex);
//            }
//            biasVarianceCorrection += currentDissimilarityWeight * currentDissimilarityWeight
//                    * dissimilarityVarianceList.get(groupIndex);
//
//            if (reused) {
//                List<Double> historicalPublicationEstimate = mixedPublicationEstimateList.get(groupIndex);
//                for (int domainIndex = 0; domainIndex < dimension; domainIndex++) {
//                    estimatedBias[domainIndex] += gamma * historicalPublicationEstimate.get(domainIndex);
//                }
//                biasVarianceCorrection += gamma * gamma * publicationVarianceList.get(groupIndex);
//            }
//            publicationVarianceSum += gamma * gamma * publicationVarianceList.get(groupIndex);
//        }
//
//        double estimatedBiasNormSquare = 0.0;
//        for (double value : estimatedBias) {
//            estimatedBiasNormSquare += value * value;
//        }
//        double correctedBiasSquare = Math.max(0.0, estimatedBiasNormSquare - biasVarianceCorrection);
//        return (publicationVarianceSum + correctedBiasSquare) / dimension;
//    }

    public static Double getPartialAbsorptionBAPUEError(
            final List<Double> piList,
            final List<Double> gammaParameterList,
            final List<List<Double>> mixedPublicationEstimateList,
            final List<Double> publicationVarianceList,
            final List<List<Double>> currentDissimilarityEstimateList,
            final List<Double> dissimilarityVarianceList,
            final Set<Integer> reusedGroupIndexSet,
            final int dimension) {
        double[] estimatedBias = new double[dimension];
        double publicationVarianceSum = 0.0;
        double biasVarianceCorrection = 0.0;

        for (int groupIndex = 0; groupIndex < piList.size(); groupIndex++) {
            double gamma = gammaParameterList.get(groupIndex);
            double pi = piList.get(groupIndex);
            boolean reused = reusedGroupIndexSet.contains(groupIndex);
            double currentDissimilarityWeight = reused ? -pi : gamma - pi;

            List<Double> currentDissimilarityEstimate = currentDissimilarityEstimateList.get(groupIndex);
            for (int domainIndex = 0; domainIndex < dimension; domainIndex++) {
                estimatedBias[domainIndex] += currentDissimilarityWeight * currentDissimilarityEstimate.get(domainIndex);
            }
            biasVarianceCorrection += currentDissimilarityWeight * currentDissimilarityWeight
                    * dissimilarityVarianceList.get(groupIndex);

            if (reused) {
                List<Double> historicalPublicationEstimate = mixedPublicationEstimateList.get(groupIndex);
                for (int domainIndex = 0; domainIndex < dimension; domainIndex++) {
                    estimatedBias[domainIndex] += gamma * historicalPublicationEstimate.get(domainIndex);
                }
                biasVarianceCorrection += gamma * gamma * publicationVarianceList.get(groupIndex);
            }
            publicationVarianceSum += gamma * gamma * publicationVarianceList.get(groupIndex);
        }

        double estimatedBiasNormSquare = 0.0;
        for (double value : estimatedBias) {
            estimatedBiasNormSquare += value * value;
        }
        double correctedBiasSquare = Math.max(0.0, estimatedBiasNormSquare - biasVarianceCorrection);
        return (publicationVarianceSum + correctedBiasSquare) / dimension;
    }


    public static List<Integer> getSamplingSizeGroupListByLargestRemainder(Integer totalSamplingSize, List<Double> weightList) {
        if (totalSamplingSize == null || totalSamplingSize < 0) {
            throw new IllegalArgumentException("totalSamplingSize must be non-negative.");
        }
        if (weightList == null || weightList.isEmpty()) {
            throw new IllegalArgumentException("weightList cannot be null or empty.");
        }

        int groupSize = weightList.size();

        // 1. Calculate the sum of weights.
        double weightSum = 0.0;
        for (Double weight : weightList) {
            if (weight == null || weight < 0) {
                throw new IllegalArgumentException("Weights must be non-negative.");
            }
            weightSum += weight;
        }

        if (weightSum <= 0.0) {
            throw new IllegalArgumentException("The sum of weights must be positive.");
        }

        // 2. Allocate the floor part first.
        List<Integer> resultList = new ArrayList<>(groupSize);
        List<Double> remainderList = new ArrayList<>(groupSize);

        int allocatedSize = 0;

        for (int i = 0; i < groupSize; i++) {
            // Normalize the weight to avoid requiring sum(weightList) == 1 exactly.
            double normalizedWeight = weightList.get(i) / weightSum;

            double exactSamplingSize = totalSamplingSize * normalizedWeight;
            int floorSamplingSize = (int) Math.floor(exactSamplingSize);

            resultList.add(floorSamplingSize);
            remainderList.add(exactSamplingSize - floorSamplingSize);

            allocatedSize += floorSamplingSize;
        }

        // 3. Number of users that have not yet been allocated.
        int remainingSize = totalSamplingSize - allocatedSize;

        // 4. Sort group indices by remainder in descending order.
        //    If the remainders are equal, the smaller group index has priority.
        List<Integer> indexList = new ArrayList<>(groupSize);
        for (int i = 0; i < groupSize; i++) {
            indexList.add(i);
        }

        indexList.sort((index1, index2) -> {
            int compareResult = Double.compare(
                    remainderList.get(index2),
                    remainderList.get(index1)
            );

            if (compareResult != 0) {
                return compareResult;
            }

            return Integer.compare(index1, index2);
        });

        // 5. Allocate the remaining users to groups with the largest remainders.
        for (int i = 0; i < remainingSize; i++) {
            int groupIndex = indexList.get(i);
            resultList.set(
                    groupIndex,
                    resultList.get(groupIndex) + 1
            );
        }

        return resultList;
    }
}






















}
