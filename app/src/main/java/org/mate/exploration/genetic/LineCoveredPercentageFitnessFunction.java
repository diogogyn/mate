package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.ui.EnvironmentManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineCoveredPercentageFitnessFunction implements IFitnessFunction<TestCase> {
    public static final String FITNESS_FUNCTION_ID = "line_covered_percentage_fitness_function";

    //todo: find better solution than static map... (i know its ugly)
    private static final Map<String, Map<IChromosome<TestCase>, Double>> cache = new HashMap<>();
    private static List<String> lines = new ArrayList<>();
    private final String line;

    public LineCoveredPercentageFitnessFunction(String line) {
        this.line = line;
        lines.add(line);
    }

    @Override
    public double getFitness(IChromosome<TestCase> chromosome) {
        if (!cache.get(line).containsKey(chromosome)) {
            throw new IllegalStateException("Fitness for chromosome " + chromosome + " not in cache. Must fetch fitness previously or performance reasons");
        }
        return cache.get(line).get(chromosome);

    }

    public static void retrieveFitnessValues(IChromosome<TestCase> chromosome) {
        if (lines.size() == 0) {
            return;
        }

        if (cache.size() == 0) {
            for (String line : lines) {
                cache.put(line, new HashMap<IChromosome<TestCase>, Double>());
            }
        }

        MATE.log_acc("retrieving fitness values for chromosome " + chromosome);
        List<Double> coveredPercentage = EnvironmentManager.getLineCoveredPercentage(chromosome, lines);
        for (int i = 0; i < coveredPercentage.size(); i++) {
            cache.get(lines.get(i)).put(chromosome, coveredPercentage.get(i));
        }
    }
}