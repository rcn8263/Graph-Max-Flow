package edu.rit.cs.labgraph;

import java.io.*;
import java.security.KeyStoreException;
import java.util.*;

/**
 * A Graph representation where nodes are just strings that contain no data,
 * and edges are named by their node endpoints, their flow, and their flow
 * capacity.
 * This graph class was custom-designed for the Max Flow Problem.
 *
 * @author RIT CS
 * @author Ryan Nowak
 */
public class FlowGraph {

    /**
     * The starting node for the {@link #doBFS()} method
     */
    private final String source;

    /**
     * The destination node for the {@link #doBFS()} method
     */
    private final String sink;

    /**
     * The internal graph data structure: a map where each node name
     * is associated with the set of edges connected to it
     */
    private final LinkedHashMap< String, LinkedHashSet< Edge > > adjList;

    /**
     * <em>This constant is only used for testing.</em>
     */
    public static final long CAP = 4L;

    /**
     * Build a fixed graph for testing.
     * <em>This method is here only for testing.</em>
     * <pre>
     *        B
     *      / | \
     *     A  |  D
     *      \ | /
     *        C
     * </pre>
     */
    public FlowGraph() {
        final String A = "A";
        final String B = "B";
        final String C = "C";
        final String D = "D";

        Edge eAB = new Edge( A, B, CAP );
        Edge eAC = new Edge( A, C, CAP );
        Edge eBC = new Edge( B, C, CAP );
        Edge eDB = new Edge( D, B, CAP );
        Edge eDC = new Edge( D, C, CAP );

        this.source = A;
        this.sink = D;
        this.adjList = new LinkedHashMap<>();

        for ( String node: List.of( A, B, C, D ) ) {
            this.adjList.put( node, new LinkedHashSet<>() );
        }
        this.adjList.get( A ).add( eAB );
        this.adjList.get( B ).add( eAB );
        this.adjList.get( A ).add( eAC );
        this.adjList.get( C ).add( eAC );
        this.adjList.get( B ).add( eBC );
        this.adjList.get( C ).add( eBC );
        this.adjList.get( D ).add( eDB );
        this.adjList.get( B ).add( eDB );
        this.adjList.get( D ).add( eDC );
        this.adjList.get( C ).add( eDC );
    }

    /**
     * Build a graph from the edge list in a file. Each line in the file
     * contains two nodes and a maximum flow capacity.
     * A BufferedReader is used to read the file contents. Note that each
     * line specifies one edge. But each edge will get attached to the lists
     * of two nodes, without modification.
     * @param graphFileName The name of the edge list file.
     * @param source The source of the flow for the maxflow problem.
     * @param sink The sink of the flow for the maxflow problem.
     * @throws IOException If there is a problem reading the input file
     * @throws GraphException If the data in the file is not correctly formatted
     * or if either the source or sink are not in the final graph.
     */
    public FlowGraph(String graphFileName, String source, String sink)
            throws IOException, GraphException {
        this.source = source;
        this.sink = sink;
        this.adjList = new LinkedHashMap<>();

        try (BufferedReader in =
                     new BufferedReader(new FileReader(graphFileName))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] arrLine = line.split(" ");
                if (!this.adjList.containsKey(arrLine[0])) {
                    this.adjList.put(arrLine[0], new LinkedHashSet<>());
                }
                if (!this.adjList.containsKey(arrLine[1])) {
                    this.adjList.put(arrLine[1], new LinkedHashSet<>());
                }
                this.adjList.get(arrLine[0]).add(new Edge(
                        arrLine[0], arrLine[1], Long.parseLong(arrLine[2])));
                this.adjList.get(arrLine[1]).add(new Edge(
                        arrLine[0], arrLine[1], Long.parseLong(arrLine[2])));
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }

    /**
     *Find the shortest path from source to sink, taking into account the
     * available flow on each edge considered. This means that edges whose
     * flows are maxed out in the direction being considered will not be used
     * for the solution path. A breadth-first-search algorithm is used, with
     * no guarantee on order of consideration of edges.
     * @return An Optional containing the list of nodes for the path, from
     * source to sink, or Optional.empty() if no path exists
     */
    public Optional<List<String>> doBFS() {
        //Make queue of nodes to go through
        List<String> queue = new LinkedList<>();
        queue.add(this.source);

        //Make map of predecessors for already visited nodes
        Map<String, String> predecessors = new HashMap<>();
        predecessors.put(this.source, this.source);

        //Construct predecessors map
        while (!queue.isEmpty()) {
            String current = queue.remove(0);
            if (current == this.sink) {
                break;
            }
            for (Edge edge: this.adjList.get(current)) {
                String neighbor = edge.getOtherEnd(current);
                if (!predecessors.containsKey(neighbor) &&
                        edge.getFlow(current, neighbor) <
                                edge.getCapacity()) {
                    predecessors.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        //Construct Path
        List<String> path = new LinkedList<>();
        if (predecessors.containsKey(this.sink)) {
            String current = this.sink;
            while (current != this.source) {
                path.add(0, current);
                current = predecessors.get(current);
            }
            path.add(0, this.source);
        }

        if (path.isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(path);
        }
    }

    /**
     * @return The name of the starting node for this graph.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * @return The name of the ending node for this graph.
     */
    public String getSink() {
        return this.sink;
    }

    /**
     * What edges are connected to this node?
     *
     * @param node the node's name
     * @return A Set of {@link Edge} objects whose in or out node equals
     *         the parameter node
     * @rit.pre node is not null and is in the graph.
     */
    public Set< Edge > getEdgesAt( String node ) {
        return this.adjList.get( node );
    }

    /**
     * Get an edge in this graph
     * @param a The name of one of the edge's nodes
     * @param b The name of one of the edge's nodes
     * @return The Edge object containing the two given nodes
     */
    public Edge getEdge(String a, String b) {
        for (Edge edge: this.adjList.get(a)) {
            if (edge.getOtherEnd(a).equals(b)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Determine how much remaining flow is available from one node to another
     * in this graph. The calculation is capacity minus current flow if flow
     * is in the same direction as previous->current or capacity plus current
     * flow if flow is in the opposite direction.
     * @param previous The name of the node that would inject more flow into the edge
     * @param current The name of the node that would pull more flow out of the edge
     * @return The amount of flow as described above
     */
    public long getAvailableFlow(String previous, String current) {
        for (Edge edge: this.adjList.get(previous)) {
            if (edge.getOtherEnd(previous).equals(current)) {
                return edge.getCapacity() - edge.getFlow(previous, current);
            }
        }
        return 0;
    }

    /**
     *Print, on standard output, a representation of this graph. The format on each
     * line is a single node, followed by a comma-separated list of edges.
     * @param showMax If true, include each edge's flow and capacity;
     *                if false, just include each edge's flow.
     */
    public void show(boolean showMax) {
        for (String key: this.adjList.keySet()) {
            System.out.print(key + ": ");
            System.out.println(String.join(", ",
                    this.adjList.get(key).toString()));
        }
    }
}
