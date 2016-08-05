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

    public DataContainer execute( OneToAllGraph graph, int source ) {
        return execute( graph, new MapDataContainer(), source );
    }

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

    public static interface OneToAllGraph {

        int getNodeCount();

        int getEdgeCount();

        double getLength( int edge );

        int getSource( int edge );

        int getTarget( int edge );

        int getOtherNode( int edge, int node );

        TIntIterator getOutgoingEdges( int node );

        TIntIterator getIncomingEdges( int node );
    }
}
