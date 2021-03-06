package org.mate.exploration.genetic.termination;

/**
 * Interface that determines if the termination condition of an
 * {@link org.mate.exploration.genetic.core.IGeneticAlgorithm} is met
 */
public interface ITerminationCondition {
    /**
     * Check if termination condition is met
     * @return whether termination condition is met
     */
    boolean isMet();
}
