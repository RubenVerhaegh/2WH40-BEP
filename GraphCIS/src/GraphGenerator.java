import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import sun.security.provider.certpath.Vertex;

import java.util.*;

public class GraphGenerator {
    private final  GraphAnalyzer  analyzer  = GraphAnalyzer.getInstance();
    private static GraphGenerator instance;

    private GraphGenerator() {}

    /**
     * Returns the instance of GraphGenerator according to the singleton design pattern.
     * @return the instance of GraphGenerator
     */
    public static GraphGenerator getInstance() {
        if (instance == null) {
            instance = new GraphGenerator();
        }
        return instance;
    }

    /**
     * Generates a cycle of n vertices
     * @param n number of vertices
     * @return Graph object consisting of an n-cycle
     */
    public Graph generateCycle(int n) {
        Graph graph = new SingleGraph("graph");
        // Add vertices
        double x;
        double y;
        for (int i = 0; i < n; i++) {
            x = Math.cos((2 * Math.PI / n) * i);
            y = Math.sin((2 * Math.PI / n) * i);
            graph.addNode(Integer.toString(i));
            graph.getNode(i).setAttribute("xy", x, y);
            graph.getNode(i).addAttribute("size", 1);
        }

        String id1;
        String id2;
        // Special case: n=2
        if (n == 2) {
            id1 = Integer.toString(0);
            id2 = Integer.toString(1);
            graph.addEdge(id1 + "-" + id2, id1, id2);
            return graph;
        }

        // Add edges
        for (int i = 0; i < n; i++) {
            id1 = Integer.toString(i);
            id2 = Integer.toString(i + 1);
            if (i + 1 == n) {
                id1 = Integer.toString(0);
                id2 = Integer.toString(n-1);
            }
            graph.addEdge(id1 + "-" + id2, id1, id2);
        }

        return graph;
    }

    /**
     * Generates a complete graph on n vertices.
     * That is: a graph with {@code n} vertices that are all connected with one another.
     * @param n number of vertices
     * @return a complete graph on {@code n} vertices
     */
    public Graph generateCompleteGraph(int n) {
        Graph graph = new SingleGraph("graph");
        // Edge case: 0 vertices
        if (n == 0) {
            return graph;
        }

        // Add all vertices
        String id;
        String previd;
        double x;
        double y;
        for (int i = 0; i < n; i++) {
            id = Integer.toString(i);
            graph.addNode(id);
            x = Math.cos((2 * Math.PI / n) * i);
            y = Math.sin((2 * Math.PI / n) * i);
            graph.getNode(i).setAttribute("xy", x, y);
        }

        // Add edges between all pairs of vertices
        for (int i = 0; i < n; i++) {
            previd = Integer.toString(i);
            for (int j = i + 1; j < n; j++) {
                id = Integer.toString(j);
                graph.addEdge(previd + "-" + id, previd, id);
            }
        }

        return graph;
    }

    /**
     * Generate a ladder graph on n vertices.
     * That is, a 2x(n/2) cross section of a grid. If n is odd, the graph is a ladder graph on n-1 vertices, with the
     * a single vertex, connected to the two endpoints on one side of the ladder.
     * @param n number of vertices
     * @return ladder graph on {@code n} vertices
     */
    public Graph generateLadder(int n) {
        Graph graph = generateCycle(n);
        int i1 = 1;
        int i2 = n - 2;
        while (i1 < i2 - 1) {
            graph.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2--;
        }

        return graph;
    }

    /**
     * Generates a cycle of n vertices, where as much random pairs of nodes as possible are linked, such that no vertex
     * has degree more than d.
     * @param n number of vertices of the graph
     * @param d maximum degree of the graph
     * @return a graph with {@code n} vertices, where every vertex has degree {@code max(d, n-1)} or if {@code n} is odd,
     *         one vertex has degree that is 1 smaller and of which a cycle is a subgraph.
     */
    public Graph generateRandomLinkedCycle(int n, int d) {
        Graph graph = generateCompleteGraph(n);

        if (n <= d + 1) {
            return graph;
        }

        // Remove outer cycle from the complete graph
        for (int i = 0; i < n; i++) {
            if (i < n-1) {
                graph.removeEdge(i, i+1);
            } else {
                graph.removeEdge(0, n-1);
            }
        }

        Set<Edge> matching;
        for (int i = 0; i < d - 2; i++) {
            matching = analyzer.getRandomMaximumMatching(graph);

            for (Edge edge : matching) {
                graph.removeEdge(edge);
            }
        }

        return complement(graph);
    }

    /**
     * Generates a "spokes graph" on n vertices.
     * That is: a cycle where each vertex has an extra edge towards the vertex that is furthest away from it in the
     * cycle.
     * @param n number of vertices
     * @return a spokes graph on {@code n} vertices
     */
    public Graph generateSpokesGraph(int n) {
        Graph cycle = generateCycle(n);

        // Special cases: n <= 3
        if (n <= 3) {
            return cycle;
        }

        int i1 = 0;
        int i2 = n/2;
        for (int i = 0; i < n/2; i++) {
            cycle.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2++;
        }

        return cycle;
    }

    public Graph generateGridLikeCycle(int n) {
        Graph graph = generateCycle(n);
        //Special case: n <= 3
        if (n <= 3) {
            return graph;
        }

        // Add horizontal edges
        int i1 = 0;
        int i2 = n * 3 / 4 - 1;
        while (i1 < n / 4) {
            graph.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2--;
        }

        // Add vertical edges
        i1 = n/4;
        i2 = n - 1;
        while (i1 < n / 2) {
            graph.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2--;
        }

        return graph;
    }

    public Graph generateWeirdGraph(int n) {
        Graph graph = generateCycle(n);
        //Special case: n <= 7
        if (n <= 7) {
            return graph;
        }

        // Add edges for the first side
        int i1 = 0;
        int i2 = n / 4;
        while (i1 < n/4) {
            graph.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2++;
        }

        // Add eges for the second side
        i1 = n / 2;
        i2 = n * 3 / 4;
        while (i1 < n * 3 / 4) {
            graph.addEdge(i1 + "-" + i2, i1, i2);
            i1++;
            i2++;
        }

        return graph;
    }

    /**
     * Generates a "ladder ring" on n vertices
     * Generates a ladder graph whose ends are connected together. This forms two cycles whose nodes are sequentially
     * linked together with an additional edge.
     * @param n number of nodes
     * @return a ladder ring on {@code n} vertices
     */
    public Graph generateClosedLadder(int n) {
        Graph graph = generateLadder(n);

        //Special case: n <= 4
        if (n <= 4) {
            return graph;
        }

        // Connect ends
        int i1 = 0;
        int i2 = n / 2 - 1;
        graph.addEdge(i1 + "-" + i2, i1, i2);

        i1 = n / 2;
        i2 = n - 1;
        graph.addEdge(i1 + "-" + i2, i1, i2);

        return graph;
    }

    /**
     * Constructs a gadget on n vertices by removing two edges from a randomly linked cycle, such that four distinct
     * vertices have degree 2 (and the remaining vertices thus have degree 3).
     * @param n number of vertices
     * @return a random gadget on {@code n} vertices
     */
    public Gadget4 generateRandomCycleGadget4(int n, int d) {
        //Special case: n <= 3
        if (n <= 3) {
            throw new IllegalArgumentException("Gadgets must have at least 4 vertices");
        }

        // Start with a randomly linked cycle
        Graph graph = generateRandomLinkedCycle(n, d);

        Node[] linkNodes = new Node[4];

        // Pick a random edge to remove and use the two vertices incident with it as link nodes
        ArrayList<Edge> edges = new ArrayList<>(graph.getEdgeSet());
        Collections.shuffle(edges);
        Edge edge = edges.get(0);
        linkNodes[0] = (edge.getNode0());
        linkNodes[1] = (edge.getNode1());
        graph.removeEdge(edge);

        // Pick the next random edge that is incident with two vertices distinct from the first two
        for (Edge e : edges) {
            if (!Arrays.asList(linkNodes).contains(e.getNode0()) &&
                    !Arrays.asList(linkNodes).contains(e.getNode1())) {
                edge = e;
                break;
            }
        }
        // Remove the edge and use the two vertices incident with it as the remaining two link nodes
        linkNodes[2] = (edge.getNode0());
        linkNodes[3] = (edge.getNode1());
        graph.removeEdge(edge);

        // Make the constructed graph a gadget.
        return new Gadget4(graph, linkNodes, true);
    }

    /**
     * Constructs a gadget on n vertices by removing two edges from a randomly linked cycle, such that four distinct
     * vertices have degree 2 (and the remaining vertices thus have degree 3).
     * @param n number of vertices
     * @return a random gadget on {@code n} vertices
     */
    public Gadget1 generateRandomCycleGadget1(int n, int d) {
        //Special case: n < 1
        if (n < 1) {
            throw new IllegalArgumentException("Gadgets must have at least 1 vertex");
        }

        // Start with a randomly linked cycle
        Graph graph = generateRandomLinkedCycle(n, d);
        Node linkNode;

        ArrayList<Node> vertices = new ArrayList<>(graph.getNodeSet());
        Collections.shuffle(vertices);

        Node minNode = vertices.get(0);
        int minDegree = minNode.getDegree();
        int maxDegree = minNode.getDegree();

        for (Node vertex : vertices) {
            if (vertex.getDegree() < minDegree) {
                minDegree = vertex.getDegree();
                minNode = vertex;
            }
            if (vertex.getDegree() > maxDegree){
                maxDegree = vertex.getDegree();
            }
        }

        if (minDegree == maxDegree) {
            //System.out.println("[WARNING] Gadget1 made where two vertices have the lowest degree. Likely suboptimal.");

            ArrayList<Edge> edges = new ArrayList<>(graph.getEdgeSet());
            Collections.shuffle(edges);

            Edge edge = edges.get(0);
            graph.removeEdge(edge);

            if (new Random().nextBoolean()) {
                linkNode = edge.getNode0();
            } else {
                linkNode = edge.getNode1();
            }
        } else {
            linkNode = minNode;
        }

        // Make the constructed graph a gadget.
        return new Gadget1(graph, linkNode);
    }

    public Gadget2 generateRandomCycleGadget2(int n, int d) {
        //Special case: n < 2
        if (n < 2) {
            throw new IllegalArgumentException("Gadgets must have at least 2 vertices");
        }

        Graph graph = generateRandomLinkedCycle(n, d);
        ArrayList<Edge> edges = new ArrayList<>(graph.getEdgeSet());

        Collections.shuffle(edges);
        Edge edge      = edges.get(0);
        Node linkNode1 = edge.getNode0();
        Node linkNode2 = edge.getNode1();

        if (n > 3) {
            graph.removeEdge(edge);
        }

        return new Gadget2(graph, linkNode1, linkNode2);
    }

    public Gadget generateRandomCycleGadget(int n, int d, int l) {
        // Gadget too small
        if (n < 2) {
            throw new IllegalArgumentException("Gadgets must have at least 2 vertices");
        }
        // More link nodes than total node count
        if (2 * l > n) {
            throw new IllegalArgumentException("Gadgets must have as least as many nodes as they have link nodes");
        }

        Graph graph = generateRandomLinkedCycle(n, d);
        return gadgetizeGraph(graph, l);
    }

    public Gadget generateRandomGadget(int n, int d, int l) {
        // Gadget too small
        if (n < 2) {
            throw new IllegalArgumentException("Gadgets must have at least 2 vertices");
        }
        // More link nodes than total node count
        if (2 * l > n) {
            throw new IllegalArgumentException("Gadgets must have as least as many nodes as they have link nodes");
        }

        Graph graph = generateRandomGraph(n, d);
        return gadgetizeGraph(graph, l);
    }

    public Graph generateRandomGraph(int n, int d) {
        Graph completeGraph = generateCompleteGraph(n);
        for (int i = 0; i < d; i++) {
            Set<Edge> matching = analyzer.getRandomMaximumMatching(completeGraph);

            for (Edge edge : matching) {
                completeGraph.removeEdge(edge);
            }
        }

        return complement(completeGraph);
    }

    public Gadget gadgetizeGraph(Graph graph, int l) {
        Node[] linkNodes = new Node[2 * l];
        ArrayList<Edge> matching = new ArrayList<>(analyzer.getRandomMatching(graph, l));

        for (int i = 0; i < matching.size(); i++) {
            Edge edge = matching.get(i);
            linkNodes[i]     = edge.getNode0();
            linkNodes[l + i] = edge.getNode1();

            graph.removeEdge(edge);
        }

        return new Gadget(graph, linkNodes);
    }

    public Gadget4 generateSquareGadget() {
        Graph graph = generateCycle(4);

        Node[] linkNodes = new Node[4];
        linkNodes[0] = graph.getNode(0);
        linkNodes[1] = graph.getNode(1);
        linkNodes[2] = graph.getNode(3);
        linkNodes[3] = graph.getNode(2);

        return new Gadget4(graph, linkNodes, false);
    }

    public Graph complement(Graph graph) {
        Graph complement = generateCompleteGraph(graph.getNodeCount());
        for (Edge edge : graph.getEachEdge()) {
            complement.removeEdge(edge.getId());
        }

        return complement;
    }
}
