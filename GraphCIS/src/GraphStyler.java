import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphStyler {
    private static GraphStyler instance;

    private GraphStyler() {}

    /**
     * Returns the instance of GraphGenerator according to the singleton design pattern.
     * @return the instance of GraphGenerator
     */
    public static GraphStyler getInstance() {
        if (instance == null) {
            instance = new GraphStyler();
        }
        return instance;
    }

    private void showIds(Graph g) {
        for (Node node : g.getNodeSet()) {
            node.setAttribute("label", node.getId());
        }
    }

    public void applyStandardStyle(Graph graph, boolean showIds) {
        String styleSheet =
                "node { " +
                        "text-style: bold;" +
                        "text-size: 40;" +
                        "size: 40;" +
                        "}" +
                        "edge { " +
                        "size: 10;" +
                        "}";

        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.addAttribute("ui.antialias");

        if (showIds) {
            showIds(graph);
        }
    }
}
