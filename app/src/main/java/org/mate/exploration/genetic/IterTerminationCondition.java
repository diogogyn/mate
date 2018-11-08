package org.mate.exploration.genetic;

public class IterTerminationCondition implements ITerminationCondition {
    private int iterations;
    private int maxIterations;

    public IterTerminationCondition(int maxIterations) {
        this.maxIterations = maxIterations;
        iterations = 0;
    }
    @Override
    public boolean isMet() {
        if (iterations == maxIterations) {
            return true;
        }

        iterations++;
        return false;
    }
}