import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
import org.graphstream.graph.implementations.MultiGraph;

import java.util.*;

public class Algorithm {
    private static Algorithm instance;

    private Algorithm() {}

    /**
     * Returns the instance of GraphAnalyzer according to the singleton design pattern.
     * @return the instance of GraphAnalyzer
     */
    public static Algorithm getInstance() {
        if (instance == null) {
            instance = new Algorithm();
        }
        return instance;
    }

    private Edge getRandomEdge(Graph graph) {
        int m = graph.getEdgeCount();
        ArrayList<Edge> edges = new ArrayList<>(graph.getEdgeSet());

        Random random = new Random();

        return edges.get(random.nextInt(m));
    }

    private MultiGraph contractEdge(MultiGraph graph, Edge edge) {
        // Clone the graph
        MultiGraph clone = (MultiGraph) Graphs.clone(graph);
        Edge contractingEdge = clone.getEdge(edge.getId());

        // Obtain the two vertices to be merged
        Node v1 = contractingEdge.getNode0();
        Node v2 = contractingEdge.getNode1();
        Set<Node> endPoints = new HashSet<>();
        endPoints.add(v1);
        endPoints.add(v2);

        // Make a new super vertex that will combine the two vertices
        String superVertexID = v1.getId() + "+" + v2.getId();
        clone.addNode(superVertexID);
        Node superVertex = clone.getNode(superVertexID);

        // For both of the two endpoints, remove any edges and instead redirect them to the super vertex
        for (Node endPoint : endPoints) {
            Set<String> edgeIds = edgeSetToIdSet(new HashSet<>(endPoint.getEdgeSet()));

            // Iterate over all edges from the current endpoint
            for (String edgeId : edgeIds) {
                // Get an edge from the current endpoint
                Edge redirectingEdge = clone.getEdge(edgeId);
                Node opposite        = redirectingEdge.getOpposite(endPoint);

                // If this is an edge between both endpoints of the contracted edge, simply remove it
                if (endPoints.contains(opposite)) {
                    clone.removeEdge(redirectingEdge);
                } else {
                    // Otherwise remove it, but also add one to the super vertex
                    clone.removeEdge(redirectingEdge);
                    clone.addEdge(edgeId, superVertex.getId(), opposite.getId());
                }
            }
        }

        // All edges have been redirected to the super vertex. We can now safely remove the original two vertices.
        clone.removeNode(v1);
        clone.removeNode(v2);

        return clone;
    }

    private MultiGraph contractRandomEdge(MultiGraph graph) {
        Edge e = getRandomEdge(graph);
        return contractEdge(graph, e);
    }

    private MultiGraph contractDownToSize(MultiGraph graph, int size) {
        int n = graph.getNodeCount();
        if (size > n) {
            throw new IllegalArgumentException("Called funtcion Algorithm.contractDownToSize() with the " +
                    "second argument 'size' being larger than the number of vertices in the inputed graph.");
        }

        while (graph.getNodeCount() > size) {
            graph = contractRandomEdge(graph);
        }

        return graph;
    }

    public Set<String> kargersCut(Graph graph) {
        MultiGraph clone = contractDownToSize((MultiGraph) graph, 2);

        Set<Edge>   cut   = new HashSet<>(clone.getEdgeSet());
        Set<String> edges = new HashSet<>();

        for (Edge cutEdge : cut) {
            edges.add(cutEdge.getId());
        }

        return edges;
    }

    private Set<Set<String>> recursiveContraction(Graph graph, double alpha) {
        int n = graph.getNodeCount();

        Set<Set<String>> output = new HashSet<>();

        if (n == 2) {
            Set<String> cut = kargersCut(graph);
            output.add(cut);

            return output;
        }

        for (int i = 0; i < 2; i++) {
            MultiGraph clone = contractDownToSize((MultiGraph) graph, (int) (n / Math.pow(2, 1 / (2 * alpha) )));
            output.addAll(recursiveContraction(clone, alpha));
        }

        return output;
    }

    public Set<Set<String>> enumerateRandomSmallCuts(Graph graph, double alpha) {
        if (alpha < 1) {
            throw new IllegalArgumentException("Algorithm.enumerateRandomSmallCuts() was called with argument alpha = " +
                    alpha + ". It should be at least 1.");
        }

        int n = graph.getNodeCount();

        Set<Set<String>> cuts = new HashSet<>();
        int iterations = (int) Math.pow(Math.log(n) / Math.log(2), 2);

        // Enumerate cuts
        for (int i = 0; i < iterations; i ++) {
            cuts.addAll(recursiveContraction(graph, alpha));
        }

        /*// Figure out what the size of the minimum found cut size is
        int minCutSize = Integer.MAX_VALUE;
        for (Set<String> cut : cuts) {
            if (cut.size() < minCutSize) {
                minCutSize = cut.size();
            }
        }

        // Only keep the cuts that are within a factor alpha of the minimum cut size
        Iterator<Set<String>> iterator = cuts.iterator();

        int dumpCounter = 0;
        while (iterator.hasNext()) {
            Set<String> cut = iterator.next();

            if (cut.size() > alpha * minCutSize) {
                iterator.remove();
                dumpCounter++;
            }
        }

        System.out.println("Dumped " + dumpCounter + " cuts under alpha = " + alpha);*/
        return cuts;
    }

    private DNFFormula transformSetOfCutsToDNFFormula(Graph graph, Set<Set<String>> cuts) {
        Set<Clause> clauses = new HashSet<>();

        // Each cut represents a clause
        for (Set<String> cut : cuts) {
            Set<Literal> literals = new HashSet<>();

            // The edges of a cut represent the literals in the clause
            for (String edge : cut) {
                literals.add(new Literal(edge, true));
            }

            clauses.add(new Clause (literals));
        }

        Set<Edge> edgeSet = new HashSet<>(graph.getEdgeSet());

        return new DNFFormula(clauses, edgeSetToIdSet(edgeSet));
    }

    public double approximateNumberOfSpanningSubgraphs(Graph graph, double epsilon) {
        int n = graph.getNodeCount();
        int m = graph.getEdgeCount();
        double alpha = 2 - Math.log(epsilon) / (2 * Math.log(n));
        //System.out.println("alpha  = " + alpha);
        //System.out.println("n^(2a) = " + Math.pow(n, 2 * alpha));

        Set<Set<String>> cuts = enumerateRandomSmallCuts(graph, alpha);
        //System.out.println(cuts.size() + " cuts found");

        DNFFormula formula = transformSetOfCutsToDNFFormula(graph, cuts);
        //System.out.println("Formula made");

        double nrOfSatisfyingTruthAssignments = formula.approximateNumberOfSatisfyingTruthAssignments(epsilon);

        return Math.pow(2, m) - nrOfSatisfyingTruthAssignments;
    }

    private Set<String> edgeSetToIdSet(Set<Edge> list) {
        Set<String> ids = new HashSet<>();

        for (Edge edge : list) {
            ids.add(edge.getId());
        }

        return ids;
    }
}
