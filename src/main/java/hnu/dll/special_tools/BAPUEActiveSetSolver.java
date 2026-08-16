package hnu.dll.special_tools;

import hnu.dll.structure.matrix_data.BlockedRiskMatrixStructure;
import org.ejml.simple.SimpleMatrix;

import java.util.Objects;

/**
 * BA-PUE Active-set 权重优化器。
 *
 * 求解：
 *
 *      min     lambda^T M lambda
 *    lambda
 *
 *      s.t.    1^T lambda = 1
 *              lambda >= 0
 *
 * 其中 M 是 PDEC 修正后的正定风险矩阵 M_tilde_H。
 */
@Deprecated
public class BAPUEActiveSetSolver {
    /**
     * 默认最大迭代次数。
     *
     * 隐私组数量通常较小时，实际迭代次数一般远小于这个值。
     */
    private static final int DEFAULT_MAX_ITERATIONS = 1000;

    /**
     * 默认浮点误差容限。
     */
    private static final double DEFAULT_TOLERANCE = 1e-10;

    private BAPUEActiveSetSolver() {
        // 工具类不需要实例化。
    }

    /**
     * 使用默认参数求解。
     *
     * @param riskMatrix 风险矩阵结构
     * @return 最优融合权重 lambda
     */
    public static double[] solveSimplexQP(
            BlockedRiskMatrixStructure riskMatrix) {

        return solveSimplexQP(
                riskMatrix,
                DEFAULT_MAX_ITERATIONS,
                DEFAULT_TOLERANCE
        );
    }

    /**
     * 与现有 RiskMatrixStructure 对接的入口。
     *
     * @param riskMatrix   风险矩阵结构
     * @param maxIterations 最大迭代次数
     * @param tolerance     浮点误差容限
     * @return 最优融合权重 lambda
     */
    public static double[] solveSimplexQP(BlockedRiskMatrixStructure riskMatrix, int maxIterations, double tolerance) {

        Objects.requireNonNull(riskMatrix, "riskMatrix 不能为 null");

        /*
         * 取得 BA-PUE 使用的风险矩阵 M_tilde_H。
         */
        SimpleMatrix matrix = riskMatrix.getDominatingVarianceMatrix();

        return solveSimplexQP(matrix, maxIterations, tolerance);
    }

    /**
     * Active-set 核心求解方法。
     *
     * @param matrix         实对称正定风险矩阵 M
     * @param maxIterations  最大迭代次数
     * @param tolerance      浮点误差容限
     * @return 最优融合权重 lambda
     */
    public static double[] solveSimplexQP(
            SimpleMatrix matrix,
            int maxIterations,
            double tolerance) {
        /*
         * ============================================================
         * 第 1 步：检查输入矩阵
         * ============================================================
         */

        Objects.requireNonNull(
                matrix,
                "风险矩阵不能为 null"
        );

        int groupCount = matrix.numRows();

        /*
         * M 必须是 K × K 方阵。
         */
        if (groupCount == 0
                || matrix.numCols() != groupCount) {

            throw new IllegalArgumentException(
                    "风险矩阵必须是非空方阵"
            );
        }

        if (maxIterations <= 0) {
            throw new IllegalArgumentException(
                    "maxIterations 必须大于 0"
            );
        }

        if (tolerance <= 0.0
                || !Double.isFinite(tolerance)) {

            throw new IllegalArgumentException(
                    "tolerance 必须是有限正数"
            );
        }

        /*
         * 检查矩阵元素是否有限，以及矩阵是否近似对称。
         */
        validateSymmetricMatrix(matrix, tolerance);

        /*
         * 如果只有一个隐私组，唯一可行解就是 lambda_1 = 1。
         */
        if (groupCount == 1) {
            return new double[]{1.0};
        }


        /*
         * ============================================================
         * 第 2 步：选择初始可行解
         * ============================================================
         *
         * 选择：
         *
         *      initialIndex = arg min_i M_ii
         *
         * 当 lambda = e_i，即只使用第 i 个隐私组时：
         *
         *      lambda^T M lambda = M_ii
         *
         * 因而从对角线最小的顶点开始。
         */

        int initialIndex =
                findMinimumDiagonalIndex(matrix);

        /*
         * inSupport[i] = true：
         *
         *      第 i 个变量属于当前支持集 S，
         *      当前允许 lambda_i 取得正值。
         *
         * inSupport[i] = false：
         *
         *      第 i 个变量属于当前零权重集合 Z，
         *      当前这一轮暂时固定 lambda_i = 0。
         *
         * S 和 Z 在迭代过程中都会变化。
         */
        boolean[] inSupport =
                new boolean[groupCount];

        inSupport[initialIndex] = true;

        /*
         * 初始权重为一个单纯形顶点：
         *
         *      lambda_initialIndex = 1
         *      其余权重为 0
         *
         * 所以初始解一定满足：
         *
         *      lambda >= 0
         *      1^T lambda = 1
         */
        SimpleMatrix lambda =
                new SimpleMatrix(groupCount, 1);

        lambda.set(initialIndex, 0, 1.0);




        /*
         * ============================================================
         * 第 3 步：开始 Active-set 迭代
         * ============================================================
         */

        for (int iteration = 0;
             iteration < maxIterations;
             iteration++) {

            /*
             * 获得当前支持集 S 的原始下标。
             *
             * 例如：
             *
             *      inSupport = [true, false, true]
             *
             * 则：
             *
             *      supportIndices = [0, 2]
             */
            int[] supportIndices =
                    getSupportIndices(inSupport);

            int supportSize =
                    supportIndices.length;

            if (supportSize == 0) {
                throw new IllegalStateException(
                        "支持集不能为空"
                );
            }

            /*
             * 提取主子矩阵 M_SS。
             *
             * 如果 S = {1, 3}，则：
             *
             *             [M_11  M_13]
             *      M_SS = [            ]
             *             [M_31  M_33]
             */
            SimpleMatrix subMatrix =
                    extractPrincipalSubmatrix(
                            matrix,
                            supportIndices
                    );

            /*
             * 构造 |S| 维全 1 列向量。
             */
            SimpleMatrix ones =
                    new SimpleMatrix(supportSize, 1);

            ones.fill(1.0);



            /*
             * ========================================================
             * 第 4 步：在当前支持集 S 上求等式约束最优解
             * ========================================================
             *
             * 暂时固定：
             *
             *      lambda_j = 0, j 不属于 S
             *
             * 只求解：
             *
             *      min lambda_S^T M_SS lambda_S
             *
             *      s.t. 1^T lambda_S = 1
             *
             * 闭式解为：
             *
             *                  M_SS^{-1} 1
             *      lambda_S = -------------------
             *                  1^T M_SS^{-1} 1
             *
             * 实现时不显式计算逆矩阵，而是先求解：
             *
             *      M_SS x = 1
             *
             * 得到：
             *
             *      x = M_SS^{-1} 1
             */

            SimpleMatrix x;

            try {
                x = subMatrix.solve(ones);
            } catch (RuntimeException exception) {
                throw new IllegalStateException(
                        "无法求解 M_SS x = 1。"
                                + "请检查风险矩阵是否正定或是否接近奇异。",
                        exception
                );
            }

            /*
             * denominator = 1^T x
             *
             * 因为 x 是列向量，所以就是所有元素之和。
             */
            double denominator =
                    x.elementSum();

            /*
             * 当 M_SS 正定时：
             *
             *      1^T M_SS^{-1} 1 > 0
             */
            if (!Double.isFinite(denominator)
                    || denominator <= tolerance) {

                throw new IllegalStateException(
                        "1^T M_SS^{-1} 1 不大于 0。"
                                + "请检查风险矩阵是否正定。"
                );
            }

            /*
             * 构造完整的候选权重 candidate。
             *
             * 对于 i 属于 S：
             *
             *      candidate_i = x_i / denominator
             *
             * 对于 j 不属于 S：
             *
             *      candidate_j = 0
             */
            SimpleMatrix candidate =
                    new SimpleMatrix(groupCount, 1);

            boolean hasNegativeCandidate = false;

            for (int localIndex = 0;
                 localIndex < supportSize;
                 localIndex++) {

                int originalIndex =
                        supportIndices[localIndex];

                double candidateValue =
                        x.get(localIndex, 0)
                                / denominator;

                if (!Double.isFinite(candidateValue)) {
                    throw new IllegalStateException(
                            "候选权重出现 NaN 或无穷大"
                    );
                }

                candidate.set(
                        originalIndex,
                        0,
                        candidateValue
                );

                /*
                 * 只有明显小于 0 才认为候选解违反非负约束。
                 *
                 * 例如 -1e-15 通常只是浮点误差，
                 * 后面会统一清理为 0。
                 */
                if (candidateValue < -tolerance) {
                    hasNegativeCandidate = true;
                }
            }


            /*
             * ========================================================
             * 第 5 步：候选解出现负权重
             * ========================================================
             *
             * candidate 虽然满足权重和为 1，
             * 但不满足 candidate >= 0，因此不能直接采用。
             *
             * 从当前合法解 lambda 向 candidate 移动：
             *
             *      lambda(alpha)
             *          = lambda
             *            + alpha(candidate - lambda)
             *
             * 只移动到某个权重刚好变成 0 的位置，
             * 不能继续走到负数区域。
             */
            if (hasNegativeCandidate) {

                /*
                 * 移动方向：
                 *
                 *      direction = candidate - lambda
                 */
                SimpleMatrix direction =
                        candidate.minus(lambda);

                /*
                 * 初始设 alpha = 1，表示直接走到 candidate。
                 *
                 * 由于 candidate 存在负权重，
                 * 后面会把 alpha 缩小到合法范围。
                 */
                double stepSize = 1.0;

                for (int index : supportIndices) {

                    double currentValue =
                            lambda.get(index, 0);

                    double directionValue =
                            direction.get(index, 0);

                    /*
                     * 只有 direction_i < 0 时，
                     * lambda_i 才会随着 alpha 增大而减小。
                     */
                    if (directionValue < -tolerance) {

                        /*
                         * 令第 i 个权重刚好到达 0：
                         *
                         *      lambda_i
                         *      + alpha direction_i = 0
                         *
                         * 得：
                         *
                         *                  -lambda_i
                         *      alpha_i = ----------------
                         *                  direction_i
                         */
                        double blockingStep =
                                -currentValue
                                        / directionValue;

                        /*
                         * 选择最先到达 0 的变量。
                         */
                        stepSize = Math.min(
                                stepSize,
                                blockingStep
                        );
                    }
                }

                /*
                 * 防止浮点误差导致步长略微超出 [0,1]。
                 */
                stepSize = Math.max(
                        0.0,
                        Math.min(1.0, stepSize)
                );

                /*
                 * 如果步长几乎为 0，通常表示支持集在数值上发生循环。
                 */
                if (stepSize <= tolerance) {
                    throw new IllegalStateException(
                            "Active-set 步长接近 0，"
                                    + "可能存在数值不稳定或矩阵接近奇异"
                    );
                }

                /*
                 * 更新：
                 *
                 *      lambda
                 *        <- lambda
                 *           + alpha(candidate - lambda)
                 *
                 * 因为 lambda 和 candidate 的元素和都是 1，
                 * 所以沿二者连线移动仍然保持权重和为 1。
                 */
                lambda = lambda.plus(
                        direction.scale(stepSize)
                );

                /*
                 * 将到达 0 的变量从支持集 S 移入 Z。
                 */
                boolean removedVariable = false;

                for (int index : supportIndices) {

                    double value =
                            lambda.get(index, 0);

                    if (value < -tolerance) {
                        throw new IllegalStateException(
                                "Active-set 更新后出现明显负权重："
                                        + value
                        );
                    }

                    /*
                     * 将数值上接近 0 的权重设置为严格的 0。
                     */
                    if (value <= tolerance) {

                        lambda.set(index, 0, 0.0);

                        /*
                         * false 表示该变量下一轮暂时固定为 0。
                         */
                        inSupport[index] = false;

                        removedVariable = true;
                    }
                }

                if (!removedVariable) {
                    throw new IllegalStateException(
                            "候选解存在负权重，"
                                    + "但没有变量到达单纯形边界"
                    );
                }

                /*
                 * 清理浮点误差并重新保证权重和为 1。
                 */
                lambda = cleanAndNormalize(
                        lambda,
                        tolerance
                );

                /*
                 * 支持集已经发生变化，
                 * 使用新的支持集进入下一轮。
                 */
                continue;
            }
            /*
             * ========================================================
             * 第 6 步：候选解没有明显负权重
             * ========================================================
             *
             * 如果某些候选权重只是非常接近 0，
             * 将其设为严格的 0，并从支持集移除。
             */
            boolean removedNearZeroVariable = false;

            for (int index : supportIndices) {

                double value =
                        candidate.get(index, 0);

                if (value <= tolerance) {

                    candidate.set(index, 0, 0.0);
                    inSupport[index] = false;

                    removedNearZeroVariable = true;
                }
            }

            candidate = cleanAndNormalize(
                    candidate,
                    tolerance
            );

            lambda = candidate;

            /*
             * 如果刚刚删除了接近 0 的变量，
             * 下一轮在更小的支持集上重新求解。
             */
            if (removedNearZeroVariable) {
                continue;
            }

            /*
             * 此时 lambda 是当前支持集对应单纯形面上的最优解。
             *
             * 由：
             *
             *      x = M_SS^{-1} 1
             *
             *      lambda_S = x / (1^T x)
             *
             * 可得：
             *
             *      M_SS lambda_S = c 1
             *
             * 其中：
             *
             *              1
             *      c = -----------
             *            1^T x
             *
             * 因此，对所有当前正权重变量 i 属于 S：
             *
             *      (M lambda)_i = c
             */
            double commonMarginalCost =
                    1.0 / denominator;

            /*
             * 计算完整向量 M lambda。
             */
            SimpleMatrix matrixTimesLambda =
                    matrix.mult(lambda);
            /*
             * ========================================================
             * 第 7 步：检查 Z 中的零权重变量
             * ========================================================
             *
             * 对零权重变量 j，最优条件要求：
             *
             *      (M lambda)_j >= c
             *
             * 如果：
             *
             *      (M lambda)_j < c
             *
             * 说明给第 j 个组分配一点权重可以降低目标函数，
             * 因此 j 应当从 Z 重新进入 S。
             */

            int enteringIndex = -1;

            /*
             * 记录最严重的 KKT 违反值。
             */
            double mostNegativeReducedCost =
                    -tolerance;

            for (int index = 0;
                 index < groupCount;
                 index++) {

                /*
                 * 只检查当前不在支持集中的零权重变量。
                 */
                if (inSupport[index]) {
                    continue;
                }

                /*
                 * reducedCost_j：
                 *
                 *      (M lambda)_j - c
                 */
                double reducedCost =
                        matrixTimesLambda.get(index, 0)
                                - commonMarginalCost;

                /*
                 * 选择 reducedCost 最小的变量，
                 * 即违反最优条件最严重的变量。
                 */
                if (reducedCost
                        < mostNegativeReducedCost) {

                    mostNegativeReducedCost =
                            reducedCost;

                    enteringIndex = index;
                }
            }

            /*
             * ========================================================
             * 第 8 步：判断是否已经达到全局最优
             * ========================================================
             *
             * enteringIndex == -1 表示：
             *
             * 对所有 i 属于 S：
             *
             *      lambda_i > 0
             *      (M lambda)_i = c
             *
             * 对所有 j 属于 Z：
             *
             *      lambda_j = 0
             *      (M lambda)_j >= c
             *
             * 当前解满足 KKT 条件。
             *
             * 当 M 正定时，目标函数严格凸，
             * 因而该解是唯一全局最优解。
             */
            if (enteringIndex == -1) {

                lambda = cleanAndNormalize(
                        lambda,
                        tolerance
                );

                return toDoubleArray(lambda);
            }


            /*
             * ========================================================
             * 第 9 步：把违反条件的变量重新加入支持集
             * ========================================================
             *
             * 此时 lambda_enteringIndex 仍然为 0。
             *
             * 这里只是允许该变量在下一轮参与优化，
             * 下一轮会使用扩大的 M_SS 重新求解权重。
             */
            inSupport[enteringIndex] = true;
        }

        throw new IllegalStateException(
                "Active-set 方法未在最大迭代次数内收敛"
        );
    }









    /**
     * 使用默认参数、直接传入 SimpleMatrix。
     */
    public static double[] solveSimplexQP(
            SimpleMatrix matrix) {

        return solveSimplexQP(
                matrix,
                DEFAULT_MAX_ITERATIONS,
                DEFAULT_TOLERANCE
        );
    }

    /**
     * 找到主对角线元素最小的位置。
     */
    private static int findMinimumDiagonalIndex(
            SimpleMatrix matrix) {

        int minimumIndex = 0;
        double minimumValue =
                matrix.get(0, 0);

        for (int index = 1;
             index < matrix.numRows();
             index++) {

            double value =
                    matrix.get(index, index);

            if (value < minimumValue) {
                minimumValue = value;
                minimumIndex = index;
            }
        }

        return minimumIndex;
    }

    /**
     * 将 boolean 支持集转换为下标数组。
     *
     * 例如：
     *
     *      [true, false, true]
     *
     * 转换为：
     *
     *      [0, 2]
     */
    private static int[] getSupportIndices(
            boolean[] inSupport) {

        int size = 0;

        for (boolean selected : inSupport) {
            if (selected) {
                size++;
            }
        }

        int[] indices =
                new int[size];

        int position = 0;

        for (int index = 0;
             index < inSupport.length;
             index++) {

            if (inSupport[index]) {
                indices[position] = index;
                position++;
            }
        }

        return indices;
    }

    /**
     * 提取主子矩阵 M_SS。
     */
    private static SimpleMatrix
    extractPrincipalSubmatrix(
            SimpleMatrix matrix,
            int[] indices) {

        int size = indices.length;

        SimpleMatrix result =
                new SimpleMatrix(size, size);

        for (int row = 0;
             row < size;
             row++) {

            for (int column = 0;
                 column < size;
                 column++) {

                result.set(
                        row,
                        column,
                        matrix.get(
                                indices[row],
                                indices[column]
                        )
                );
            }
        }

        return result;
    }

    /**
     * 检查矩阵元素是否有限，以及矩阵是否近似对称。
     */
    private static void validateSymmetricMatrix(
            SimpleMatrix matrix,
            double tolerance) {

        int size = matrix.numRows();

        /*
         * 计算矩阵元素的最大绝对值，
         * 用于设置相对对称误差尺度。
         */
        double scale = 1.0;

        for (int row = 0;
             row < size;
             row++) {

            for (int column = 0;
                 column < size;
                 column++) {

                double value =
                        matrix.get(row, column);

                if (!Double.isFinite(value)) {
                    throw new IllegalArgumentException(
                            "风险矩阵包含 NaN 或无穷大"
                    );
                }

                scale = Math.max(
                        scale,
                        Math.abs(value)
                );
            }
        }

        double symmetryTolerance =
                tolerance * scale;

        for (int row = 0;
             row < size;
             row++) {

            for (int column = row + 1;
                 column < size;
                 column++) {

                double difference = Math.abs(
                        matrix.get(row, column)
                                - matrix.get(column, row)
                );

                if (difference > symmetryTolerance) {
                    throw new IllegalArgumentException(
                            "风险矩阵不是实对称矩阵："
                                    + "M[" + row + "]["
                                    + column + "] 与 M["
                                    + column + "]["
                                    + row + "] 不相等"
                    );
                }
            }
        }
    }

    /**
     * 清理浮点误差，并重新归一化权重。
     */
    private static SimpleMatrix cleanAndNormalize(
            SimpleMatrix lambda,
            double tolerance) {

        SimpleMatrix result =
                lambda.copy();

        double sum = 0.0;

        for (int index = 0;
             index < result.numRows();
             index++) {

            double value =
                    result.get(index, 0);

            if (!Double.isFinite(value)) {
                throw new IllegalStateException(
                        "权重中出现 NaN 或无穷大"
                );
            }

            /*
             * 明显小于 0，说明算法或输入存在问题。
             */
            if (value < -tolerance) {
                throw new IllegalStateException(
                        "出现明显负权重："
                                + value
                );
            }

            /*
             * 将非常接近 0 的数设置为严格的 0。
             */
            if (Math.abs(value) <= tolerance) {
                value = 0.0;
            }

            result.set(index, 0, value);
            sum += value;
        }

        if (!Double.isFinite(sum)
                || sum <= tolerance) {

            throw new IllegalStateException(
                    "权重之和不大于 0"
            );
        }

        /*
         * 消除浮点误差，例如：
         *
         *      sum(lambda) = 0.9999999999998
         *
         * 重新归一化为严格的 1。
         */
        return result.divide(sum);
    }

    /**
     * 将 K × 1 的 SimpleMatrix 转换为 double[]。
     */
    private static double[] toDoubleArray(
            SimpleMatrix vector) {

        double[] result =
                new double[vector.numRows()];

        for (int index = 0;
             index < vector.numRows();
             index++) {

            result[index] =
                    vector.get(index, 0);
        }

        return result;
    }

    /**
     * 计算目标函数值：
     *
     *      lambda^T M lambda
     */
    public static double objectiveValue(
            SimpleMatrix matrix,
            double[] lambda) {

        if (lambda.length != matrix.numRows()) {
            throw new IllegalArgumentException(
                    "权重维度与矩阵维度不一致"
            );
        }

        SimpleMatrix lambdaVector =
                new SimpleMatrix(lambda.length, 1);

        for (int index = 0;
             index < lambda.length;
             index++) {

            lambdaVector.set(
                    index,
                    0,
                    lambda[index]
            );
        }

        return lambdaVector.transpose()
                .mult(matrix)
                .mult(lambdaVector)
                .get(0, 0);
    }

}
