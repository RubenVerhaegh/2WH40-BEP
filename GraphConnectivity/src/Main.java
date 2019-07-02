import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        (new Main()).run();
    }

    private void run() {
        Graph graph = GraphGenerator.getInstance().generateRandomLinkedCycle(10, 3);
        GraphStyler.getInstance().applyStandardStyle(graph, false);
        graph.display();

        outputMinMaxApproximations(graph, 2, 2, 0.1, 1);
    }

    private void outputMinMaxApproximations(Graph graph, double epsilonMin, double epsilonMax,
                                            double epsilonStep, int iterations) {
        int steps = (int) ((epsilonMax - epsilonMin) / epsilonStep) + 1;
        double[] max = new double[steps];
        double[] min = new double[steps];
        for (int i = 0; i < steps; i++) {
            min[i] = Double.MAX_VALUE;
        }

        double approximation;
        for (int k = 0; k < steps; k++) {
            double epsilon = epsilonMin + (k * epsilonStep);
            System.out.print(round(epsilon, 5));
            System.out.print("; ");
            for (int i = 0; i < iterations; i++){
                approximation = Algorithm.getInstance().approximateNumberOfSpanningSubgraphs(graph, epsilon);
                if (approximation > max[k]) {
                    max[k] = approximation;
                }
                if (approximation < min[k]) {
                    min[k] = approximation;
                }
                System.out.println(i);
            }
            System.out.println("MAX = " + max[k]);
            System.out.println("MIN = " + min[k]);
        }
        System.out.println();

        for (double minValue : min) {
            System.out.print(minValue);
            System.out.print("; ");
        }
        System.out.println();
        for (double maxValue : max) {
            System.out.print(maxValue);
            System.out.print("; ");
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void testFormulas() {
        double epsilon = 3;
        DNFFormula formula = customFormula();
        int nrOfVariables = formula.getVariables().size();

        System.out.println("Approximation with epsilon = " + epsilon + ":");
        double approximate    = formula.approximateNumberOfSatisfyingTruthAssignments(epsilon);
        double approximateInv = Math.pow(2, nrOfVariables) - approximate;
        System.out.println("  " + approximate);
        System.out.println("  ->" + approximateInv);
        System.out.println();
        System.out.println("Exact number of truth assignments:");
        int exact    = formula.countNumberOfSatisfyingTruthAssignments();
        int exactInv = (int) (Math.pow(2, nrOfVariables) - exact);
        System.out.println("  " + exact);
        System.out.println("  ->" + exactInv);

        System.out.println();
        System.out.println();

        System.out.println("Relative error original:");
        double error = (approximate - exact) / exact;
        System.out.println("  " + error);
        System.out.println("Relative error inverse statement:");
        double errorInv = (approximateInv - exactInv) / exactInv;
        System.out.println("  " + errorInv);
    }

    private DNFFormula customFormula() {
        Literal x1  = new Literal("x1", true);
        Literal nx1 = new Literal("x1", false);
        Literal x2  = new Literal("x2", true);
        Literal nx2 = new Literal("x2", false);
        Literal x3  = new Literal("x3", true);
        Literal nx3 = new Literal("x3", false);
        Literal x4  = new Literal("x4", true);
        Literal nx4 = new Literal("x4", false);
        Literal x5  = new Literal("x5", true);
        Literal nx5 = new Literal("x5", false);
        Literal x6  = new Literal("x6", true);
        Literal nx6 = new Literal("x6", false);
        Literal x7  = new Literal("x7", true);
        Literal nx7 = new Literal("x7", false);
        Literal x8  = new Literal("x8", true);
        Literal nx8 = new Literal("x8", false);
        Literal x9  = new Literal("x9", true);
        Literal nx9 = new Literal("x9", false);
        Literal x10  = new Literal("x10", true);
        Literal nx10 = new Literal("x10", false);
        Literal x11  = new Literal("x11", true);
        Literal nx11 = new Literal("x11", false);

        Set<Literal> set1 = new HashSet<>();
        set1.add(x1);
        set1.add(nx2);
        set1.add(x3);

        Set<Literal> set2 = new HashSet<>();
        set2.add(nx2);
        set2.add(nx3);
        set2.add(x4);
        set2.add(x5);

        Set<Literal> set3 = new HashSet<>();
        set3.add(x1);
        set3.add(x2);

        Set<Literal> set4 = new HashSet<>();
        set4.add(x4);
        set4.add(x5);

        Set<Literal> set5 = new HashSet<>();
        set5.add(x5);
        set5.add(x6);
        set5.add(x7);
        set5.add(x8);
        set5.add(x9);
        set5.add(x10);

        Set<Literal> set6 = new HashSet<>();
        set6.add(x11);

        Set<Literal> set7 = new HashSet<>();
        set7.add(x3);

        Set<Clause> clauses = new HashSet<>();
        clauses.add(new Clause(set1));
        clauses.add(new Clause(set2));
        clauses.add(new Clause(set3));
        clauses.add(new Clause(set4));
        clauses.add(new Clause(set5));
        clauses.add(new Clause(set6));
        clauses.add(new Clause(set7));

        return new DNFFormula(clauses);
    }

    private void neighborhood() {
        Graph graph = GraphGenerator.getInstance().edgeNeighborhood();
        showIds(graph);
        graph.display();

        ArrayList<Boolean> decision = new ArrayList<>();

        int nrOfFaultySubgraphs = countFaultySubgraphs(graph, decision);

        System.out.println(nrOfFaultySubgraphs);
    }

    private int countFaultySubgraphs(Graph graph, ArrayList<Boolean> decision) {
        if (decision.size() == graph.getEdgeCount()) {
            if (isFaulty(graph, decision)) {
                return 1;
            } else {
                return 0;
            }
        }

        decision.add(true);
        int sum = countFaultySubgraphs(graph, decision);

        decision.set(decision.size() - 1, false);
        sum += countFaultySubgraphs(graph, decision);

        decision.remove(decision.size() - 1);

        return sum;
    }

    private boolean isFaulty(Graph graph, ArrayList<Boolean> decision) {
        ArrayList<Edge> edges       = new ArrayList<>(graph.getEdgeSet());
        ArrayList<Edge> chosenEdges = new ArrayList<>();

        for (int i = 0; i < edges.size(); i++) {
            if (decision.get(i)) {
                chosenEdges.add(edges.get(i));
            }
        }

        if (hasLeftOutVertex(graph, chosenEdges)) {
            System.out.println(1);
            return true;
        }

        if (isDisconnected(graph, decision)) {
            System.out.println(2);
            return true;
        }

        System.out.println("#################################");
        return false;
    }

    private boolean isDisconnected(Graph graph, ArrayList<Boolean> decision) {
        Graph clone = Graphs.clone(graph);
        ArrayList<Edge> edges = new ArrayList<>(clone.getEdgeSet());

        GraphGenerator.getInstance().addRing(clone);

        for (int i = 0; i < decision.size(); i++) {
            if (decision.get(i) == false) {
                clone.removeEdge(edges.get(i));
            }
        }

        return !GraphAnalyzer.getInstance().isConnected(clone);
    }

    private boolean hasLeftOutVertex(Graph graph, ArrayList<Edge> edges) {
        Set<Node> coveredNodes = new HashSet<>();
        for (Edge edge : edges) {
            coveredNodes.add(edge.getNode0());
            coveredNodes.add(edge.getNode1());
        }

        ArrayList<Node> requiredNodes = new ArrayList<>();
        requiredNodes.add(graph.getNode(2));
        requiredNodes.add(graph.getNode(3));
        requiredNodes.add(graph.getNode(4));
        requiredNodes.add(graph.getNode(5));
        requiredNodes.add(graph.getNode(8));
        requiredNodes.add(graph.getNode(11));

        for (Node node : requiredNodes) {
            if (!coveredNodes.contains(node)) {
                return true;
            }
        }

        return false;
    }

    private void showIds(Graph g) {
        for (Node node : g.getNodeSet()) {
            node.setAttribute("label", node.getId());
        }
    }
}
