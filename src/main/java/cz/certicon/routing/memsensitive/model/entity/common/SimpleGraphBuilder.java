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
    private final Map<Long, List<Integer>> outgoingEdgesMap = new HashMap<>();
    private final Map<Long, List<Integer>> incomingEdgesMap = new HashMap<>();
    private int nodeCounter = 0;
    private int edgeCounter = 0;

    public SimpleGraphBuilder( int nodeCount, int edgeCount, DistanceType distanceType ) {
        this.graph = new NeighbourlistGraph( nodeCount, edgeCount );
        this.distanceType = distanceType;
    }

    @Override
    public void addNode( long id, long dataId, long osmId, double latitude, double longitude ) {
        graph.setNodeOrigId( nodeCounter++, id );
    }

    @Override
    public void addEdge( long id, long dataId, long osmId, long sourceId, long targetId, double length, double speed, boolean isPaid ) {
        graph.setEdgeOrigId( edgeCounter, id );
        getEdgeList( outgoingEdgesMap, sourceId ).add( edgeCounter );
        getEdgeList( incomingEdgesMap, targetId ).add( edgeCounter );
        graph.setLength( edgeCounter, distanceType.calculateDistance( length, speed ) );
        edgeCounter++;
    }

    @Override
    public Graph build() {
        for ( Map.Entry<Long, List<Integer>> entry : outgoingEdgesMap.entrySet() ) {
            long origNodeId = entry.getKey();
            int[] outgoingEdgesArray = toArray( entry.getValue() );
            graph.setOutgoingEdges( graph.getNodeByOrigId( origNodeId ), outgoingEdgesArray );
        }
        for ( Map.Entry<Long, List<Integer>> entry : incomingEdgesMap.entrySet() ) {
            long origNodeId = entry.getKey();
            int[] incomingEdgesArray = toArray( entry.getValue() );
            graph.setIncomingEdges( graph.getNodeByOrigId( origNodeId ), incomingEdgesArray );
        }
        return graph;
    }

    private List<Integer> getEdgeList( Map<Long, List<Integer>> map, long node ) {
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
