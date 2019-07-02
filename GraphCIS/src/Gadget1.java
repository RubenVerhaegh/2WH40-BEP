import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.Set;

public class Gadget1 extends SingleGraph{
    private Node linkNode;
    private int CISp = -1;

    public Gadget1(Graph graph, Node linkNode) {
        super(graph.getId(), true, false);

        // Use the same nodes in this gadget as in the input graph
        for (Node v : graph.getNodeSet()) {
            this.addNode(v.getId());
        }

        // Use the same edges in this gadget as in the input graph
        for (Edge e : graph.getEdgeSet()) {
            this.addEdge(e.getId(), e.getNode0().getId(), e.getNode1().getId());
        }

        // Set the link node as defined by the input parameter linkNode
        this.linkNode = this.getNode(linkNode.getId());
    }

    /**
     * Draws the gadget, where the link node is colored red.
     * The boolean autoLayout indicates whether to use the automatic layout functionalities of GraphStream.
     * @param autoLayout whether or not to use the automatic layout functionalities of GraphStream
     * @return the viewer with the gadget drawn in
     */
    @Override
    public Viewer display(boolean autoLayout) {
        linkNode.addAttribute("ui.style","fill-color: red;");

        return super.display(autoLayout);
    }

    /**
     * Draws the gadget, where the link node is colored red.
     * @return the viewer with the gadget drawn in
     */
    @Override
    public Viewer display() {
        return display(true);
    }

    public Node getLinkNode() {
        return linkNode;
    }

    public int getCISp() {
        if (CISp == -1) {
            computeCISp();
        }

        return CISp;
    }

    private void computeCISp() {
        CISp = 0;
        Set<Set<Node>> sets = GraphAnalyzer.getInstance().getConnectedSubsets(this);

        for (Set<Node> set : sets) {
            if (set.contains(linkNode)) {
                CISp++;
            }
        }
    }
}
