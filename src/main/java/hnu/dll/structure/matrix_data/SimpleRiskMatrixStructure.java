package hnu.dll.structure.matrix_data;

import org.ejml.simple.SimpleMatrix;

public class SimpleRiskMatrixStructure extends RiskMatrixStructure{
    public SimpleRiskMatrixStructure(SimpleMatrix riskMatrix){
        super(riskMatrix);
    }

    @Override
    public String toString() {
        return "SimpleRiskMatrixStructure{" +
                "riskMatrix=" + riskMatrix +
                '}';
    }
}
