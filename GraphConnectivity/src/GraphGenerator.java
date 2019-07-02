import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GraphGenerator {
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

    public MultiGraph createNewMultiGraph(int n) {
        MultiGraph g = new MultiGraph("");
        for (int i = 0; i < n; i ++) {
            g.addNode(String.valueOf(i));
        }

        return g;
    }

    public SingleGraph createNewSingleGraph(int n) {
        SingleGraph g = new SingleGraph("");
        for (int i = 0; i < n; i ++) {
            g.addNode(String.valueOf(i));
        }

        return g;
    }

    public Graph connectVertices(Graph g, int id1, int id2) {
        Node v1 = g.getNode(id1);
        Node v2 = g.getNode(id2);

        return connectVertices(g, v1, v2);
    }

    public Graph connectVertices(Graph g, String id1, String id2) {
        Node v1 = g.getNode(id1);
        Node v2 = g.getNode(id2);

        return connectVertices(g, v1, v2);
    }

    public Graph connectVertices(Graph g, Node v1, Node v2) {
        int i = 0;
        Edge edge = null;
        String id = "";
        do {
            id = v1.getId() + "-" + v2.getId() + "." + i;
            edge = g.getEdge(id);
            i++;
        } while (edge != null);

        g.addEdge(id, v1, v2);

        return  g;
    }

    public MultiGraph test() {
        MultiGraph graph = createNewMultiGraph(8);

        connectVertices(graph, 0, 3);
        connectVertices(graph, 1, 2);
        connectVertices(graph, 2, 3);
        connectVertices(graph, 2, 5);
        connectVertices(graph, 4, 5);
        connectVertices(graph, 5, 6);
        connectVertices(graph, 5, 7);

        connectVertices(graph, 1, 3);

        return graph;
    }

    /**
     * Generates a complete graph on n vertices.
     * That is: a graph with {@code n} vertices that are all connected with one another.
     * @param n number of vertices
     * @return a complete graph on {@code n} vertices
     */
    public Graph generateCompleteGraph(int n) {
        Graph graph = new MultiGraph("graph");
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
                connectVertices(graph, previd, id);
            }
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
            matching = getRandomMaximumMatching(graph);

            for (Edge edge : matching) {
                graph.removeEdge(edge);
            }
        }

        return complement(graph);
    }

    /**
     * Returns a maximum matching of a graph. That is, a matching that has a maximum size of all matchings of the graph.
     * @param graph  input graph
     * @return a maximum matching of {@code graph}
     */
    public Set<Edge> getRandomMaximumMatching(Graph graph) {
        Set<Edge> M = new HashSet<>();
        int previousMatchingSize;
        do {
            previousMatchingSize = M.size();
            M = augmentMatching(graph, M);
        } while (M.size() > previousMatchingSize);

        return M;
    }

    /**
     * Augments a matching in a graph.
     * This method returns a matching in the input graph that has cardinality 1 higher than the given matching M.
     * If there is no such matching (i.e.: M is a maximum matching), the method returns M instead.
     * @param graph input graph
     * @param M     matching that needs to be augmented
     * @return a matching {@code M'} of {@code graph}, such that {@code M'.size() = M.size() + 1} if such a matching
     *         exists or {@code M} if no such matching exists.
     */
    private Set<Edge> augmentMatching(Graph graph, Set<Edge> M) {
        // Special case: in case of a (near-)perfect matching, there is no need to augment it.
        // This is not necessary for correctness but serves as a speedup
        if (M.size() >= graph.getNodeCount() / 2) {
            return M;
        }

        ArrayList<Node> free    = new ArrayList<>(graph.getNodeSet());
        ArrayList<Node> covered = new ArrayList<>();

        for (Edge edge : M) {
            free.remove(edge.getNode0());
            free.remove(edge.getNode1());

            covered.add(edge.getNode0());
            covered.add(edge.getNode1());
        }

        ArrayList<Node> augmentingPath = new ArrayList<>();
        // Try to find augmenting paths from any free node in a random order
        Collections.shuffle(free);
        for (Node s : free) {
            ArrayList<Node> partialPath = new ArrayList<>();
            partialPath.add(s);
            augmentingPath = findAugmentingPath(graph, M, free, covered, partialPath);
        }

        if (augmentingPath == null) {
            return M;
        } else {
            String vertex1;
            String vertex2;
            Edge   edge;
            for (int i = 0; i < augmentingPath.size() - 1; i++) {
                vertex1 = augmentingPath.get(i).getId();
                vertex2 = augmentingPath.get(i+1).getId();
                edge = graph.getNode(vertex1).getEdgeBetween(vertex2);
                if (i % 2 == 0) {
                    M.add(edge);
                } else {
                    M.remove(edge);
                }
            }

            return M;
        }
    }

    /**
     * Returns an augmenting path given a certain matching M in a graph. That is: a path in the graph that starts and
     * ends in a vertex not yet covered by M and whose edges are alternatingly in M and not in M. It recursively finds
     * one by trying to extend the path given by partialPath
     * @param graph       input graph
     * @param M           matching M to augment
     * @param free        set of vertices that are not covered by {@code M}
     * @param covered     set of vertices that are covered by {@code M}
     * @param partialPath path to extend into an augmenting path
     * @return an extension of {@code partialPath} that is an augmenting path for {@code M}
     */
    private ArrayList<Node> findAugmentingPath(Graph graph, Set<Edge> M,
                                               ArrayList<Node> free, ArrayList<Node> covered,
                                               ArrayList<Node> partialPath) {
        Node lastInPath = partialPath.get(partialPath.size() - 1);

        if (partialPath.size() > 1 && free.contains(lastInPath)) {
            return partialPath;
        }

        Node next1 = null;
        if (partialPath.size() > 1) {
            for (Edge edge : lastInPath.getEachEdge()) {
                if (M.contains(edge)) {
                    next1 = edge.getOpposite(lastInPath);
                }
            }

            try {
                if (next1 == null) {
                    throw new UnexpectedException("A path ended in a node that is supposedly covered, yet no edge" +
                            "from the matching can be found.");
                } else if (partialPath.contains(next1)) {
                    throw new UnexpectedException("A path ended in a node that is covered, but the vertex at the" +
                            "other end of the edge in the matching is already in the path.");
                }
            } catch (UnexpectedException e) {
                e.printStackTrace();
            }

            partialPath.add(next1);
        } else {
            next1 = lastInPath;
        }

        Node next2 = null;
        ArrayList<Edge> edges = new ArrayList<>(next1.getEdgeSet());
        Collections.shuffle(edges);
        for (Edge edge : edges) {
            if (!M.contains(edge) && !partialPath.contains(edge.getOpposite(next1))) {
                next2 = edge.getOpposite(next1);
                partialPath.add(next2);
                if (findAugmentingPath(graph, M, free, covered, partialPath) != null) {
                    return partialPath;
                }

                // Remove next2 from the path, ready to try a new one
                partialPath.remove(partialPath.size() - 1);
            }
        }

        // The partial path that we were given does not extend to a valid augmenting path. We remove next1 from the path
        partialPath.remove(partialPath.size() - 1);
        return null;
    }

    public Graph edgeNeighborhood() {
        Graph graph = createNewSingleGraph(14);
        connectVertices(graph, 0, 2);
        connectVertices(graph, 1, 2);
        connectVertices(graph, 2, 3);
        connectVertices(graph, 3, 4);
        connectVertices(graph, 4, 5);
        connectVertices(graph, 5, 6);
        connectVertices(graph, 5, 7);
        connectVertices(graph, 4, 8);
        connectVertices(graph, 8, 9);
        connectVertices(graph, 8, 10);
        connectVertices(graph, 3, 11);
        connectVertices(graph, 11, 12);
        connectVertices(graph, 11, 13);

        return graph;
    }

    public void addRing(Graph graph) {
        connectVertices(graph, 1, 6);
        connectVertices(graph, 6, 7);
        connectVertices(graph, 7, 9);
        connectVertices(graph, 9, 10);
        connectVertices(graph, 10, 13);
        connectVertices(graph, 12, 13);
        connectVertices(graph, 0, 12);
        connectVertices(graph, 0, 1);
    }

    public Graph complement(Graph graph) {
        Graph complement = generateCompleteGraph(graph.getNodeCount());
        for (Edge edge : graph.getEachEdge()) {
            complement.removeEdge(edge.getId());
        }

        return complement;
    }
}
