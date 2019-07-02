import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
}
