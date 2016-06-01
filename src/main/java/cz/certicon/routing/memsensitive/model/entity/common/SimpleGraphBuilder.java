/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.common;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.neighbourlist.NeighbourlistGraph;
import cz.certicon.routing.model.entity.GraphBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleGraphBuilder implements GraphBuilder<Graph> {

    private final Graph graph;
    private final DistanceType distanceType;
    private final Map<Integer, List<Integer>> outgoingEdgesMap = new HashMap<>();
    private final Map<Integer, List<Integer>> incomingEdgesMap = new HashMap<>();
    private int nodeCounter = 0;
    private int edgeCounter = 0;

    public SimpleGraphBuilder( int nodeCount, int edgeCount, DistanceType distanceType ) {
        this.graph = new NeighbourlistGraph( nodeCount, edgeCount );
        this.distanceType = distanceType;
        for ( int i = 0; i < nodeCount; i++ ) {
            getEdgeList( incomingEdgesMap, i );
            getEdgeList( outgoingEdgesMap, i );
        }
    }

    @Override
    public void addNode( long id, long dataId, long osmId, double latitude, double longitude ) {
//        System.out.println( "adding node: " + id + ", idx = " + nodeCounter );
        graph.setNodeOrigId( nodeCounter, id );
        graph.setCoordinate( nodeCounter, (float) latitude, (float) longitude );
        nodeCounter++;
    }

    @Override
    public void addEdge( long id, long dataId, long osmId, long sourceId, long targetId, double length, double speed, boolean isPaid ) {
//        System.out.println( "adding edge: " + id + ", idx = " + edgeCounter + ", source = " + sourceId + ", target = " + targetId );
        graph.setEdgeOrigId( edgeCounter, id );
        int source = graph.getNodeByOrigId( sourceId );
        int target = graph.getNodeByOrigId( targetId );
        getEdgeList( outgoingEdgesMap, source ).add( edgeCounter );
        getEdgeList( incomingEdgesMap, target ).add( edgeCounter );
        graph.setLength( edgeCounter, (float) distanceType.calculateDistance( length, speed ) );
        graph.setSource( edgeCounter, source );
        graph.setTarget( edgeCounter, target );
        edgeCounter++;
    }

    @Override
    public Graph build() {
        for ( Map.Entry<Integer, List<Integer>> entry : outgoingEdgesMap.entrySet() ) {
            int nodeId = entry.getKey();
            int[] outgoingEdgesArray = toArray( entry.getValue() );
            graph.setOutgoingEdges( nodeId, outgoingEdgesArray );
//            System.out.println( "outgoing edges for: " + nodeId + " = " + Arrays.toString( outgoingEdgesArray ) );
        }
        for ( Map.Entry<Integer, List<Integer>> entry : incomingEdgesMap.entrySet() ) {
            int nodeId = entry.getKey();
            int[] incomingEdgesArray = toArray( entry.getValue() );
            graph.setIncomingEdges( nodeId, incomingEdgesArray );
//            System.out.println( "incoming edges for: " + nodeId + " = " + Arrays.toString( incomingEdgesArray ) );
        }
        for ( int i = 0; i < nodeCounter; i++ ) {
            if ( graph.getIncomingEdges( i ) == null ) {
                graph.setIncomingEdges( i, new int[0] );
            }
        }
        for ( int i = 0; i < nodeCounter; i++ ) {
            if ( graph.getOutgoingEdges( i ) == null ) {
                graph.setOutgoingEdges( i, new int[0] );
            }
        }
        return graph;
    }

    private List<Integer> getEdgeList( Map<Integer, List<Integer>> map, int node ) {
        List<Integer> list = map.get( node );
        if ( list == null ) {
            list = new ArrayList<>();
            map.put( node, list );
        }
        return list;
    }

    private int[] toArray( List<Integer> list ) {
        int[] array = new int[list.size()];
        for ( int i = 0; i < list.size(); i++ ) {
            array[i] = list.get( i );
        }
        return array;
    }
}
