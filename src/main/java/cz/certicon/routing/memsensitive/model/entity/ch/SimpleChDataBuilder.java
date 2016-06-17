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
    private Map<Long, Integer> sourceMap;
    private Map<Long, Integer> targetMap;
    private long minId = Long.MAX_VALUE;
    private long maxId = -1;
    private int counter;
    private long startId = 0;
    private Map<Long, Integer> shortcutIdMap = new HashMap<>();

    public SimpleChDataBuilder( Graph graph, DistanceType distanceType ) {
        this.graph = graph;
        this.distanceType = distanceType;
        this.shortcuts = new HashMap<>();
        this.sourceMap = new HashMap<>();
        this.targetMap = new HashMap<>();
        this.ranks = new int[graph.getNodeCount()];
        this.counter = 0;
        this.shortcutIdMap.clear();
    }

    @Override
    public void setStartId( long startId ) {
        this.startId = startId;
    }

    @Override
    public void setRank( long nodeId, int rank ) {
        ranks[graph.getNodeByOrigId( nodeId )] = rank;
    }

    @Override
    public void addShortcut( long shortcutId, long sourceEdgeId, long targetEdgeId ) {
        if ( shortcutId == sourceEdgeId || shortcutId == targetEdgeId ) {
            throw new AssertionError( "shortcut #" + shortcutId + " = " + sourceEdgeId + " -> " + targetEdgeId );
        }
        if(shortcutId == 127945){
            throw new AssertionError( "shortcut #" + shortcutId + " = " + sourceEdgeId + " -> " + targetEdgeId );
        }
        shortcuts.put( shortcutId, new Pair<>( sourceEdgeId, targetEdgeId ) );
        shortcutIdMap.put( shortcutId, counter++ );
        sourceMap.put( shortcutId, getSourceNode( shortcutId ) );
        targetMap.put( shortcutId, getTargetNode( shortcutId ) );
    }

    @Override
    public DistanceType getDistanceType() {
        return distanceType;
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
        for ( long shortcutId : shortcuts.keySet() ) {
            int id = shortcutIdMap.get( shortcutId );
            int source = getSourceNode( shortcutId );
            int target = getTargetNode( shortcutId );
//            System.out.println( "adding #" + id + " with: " + source + " -> " + target );
            sources[id] = source;
            targets[id] = target;
            getList( nodeOutgoing, source ).add( id );
            getList( nodeIncoming, target ).add( id );
        }
        for ( long shortcutId : shortcuts.keySet() ) {
            int id = shortcutIdMap.get( shortcutId );
            Pair<Long, Long> p = shortcuts.get( shortcutId );
//            System.out.println( "shortcut #" + shortcutId );
//            System.out.println( "shortcut #" + id );
            if ( graph.containsEdge( p.a ) ) {
                int edge = graph.getEdgeByOrigId( p.a );
                startEdges[id] = edge;
//                System.out.println( "sedge = " + edge );
            } else {
                int shortcut = shortcutIdMap.get( p.a );
                startEdges[id] = shortcut + graph.getEdgeCount();
//                System.out.println( "sshortcut = " + shortcut );
            }
            if ( graph.containsEdge( p.b ) ) {
                int edge = graph.getEdgeByOrigId( p.b );
                endEdges[id] = edge;
//                System.out.println( "eedge = " + edge );
            } else {
                int shortcut = shortcutIdMap.get( p.b );
                endEdges[id] = shortcut + graph.getEdgeCount();
//                System.out.println( "eshortcut = " + shortcut );
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
        for ( int i = 0; i < incomingShortcuts.length; i++ ) {
            if ( incomingShortcuts[i] == null ) {
                incomingShortcuts[i] = new int[0];
            }
        }
        for ( int i = 0; i < outgoingShortcuts.length; i++ ) {
            if ( outgoingShortcuts[i] == null ) {
                outgoingShortcuts[i] = new int[0];
            }
        }
        return new PreprocessedData( ranks, incomingShortcuts, outgoingShortcuts, sources, targets, startEdges, endEdges, startId );
    }

    private int getSourceNode( long shortcutId ) {
        Integer source = sourceMap.get( shortcutId );
        if ( source == null ) {
//            System.out.println( "#" + shortcutId + " - source is null" );
            long sourceEdge = shortcuts.get( shortcutId ).a;
//            System.out.println( "#" + shortcutId + " - source edge is = " + sourceEdge );
            if ( shortcuts.containsKey( sourceEdge ) ) {
                source = getSourceNode( sourceEdge );
            } else {
                source = graph.getSource( graph.getEdgeByOrigId( sourceEdge ) );
            }
            sourceMap.put( shortcutId, source );
        }
        return source;
    }

    private int getTargetNode( long shortcutId ) {
        Integer target = targetMap.get( shortcutId );
        if ( target == null ) {
//            System.out.println( "#" + shortcutId + " - target is null" );
            long targetEdge = shortcuts.get( shortcutId ).b;
            if ( shortcuts.containsKey( targetEdge ) ) {
                target = getTargetNode( targetEdge );
            } else {
                target = graph.getTarget( graph.getEdgeByOrigId( targetEdge ) );
            }
            targetMap.put( shortcutId, target );
        }
        return target;
    }

}
