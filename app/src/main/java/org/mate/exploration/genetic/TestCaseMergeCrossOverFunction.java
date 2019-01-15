package org.mate.exploration.genetic;

import org.mate.MATE;
import org.mate.model.TestCase;
import org.mate.model.graph.EventEdge;
import org.mate.model.graph.GraphGUIModel;
import org.mate.model.graph.StateGraph;
import org.mate.state.IScreenState;
import org.mate.ui.Action;
import org.mate.ui.EnvironmentManager;
import org.mate.utils.Optional;
import org.mate.utils.Randomness;

import java.util.ArrayList;
import java.util.List;

public class TestCaseMergeCrossOverFunction implements ICrossOverFunction<TestCase> {
    public static final String CROSSOVER_FUNCTION_ID = "test_case_merge_crossover_function";
    private static final double MAX_LENGTH_DEVIATION = 0.25;
    private boolean storeCoverage;
    private boolean executeActions;

    public TestCaseMergeCrossOverFunction() {
        this(true);
    }

    public TestCaseMergeCrossOverFunction(boolean storeCoverage) {
        this.storeCoverage = storeCoverage;
        executeActions = true;
    }

    public void setExecuteActions(boolean executeActions) {
        this.executeActions = executeActions;
    }

    @Override
    public IChromosome<TestCase> cross(List<IChromosome<TestCase>> parents) {
        List<Action> l1 = parents.get(0).getValue().getEventSequence();
        List<Action> l2 = parents.get(1).getValue().getEventSequence();
        if (l2.size() < l1.size()) {
            List<Action> tmp = l1;
            l1 = l2;
            l2 = tmp;
        }

        //Randomly select whether final length should be floored or ceiled
        int lengthBias = Randomness.getRnd().nextInt(2);
        int finalSize = (l1.size() + l2.size() + lengthBias) / 2;


        int choice = Randomness.getInRangeStd(l1.size());
        boolean right = choice != l1.size() - 1;
        int d = 0;
        boolean matched = false;
        StateGraph sg = ((GraphGUIModel) MATE.guiModel).getStateGraph();

        //traverse the list from chosen start point to the right and to the left in an alternating pattern
        for (int i = 0; i < l1.size(); i++) {
            int idx = choice + d;
            EventEdge e1 = sg.getEdgeByAction(l1.get(idx));

            //don't consider actions that result in leaving the app
            if (e1 != null && e1.getTarget().getScreenState().getPackageName().equals(MATE.packageName)) {
                int cc = l2.size() / 2 + (l1.size() + 1) / 2 - idx;
                // keep starting index within list bounds
                cc = Math.min(Math.max(0, cc), l1.size() - 1);
                Optional<Integer> match = findMatch(l1.get(idx), l2, cc);
                if (match.hasValue()) {
                    MATE.log_acc("Found match: " + idx + ", " + match.getValue());
                    return merge(l1.subList(0, idx + 1), l2.subList(match.getValue(), l2.size()), finalSize);
                }
            }

            if (right) {
                if (d < 0) {
                    d = -d;
                }
                d = d + 1;
                if (choice - d >= 0) {
                    right = false;
                }
            } else {
                if (d > 0) {
                    d = -d;
                } else {
                    d -= 1;
                }
                if (choice - d + 1 < l1.size()) {
                    right = true;
                }
            }
        }
        MATE.log_acc("No match found.");
        return parents.get(0);
    }

    private Optional<Integer> findMatch(Action from, List<Action> l, int start) {
        boolean right = start == 0;
        int d = 0;
        StateGraph sg = ((GraphGUIModel) MATE.guiModel).getStateGraph();

        //traverse the list from chosen start point to the left and to the right in an alternating pattern
        for (int i = 0; i < l.size(); i++) {
            int idx = start + d;

            EventEdge e1 = sg.getEdgeByAction(from);
            EventEdge e2 = sg.getEdgeByAction(l.get(idx));

            if (e1 != null && e2 != null
                    && e1.getTarget().getScreenState().equals(e2.getSource().getScreenState())) {
                return Optional.some(idx);
            }

            if (right) {
                if (d < 0) {
                    d = -d;
                } else {
                    d = d + 1;
                }
                if (start - d - 1>= 0) {
                    right = false;
                }
            } else {
                if (d > 0) {
                    d = -d;
                }
                d -= 1;

                if (start - d < l.size()) {
                    right = true;
                }
            }
        }
        return Optional.none();
    }

    private IChromosome<TestCase> merge(List<Action> l1, List<Action> l2, int finalSize) {
        List<Action> all = new ArrayList<>(l1);
        all.addAll(l2);

        int lowerChangeBound = (int) Math.floor(finalSize * (1 - MAX_LENGTH_DEVIATION));
        int upperChangeBound = (int) Math.ceil(finalSize * (1 + MAX_LENGTH_DEVIATION));

        //keep final size within max deviation
        finalSize = Math.min(Math.max(all.size(), lowerChangeBound), upperChangeBound);

        TestCase testCase = new TestCase("dummy");
        testCase.setDesiredSize(Optional.some(finalSize));
        testCase.getEventSequence().addAll(all);

        if (executeActions) {
            TestCase executedTestCase = TestCase.fromDummy(testCase);
            Chromosome<TestCase> chromosome = new Chromosome<>(executedTestCase);

            if (storeCoverage) {
                EnvironmentManager.storeCoverageData(chromosome, null);

                MATE.log_acc("After test case merge crossover:");
                MATE.log_acc("Coverage of: " + chromosome.toString() + ": " + EnvironmentManager
                        .getCoverage(chromosome));
                MATE.log_acc("Found crash: " + String.valueOf(chromosome.getValue().getCrashDetected()));
            }

            return chromosome;
        }

        return new Chromosome<>(testCase);
    }
}
