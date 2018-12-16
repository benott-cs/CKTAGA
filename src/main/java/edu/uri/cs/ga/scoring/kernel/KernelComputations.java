package edu.uri.cs.ga.scoring.kernel;

/**
 * Created by Ben on 12/13/18.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import libsvm.svm_parameter;

public class KernelComputations {

    private final int kernel_type;
    private final int degree;
    private final double gamma;
    private final double coef0;

    public KernelComputations(svm_parameter svm_params) {
        this.kernel_type = svm_params.kernel_type;
        this.coef0 = svm_params.coef0;
        this.degree = svm_params.degree;
        this.gamma = svm_params.gamma;
    }

    private static double powi(double x, int k) {
        if (x == 0 && k == 0) {
            return Double.NaN;
        }
        double total = 1;
        for (int i = 0; i < Math.abs(k); i++) {
            total *= x;
        }
        return (k < 0) ? (1/total) : total;
    }

    public double kernel_function(double[] var1, double[] var2) {
        switch(this.kernel_type) {
            case 0:
                return dot(var1, var2);
            case 1:
                return powi(this.gamma * dot(var1, var2) + this.coef0, this.degree);
            case 2:
                return Math.exp(-this.gamma * (dot(var1, var1) + dot(var2, var2) - 2.0D * dot(var1, var2)));
            case 3:
                return Math.tanh(this.gamma * dot(var1, var2) + this.coef0);
            default:
                return 0.0D;
        }
    }

    private static double dot(double[] var0, double[] var1) {
        int length1 = var0.length;
        if (length1 != var1.length) {
            throw new IllegalArgumentException("Vectors must have the same size");
        }
        double ret = 0.0;
        for (int i = 0; i < length1; i++) {
            ret += var0[i] * var1[i];
        }

        return ret;
    }

}
