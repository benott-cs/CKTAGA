package edu.uri.cs.classifier;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ben on 1/26/19.
 */
public class FeaturesAndTargets {

    private Map<String, ArrayList<Double>> featureVectors;
    private Map<String, Double> targets;

    public FeaturesAndTargets(Map<String, ArrayList<Double>> featureVectors, Map<String, Double> targets) {
        this.featureVectors = featureVectors;
        this.targets = targets;
    }

    public Map<String, ArrayList<Double>> getFeatureVectors() {
        return featureVectors;
    }

    public void setFeatureVectors(Map<String, ArrayList<Double>> featureVectors) {
        this.featureVectors = featureVectors;
    }

    public Map<String, Double> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, Double> targets) {
        this.targets = targets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeaturesAndTargets that = (FeaturesAndTargets) o;

        if (getFeatureVectors() != null ? !getFeatureVectors().equals(that.getFeatureVectors()) : that.getFeatureVectors() != null)
            return false;
        return getTargets() != null ? getTargets().equals(that.getTargets()) : that.getTargets() == null;
    }

    @Override
    public int hashCode() {
        int result = getFeatureVectors() != null ? getFeatureVectors().hashCode() : 0;
        result = 31 * result + (getTargets() != null ? getTargets().hashCode() : 0);
        return result;
    }
}
