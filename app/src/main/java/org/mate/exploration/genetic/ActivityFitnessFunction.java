package org.mate.exploration.genetic;

import org.mate.model.TestCase;

public class ActivityFitnessFunction implements IFitnessFunction<TestCase> {
    public static final String FITNESS_FUNCTION_ID = "activity_fitness_function";

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        return chromosome.getValue().getVisitedActivities().size();
    }
}
