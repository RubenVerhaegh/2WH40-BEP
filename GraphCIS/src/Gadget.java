import jeigen.DenseMatrix;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Gadget extends SingleGraph {
    private Node[] linkNodes;
    private int[]  pathValues;
    private int CISp = -1;

    public Gadget(Graph graph, Node[] linkNodes) {
        super(graph.getId(), true, false);

        // Use the same nodes in this gadget as in the input graph
        for (Node v : graph.getNodeSet()) {
            this.addNode(v.getId());
            this.getNode(v.getId()).addAttribute("xy", v.getAttribute("xy"));
        }

        // Use the same edges in this gadget as in the input graph
        for (Edge e : graph.getEdgeSet()) {
            this.addEdge(e.getId(), e.getNode0().getId(), e.getNode1().getId());
        }

        // Store the link nodes in an ArrayList and shuffle it
        ArrayList<Node> linkNodesArray = new ArrayList<>(Arrays.asList(linkNodes));
        Collections.shuffle(linkNodesArray);

        // Copy the link nodes from the random order
        this.linkNodes = new Node[linkNodes.length];
        for (int i = 0; i < linkNodes.length; i++) {
            this.linkNodes[i] = this.getNode(linkNodesArray.get(i).getId());
        }

        // Initialize path values to all be -1
        pathValues = new int[(int) Math.pow(2, linkNodes.length)];
        for (int i = 0; i < pathValues.length; i++) {
            pathValues[i] = -1;
        }
    }

    @Override
    public Viewer display(boolean autoLayout) {
        Node ui;
        Node vi;
        for (int i = 0; i < linkNodes.length / 2; i++) {
            ui = linkNodes[i];
            vi = linkNodes[linkNodes.length / 2 + i];

            /*ui.addAttribute("ui.style","fill-color: red;  text-size: 14; text-style: bold;");
            vi.addAttribute("ui.style","fill-color: blue; text-size: 14; text-style: bold;");*/

            ui.addAttribute("ui.style","fill-color: rgb(110,110,110);  text-size: 14; text-style: bold;");
            vi.addAttribute("ui.style","fill-color: rgb(110,110,110); text-size: 14; text-style: bold;");

            ui.addAttribute("ui.label", "u" + i);
            vi.addAttribute("ui.label", "v" + i);
        }

        return super.display(autoLayout);
    }

    @Override
    public Viewer display() {
        return display(true);
    }

    public int[] computePathValues() {
        for (int i = 0; i < pathValues.length; i++) {
            pathValues[i] = 0;
        }
        ArrayList<Node> yes  = new ArrayList<>();
        ArrayList<Node> no   = new ArrayList<>();
        ArrayList<Node> todo = new ArrayList<>(this.getNodeSet());
        boolean[] bitstring  = new boolean[linkNodes.length];

        computePathValues(yes, no, todo, bitstring);

        return pathValues;
    }

    private void computePathValues(ArrayList<Node> yes, ArrayList<Node> no, ArrayList<Node> todo, boolean[] bitstring) {
        if (todo.isEmpty()) {
            pathValues[reverseBitStringToInt(bitstring)]++;
            return;
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
            pathValues[reverseBitStringToInt(bitstring)]++;
        } else {
            no.add(v);
            computePathValues(yes, no, todo, bitstring);
            no.remove(v);

            yes.add(v);
            int index = -1;
            for (int i = 0; i < linkNodes.length; i++) {
                if (linkNodes[i] == v) {
                    index = i;
                }
            }
            if (index > -1) bitstring[index] = true;

            computePathValues(yes, no, todo, bitstring);
            yes.remove(v);
            todo.add(v);
            if(index > -1) bitstring[index] = false;
        }
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

    private boolean[] intToReverseBitstring(int number, int size) {
        if (number >= Math.pow(2, size)) {
            throw new IllegalArgumentException("Number (" + number + ") too large to store in bitstring of size " + size);
        }

        boolean[] bitString = new boolean[size];

        for (int i = size - 1; i >= 0; i--) {
            int k = (int) Math.pow(2, i);
            if (number >= k) {
                bitString[i] = true;
                number -= k;
            } else {
                bitString[i] = false;
            }
        }

        return bitString;
    }

    private int[] getPathValues() {
        if (pathValues[0] == -1) {
            pathValues = computePathValues();
        }

        return pathValues;
    }

    public DenseMatrix getRecursionMatrix() {
        getPathValues();
        int n = (int) Math.pow(2, linkNodes.length / 2);
        double[][] data = new double[n-1][n-1];
        for (int i = 1; i < n; i++) {
            boolean[] rightPart = intToReverseBitstring(i, linkNodes.length / 2);
            int mainTerm = leftToBitstring(rightPart);

            for (int j = 1; j < n; j++) {
                data[i-1][j-1] = mainTerm;
                data[i-1][j-1] -= excludableSubsets(intToReverseBitstring(j, linkNodes.length / 2), rightPart);
            }
        }

        return new DenseMatrix(data);

        /*double[][] data = new double[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                data[i][j] = 10000*i - 1000*j;
            }
        }

        return MatrixUtils.createRealMatrix(data);*/
    }

    public double getMaxEigenvalue() {
        DenseMatrix matrix = getRecursionMatrix();
        DenseMatrix.EigenResult eigenResult = matrix.eig();

        double max = -1;
        for (int i = 0; i < eigenResult.values.real().rows; i++) {
            double real = eigenResult.values.getReal(i, 0);
            double imag = eigenResult.values.getImag(i, 0);
            double abs = Math.sqrt(Math.pow(real, 2) + Math.pow(imag, 2));

            if (abs > max) {
                max = abs;
            }
        }

        if (Math.pow(max, 1.0 / this.nodeCount) > 2) {
            max = -1;
        }

        return max;
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

    private int leftToBitstring(boolean[] bitstring) {
        getPathValues();
        int n = linkNodes.length / 2;
        if (bitstring.length != n) {
            throw new IllegalArgumentException("Method leftToBitstring() called with input of wrong size. Expected: " +
                    "boolean array of size " + n + ". Actually got: boolean array of size " + bitstring.length);
        }

        int value = 0;
        for (int i = 0; i < Math.pow(2, n); i++) {
            boolean[] leftPart = intToReverseBitstring(i, n);
            boolean[] subset = new boolean[2 * n];

            System.arraycopy(leftPart, 0, subset, 0, n);
            System.arraycopy(bitstring, 0, subset, n, n);

            value += pathValues[reverseBitStringToInt(subset)];
        }

        return value;
    }

    private int excludableSubsets(boolean[] prevExitPoints, boolean[] finalExitPoints) {
        int n = linkNodes.length / 2;
        if (prevExitPoints.length != n) {
            throw new IllegalArgumentException("Method leftToBitstring() called with input of wrong size. Expected: " +
                    "boolean array of size " + n + ". Actually got: boolean array of size " + prevExitPoints.length +
                    ". (Concerns prevExitPoints).");
        }
        if (finalExitPoints.length != n) {
            throw new IllegalArgumentException("Method leftToBitstring() called with input of wrong size. Expected: " +
                    "boolean array of size " + n + ". Actually got: boolean array of size " + finalExitPoints.length +
                    ". (Concerns finalExitPoints).");
        }

        boolean[] soFar = new boolean[2 * n];
        System.arraycopy(finalExitPoints, 0, soFar, n, n);

        return excludableSubsets(prevExitPoints, soFar, 0);
    }

    private int excludableSubsets(boolean[] prevExitPoints, boolean[] soFar, int i) {
        /*for (int j = 0; j < i; j ++) {
            System.out.print(" ");
        }
        System.out.println("i = " + i);
        for (int j = 0; j < i; j ++) {
            System.out.print(" ");
        }
        System.out.println("prevExitPoints: " + Arrays.toString(prevExitPoints));
        for (int j = 0; j < i; j ++) {
            System.out.print(" ");
        }
        System.out.println("soFar:          " + Arrays.toString(soFar));*/

        if (i == linkNodes.length / 2) {
            if (isFalse(soFar)) {
                return 0;
            } else {
                return getPathValues()[reverseBitStringToInt(soFar)];
            }
        }

        if (prevExitPoints[i]) {
            soFar[i] = false;
            return excludableSubsets(prevExitPoints, soFar, i + 1);
        } else {
            soFar[i] = false;
            int s = excludableSubsets(prevExitPoints, soFar, i + 1);
            soFar[i] = true;
            s += excludableSubsets(prevExitPoints, soFar, i + 1);


            return s;
        }
    }

    private boolean isFalse(boolean[] array) {
        for (boolean b : array) {
            if (b) {
                return false;
            }
        }

        return true;
    }

    public String getAdjacencyMatrixString() {
        int n = this.getNodeCount();
        byte[][] adjacencyMatrix = new byte[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                adjacencyMatrix[i][j] = (byte) (this.getNode(i).hasEdgeBetween(j) ? 1 : 0);
            }
        }

        return Arrays.deepToString(adjacencyMatrix);
    }

}