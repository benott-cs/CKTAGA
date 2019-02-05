package edu.uri.cs.classifier;

import libsvm.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Ben on 1/26/19.
 */
public class SVM {

    private svm_model svm_model = null;
    private svm_problem svm_problem = null;
    private double cVal = 1.0;

    public SVM() {
    }

    public double getAccuracyOnProvidedSample(svm_problem prob) {
        double ret = 0;
        if (Objects.nonNull(svm_model)) {
            double numSamples = prob.l * 1.0;
            int correct = 0;
            for (int i = 0; i < prob.l; i++) {
                svm_node[] input = prob.x[i];
                double result = svm.svm_predict(svm_model, input);
                if (result == prob.y[i]) {
                    correct++;
                }
            }
            ret = correct/numSamples;
        }
        return ret;
    }

    public void trainClassifier(svm_problem problem, svm_parameter svm_params) {
        if (Objects.nonNull(problem)) {
            this.cVal = svm_params.C;
            svm_model = svm.svm_train(problem, svm_params);
        }
    }

    public static svm_problem createProblem(FeaturesAndTargets featuresAndTargets) {
        Map<String, ArrayList<Double>> featureVectors = featuresAndTargets.getFeatureVectors();
        Map<String, Double> targets = featuresAndTargets.getTargets();
        if (featureVectors.size() != targets.size()) {
            throw new IllegalArgumentException("Number of feature vectors " +
                    "does not match the number of target vectors");
        }
        svm_problem problem = new svm_problem();
        int numSamples = targets.size();
        problem.l = numSamples;
        problem.x = new svm_node[problem.l][];
        problem.y = new double[problem.l];
        int i = 0;
        for (String key : targets.keySet()) {
            problem.y[i] = targets.get(key).doubleValue();
            ArrayList<Double> featureVec = featureVectors.get(key);
            svm_node[] x = new svm_node[featureVec.size()];
            int j = 0;
            for (Double d : featureVec) {
                x[j] = new svm_node();
                x[j].index = j;
                x[j].value = d.doubleValue();
                j++;
            }
            problem.x[i] = x;
            i++;
        }
        return problem;
    }

    public libsvm.svm_problem getSvm_problem() {
        return svm_problem;
    }

    public void setSvm_problem(libsvm.svm_problem svm_problem) {
        this.svm_problem = svm_problem;
    }

    public double getcVal() {
        return cVal;
    }
}
