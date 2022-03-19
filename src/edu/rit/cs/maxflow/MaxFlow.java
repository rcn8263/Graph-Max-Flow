package edu.rit.cs.maxflow;

import edu.rit.cs.labgraph.Edge;
import edu.rit.cs.labgraph.GraphException;
import edu.rit.cs.labgraph.FlowGraph;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * A program that reads in a graph and computes the maximum flow possible
 * from the designated source node to the designated sink node
 *
 * @author RIT CS
 * @author Ryan Nowak
 */
public class MaxFlow {

    /**
     * Provided for strings that have new-line characters in them
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * The default name of the source node if not given on the command line
     */
    public static final String DEFAULT_SOURCE = "source";

    /**
     * The default name of the sink node if not given on the command line
     */
    public static final String DEFAULT_SINK = "sink";

    /**
     * Process the command line, create a FlowGraph, and perform the
     * Max Flow algorithm on it.
     * Details of output format are shown in the lab document.
     * @param args [0] graph file name; [1],[2] source and sink names (optional)
     */
    public static void main( String[] args ) {
        try {
            FlowGraph graph = readFlowGraph( args );

            System.out.println( "Initial Graph:" + NEW_LINE );
            graph.show( true );
            System.out.println();


            // Uncomment these lines after you have finished the in-lab part.
            final long maxFlow = maxFlow( graph );
            System.out.println( "Max flow is " + maxFlow );

            System.out.println( NEW_LINE + "Solution:" + NEW_LINE );
            graph.show( false );

        }
        catch( IOException ioe ) {
            System.err.println( "File IO Problem" );
            System.out.println( ioe.getMessage() );
        }
        catch( GraphException ge ) {
            System.err.println( ge.getMessage() );
        }
    }

    /**
     * Perform the Max Flow algorithm on the given graph. The algorithm is
     * described elsewhere. The graph's edges' flows are modified by this method.
     * @param graph The graph used in the computation of max flow.
     * @return The amount of flow that can be sent from the graph's source
     * to its' sink.
     */
    private static long maxFlow(FlowGraph graph) {
        Optional<List<String>> path = graph.doBFS();
        long maxFlow = 0;
        while (path.isPresent()) {
            //Compute most additional traffic tmax that can be added
            System.out.print("Trying " + String.join("->", path.get()) + "; ");
            long tmax = graph.getAvailableFlow(path.get().get(0), path.get().get(1));
            for (int i = 1; i < path.get().size()-1; i++) {
                String in = path.get().get(i);
                String out = path.get().get(i+1);
                if (graph.getEdge(in, out).availableFlow(in, out) < tmax) {
                    tmax = graph.getEdge(in, out).availableFlow(in, out);
                }
            }
            //for each edge, add tmax to the edge
            for (int i = 0; i < path.get().size()-1; i++) {
                String in = path.get().get(i);
                String out = path.get().get(i+1);
                graph.getEdge(in, out).changeFlow(in, out, tmax);
            }
            System.out.println("adding " + tmax + " to path.");
            maxFlow += tmax;
            //Find shortest path using BFS
            path = graph.doBFS();
        }

        //Get max flow
        /*long maxFlow = 0;
        for (Edge edge: graph.getEdgesAt(graph.getSink())) {
            maxFlow += edge.getFlow(graph.getSink(),
                    edge.getOtherEnd(graph.getSink()));
        }*/
        return maxFlow;
    }

    /**
     * Build a FlowGraph object from the command line arguments and
     * the named edge-list file.
     * @param args [0] graph file name; [1],[2] source and sink names (optional)
     * @return a fully built FlowGraph object
     * @throws GraphException if there is a problem with the command line
     *                         values or the contents of the file
     * @throws IOException if there is a problem opening or reading the file
     */
    private static FlowGraph readFlowGraph( String[] args ) throws
            GraphException, IOException {
        boolean nonStdEndpoints = args.length == 3;
        if ( !( args.length == 1 || nonStdEndpoints ) ) {
            System.err.println(
                    "Usage: java MaxFlow graph-file source sink"
            );
            System.exit( 1 );
        }

        // Read the source and sink nodes from the command line.
        String source = nonStdEndpoints ? args[ 1 ] : DEFAULT_SOURCE;
        String sink = nonStdEndpoints ? args[ 2 ] : DEFAULT_SINK;
        if ( source.equals( sink ) ) {
            throw new GraphException(
                    "Source and sink are the same: " + sink );
        }

        // Read the graph into a variable called graph.
        return new FlowGraph( args[ 0 ], source, sink );
    }

}
