package hnu.dll.structure.matrix_data;

import org.ejml.simple.SimpleMatrix;

public class RiskMatrixStructure {
    protected Double targetVariance;
    protected SimpleMatrix dominatingVarianceMatrix;

    public RiskMatrixStructure(Double targetVariance, SimpleMatrix dominatingVarianceMatrix) {
        this.targetVariance = targetVariance;
        this.dominatingVarianceMatrix = dominatingVarianceMatrix;
    }

    public Double getTargetVariance() {
        return targetVariance;
    }

    public SimpleMatrix getDominatingVarianceMatrix() {
        return dominatingVarianceMatrix;
    }
}
