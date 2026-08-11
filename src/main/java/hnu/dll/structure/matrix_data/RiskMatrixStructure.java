package hnu.dll.structure.matrix_data;

import org.ejml.simple.SimpleMatrix;

public abstract class RiskMatrixStructure {
    protected SimpleMatrix riskMatrix;

    public RiskMatrixStructure(SimpleMatrix riskMatrix) {
        this.riskMatrix = riskMatrix;
    }

    public RiskMatrixStructure() {
    }

    public SimpleMatrix getRiskMatrix() {
        return riskMatrix;
    }
}
