import jeigen.DenseMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.graphstream.graph.Graph;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        (new Main()).run();
    }

    private final GraphGenerator generator = GraphGenerator.getInstance();
    private final GraphCombiner  combiner  = GraphCombiner.getInstance();
    private final GraphAnalyzer  analyzer  = GraphAnalyzer.getInstance();

    private void run() {
        generateGadgets(10, 10, 3, 3, 3, 3, 100, 50);
    }
    private void generateGadgets(int minsize, int maxsize, int mindegree, int maxdegree, int minlinks, int maxlinks,
                                  int iterations, int notifyInterval) {

        int nrDegrees = maxdegree - mindegree + 1;
        int nrSizes = maxsize - minsize + 1;
        int nrLinks = maxlinks - minlinks + 1;
        double[][][] stats = new double[nrDegrees][nrSizes][nrLinks];
        String[][][] graphs = new String[nrDegrees][nrSizes][nrLinks];
        try {
            String degs;
            if (mindegree == maxdegree) {
                degs = String.valueOf(mindegree);
            } else {
                degs = mindegree + "-" + maxdegree;
            }
            String sizes;
            if (minsize == maxsize) {
                sizes = String.valueOf(minsize);
            } else {
                sizes = minsize + "-" + maxsize;
            }
            String links;
            if (minlinks == maxlinks) {
                links = String.valueOf(minlinks);
            } else {
                links = minlinks + "-" + maxlinks;
            }
            String filenamePrefix = "D" + degs + "_N" + sizes + "_L" + links + "_I" + iterations;
            PrintWriter valueWriter = new PrintWriter(filenamePrefix + "_values" + ".csv", "UTF-8");
            PrintWriter graphWriter = new PrintWriter(filenamePrefix + "_graphs" + ".csv", "UTF-8");
            for (int d = mindegree; d <= maxdegree; d++) {

                StringBuilder header = new StringBuilder();
                for (int l = minlinks; l <= maxlinks; l += 1) {
                    header.append(";").append(l);
                }
                valueWriter.println(header);
                graphWriter.println(header);

                for (int n = minsize; n <= maxsize; n += 2) {
                    valueWriter.print(n + ";");
                    graphWriter.print(n + ";");
                    for (int l = minlinks; l <= maxlinks; l += 1) {
                        if (2 * (l + 1) <= n) {
                            Gadget bestGadget = null;
                            double bestValue = -2;
                            for (int i = 0; i < iterations; i++) {
                                if (i < iterations - 1 && (i + 1) % notifyInterval == 0) {
                                    System.out.println(i + 1);
                                }

                                Gadget gadget;
                                do {
                                    gadget = generator.generateRandomCycleGadget(n, d, l);
                                } while (!analyzer.isConnected(gadget));
                                double value = gadget.getMaxEigenvalue();

                                if (value > bestValue) {
                                    bestValue = value;
                                    bestGadget = gadget;
                                }
                        /*System.out.println(Arrays.deepToString(gadget.getRecursionMatrix().getData()));
                        gadget.display();*/
                            }

                            DenseMatrix orig = bestGadget.getRecursionMatrix();
                            double lowerBound = Math.pow(bestValue, 1.0 / n);

                            System.out.println("n = " + n + ", d = " + d + ", l = " + l);
                            System.out.println("    #CIS = O(" + lowerBound + "^n)");
                            //System.out.println("    Recursion Matrix:" + Arrays.deepToString(orig));
                            System.out.println("    Max. eigenvalue: " + bestValue);
                            System.out.println();


                            stats[d - mindegree][n - minsize][l - minlinks] = lowerBound;
                            graphs[d - mindegree][n - minsize][l - minlinks] = bestGadget.getAdjacencyMatrixString();
                            GraphStyler.getInstance().applyStandardStyle(bestGadget, false);
                            bestGadget.display(false);

                            valueWriter.print(lowerBound + ";");
                            graphWriter.print(bestGadget.getAdjacencyMatrixString() + ";");
                        } else {
                            stats[d - mindegree][n - minsize][l - minlinks] = -1;
                            graphs[d - mindegree][n - minsize][l - minlinks] = "";
                        }
                    }
                    valueWriter.println();
                    graphWriter.println();
                }
            }

            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println("HERE ARE ALL THE STATS (d, n, l):");
            System.out.println(Arrays.deepToString(stats));
            System.out.println();
            System.out.println("AND HERE ARE ALL THE GRAPHS (d, n, l)");
            System.out.println(Arrays.deepToString(graphs));

            valueWriter.close();
            graphWriter.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void findGoodAndBadCycles(int n, int d, int iterations, int notifyInterval) {
        Graph bestGraph = null;
        Graph worstGraph = null;
        int best  = 0;
        int worst = Integer.MAX_VALUE;
        for (int i = 0; i < iterations; i++) {
            if (i < iterations - 1 && (i + 1) % notifyInterval == 0) {
                System.out.println(i + 1);
            }

            Graph graph = generator.generateRandomLinkedCycle(n, d);
            int CIS = analyzer.computeCISSmart(graph);

            if (CIS > best) {
                best = CIS;
                bestGraph = graph;
            }

            if (CIS < worst) {
                worst = CIS;
                worstGraph = graph;
            }
        }

        System.out.println("BEST:  " + best);
        System.out.println("WORST: " + worst);

        GraphStyler.getInstance().applyStandardStyle(bestGraph, false);
        GraphStyler.getInstance().applyStandardStyle(worstGraph, false);

        bestGraph.display(false);
        worstGraph.display(false);
    }

}