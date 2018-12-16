package edu.uri.cs.ga.scoring.kernel;

import edu.uri.cs.util.PropertyManager;
import libsvm.svm_parameter;

/**
 * Created by Ben on 12/13/18.
 */
public class KernelHelper {

    private svm_parameter svm_params;
    private KernelComputations kernelComputations;

    public KernelHelper(PropertyManager propertyManager) {
        int kernelType = propertyManager.getPropAsInt(PropertyManager.KERNEL_TYPE);
        int degree = propertyManager.getPropAsInt(PropertyManager.KERNEL_DEGREE);
        double coefficient = propertyManager.getPropAsDouble(PropertyManager.KERNEL_COEFFICIENT);
        double gamma = propertyManager.getPropAsDouble(PropertyManager.KERNEL_GAMMA);
        svm_params = new svm_parameter();
        svm_params.kernel_type = kernelType;
        svm_params.coef0 = coefficient;
        svm_params.degree = degree;
        svm_params.gamma = gamma;
        kernelComputations = new KernelComputations(svm_params);
    }

    public double computeKernel(double[] vec1, double[] vec2) {
        return kernelComputations.kernel_function(vec1, vec2);
    }
}
