import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.Set;

/**
 * A gadget is a simple graph with four special vertices. These are called link nodes, because they can be used
 * to link two gadgets together. The link nodes are referred to as 'a', 'b', 'c' and 'd'. When linking two
 * gadgets together, nodes 'a' and 'b' from one gadget are linked to nodes 'c' and 'd' from the other gadget
 * respectively
 */
public class Gadget4 extends SingleGraph {
    private Node[] linkNodes;
    private int ac, ad, bc, bd, acd, bcd, abc, abd, abcd; // These variables are referred to as "path values"

    public Gadget4(Graph graph, Node[] linkNodes, boolean optimizeOnCreation) {
        super(graph.getId(), true, false);

        // Use the same nodes in this gadget as in the input graph
        for (Node v : graph.getNodeSet()) {
            this.addNode(v.getId());
        }

        // Use the same edges in this gadget as in the input graph
        for (Edge e : graph.getEdgeSet()) {
            this.addEdge(e.getId(), e.getNode0().getId(), e.getNode1().getId());
        }

        // Set the link nodes as defined by the input parameter linkNodes
        this.linkNodes = new Node[4];
        this.linkNodes[0] = this.getNode(linkNodes[0].getId());
        this.linkNodes[1] = this.getNode(linkNodes[1].getId());
        this.linkNodes[2] = this.getNode(linkNodes[2].getId());
        this.linkNodes[3] = this.getNode(linkNodes[3].getId());

        // Path values not yet computed here, so to indicate that, all of them are set to -1
        ac = ad = bc = bd = acd = bcd = abc = abd = abcd = -1;

        // (Possibly) change which link node is which to maximize the LR value
        if (optimizeOnCreation) {
            maximizeLR();
        }
    }

    /**
     * Draws the gadget, where nodes 'a' and 'b' are red and nodes 'c' and 'd' are blue. The boolean autoLayout
     * indicates whether to use the automatic layout functionalities of GraphStream.
     * @param autoLayout whether or not to use the automatic layout functionalities of GraphStream
     * @return the viewer with the gadget drawn in
     */
    @Override
    public Viewer display(boolean autoLayout) {
        linkNodes[0].addAttribute("ui.style","fill-color: red; text-size: 14; text-style: bold;");
        linkNodes[1].addAttribute("ui.style","fill-color: red; text-size: 14; text-style: bold;");
        linkNodes[2].addAttribute("ui.style","fill-color: blue; text-size: 14; text-style: bold;");
        linkNodes[3].addAttribute("ui.style","fill-color: blue; text-size: 14; text-style: bold;");

        linkNodes[0].addAttribute("ui.label", "a");
        linkNodes[1].addAttribute("ui.label", "b");
        linkNodes[2].addAttribute("ui.label", "c");
        linkNodes[3].addAttribute("ui.label", "d");
        return super.display(autoLayout);
    }

    /**
     * Draws the gadget, where nodes 'a' and 'c' are red and nodes 'b' and 'd' are blue
     * @return the viewer with the gadget drawn in
     */
    @Override
    public Viewer display() {
        return display(true);
    }

    /**
     *  Possibly rearrange which of the link nodes is which. The choice should be such that the number of connected
     *  induced subsets linking 'a' and/or 'b' to 'c' and/or 'd' is maximal.
     */
    public void maximizeLR() {
        int[] values = computePaths();

        int abcd = values[3] + values[12];
        int acbd = values[5] + values[10];
        int adbc = values[9] + values[6];
        // Choose the order such that the number of combinations that should no longer be counted is as small
        // as possible
        if (abcd < acbd && abcd < adbc) {
            // Change nothing. The current combination is already optimal
        } else if (acbd < adbc) {
            switchValues(values, 2, 4);
            switchValues(values, 3, 5);
            switchValues(values, 10,12);
            switchValues(values, 11,13);
            updateLinkNodes(linkNodes[0], linkNodes[2], linkNodes[1], linkNodes[3], values);
        } else {
            switchValues(values, 2, 8);
            switchValues(values, 3, 9);
            switchValues(values, 6, 12);
            switchValues(values, 7, 13);
            updateLinkNodes(linkNodes[0], linkNodes[3], linkNodes[2], linkNodes[1], values);
        }
    }

    /**
     * Change the link nodes of this gadget and recalculate the path values
     * @param newA the new 'a' node
     * @param newB the new 'b' node
     * @param newC the new 'c' node
     * @param newD the new 'd' node
     */
    public void updateLinkNodes(Node newA, Node newB, Node newC, Node newD) {
        linkNodes[0] = newA;
        linkNodes[1] = newB;
        linkNodes[2] = newC;
        linkNodes[3] = newD;

        updatePaths();
    }

    /**
     * Change the link nodes of this gadget and update the path values given in {@code values}
     * @param newA the new 'a' node
     * @param newB the new 'b' node
     * @param newC the new 'c' node
     * @param newD the new 'd' node
     */
    public void updateLinkNodes(Node newA, Node newB, Node newC, Node newD, int[] values) {
        linkNodes[0] = newA;
        linkNodes[1] = newB;
        linkNodes[2] = newC;
        linkNodes[3] = newD;

        updatePaths(values);
    }

    /**
     * Compute the value of a boolean string if it were reversed and interpreted as a binary number.
     * @param reverseBitString input string
     * @return the binary interpretation of the reverse of {@code reverseBitString}
     */
    private int reverseBitStringToInt(boolean[] reverseBitString) {
        int s = 0;
        for (int i = 0; i < reverseBitString.length; i++) {
            if (reverseBitString[i]) {
                s += Math.pow(2, i);
            }
        }

        return s;
    }

    /**
     * Computes the number of connected subsets connecting any combination of 'a', 'b', 'c' and 'd'.
     * @return the number of connected subset connecting any combination of 'a', 'b', 'c' and 'd'.
     *         The binary value of the index in an array indicates sequentially for 'a', 'b', 'c' and 'd' whether this
     *         node is considered for the value at this index.
     */
    public int[] computePaths() {
        Set<Set<Node>> sets = GraphAnalyzer.getInstance().getConnectedSubsets(this);
        int[]     values    = new int[16];
        boolean[] bitString = new boolean[4];

        for (Set set : sets) {
            for (int i = 0; i < 4; i ++) {
                /* The bits in the string of length 4 represent whether 'a', 'b', 'c' or 'd' respectively is present
                 * in a certain connected subset
                 */
                bitString[i] = set.contains(linkNodes[i]);
            }

            // Increment the count for how many subsets there are containing the specific combination of link nodes
            values[reverseBitStringToInt(bitString)]++;
        }

        return values;
    }

    /**
     * Re-(calculates) the path values
     */
    public void updatePaths() {
        int[] values = computePaths();
        updatePaths(values);
    }

    /**
     * Updates the path values based on already computed path values between all combinations of 'a', 'b', 'c' and 'd'.
     * @param values array of integers that stores the number of connected subsets of vertices in the gadget
     *               containing different subsets of the the link nodes. The binary value of the index in the array
     *               indicates sequentially for 'a', 'b', 'c' and 'd' whether this node is considered for the value at
     *               this index.
     */
    public void updatePaths(int[] values) {
        ac   = values[5];
        bc   = values[6];
        abc  = values[7];
        ad   = values[9];
        bd   = values[10];
        abd  = values[11];
        acd  = values[13];
        bcd  = values[14];
        abcd = values[15];
    }

    /**
     * This modifies array. It switches the values at the i-th and j-th position in {@code array}
     * @param array  array that needs two values switched
     * @param i      index of first value
     * @param j      index of second value
     */
    private void switchValues(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public Node[] getLinkNodes() {
        return linkNodes;
    }

    public Node getA() {
        return linkNodes[0];
    }

    public Node getB() {
        return linkNodes[1];
    }

    public Node getC() {
        return linkNodes[2];
    }

    public Node getD() {
        return linkNodes[3];
    }


    /**
     * This method is called when a request is made from outside this class to get any of the path values. It checks
     * whether these values have already been computed. If so, it does nothing, but otherwise it computes them.
     */
    private void pathGetterUpdater() {
        if (ac == -1) {
            updatePaths();
        }
    }

    public int getAC() {
        pathGetterUpdater();
        return ac;
    }

    public int getAD() {
        pathGetterUpdater();
        return ad;
    }

    public int getBC() {
        pathGetterUpdater();
        return bc;
    }

    public int getBD() {
        pathGetterUpdater();
        return bd;
    }

    public int getACD() {
        pathGetterUpdater();
        return acd;
    }

    public int getBCD() {
        pathGetterUpdater();
        return bcd;
    }

    public int getABC() {
        pathGetterUpdater();
        return abc;
    }

    public int getABD() {
        pathGetterUpdater();
        return abd;
    }

    public int getABCD() {
        pathGetterUpdater();
        return abcd;
    }

    /**
     * Returns the number of connected subsets of vertices that span all the way from left to right. That is: the number
     * of connected subsets that contain both 'a' and/or 'b' AND 'c' and/or 'd'.
     * @return the number of connected subsets of vertices that span from left to right in the gadget
     */
    public int getLR() {
        pathGetterUpdater();
        return ac + ad + bc + bd + acd + bcd + abc + abd + abcd;
    }

    /**
     * Returns the number of connected subsets that link 'a' and/or 'b' to 'c'.
     * @return the number of connected subsets that link 'a' and/or 'b' to 'c'.
     */
    public int getLc() {
        pathGetterUpdater();
        return ac + bc + abc;
    }

    /**
     * Returns the number of connected subsets that link 'a' and/or 'b' to 'c'.
     * @return the number of connected subsets that link 'a' and/or 'b' to 'c'.
     */
    public int getLd() {
        pathGetterUpdater();
        return ad + bd + abd;
    }

    /**
     * Returns the number of connected subsets that link 'a' and/or 'b' to 'c'.
     * @return the number of connected subsets that link 'a' and/or 'b' to 'c'.
     */
    public int getLcd() {
        pathGetterUpdater();
        return acd + bcd + abcd;
    }

    /**
     * Returns the path values for all combinations of 'a', 'b', 'c' and 'd' that span from left to right.
     * Note: only the connected subsets that span from left to right. This excludes:
     * ab and cd, the singletons a, b, c and d and the empty set.
     * @return the path values for all combinations of 'a', 'b', 'c' and 'd' that span from left to right.
     *         The binary value of the index in the array indicates sequentially for 'a', 'b', 'c' and 'd' whether this
     *         node is considered for the value at this index. Any combination that does not correspond to a combination
     *         spanning from left to right is paired with a value of -1.
     */
    public int[] getPathValues() {
        pathGetterUpdater();

        int[] values = new int[16];
        values[0] = values[1] = values[2] = values[3] = values[4] = values[8] = values[12] = -1;

        values[5]  = ac;
        values[6]  = bc;
        values[7]  = abc;
        values[9]  = ad;
        values[10] = bd;
        values[11] = abd;
        values[13] = acd;
        values[14] = bcd;
        values[15] = abcd;

        return values;
    }

    public RealMatrix getRecursionMatrix() {
        double[][] data = new double[3][3];

        data[0][0] = getLc() - getBC();
        data[0][1] = getLc() - getAC();
        data[0][2] = getLc();

        data[1][0] = getLd() - getBD();
        data[1][1] = getLd() - getAD();
        data[1][2] = getLd();

        data[2][0] = getLcd() - getBCD();
        data[2][1] = getLcd() - getACD();
        data[2][2] = getLcd();

        RealMatrix matrix = MatrixUtils.createRealMatrix(data);

        return matrix;
    }

    public double getMaxRealEigenvalue() {
        RealMatrix matrix = getRecursionMatrix();
        EigenDecomposition eigenDecomposition = new EigenDecomposition(matrix);

        double max;
        if (eigenDecomposition.hasComplexEigenvalues()) {
            max = Double.MIN_VALUE;
        } else {
            max = max(eigenDecomposition.getRealEigenvalues());
        }

        return max;
    }

    public double[] getLowerBoundFactors() {
        double factors[] = new double[3];

        RealMatrix orig = getRecursionMatrix();
        EigenDecomposition eigenDecomposition = new EigenDecomposition(orig);

        RealMatrix A = eigenDecomposition.getV();
        RealMatrix L = eigenDecomposition.getD();
        RealMatrix B = MatrixUtils.inverse(A);
        double[][] a = A.getData();
        double[][] l = L.getData();
        double[][] b = B.getData();

        double[] c = new double[3];
        c[0] = getLc();
        c[1] = getLd();
        c[2] = getLcd();

        RealVector C = MatrixUtils.createRealVector(c);

        factors[0] = (a[0][0] + a[1][0] + a[2][0]) * (c[0]*b[0][0] + c[1]*b[0][1] + c[2]*b[0][2]);
        factors[1] = (a[0][1] + a[1][1] + a[2][1]) * (c[0]*b[1][0] + c[1]*b[1][1] + c[2]*b[1][2]);
        factors[2] = (a[0][2] + a[1][2] + a[2][2]) * (c[0]*b[2][0] + c[1]*b[2][1] + c[2]*b[2][2]);

        /*System.out.println(A + " * " );
        System.out.println(L + " * " );
        System.out.println(B + " = " );
        System.out.println(A.multiply(L.multiply(B)));*/

        return factors;
    }

    private double max(double[] values) {
        double max = Double.MIN_VALUE;
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    private int subsetToInt(ArrayList<Node> set) {
        boolean[] bitString = new boolean[4];
        for (int i = 0; i < 4; i ++) {
            /* The bits in the string of length 4 represent whether 'a', 'b', 'c' or 'd' respectively is present
             * in a certain connected subset
             */
            bitString[i] = set.contains(linkNodes[i]);
        }
        return reverseBitStringToInt(bitString);
    }
}
