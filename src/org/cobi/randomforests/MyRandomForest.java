/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.randomforests;

import java.io.Serializable;

/**
 *
 * @author mxli
 */
public class MyRandomForest implements Serializable {
      private static final long serialVersionUID = 301L;
    /**
     * The small deviation allowed in double comparisons.
     */
    public static double SMALL = 1e-6;

    public int m_numTrees;
    public MyRandomTree[] m_Classifiers;
    String name;
    double auc = Double.NaN;

    public String getName() {
        return name;
    }

    public double getAuc() {
        return auc;
    }

    public void setAuc(double auc) {
        this.auc = auc;
    }

    /**
     * Calculates the class membership probabilities for the given test
     * instance.
     *
     * @param instance the instance to be classified
     *
     * @return preedicted class probability distribution
     *
     * @throws Exception if distribution can't be computed successfully
     */
    protected double[] distributionForInstance(double[] instance) throws Exception {
        int numClasses = 2;
        double[] sums = new double[numClasses], newProbs;

        double sum = 0;

        for (int i = 0; i < m_numTrees; i++) {
            newProbs = m_Classifiers[i].distributionForInstance(instance);
            for (int j = 0; j < newProbs.length; j++) {
                sums[j] += newProbs[j];
            }
        }
        for (int i = 0; i < sums.length; i++) {
            sum += sums[i];
        }
        for (int i = 0; i < sums.length; i++) {
            sums[i] /= sum;
        }
        return sums;
    }

    /**
     * Classifies the given test instance. The instance has to belong to a
     * dataset when it's being classified. Note that a classifier MUST implement
     * either this or distributionForInstance().
     *
     * @param instance the instance to be classified
     * @return the predicted most likely class for the instance or
     * Utils.missingValue() if no prediction is made
     * @exception Exception if an error occurred during the prediction
     */
    public double classifyInstance(double[] instance) throws Exception {
        double[] dist = distributionForInstance(instance);
        if (dist == null) {
            throw new Exception("Null distribution predicted");
        }
        double max = 0;
        int maxIndex = 0;
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > max) {
                maxIndex = i;
                max = dist[i];
            }
        }
        if (max > 0) {
            return maxIndex;
        } else {
            return Double.NaN;
        }

    }

    public double[] getClassifyDistribution(double[] instance) throws Exception {
        double[] tmp = new double[2];
        double[] dist = distributionForInstance(instance);
        if (dist == null) {
            throw new Exception("Null distribution predicted");
        }
        double max = 0;
        int maxIndex = 0;
        for (int i = 0; i < dist.length; i++) {
            if (dist[i] > max) {
                maxIndex = i;
                max = dist[i];
            }
        }
        if (max > 0) {
            tmp[0] = maxIndex;
        } else {
            tmp[0] = Double.NaN;
        }
        tmp[1] = dist[1];
        return tmp;
    }

    public static void main(String[] args) throws Exception {

    }
}
