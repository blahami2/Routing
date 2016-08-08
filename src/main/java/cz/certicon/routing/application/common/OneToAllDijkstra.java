/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.common;

import cz.certicon.routing.application.NodeDataStructure;
import cz.certicon.routing.application.datastructures.JgraphtFibonacciDataStructure;
import gnu.trove.iterator.TIntIterator;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OneToAllDijkstra {

    /**
     * Executes routing
     *
     * @param graph topology
     * @param source source node
     * @return distances and predecessors for all the reached nodes
     */
    public DataContainer execute( OneToAllGraph graph, int source ) {
        return execute( graph, new MapDataContainer(), source );
    }

    /**
     * Executes routing
     *
     * @param graph topology
     * @param dataContainer prepared data container
     * @param source source node
     * @return distances and predecessors for all the reached nodes
     */
    public DataContainer execute( OneToAllGraph graph, DataContainer dataContainer, int source ) {
//        DataContainer dataContainer = new ArrayDataContainer( graph.getNodeCount() );
        dataContainer.setDistance( source, 0 );

        NodeDataStructure<Integer> queue = new JgraphtFibonacciDataStructure<>();
        queue.add( source, 0.0 );
        while ( !queue.isEmpty() ) {
            int node = queue.extractMin();
            double distance = dataContainer.getDistance( node );
            TIntIterator outgoingEdges = graph.getOutgoingEdges( node );
            while ( outgoingEdges.hasNext() ) {
                int edge = outgoingEdges.next();
                int target = graph.getOtherNode( edge, node );
                double distanceViaNode = distance + graph.getLength( edge );
                if ( distanceViaNode < dataContainer.getDistance( target ) ) {
                    dataContainer.setDistance( target, distanceViaNode );
                    dataContainer.setPredecessor( target, edge );
                    queue.notifyDataChange( target, distanceViaNode );
                }
            }
        }
        return dataContainer;
    }

    /**
     * Definition of the graph
     */
    public static interface OneToAllGraph {

        /**
         * Returns amount of nodes
         *
         * @return amount of nodes
         */
        int getNodeCount();

        /**
         * Returns amount of edges
         *
         * @return amount of edges
         */
        int getEdgeCount();

        /**
         * Returns length of the given edge
         *
         * @param edge given edge
         * @return length of the edge
         */
        double getLength( int edge );

        /**
         * Returns source node of the given edge
         *
         * @param edge given edge
         * @return source node
         */
        int getSource( int edge );

        /**
         * Returns target node of the given edge
         *
         * @param edge given edge
         * @return target node
         */
        int getTarget( int edge );

        /**
         * Returns other (on the other side from the given node) node of the
         * given edge
         *
         * @param edge given edge
         * @param node given node
         * @return other node
         */
        int getOtherNode( int edge, int node );

        /**
         * Returns iterator over outgoing edges for the given node
         *
         * @param node given node
         * @return iterator for outgoing edges
         */
        TIntIterator getOutgoingEdges( int node );

        /**
         * Returns iterator over incoming edges for the given node
         *
         * @param node given node
         * @return iterator for incoming edges
         */
        TIntIterator getIncomingEdges( int node );
    }
}
