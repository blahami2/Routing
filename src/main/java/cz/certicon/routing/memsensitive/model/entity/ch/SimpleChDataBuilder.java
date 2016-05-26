/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.model.entity.ch;

import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import static cz.certicon.routing.utils.CollectionUtils.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class SimpleChDataBuilder implements ChDataBuilder<PreprocessedData> {

    private final Graph graph;
    private final DistanceType distanceType;

    private int[] ranks;
    private Map<Long, Pair<Long, Long>> shortcuts;
    private long minId = Long.MAX_VALUE;
    private long maxId = -1;

    public SimpleChDataBuilder( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
        this.shortcuts = new HashMap<>();
        this.ranks = new int[graph.getNodeCount()];
    }

    @Override
    public void setRank( long nodeId, int rank ) {
        ranks[graph.getNodeByOrigId( nodeId )] = rank;
    }

    @Override
    public void addShortcut( long shortcutId, long sourceEdgeId, long targetEdgeId ) {
        shortcuts.put( shortcutId, new Pair<>( sourceEdgeId, targetEdgeId ) );
    }

    @Override
    public int getDistanceTypeIntValue() {
        return DistanceType.toInt( distanceType );
    }

    @Override
    public PreprocessedData build() {
        int nodeCount = graph.getNodeCount();
        int shortcutCount = shortcuts.size();
        int[][] incomingShortcuts = new int[nodeCount][];
        int[][] outgoingShortcuts = new int[nodeCount][];
        int[] sources = new int[shortcutCount];
        int[] targets = new int[shortcutCount];

        int[] startEdges = new int[shortcutCount];
        int[] endEdges = new int[shortcutCount];

        Map<Integer, List<Integer>> nodeOutgoing = new HashMap<>();
        Map<Integer, List<Integer>> nodeIncoming = new HashMap<>();
        Map<Long, Integer> shortcutIdMap = new HashMap<>();
        int counter = 0;
        for ( long shortcutId : shortcuts.keySet() ) {
            int source = getSource( shortcutId );
            int target = getTarget( shortcutId );
            sources[counter] = source;
            targets[counter] = target;
            getList( nodeOutgoing, source ).add( counter );
            getList( nodeIncoming, target ).add( counter );
            shortcutIdMap.put( shortcutId, counter );
            counter++;
        }
        for ( Long shortcutId : shortcuts.keySet() ) {
            Pair<Long, Long> p = shortcuts.get( shortcutId );
            if ( graph.containsEdge( p.a ) ) {
                int edge = graph.getEdgeByOrigId( p.a );
                startEdges[counter] = edge;
            } else {
                int shortcut = shortcutIdMap.get( p.a );
                startEdges[counter] = shortcut + graph.getEdgeCount();
            }
            if ( graph.containsEdge( p.b ) ) {
                int edge = graph.getEdgeByOrigId( p.a );
                endEdges[counter] = edge;
            } else {
                int shortcut = shortcutIdMap.get( p.b );
                endEdges[counter] = shortcut + graph.getEdgeCount();
            }

        }
        for ( Map.Entry<Integer, List<Integer>> entry : nodeOutgoing.entrySet() ) {
            int node = entry.getKey();
            List<Integer> outgoing = entry.getValue();
            outgoingShortcuts[node] = toIntArray( outgoing );
        }
        for ( Map.Entry<Integer, List<Integer>> entry : nodeIncoming.entrySet() ) {
            int node = entry.getKey();
            List<Integer> incoming = entry.getValue();
            incomingShortcuts[node] = toIntArray( incoming );
        }
        return new PreprocessedData( ranks, incomingShortcuts, outgoingShortcuts, sources, targets, startEdges, endEdges );
    }

    private int getSource( long shortcutId ) {
        long sourceEdge = shortcuts.get( shortcutId ).a;
        if ( shortcuts.containsKey( sourceEdge ) ) {
            return getSource( sourceEdge );
        } else {
            return graph.getSource( graph.getEdgeByOrigId( sourceEdge ) );
        }
    }

    private int getTarget( long shortcutId ) {
        long targetEdge = shortcuts.get( shortcutId ).b;
        if ( shortcuts.containsKey( targetEdge ) ) {
            return getTarget( targetEdge );
        } else {
            return graph.getTarget( graph.getEdgeByOrigId( targetEdge ) );
        }
    }

}
