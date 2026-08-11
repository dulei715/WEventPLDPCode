package hnu.dll.structure.matrix_data;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.simple.SimpleMatrix;

public class BlockedRiskMatrixStructure extends RiskMatrixStructure{
    protected Double targetVariance;
    protected SimpleMatrix dominatingVarianceMatrix;


    public BlockedRiskMatrixStructure(Double targetVariance, SimpleMatrix dominatingVarianceMatrix) {
        this.targetVariance = targetVariance;
        this.dominatingVarianceMatrix = dominatingVarianceMatrix;
        int dominateMatrixSize = dominatingVarianceMatrix.numRows();
        DMatrixRMaj blockMatrix = new DMatrixRMaj(dominateMatrixSize + 1, dominateMatrixSize + 1);
        blockMatrix.set(0, 0, targetVariance);
        CommonOps_DDRM.insert(dominatingVarianceMatrix.getDDRM(), blockMatrix, 1, 1);
        super.riskMatrix = new SimpleMatrix(blockMatrix);
    }

    public Double getTargetVariance() {
        return targetVariance;
    }

    public SimpleMatrix getDominatingVarianceMatrix() {
        return dominatingVarianceMatrix;
    }

    @Override
    public String toString() {
        return "BlockedRiskMatrixStructure{" +
                "riskMatrix=" + riskMatrix +
                '}';
    }
}
