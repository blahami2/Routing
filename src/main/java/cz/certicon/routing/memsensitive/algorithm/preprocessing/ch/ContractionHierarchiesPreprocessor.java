/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators.BasicEdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.NeighboursOnlyRecalculationStrategy;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.NodeState;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.model.utility.progress.EmptyProgressListener;
import cz.certicon.routing.utils.DoubleComparator;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesPreprocessor implements Preprocessor<PreprocessedData> {

    // DEBUG
    public PrintStream out = null;
//    public List<Pair<Integer, String>> shortcutCounts = new ArrayList<>();
    public int nodeOfInterest = 15318;
//    public Graph graph;
    private static final int THREADS = 8;

    private static final double INIT_NODE_RANKING = 0.1;

    private static final double PRECISION = 10E-3;

    private NodeRecalculationStrategy nodeRecalculationStrategy;
    private EdgeDifferenceCalculator edgeDifferenceCalculator;

    public ContractionHierarchiesPreprocessor() {
        this.nodeRecalculationStrategy = new NeighboursOnlyRecalculationStrategy();
//        this.nodeRecalculationStrategy = new LazyRecalculationStrategy();
        this.edgeDifferenceCalculator = new BasicEdgeDifferenceCalculator();
        // DEBUG
        try {
            out = new PrintStream( new File( "C:\\Routing\\Testing\\minimized.txt" ) );
            ( (NeighboursOnlyRecalculationStrategy) nodeRecalculationStrategy ).preprocessor = this;
        } catch ( FileNotFoundException ex ) {
            Logger.getLogger( ContractionHierarchiesPreprocessor.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    public ContractionHierarchiesPreprocessor( NodeRecalculationStrategy nodeRecalculationStrategy ) {
        this.nodeRecalculationStrategy = nodeRecalculationStrategy;
        this.edgeDifferenceCalculator = new BasicEdgeDifferenceCalculator();
        this.nodeRecalculationStrategy.setEdgeDifferenceCalculator( edgeDifferenceCalculator );
    }

    @Override
    public void setNodeRecalculationStrategy( NodeRecalculationStrategy nodeRecalculationStrategy ) {
        this.nodeRecalculationStrategy = nodeRecalculationStrategy;
        this.nodeRecalculationStrategy.setEdgeDifferenceCalculator( edgeDifferenceCalculator );
    }

    @Override
    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator ) {
        this.edgeDifferenceCalculator = edgeDifferenceCalculator;
        this.nodeRecalculationStrategy.setEdgeDifferenceCalculator( edgeDifferenceCalculator );
        // DEBUG
//        if ( edgeDifferenceCalculator instanceof SpatialHeuristicEdgeDifferenceCalculator ) {
//            ( (SpatialHeuristicEdgeDifferenceCalculator) edgeDifferenceCalculator ).preprocessor = this;
//        }
    }

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId ) {
        return preprocess( dataBuilder, graph, distanceType, startId, new EmptyProgressListener() );
    }

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId, ProgressListener progressListener ) {
        // DEBUG
//        this.graph = graph;
//        this.nodeRecalculationStrategy.setEdgeDifferenceCalculator( edgeDifferenceCalculator );

        System.out.println( "================================ preprocessing ================================" );
        System.out.println( "start id = " + startId );
        dataBuilder.setStartId( startId );

        int nodeCount = graph.getNodeCount();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        ProcessingData data = new ProcessingData( graph );
        BitArray removedNodes = new LongBitArray( graph.getNodeCount() );
        BitArray calculatedNodes = new LongBitArray( graph.getNodeCount() );
        int[] nodeDegrees = new int[graph.getNodeCount()];
//
        progressListener.init( nodeCount, INIT_NODE_RANKING );

        // DEBUG
//        List<Pair<Integer, Long>> sortedNodes = new ArrayList<>();
        for ( int node = 0; node < nodeCount; node++ ) {
            nodeDegrees[node] = graph.getNodeDegree( node );
            // DEBUG
//            sortedNodes.add( new Pair<>( node, graph.getNodeOrigId( node ) ) );
//            if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                out.println( "node degree #" + graph.getNodeOrigId( node ) + " = " + graph.getNodeDegree( node ) );
//            }
        }
//        Collections.sort( sortedNodes, new Comparator<Pair<Integer, Long>>() {
//            @Override
//            public int compare( Pair<Integer, Long> o1, Pair<Integer, Long> o2 ) {
//                return Long.compare( o1.b, o2.b );
//            }
//        } );
        // DEBUG
        for ( int node = 0; node < nodeCount; node++ ) {
//        for ( int i = 0; i < nodeCount; i++ ) {
//            int node = sortedNodes.get( i ).a;
//            System.out.println( "node#" + node + " = " + graph.getNodeOrigId( node ) );
            int numberOfShortcuts = calculateShortcuts( data, removedNodes, node, graph );
            int ed = nodeRecalculationStrategy.getEdgeDifferenceCalculator().calculate( -1, nodeDegrees, node, numberOfShortcuts );
//            System.out.println( "#" + node + " = " + ed );
            priorityQueue.add( node, ed );
            progressListener.nextStep();
        }

        // DEBUG
//        this.graph = graph;
//        Collections.sort( shortcutCounts, new Comparator<Pair<Integer, String>>() {
//            @Override
//            public int compare( Pair<Integer, String> o1, Pair<Integer, String> o2 ) {
//                return Integer.compare( o1.a, o2.a );
//            }
//        } );
//        for ( Pair<Integer, String> shortcutCount : shortcutCounts ) {
//            if ( shortcutCount.a == nodeOfInterest || nodeOfInterest < 0 ) {
//                out.println( "shortcut: #" + shortcutCount.a + ": " + shortcutCount.b );
//            }
//        }
        // DEBUG
//        if ( true ) {
//            return dataBuilder.build();
//        }
        int rank = 1;
        progressListener.init( priorityQueue.size(), 1.0 - INIT_NODE_RANKING );
        while ( !priorityQueue.isEmpty() ) {
            Pair<Integer, Double> extractMin = extractMin( priorityQueue, graph );
            int node = extractMin.a;
            // DEBUG
//            if ( graph.getNodeOrigId( node ) == nodeOfInterest ) {
//                break;
//            }
            // shortcuts
//            System.out.println( "contracting: " + node );
            contractNode( data, nodeDegrees, removedNodes, node, graph );

            TIntIterator it = nodeRecalculationStrategy.recalculationIterator( graph, data, node, priorityQueue );
            // DEBUG
//            List<Pair<Integer, Long>> sortedNeighbours = new ArrayList<>();
//            while ( it.hasNext() ) {
//                int n = it.next();
//                sortedNeighbours.add( new Pair<>( n, graph.getNodeOrigId( n ) ) );
//            }
//            Collections.sort( sortedNeighbours, new Comparator<Pair<Integer, Long>>() {
//                @Override
//                public int compare( Pair<Integer, Long> o1, Pair<Integer, Long> o2 ) {
//                    return Long.compare( o1.b, o2.b );
//                }
//            } );
//            for ( Pair<Integer, Long> p : sortedNeighbours ) {
//                int n = p.a;
            while ( it.hasNext() ) {
                int n = it.next();
//                System.out.println( "#" + node + " - iterated to: " + n );
                // if not calculated yet and not removed yet
                if ( !calculatedNodes.get( n ) && !removedNodes.get( n ) ) {
                    int numberOfShortcuts = calculateShortcuts( data, removedNodes, n, graph );
                    nodeRecalculationStrategy.onShortcutsCalculated( graph, nodeDegrees, n, priorityQueue, numberOfShortcuts, node );
                    calculatedNodes.set( n, true );
                }
            }
            // reset calculated neighbours
            it = nodeRecalculationStrategy.recalculationIterator( graph, data, node, priorityQueue );
            while ( it.hasNext() ) {
                calculatedNodes.set( it.next(), false );
            }
            dataBuilder.setRank( graph.getNodeOrigId( node ), rank++ );
            progressListener.nextStep();
        }
        for ( int i = 0; i < data.size(); i++ ) {
            // DEBUG
//            System.out.println( "adding shortcut: #" + ( startId + i ) + " where i = " + i  + ", startEdge = " + data.startEdges.get( i ) + ", endEdge = " + data.endEdges.get( i ) );
            dataBuilder.addShortcut( startId + i, data.getEdgeOrigId( data.startEdges.get( i ), startId ), data.getEdgeOrigId( data.endEdges.get( i ), startId ) );
        }
        int[][][] tts = new int[graph.getNodeCount()][][];
//        int maxLen = 0;
        for ( int i = 0; i < tts.length; i++ ) {
            if ( data.turnRestrictions.containsKey( i ) ) { // has turntables for node
                List<TIntList> list = data.turnRestrictions.get( i );
                tts[i] = new int[list.size()][];
//                maxLen = Math.max( maxLen, tts[i].length );
                for ( int j = 0; j < tts[i].length; j++ ) { // forall sequences
                    TIntList sequence = list.get( j );
                    tts[i][j] = sequence.toArray(); // add
                }
            }
        }
//        System.out.println( getClass().getSimpleName() + "-maxlen = " + maxLen );
        dataBuilder.setTurnTables( tts );
        // DEBUG
//        out.flush();
        return dataBuilder.build();
    }

    private int calculateShortcuts( ProcessingData data, BitArray removedNodes, int node, Graph graph ) {
//        System.out.println( "-------- shortcut calculation --------" );
        if ( removedNodes.get( node ) ) {
            return 0;
        }
        NodeDataStructure<NodeState> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        // DEBUG
//        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            out.println( "shortcut for #" + graph.getNodeOrigId( node ) );
//        }
        removedNodes.set( node, true );
        Set<Integer> sources = new HashSet<>();
        Set<Integer> targets = new HashSet<>();
        Map<Integer, Float> upperBounds = new HashMap<>();
        Map<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> fromToDistanceMap = new HashMap<>();
        // for each pair of edges going through node:
        TIntIterator incomingIterator = data.getIncomingEdgesIterator( node );
        while ( incomingIterator.hasNext() ) {
            int incomingEdge = incomingIterator.next();
            int sourceNode = data.getSource( incomingEdge );
            if ( removedNodes.get( sourceNode ) ) {
                continue;
            }
            TIntIterator outgoingIterator = data.getOutgoingEdgesIterator( node );
            while ( outgoingIterator.hasNext() ) {
                int outgoingEdge = outgoingIterator.next();
                if ( incomingEdge != outgoingEdge ) {
                    int targetNode = data.getTarget( outgoingEdge );
                    //   calculate shortest distance
                    if ( !removedNodes.get( targetNode ) && sourceNode != targetNode ) {
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        float newDistance = data.getLength( incomingEdge ) + data.getLength( outgoingEdge );
                        if ( !fromToDistanceMap.containsKey( pair ) || DoubleComparator.isLowerThan( newDistance, fromToDistanceMap.get( pair ).c, PRECISION ) ) {
                            if ( data.isValidShortcut( incomingEdge, node, outgoingEdge ) ) {
                                fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                                //   collect sources in a set
                                sources.add( sourceNode );
                                //   collect targets in a set
                                targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance );
                            }
                        }
//                        if ( !upperBounds.containsKey( sourceNode ) || upperBounds.get( sourceNode ) < newDistance ) {
//                            upperBounds.put( sourceNode, newDistance );
//                        }
                    }
                }
            }
        }
        //   find the longest distance for each source
        for ( Map.Entry<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> entry : fromToDistanceMap.entrySet() ) {
            int from = entry.getKey().a;
            float distance = entry.getValue().c;
            if ( !upperBounds.containsKey( from ) || DoubleComparator.isLowerThan( upperBounds.get( from ), distance, PRECISION ) ) {
                upperBounds.put( from, distance );
            }
        }

        // DEBUG
//        List<Integer> sources = new ArrayList<>( sourceSet );
//        Collections.sort( sources );
//        List<Integer> targets = new ArrayList<>( targetSet );
//        Collections.sort( targets );
//        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            for ( int from : sources ) {
//                out.println( "#" + graph.getNodeOrigId( node ) + "-neighbour incoming: #" + graph.getNodeOrigId( from ) );
//            }
//            for ( int to : targets ) {
//                out.println( "#" + graph.getNodeOrigId( node ) + "-neighbour outgoing: #" + graph.getNodeOrigId( to ) );
//            }
//            for ( Map.Entry<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> entry : fromToDistanceMap.entrySet() ) {
//                out.println( "#" + graph.getNodeOrigId( node ) + "-path: from #" + graph.getNodeOrigId( entry.getKey().a ) + " to #" + graph.getNodeOrigId( entry.getKey().b ) + " in " + entry.getValue().c );
//            }
//        }
        int addedShortcuts = 0;
        Map<NodeState, NodeState> nodePredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeDistanceArray = new HashMap<>();
        Set<NodeState> nodeClosedArray = new HashSet<>();
        TIntFloatMap nodeMinDistanceMap = new TIntFloatHashMap();
        // for each source calculate Dijkstra to all the targets {without using the node)
        for ( int from : sources ) {
//            System.out.println( "Dijkstra-from = " + from );
            float upperBound = upperBounds.get( from );
            NodeState state = NodeState.Factory.newInstance( from, -1 );
            nodeDistanceArray.put( state, 0.0f );
            dijkstraPriorityQueue.add( state, 0.0f );
            nodeMinDistanceMap.put( from, 0.0f );
//            System.out.println( "Putting-distance-#" + from + ": " + 0.0f );
            // DEBUG
//            if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                out.println( "#" + graph.getNodeOrigId( node ) + "-Dijkstra for #" + graph.getNodeOrigId( from ) + ", upper bound = " + upperBound );
//            }
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                state = dijkstraPriorityQueue.extractMin();
                float currentDistance = nodeDistanceArray.get( state );
//                System.out.println( "Extracted-" + state + ", distance = " + currentDistance );
                // DEBUG
//                if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                    out.println( "#" + graph.getNodeOrigId( node ) + "-extracted #" + graph.getNodeOrigId( currentNode ) + " -> " + currentDistance );
//                }
                //   use the longest distance as an upper bound
                if ( DoubleComparator.isLowerThan( upperBound, currentDistance, PRECISION ) ) {
                    dijkstraPriorityQueue.clear();
                    break;
                }
                // calculate SP ...
                TIntIterator it = data.getOutgoingEdgesIterator( state.getNode() );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    if ( !data.isValidWay( state, edge, nodePredecessorArray ) ) { // TODO or contains any valid turnrestriction, maybe add to the function? edit also for graph, not IN graph though
                        continue;
                    }
                    int target = data.getTarget( edge );
                    NodeState targetState = NodeState.Factory.newInstance( target, edge );
                    float targetDistance = ( nodeDistanceArray.containsKey( targetState ) ) ? nodeDistanceArray.get( targetState ) : Float.MAX_VALUE;
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && DoubleComparator.isLowerThan( newDistance, targetDistance, PRECISION ) ) {
                        nodeDistanceArray.put( targetState, newDistance );
                        nodePredecessorArray.put( targetState, state );
                        dijkstraPriorityQueue.notifyDataChange( targetState, newDistance );
                        if ( !nodeMinDistanceMap.containsKey( target ) || nodeMinDistanceMap.get( target ) > newDistance ) {
//                            System.out.println( "Putting-distance-#" + target + ": " + newDistance );
                            nodeMinDistanceMap.put( target, newDistance );
                        }
                    }
                }
            }
            //   for each target compare calculated SP with the shortest distance
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }
                float distance = nodeMinDistanceMap.containsKey( to ) ? nodeMinDistanceMap.get( to ) : Float.MAX_VALUE;
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
//                System.out.println( "#" + node + " - path: " + from + " -> " + to + " => shortcut distance = " + shortcutDistanceT.c + ", path distance = " + distance );
                //   create shortcut if longer
                if ( shortcutDistanceT != null && DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) {
                    data.addTemporaryShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                    addedShortcuts++;
                    // DEBUG
//                    if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                        out.println( "#" + graph.getNodeOrigId( node ) + "- adding shortcut #" + graph.getNodeOrigId( from ) + " -> #" + graph.getNodeOrigId( to ) );
//                    }
//                    System.out.println( "#" + node + " - creating temporary shortcut: " + from + " -> " + to );
                }
            }
            // reset node distances
            nodeDistanceArray.clear();
            nodeMinDistanceMap.clear();
            nodePredecessorArray.clear();
        }
        // delete added shortcuts
        data.clearTemporaryShortcuts();
//        for ( Pair<Integer, Integer> addedShortcut : addedShortcuts ) {
//            int source = addedShortcut.a;
//            int target = addedShortcut.b;
//            System.out.println( "#" + node + " - removing temporary shortcut: " + source + " -> " + target );
//            data.removeLastShortcut();
//        }
        removedNodes.set( node, false );
        // DEBUG
//        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            out.println( "shortcut for #" + graph.getNodeOrigId( node ) + " = " + addedShortcuts.size() );
//        }
//        shortcutCounts.add( new Pair<>( (int) graph.getNodeOrigId( node ), "" + addedShortcuts.size() ) );
        return addedShortcuts;
    }

    private void contractNode( ProcessingData data, int[] nodeDegrees, BitArray removedNodes, int node, Graph graph ) {
//        System.out.println( "-------- contracting node --------" );
        // disable node
        removedNodes.set( node, true );

        // DEBUG
        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
            out.println( "contract for #" + graph.getNodeOrigId( node ) );
        }
        NodeDataStructure<NodeState> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        // lower neighbour's degree
        TIntIterator incIt = data.getIncomingEdgesIterator( node );
        while ( incIt.hasNext() ) {
            int edge = incIt.next();
            int source = data.getOtherNode( edge, node );
            if ( !removedNodes.get( source ) ) {
                //  DEBUG
//                if ( graph.getNodeOrigId( source ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                    out.println( "#" + graph.getNodeOrigId( source ) + "-removing outgoing edge #" + ( ( edge < graph.getEdgeCount() ) ? graph.getEdgeOrigId( edge ) : edge ) );
//                }
                nodeDegrees[source]--;
            }
        }
        TIntIterator outIt = data.getOutgoingEdgesIterator( node );
        while ( outIt.hasNext() ) {
            int edge = outIt.next();
            int target = data.getOtherNode( edge, node );
            if ( !removedNodes.get( target ) ) {
                //  DEBUG
//                if ( graph.getNodeOrigId( target ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                    out.println( "#" + graph.getNodeOrigId( target ) + "-removing incoming edge #" + ( ( edge < graph.getEdgeCount() ) ? graph.getEdgeOrigId( edge ) : edge ) );
//                }
                nodeDegrees[target]--;
            }
        }
        Set<Integer> sources = new HashSet<>();
        Set<Integer> targets = new HashSet<>();
        Map<Integer, Float> upperBounds = new HashMap<>();
        Map<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> fromToDistanceMap = new HashMap<>();
        // for each pair of edges going through node:
        TIntIterator incomingIterator = data.getIncomingEdgesIterator( node );
        while ( incomingIterator.hasNext() ) {
            int incomingEdge = incomingIterator.next();
            int sourceNode = data.getSource( incomingEdge );
            if ( removedNodes.get( sourceNode ) ) {
                continue;
            }
            TIntIterator outgoingIterator = data.getOutgoingEdgesIterator( node );
            while ( outgoingIterator.hasNext() ) {
                int outgoingEdge = outgoingIterator.next();
                if ( incomingEdge != outgoingEdge ) {
                    int targetNode = data.getTarget( outgoingEdge );
                    //   calculate shortest distance
                    if ( !removedNodes.get( targetNode ) && sourceNode != targetNode ) {

                        // DEBUG
//                        if ( graph.getNodeOrigId( sourceNode ) == nodeOfInterest ) {
//                            out.println( "#" + graph.getNodeOrigId( sourceNode ) + "-removing outgoing edge #-" );
//                        }
//                        if ( graph.getNodeOrigId( targetNode ) == nodeOfInterest ) {
//                            out.println( "#" + graph.getNodeOrigId( targetNode ) + "-removing outgoing edge #-" );
//                        }
//                        nodeDegrees[sourceNode]--;
//                        nodeDegrees[targetNode]--;
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        float newDistance = data.getLength( incomingEdge ) + data.getLength( outgoingEdge );
                        if ( !fromToDistanceMap.containsKey( pair ) || DoubleComparator.isLowerThan( newDistance, fromToDistanceMap.get( pair ).c, PRECISION ) ) {
                            if ( sourceNode == graph.getNodeByOrigId( 12410 ) && targetNode == graph.getNodeByOrigId( 41329 ) ) {
                                out.println( "#" + graph.getNodeOrigId( node ) + "-path from 12410 to 41329: " + newDistance + " -> " + ( data.isValidShortcut( incomingEdge, node, outgoingEdge ) ) );
                            }
                            if ( data.isValidShortcut( incomingEdge, node, outgoingEdge ) ) {
                                fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                                //   collect sources in a set
                                sources.add( sourceNode );
                                //   collect targets in a set
                                targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance + "[" + incomingEdge + " -> " + outgoingEdge + "]" );
                            }
                        }
                        //   find the longest distance for each source
//                        if ( !upperBounds.containsKey( sourceNode ) || upperBounds.get( sourceNode ) < newDistance ) {
//                            upperBounds.put( sourceNode, newDistance );
//                        }
                    }
                }
            }
        }
        //   find the longest distance for each source
        for ( Map.Entry<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> entry : fromToDistanceMap.entrySet() ) {
            int from = entry.getKey().a;
            float distance = entry.getValue().c;
            if ( !upperBounds.containsKey( from ) || DoubleComparator.isLowerThan( upperBounds.get( from ), distance, PRECISION ) ) {
                upperBounds.put( from, distance );
            }
        }

        // DEBUG
//        List<Integer> sources = new ArrayList<>( sourceSet );
//        Collections.sort( sources );
//        List<Integer> targets = new ArrayList<>( targetSet );
//        Collections.sort( targets );
        Map<NodeState, NodeState> nodePredecessorArray = new HashMap<>();
        Map<NodeState, Float> nodeDistanceArray = new HashMap<>();
        Set<NodeState> nodeClosedArray = new HashSet<>();
        TIntFloatMap nodeMinDistanceMap = new TIntFloatHashMap();
        Set<Integer> visitedNodes = new HashSet<>();
        // for each source calculate Dijkstra to all the targets {without using the node)
        for ( int from : sources ) {
//            System.out.println( "Dijkstra-from = " + from );
            float upperBound = upperBounds.get( from );
            NodeState state = NodeState.Factory.newInstance( from, -1 );
            nodeDistanceArray.put( state, 0.0f );
            dijkstraPriorityQueue.add( state, 0.0f );
            nodeMinDistanceMap.put( from, 0.0f );

            if ( graph.getNodeOrigId( node ) == nodeOfInterest && ( graph.getNodeOrigId( from ) == 12410 || nodeOfInterest < 0 ) ) {
                out.println( "#" + graph.getNodeOrigId( node ) + "-dijkstra: " );
            }
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                state = dijkstraPriorityQueue.extractMin();

                // DEBUG
                if ( graph.getNodeOrigId( node ) == nodeOfInterest && ( graph.getNodeOrigId( state.getNode() ) == 41329 || nodeOfInterest < 0 ) ) {
                    out.println( "#" + graph.getNodeOrigId( node ) + "-extracted: " + nodeDistanceArray.get( state ) + ", " + toString( nodePredecessorArray, state, graph, data ) );
                }

                float currentDistance = nodeDistanceArray.get( state );
//                System.out.println( "Extracted-" + state + ", distance = " + currentDistance );
                //   use the longest distance as an upper bound
                if ( DoubleComparator.isLowerThan( upperBound, currentDistance, PRECISION ) ) {
                    dijkstraPriorityQueue.clear();
                    break;
                }
                // calculate SP ...
                TIntIterator it = data.getOutgoingEdgesIterator( state.getNode() );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    int target = data.getTarget( edge );
//                    if ( graph.getNodeOrigId( node ) == nodeOfInterest && ( graph.getNodeOrigId( from ) == 12410 || nodeOfInterest < 0 ) ) {
//                        out.println( "#" + graph.getNodeOrigId( node ) + "-validity-from: " + graph.getNodeOrigId( state.getNode() ) + ", to: " + graph.getNodeOrigId( target ) + " = " + data.isValidWay( state, edge, nodePredecessorArray ) );
//                    }
                    if ( !data.isValidWay( state, edge, nodePredecessorArray ) ) {
                        continue;
                    }
                    NodeState targetState = NodeState.Factory.newInstance( target, edge );
                    float targetDistance = ( nodeDistanceArray.containsKey( targetState ) ) ? nodeDistanceArray.get( targetState ) : Float.MAX_VALUE;
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && DoubleComparator.isLowerThan( newDistance, targetDistance, PRECISION ) ) {
                        nodeDistanceArray.put( targetState, newDistance );
                        nodePredecessorArray.put( targetState, state );
                        dijkstraPriorityQueue.notifyDataChange( targetState, newDistance );
                        if ( !nodeMinDistanceMap.containsKey( target ) || nodeMinDistanceMap.get( target ) > newDistance ) {
                            nodeMinDistanceMap.put( target, newDistance );
                        }
                    }
                }
            }
            //   for each target compare calculated SP with the shortest distance
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }
//                System.out.println( "#" + node + " - path: " + from + " -> " + to );
                float distance = nodeMinDistanceMap.containsKey( to ) ? nodeMinDistanceMap.get( to ) : Float.MAX_VALUE;
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
//                System.out.println( "#" + node + " - path: " + from + " -> " + to + " => shortcut distance = " + shortcutDistanceT.c + ", path distance = " + distance );

                // DEBUG
                if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
                    out.println( "#" + graph.getNodeOrigId( node ) + "-comparing shortcuts: #" + graph.getNodeOrigId( from ) + "-" + graph.getNodeOrigId( to ) + " -> "
                            + ( shortcutDistanceT != null && DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) + " -> " + shortcutDistanceT.c + " < " + distance + " ?" );
                }
                if ( from == graph.getNodeByOrigId( 12410 ) && to == graph.getNodeByOrigId( 41329 ) ) {
                    out.println( "#" + graph.getNodeOrigId( node ) + "-found path from 12410 to 41329: " + ( ( shortcutDistanceT != null ) ? shortcutDistanceT.c : 0 ) + " -> " + ( shortcutDistanceT != null && DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) );
                }

                //   create shortcut if longer
                if ( shortcutDistanceT != null && DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) {
//                    System.out.println( "#" + node + " - creating shortcut: " + from + " -> " + to );
                    // DEBUG
//                    if ( graph.getNodeOrigId( from ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                        out.println( "#" + graph.getNodeOrigId( from ) + "-adding outgoing edge #" + data.shortcutCounter );
//                    }
//                    if ( graph.getNodeOrigId( to ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                        out.println( "#" + graph.getNodeOrigId( to ) + "-adding outgoing edge #" + data.shortcutCounter );
//                    }
                    nodeDegrees[from]++;
                    nodeDegrees[to]++;
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                } else {
//                    System.out.println( "#" + node + " - found path: " + distance );
                }
            }
            // reset node distances
            nodeDistanceArray.clear();
            nodeMinDistanceMap.clear();
            nodePredecessorArray.clear();
        }

    }

    private String toString( Map<NodeState, NodeState> nodePredecessorArray, NodeState nodeState, Graph graph, ProcessingData data ) {
        StringBuilder sb = new StringBuilder();
        NodeState state = nodeState;
        while ( state != null ) {
            sb.append( graph.getNodeOrigId( state.getNode() ) ).append( " " );
            state = nodePredecessorArray.get( state );
        }
        return sb.toString();
    }

    private Pair<Integer, Double> extractMin( NodeDataStructure<Integer> priorityQueue, Graph graph ) {
        double minValue = priorityQueue.minValue();
        int minNode = priorityQueue.extractMin();
        // DEBUG
//        double precision = 0.001;
//        List<Integer> mins = new ArrayList<>();
//        int minNode = -1;
//        while ( DoubleComparator.isEqualTo( priorityQueue.minValue(), minValue, precision ) ) {
//            int n = priorityQueue.extractMin();
//            mins.add( n );
//            if ( minNode == -1 || graph.getNodeOrigId( n ) < graph.getNodeOrigId( minNode ) ) {
//                minNode = n;
//            }
//        }
//        for ( Integer min : mins ) {
//            if ( !min.equals( minNode ) ) {
//                priorityQueue.add( min, minValue );
//            }
//        }
//        if ( graph.getNodeOrigId( minNode ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            out.println( "extracted: " + graph.getNodeOrigId( minNode ) + ", " + minValue );
//        }
        return new Pair<>( minNode, minValue );
    }

    private static class IntegerArray {

        final int[] array;

        public IntegerArray( int size ) {
            array = new int[size];
        }

    }

}
