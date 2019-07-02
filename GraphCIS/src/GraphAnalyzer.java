import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.rmi.UnexpectedException;
import java.util.*;

public class GraphAnalyzer {
    private static GraphAnalyzer instance;

    private GraphAnalyzer() {}

    /**
     * Returns the instance of GraphAnalyzer according to the singleton design pattern.
     * @return the instance of GraphAnalyzer
     */
    public static GraphAnalyzer getInstance() {
        if (instance == null) {
            instance = new GraphAnalyzer();
        }
        return instance;
    }

    /**
     * Method that uses {@code computeCIS(Graph, Set<Node>, int)} to compute the value of the #CIS parameter of a graph.
     * That is: the number of subsets of the vertices of {@code graph}, such that the graph induced by these vertices
     * is connected.
     * @param graph input graph
     * @return the #CIS parameter of {@code graph}
     */
    public int computeCIS(Graph graph) {
        // Simply run the method {@code computeCIS(Graph, Set<Node>, int)} with the right initial values
        Set<Node> emptySet = new HashSet<>();
        return computeCIS(graph, emptySet, 0);
    }

    /**
     * Recursive method, that can be used to compute the #CIS parameter of a graph.
     * The method recursively loops through all possible subsets of the last {@code |V|-k} vertices in {@code graph},
     * to check whether the graph induced by these vertices union the vertices already in {@code nodes} is connected.
     * The number of subsets with this property is returned.
     * To compute the #CIS parameter of a graph, one could call this method with an empty set as the second argument and
     * 0 as the third argument. For the sake of usability, this is done by the method {@code computeCIS(Graph)}.
     * @param graph input graph
     * @param nodes subset of the first {@code k} vertices to always include
     * @param k     number of vertices already considered in constructing a subset of the vertices
     * @return the number of subsets of the last {@code |V|-k} vertices in {@code graph}, such that the graph induced by
     *         this subset union the vertices in {@code nodes} is connected.
     */
    private int computeCIS(Graph graph, Set<Node> nodes, int k) {
        // If we have already considered all vertices, then return whether the found subset induces a connected graph
        if (k == graph.getNodeCount()) {
            if (isConnected(inducedGraph(graph, nodes))) {
                /*for (Node node : nodes) {
                    int size = (int) node.getAttribute("size") + 1;
                    node.setAttribute("size", size);
                    //node.addAttribute("ui.style","size: " + size + ";");
                    node.addAttribute("ui.label", (Integer) node.getAttribute("size"));
                    node.addAttribute("ui.style", "fill-color: gray;");
                }*/
                return 1;
            } else {
                return 0;
            }
        }

        int sum;
        // Number of subsets without including the k-th vertex
        sum  = computeCIS(graph, nodes, k + 1);
        // Number of subsets when including the k-th vertex
        nodes.add(graph.getNode(k));
        sum += computeCIS(graph, nodes, k+1);

        // Remove edge again for further use
        nodes.remove(graph.getNode(k));

        return sum;
    }

    public int computeCISSmart(Graph graph) {
        ArrayList<Node> yes  = new ArrayList<>();
        ArrayList<Node> no   = new ArrayList<>();
        ArrayList<Node> todo = new ArrayList<>(graph.getNodeSet());

        return computeCISSmart(yes, no, todo);
    }

    private int computeCISSmart(ArrayList<Node> yes, ArrayList<Node> no, ArrayList<Node> todo) {
        if (todo.isEmpty()) {
            return 1;
        }

        Node v = null;
        if (yes.isEmpty()) {
            v = todo.remove(0);
        } else {
            Boolean cont = true;
            int i = 0;
            Node u;
            while (cont && i < yes.size()) {
                u = yes.get(i);
                ArrayList<Edge> neighbors = new ArrayList<>(u.getEdgeSet());
                int k = 0;
                while (cont && k < neighbors.size()) {
                    v = neighbors.get(k).getOpposite(u);
                    if (todo.contains(v)) {
                        cont = false;
                        todo.remove(v);
                    }
                    k++;
                }

                i++;
                if (i == yes.size() && cont) {
                    v = null;
                }
            }
        }

        if (v == null) {
            return 1;
        } else {
            no.add(v);
            int s = computeCISSmart(yes, no, todo);
            no.remove(v);
            yes.add(v);
            s += computeCISSmart(yes, no, todo);
            yes.remove(v);
            todo.add(v);
            return s;
        }
    }

    /**
     * Prints the #CIS parameter of a graph. Both as integer representation and in the form 2^(c*n)
     * @param graph
     */
    public void printCIS(Graph graph) {
        int CIS = computeCISSmart(graph);
        double factor = Math.log(CIS) / Math.log(2) ;
        factor /= graph.getNodeCount();
        System.out.println(CIS + " = 2^(" + factor + "*n)");
    }

    /**
     * Returns the graph induced by the vertices {@code nodes} on the graph {@code graph}.
     * That is: the subgraph of {@code graph} that only includes the vertices in {@code nodes} and that only
     * includes the edges that have both endpoints in {@code nodes}.
     * @param graph original graph
     * @param nodes inducing nodes
     * @return the graph induced by the vertices {@code nodes}
     */
    public Graph inducedGraph(Graph graph, Set<Node> nodes) {
        // Create clone of original graph
        Graph inducedGraph = Graphs.clone(graph);
        // Remove all vertices that are not in {@code: nodes}
        for (Node v : graph) {
            if (!nodes.contains(v)) {
                inducedGraph.removeNode(v.getId());
            }
        }

        return inducedGraph;
    }

    /**
     * Returns whether or not a graph is connected.
     * That is: whether or not between any pair of vertices in {@code graph} there is a path.
     * @param graph input graph
     * @return whether {@code graph} is connected or not
     */
    public boolean isConnected(Graph graph) {
        // Edge case: no vertices
        if (graph.getNodeCount() == 0) {
            return true;
        }

        // Source vertex to start a breadth first search from
        Node source = graph.getNode(0);
        // Set of all the vertices that are reachable from the source
        Set<Node> nodes = new HashSet<>();

        // Perform breadth first search: in each iteration, add the visited vertex to {@code nodes}
        Iterator iterator = source.getBreadthFirstIterator();
        while (iterator.hasNext()) {
            nodes.add((Node) iterator.next());
        }

        return nodes.size() == graph.getNodeCount();
    }

    public Set<Set<Node>> getConnectedSubsets(Graph graph) {
        ArrayList<Node> yes  = new ArrayList<>();
        ArrayList<Node> no   = new ArrayList<>();
        ArrayList<Node> todo = new ArrayList<>(graph.getNodeSet());

        return getConnectedSubsets(yes, no, todo);
    }

    public Set<Set<Node>> getConnectedSubsets(ArrayList<Node> yes, ArrayList<Node> no, ArrayList<Node> todo) {
        Set<Set<Node>> sets = new HashSet<>();

        if (todo.isEmpty()) {
            sets.add(new HashSet<>(yes));
            return sets;
        }

        Node v = null;
        if (yes.isEmpty()) {
            v = todo.remove(0);
        } else {
            Boolean cont = true;
            int i = 0;
            Node u;
            while (cont && i < yes.size()) {
                u = yes.get(i);
                ArrayList<Edge> neighbors = new ArrayList<>(u.getEdgeSet());
                int k = 0;
                while (cont && k < neighbors.size()) {
                    v = neighbors.get(k).getOpposite(u);
                    if (todo.contains(v)) {
                        cont = false;
                        todo.remove(v);
                    }
                    k++;
                }

                i++;
                if (i == yes.size() && cont) {
                    v = null;
                }
            }
        }

        if (v == null) {
            sets.add(new HashSet<>(yes));
            return sets;
        } else {
            no.add(v);
            sets = getConnectedSubsets(yes, no, todo);
            no.remove(v);
            yes.add(v);
            sets.addAll(getConnectedSubsets(yes, no, todo));
            yes.remove(v);
            todo.add(v);
            return sets;
        }
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

    public Set<Edge> getRandomMatching(Graph graph, int size) {
        Set<Edge> M = new HashSet<>();
        int previousMatchingSize;
        do {
            previousMatchingSize = M.size();
            M = augmentMatching(graph, M);
        } while (M.size() > previousMatchingSize && M.size() < size);
        if (M.size() == previousMatchingSize) {
            M = null;
        }

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
}
