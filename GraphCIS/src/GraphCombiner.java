import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class GraphCombiner {
    private static GraphCombiner instance;

    private GraphCombiner() {}

    /**
     * Returns the instance of GraphCombiner according to the singleton design pattern.
     * @return the instance of GraphCombiner
     */
    public static GraphCombiner getInstance() {
        if (instance == null) {
            instance = new GraphCombiner();
        }
        return instance;
    }

    public Gadget4 linkGadgets4(Gadget4 gadget1, Gadget4 gadget2) {
        Graph graph = combineGraphs(gadget1, gadget2);

        graph.addEdge("1." + gadget1.getC().getId() + "-2." + gadget2.getA().getId(),
                "1." + gadget1.getC().getId(), "2." + gadget2.getA().getId());
        graph.addEdge("1." + gadget1.getD().getId() + "-2." + gadget2.getB().getId(),
                "1." + gadget1.getD().getId(), "2." + gadget2.getB().getId());

        Node[] linkNodes = new Node[4];
        linkNodes[0] = graph.getNode("1." + gadget1.getA());
        linkNodes[1] = graph.getNode("1." + gadget1.getB());
        linkNodes[2] = graph.getNode("2." + gadget2.getC());
        linkNodes[3] = graph.getNode("2." + gadget2.getD());

        Gadget4 gadget = new Gadget4(graph, linkNodes, false);

        return gadget;
    }

    public Gadget4 makeGadget4Link(Gadget4 gadget, int repeats) {
        Gadget4 link = gadget;
        for (int i = 1; i < repeats; i++) {
            link = linkGadgets4(link, gadget);
        }

        return link;
    }

    private Graph combineGraphs(Graph graph1, Graph graph2) {
        Graph graph = new SingleGraph("combined");

        for (Node v : graph1.getNodeSet()) {
            graph.addNode("1." + v.getId());
        }
        for (Edge e : graph1.getEdgeSet()) {
            graph.addEdge("1." + e.getId(), "1." + e.getNode0(), "1." + e.getNode1());
        }

        for (Node v : graph2.getNodeSet()) {
            graph.addNode("2." + v.getId());
        }
        for (Edge e : graph2.getEdgeSet()) {
            graph.addEdge("2." + e.getId(), "2." + e.getNode0(), "2." + e.getNode1());
        }

        return graph;
    }

    private Gadget1 linkGadgets1(Gadget1 gadget1, Gadget1 gadget2) {
        Graph graph = combineGraphs(gadget1, gadget2);
        String newNodeName = gadget1.getLinkNode().getId() + "," + gadget2.getLinkNode().getId();

        graph.addNode(newNodeName);
        graph.addEdge("1." + gadget1.getLinkNode().getId() + "-" + newNodeName,
                "1." + gadget1.getLinkNode().getId(), newNodeName);
        graph.addEdge("2." + gadget2.getLinkNode().getId() + "-" + newNodeName,
                "2." + gadget2.getLinkNode().getId(), newNodeName);


        Gadget1 gadget = new Gadget1(graph, graph.getNode(newNodeName));

        return gadget;
    }

    public Gadget1 makeGadget1Link(Gadget1 gadget, int repeats) {
        if (repeats > 0 && ((repeats & (repeats - 1)) == 0)) {
            throw new IllegalArgumentException("You can only repeat a Gadget1 by a power of 2 times.");
        }

        Gadget1 link = gadget;
        for (int i = 1; i < repeats; i *= 2) {
            link = linkGadgets1(link, link);
        }

        return link;
    }
}
