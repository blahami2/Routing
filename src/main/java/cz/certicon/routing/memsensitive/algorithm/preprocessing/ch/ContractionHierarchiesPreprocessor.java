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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesPreprocessor implements Preprocessor<PreprocessedData> {

    // DEBUG
//    public PrintStream out = null;
//    public List<Pair<Integer, String>> shortcutCounts = new ArrayList<>();
//    public int nodeOfInterest = -1;
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
//        try {
//            out = new PrintStream( new File( "C:\\Routing\\Testing\\minimized.txt" ) );
//            ( (NeighboursOnlyRecalculationStrategy) nodeRecalculationStrategy ).preprocessor = this;
//        } catch ( FileNotFoundException ex ) {
//            Logger.getLogger( ContractionHierarchiesPreprocessor.class.getName() ).log( Level.SEVERE, null, ex );
//        }
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
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance );
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
                    if ( !data.isValidWay( state, edge, nodePredecessorArray ) ) {
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
                if ( DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) {
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
//        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            out.println( "contract for #" + graph.getNodeOrigId( node ) );
//        }
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
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance + "[" + incomingEdge + " -> " + outgoingEdge + "]" );
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
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                state = dijkstraPriorityQueue.extractMin();
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
                    if ( !data.isValidWay( state, edge, nodePredecessorArray ) ) {
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
                //   create shortcut if longer
                if ( DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) {
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

    public static class ProcessingData {

        public final TIntList sources = new TIntArrayList();
        public final TIntList targets = new TIntArrayList();
        public final TIntList startEdges = new TIntArrayList();
        public final TIntList endEdges = new TIntArrayList();
        public final TIntList[] incomingShortcuts;
        public final TIntList[] outgoingShortcuts;
        public final TFloatList lengths = new TFloatArrayList();

        public final TIntList tmpSources = new TIntArrayList();
        public final TIntList tmpTargets = new TIntArrayList();
        public final TIntList tmpStartEdges = new TIntArrayList();
        public final TIntList tmpEndEdges = new TIntArrayList();
        public final TIntList[] tmpIncomingShortcuts;
        public final TIntList[] tmpOutgoingShortcuts;
        public final TFloatList tmpLengths = new TFloatArrayList();
        public final TIntSet tmpNodes = new TIntHashSet();
        public final Graph graph;
        public final TIntObjectMap<List<ShortcutLocator>> edgeTrs = new TIntObjectHashMap<>(); // key = edge, value = list{a = node, b = sequence}
        public final TIntObjectMap<List<TIntList>> turnRestrictions = new TIntObjectHashMap<>();
        public final TIntObjectMap<List<ShortcutLocator>> shortcutsTrs = new TIntObjectHashMap<>(); // key = shortcut, value = list{a = node, b = sequence}
        public final TIntObjectMap<List<TIntList>> tmpTurnRestrictions = new TIntObjectHashMap<>();
        public final TIntObjectMap<List<ShortcutLocator>> tmpShortcutsTrs = new TIntObjectHashMap<>(); // key = shortcut, value = list{a = node, b = sequence}
        private int shortcutCounter = 0;
        private int tmpShortcutCounter = -1;

        public ProcessingData( Graph graph ) {
            this.graph = graph;
            incomingShortcuts = new TIntArrayList[graph.getNodeCount()];
            outgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
            tmpIncomingShortcuts = new TIntArrayList[graph.getNodeCount()];
            tmpOutgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
            // add only to edgeTrs
            if ( graph.getTurnRestrictions() != null ) {
                int[][][] tr = graph.getTurnRestrictions();
                for ( int i = 0; i < graph.getNodeCount(); i++ ) {
                    int[][] nodeTr = tr[i];
                    if ( nodeTr != null ) {
                        for ( int j = 0; j < nodeTr.length; j++ ) {
                            for ( int k = 0; k < nodeTr[j].length; k++ ) {
                                int edge = nodeTr[j][k];
                                List<ShortcutLocator> pairs = edgeTrs.get( edge );
                                if ( pairs == null ) {
                                    pairs = new ArrayList<>();
                                    edgeTrs.put( edge, pairs );
                                }
                                pairs.add( new ShortcutLocator( i, j ) );
                            }
                        }
                    }
                }
            }
        }

        public void addShortcut( int startEdge, int endEdge ) {
//            System.out.println( "Adding shortcut: #" + ( shortcutCounter + graph.getEdgeCount() ) + " = " + startEdge + " -> " + endEdge );
            int source = getSource( startEdge );
            int target = getTarget( endEdge );

            int thisId = shortcutCounter + graph.getEdgeCount();
            if ( thisId == source || thisId == target ) {
//                throw new AssertionError( "shortcut #" + thisId + " = " + source + " -> " + target );
            }

//            System.out.println( "#" + shortcutCounter + " - adding shortcut[edges] - " + startEdge + " -> " + endEdge );
//            System.out.println( "shortcut[nodes] - " + source + " -> " + target );
            sources.add( source );
//            System.out.println( "shortcut - sources = " + sources );
            targets.add( target );
//            System.out.println( "shortcut - targets = " + targets );
            startEdges.add( startEdge );
//            System.out.println( "shortcut - start edges = " + startEdges );
            endEdges.add( endEdge );
//            System.out.println( "shortcut - end edges = " + endEdges );
            lengths.add( getLength( startEdge ) + getLength( endEdge ) );
            if ( incomingShortcuts[target] == null ) {
                incomingShortcuts[target] = new TIntArrayList();
            }
            incomingShortcuts[target].add( thisId );
//            System.out.println( "shortcut - incoming[#" + target + "] = " + incomingShortcuts[target] );
            if ( outgoingShortcuts[source] == null ) {
                outgoingShortcuts[source] = new TIntArrayList();
            }
            outgoingShortcuts[source].add( thisId );
//            System.out.println( "shortcut - outgoing[#" + source + "] = " + outgoingShortcuts[source] );

            // ADD TR if needed
            Set<ShortcutLocator> trSet = getShortcutLocators( edgeTrs, startEdge, endEdge );
            addTurnRestrictions( trSet, turnRestrictions, shortcutsTrs, startEdge, endEdge, thisId );
            trSet = getShortcutLocators( turnRestrictions, shortcutsTrs, startEdge, endEdge );
            addTurnRestrictions( trSet, turnRestrictions, turnRestrictions, shortcutsTrs, startEdge, endEdge, thisId );
            shortcutCounter++;
        }

        private Set<ShortcutLocator> getShortcutLocators( TIntObjectMap<List<TIntList>> trs, TIntObjectMap<List<ShortcutLocator>> trMap, int startEdge, int endEdge ) {
            Set<ShortcutLocator> shortcutLocators = new HashSet<>();
            int[] edges = { startEdge, endEdge };
            for ( int currentEdge : edges ) {
                if ( trMap.containsKey( currentEdge ) ) { // if there are turn restrictions on the startEdge, apply the restrictions to this shortcut (prepare them for addition)
                    List<ShortcutLocator> pairs = trMap.get( currentEdge ); // obtain all the pairs of starting edge
                    for ( ShortcutLocator pair : pairs ) {
                        TIntList sequence = trs.get( pair.getNode() ).get( pair.getSequenceIndex() ); // get sequence on the given index for the given node
                        if ( endEdge == sequence.get( 0 ) ) { // if the sequence begins with the endEdge, add
                            shortcutLocators.add( pair );
                        } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) { // if the sequence ends with the startEdge, add
                            shortcutLocators.add( pair );
                        } else { // if the sequence just contains the edge, find out whether a turn restriction contains this whole shortcut
                            for ( int i = 0; i < sequence.size(); i++ ) {
                                int edge = sequence.get( i );
                                if ( edge == startEdge ) {
                                    if ( i + 1 < sequence.size() && sequence.get( i + 1 ) == endEdge ) {
                                        shortcutLocators.add( pair );
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return shortcutLocators;
        }

        private Set<ShortcutLocator> getShortcutLocators( /* turn restrictions provided by graph, */ TIntObjectMap<List<ShortcutLocator>> trMap, int startEdge, int endEdge ) {
            Set<ShortcutLocator> shortcutLocators = new HashSet<>();
            int[] edges = { startEdge, endEdge };
            for ( int currentEdge : edges ) {
                if ( trMap.containsKey( currentEdge ) ) { // if there are turn restrictions on the startEdge, apply the restrictions to this shortcut (prepare them for addition)
                    List<ShortcutLocator> pairs = trMap.get( currentEdge ); // obtain all the pairs of starting edge
                    for ( ShortcutLocator pair : pairs ) {
                        int[][][] trs = graph.getTurnRestrictions();
                        int[] sequence = trs[pair.getNode()][pair.getSequenceIndex()]; // get sequence on the given index for the given node
                        if ( endEdge == sequence[0] ) { // if the sequence begins with the endEdge, add
                            shortcutLocators.add( pair );
                        } else if ( startEdge == sequence[sequence.length - 1] ) { // if the sequence ends with the startEdge, add
                            shortcutLocators.add( pair );
                        } else { // if the sequence just contains the edge, find out whether a turn restriction contains this whole shortcut
                            for ( int i = 0; i < sequence.length; i++ ) {
                                int edge = sequence[i];
                                if ( edge == startEdge ) {
                                    if ( i + 1 < sequence.length && sequence[i + 1] == endEdge ) {
                                        shortcutLocators.add( pair );
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return shortcutLocators;
        }

        private void addTurnRestrictions( Set<ShortcutLocator> trSet, TIntObjectMap<List<TIntList>> sourceTurnRestrictions, TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
            for ( ShortcutLocator pair : trSet ) { // for each relevant <node,sequence> pair add new turn-restriction
                int node = pair.getNode();
                int seqId = pair.getSequenceIndex();
                TIntList sequence = sourceTurnRestrictions.get( node ).get( seqId ); // get current sequence
                addTurnRestriction( node, sequence, targetTurnRestrictions, targetTurnRestrictionMap, startEdge, endEdge, shortcutId );
            }
        }

        private void addTurnRestrictions( Set<ShortcutLocator> trSet, /* turn restrictions provided by graph, */ TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
            for ( ShortcutLocator pair : trSet ) { // for each relevant <node,sequence> pair add new turn-restriction
                int node = pair.getNode();
                int seqId = pair.getSequenceIndex();
                TIntList sequence = new TIntArrayList( graph.getTurnRestrictions()[node][seqId] ); // get current sequence
                addTurnRestriction( node, sequence, targetTurnRestrictions, targetTurnRestrictionMap, startEdge, endEdge, shortcutId );
            }
        }

        private void addTurnRestriction( int node, TIntList sequence, TIntObjectMap<List<TIntList>> targetTurnRestrictions, TIntObjectMap<List<ShortcutLocator>> targetTurnRestrictionMap, int startEdge, int endEdge, int shortcutId ) {
            TIntList seq = new TIntArrayList(); // create new sequence for the new turn-restriction
            int lastNode = -1;
            if ( endEdge == sequence.get( 0 ) ) { // if the sequence begins with the endEdge, add this shortcut to the beginning and then copy the rest of the turn-restriction sequence
                seq.add( shortcutId );
                for ( int i = 1; i < sequence.size(); i++ ) {
                    seq.add( sequence.get( i ) );
                }
                lastNode = node;
            } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) { // if the sequence ends with the startEdge, copy all but the last of the turn-restriction, then add this shortcut
                for ( int i = 0; i < sequence.size() - 1; i++ ) {
                    seq.add( sequence.get( i ) );
                }
                seq.add( shortcutId );
                lastNode = node;
            } else {
                int lastEdge = -1;
                { // get the FIRST node independent of the direction // limit variables to this block // TODO what is this for???
                    int e1 = sequence.get( 0 ); // first edge
                    int s1 = getSource( e1 ); // its source and target
                    int t1 = getTarget( e1 );
                    int e2 = sequence.get( 1 ); // second edge
                    int s2 = getSource( e2 ); // its source and target
                    int t2 = getTarget( e2 );
                    if ( t1 == s2 || t1 == t2 ) { // if the target is connected to the second edge - regular direction
                        lastNode = s1;
                    } else if ( s1 == s2 || s1 == t2 ) { // if the source is connected to the second edge - opposite direction
                        lastNode = t1;
                    }
                } // now the 'lastNode' contains the first node of the sequence
                for ( int i = 0; i < sequence.size(); i++ ) { // for each part of this sequence
                    // add to the list until the start and edge are met - add them as one
                    // save last node
                    if ( lastEdge != -1 ) {
                        lastNode = getOtherNode( lastEdge, lastNode ); // get other node at the beginning - will not set after setting the last edge
                    }
                    if ( sequence.get( i ) == startEdge && sequence.get( i + 1 ) == endEdge ) {
                        lastEdge = shortcutId;
                        i++; // move past the target edge
                    } else {
                        lastEdge = sequence.get( i );
                    }
                    seq.add( lastEdge );
                }
            }
            List<TIntList> get = targetTurnRestrictions.get( lastNode ); // add new turn-restriction to trs (create new list if necessary)
            if ( get == null ) {
                get = new ArrayList<>();
                targetTurnRestrictions.put( lastNode, get );
            }
            get.add( seq );
            List<ShortcutLocator> strs = targetTurnRestrictionMap.get( shortcutId ); // add new locator to map (create new list if necessary)
            if ( strs == null ) {
                strs = new ArrayList<>();
                targetTurnRestrictionMap.put( shortcutId, strs );
            }
            strs.add( new ShortcutLocator( lastNode, get.size() - 1 ) );
        }

        public void addTemporaryShortcut( int startEdge, int endEdge ) {
//            System.out.println( "Adding temporary shortcut: #" + ( tmpShortcutCounter + graph.getEdgeCount() ) + " = " + startEdge + " -> " + endEdge );
            int source = getSource( startEdge );
            int target = getTarget( endEdge );
            int thisId = tmpShortcutCounter + shortcutCounter + graph.getEdgeCount();
            if ( thisId == source || thisId == target ) {
//                throw new AssertionError( "shortcut #" + thisId + " = " + source + " -> " + target );
            }
            tmpSources.add( source );
            tmpTargets.add( target );
            tmpStartEdges.add( startEdge );
            tmpEndEdges.add( endEdge );
            tmpLengths.add( getLength( startEdge ) + getLength( endEdge ) );
            if ( tmpIncomingShortcuts[target] == null ) {
                tmpIncomingShortcuts[target] = new TIntArrayList();
            }
            tmpIncomingShortcuts[target].add( thisId );
            tmpNodes.add( target );
            if ( tmpOutgoingShortcuts[source] == null ) {
                tmpOutgoingShortcuts[source] = new TIntArrayList();
            }
            tmpNodes.add( source );
            tmpOutgoingShortcuts[source].add( thisId );

            // ADD TR if needed
            Set<ShortcutLocator> trSet = getShortcutLocators( edgeTrs, startEdge, endEdge );
            addTurnRestrictions( trSet, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
            trSet = getShortcutLocators( turnRestrictions, shortcutsTrs, startEdge, endEdge );
            addTurnRestrictions( trSet, turnRestrictions, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
            trSet = getShortcutLocators( tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge );
            addTurnRestrictions( trSet, tmpTurnRestrictions, tmpTurnRestrictions, tmpShortcutsTrs, startEdge, endEdge, thisId );
            tmpShortcutCounter++;
        }

        public void clearTemporaryShortcuts() {
//            System.out.println( "clearing tmp shortcuts" );
            tmpShortcutsTrs.clear();
            tmpTurnRestrictions.clear();
            tmpShortcutCounter = 0;
            tmpSources.clear();
            tmpTargets.clear();
            tmpStartEdges.clear();
            tmpEndEdges.clear();
            TIntIterator iterator = tmpNodes.iterator();
            while ( iterator.hasNext() ) {
                int n = iterator.next();
                tmpIncomingShortcuts[n] = null;
                tmpOutgoingShortcuts[n] = null;
            }
            tmpLengths.clear();
            tmpNodes.clear();
//            System.out.println( "cleared" );
        }

        public int size() {
            return shortcutCounter;
        }

//        public boolean evaluableEdge( int edge ) {
//            return edge < ( graph.getEdgeCount() + size() );
//        }
        public TIntIterator getIncomingEdgesIterator( int node ) {
            return new IncomingIterator( graph, node );
        }

        public TIntIterator getOutgoingEdgesIterator( int node ) {
            return new OutgoingIterator( graph, node );
        }

        public long getEdgeOrigId( int edge, long startId ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getEdgeOrigId( edge );
            }
            if ( edge >= graph.getEdgeCount() + shortcutCounter ) {
//                throw new AssertionError( "Temporary shortcut@getEdgeOrigId: edge = " + edge + ", edge count = " + graph.getEdgeCount() + ", shortcut counter = " + shortcutCounter );
            }
//            System.out.println( startId + " + " + edge + " - " + graph.getEdgeCount() );
            return startId + edge - graph.getEdgeCount();
        }

        public int getOtherNode( int edge, int node ) {
            int source = getSource( edge );
            if ( source != node ) {
                return source;
            }
            return getTarget( edge );
        }

        public int getSource( int edge ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getSource( edge );
            }
            if ( edge < graph.getEdgeCount() + shortcutCounter ) {
                return sources.get( edge - graph.getEdgeCount() );
            }
            return tmpSources.get( edge - graph.getEdgeCount() - shortcutCounter );
        }

        public int getTarget( int edge ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getTarget( edge );
            }
            if ( edge < graph.getEdgeCount() + shortcutCounter ) {
                return targets.get( edge - graph.getEdgeCount() );
            }
            return tmpTargets.get( edge - graph.getEdgeCount() - shortcutCounter );
        }

        public float getLength( int edge ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getLength( edge );
            }
            if ( edge < graph.getEdgeCount() + shortcutCounter ) {
                return lengths.get( edge - graph.getEdgeCount() );
            }
            return tmpLengths.get( edge - graph.getEdgeCount() - shortcutCounter );
        }

        public boolean isValidWay( NodeState state, int targetEdge, Map<NodeState, NodeState> predecessorArray ) {
            return isValidWay( state, targetEdge, predecessorArray, turnRestrictions ) && isValidWay( state, targetEdge, predecessorArray, tmpTurnRestrictions ) && graph.isValidWay( state, targetEdge, predecessorArray );
        }

        private boolean isValidWay( NodeState state, int targetEdge, Map<NodeState, NodeState> predecessorArray, TIntObjectMap<List<TIntList>> trs ) {
            // what if predecessor is a shortcut... ???
            if ( trs == null ) { // without turn restrictions, everything is valid
                return true;
            }
            int node = state.getNode();
            if ( !trs.containsKey( node ) ) { // without turn restrictions for the concrete node, every turn is valid
                return true;
            }
            List<TIntList> sequences = trs.get( node );
            for ( int i = 0; i < sequences.size(); i++ ) { // for all restrictions for this node
                TIntList edgeSequence = sequences.get( i ); // load the edge sequence of this particular restrictions
                if ( edgeSequence.get( edgeSequence.size() - 1 ) == targetEdge ) { // if the last edge of this sequence is the target edge
                    NodeState currState = state;
                    for ( int j = edgeSequence.size() - 2; j >= 0; j-- ) { // for every edge in the sequence (except for the last, it is already checked) compare it with the predecessor
                        if ( currState.getEdge() != edgeSequence.get( j ) ) {
                            break;
                        }
                        if ( j == 0 ) { // all passed, the turn restriction edge sequence matches the way, therefore it is forbidden
                            return false;
                        }
                        currState = predecessorArray.get( currState );
                    }
                }
            }
            return true;
        }

        private class IncomingIterator implements TIntIterator {

            private final int node;
            private final Graph graph;
            private int position = -1;

            public IncomingIterator( Graph graph, int node ) {
//                System.out.println( "#" + node + " - IN iterator creation" );
                this.node = node;
                this.graph = graph;
            }

            @Override
            public boolean hasNext() { // ... see note at NeighbourListGraph
                int size = incomingShortcuts[node] != null ? incomingShortcuts[node].size() : 0;
                int tmpSize = tmpIncomingShortcuts[node] != null ? tmpIncomingShortcuts[node].size() : 0;
                return ( position + 1 < graph.getIncomingEdges( node ).length + size + tmpSize );
            }

            @Override
            public int next() {
                int next;
                position++;
                int size = incomingShortcuts[node] != null ? incomingShortcuts[node].size() : 0;
                if ( position < graph.getIncomingEdges( node ).length ) {
                    next = graph.getIncomingEdges( node )[position];
                } else if ( position < graph.getIncomingEdges( node ).length + size ) {
                    next = incomingShortcuts[node].get( position - graph.getIncomingEdges( node ).length );
                } else {
                    next = tmpIncomingShortcuts[node].get( position - graph.getIncomingEdges( node ).length - size );
                }
//                System.out.println( "#" + node + " - next = " + next );
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }

        }

        private class OutgoingIterator implements TIntIterator {

            private final int node;
            private int position = -1;
            private final Graph graph;

            public OutgoingIterator( Graph graph, int node ) {
//                System.out.println( "#" + node + " - OUT iterator creation" );
                this.node = node;
                this.graph = graph;
            }

            @Override
            public boolean hasNext() { // see above, analogically
                boolean hasNext;
                int size = outgoingShortcuts[node] != null ? outgoingShortcuts[node].size() : 0;
                int tmpSize = tmpOutgoingShortcuts[node] != null ? tmpOutgoingShortcuts[node].size() : 0;
                hasNext = position + 1 < graph.getOutgoingEdges( node ).length + size + tmpSize;
//                System.out.println( "hasNext=" + hasNext + ", position = " + position + ", edges = " + graph.getOutgoingEdges( node ).length + ", shortcuts = " + size + ", tmpshortcuts = " + tmpSize );
                return hasNext;
            }

            @Override
            public int next() {
                int next;
                position++;
                int size = outgoingShortcuts[node] != null ? outgoingShortcuts[node].size() : 0;
                if ( position < graph.getOutgoingEdges( node ).length ) {
                    next = graph.getOutgoingEdges( node )[position];
                } else if ( position < graph.getOutgoingEdges( node ).length + size ) {
                    next = outgoingShortcuts[node].get( position - graph.getOutgoingEdges( node ).length );
                } else {
//                    System.out.println( "index = " + ( position - graph.getOutgoingEdges( node ).length - size ) );
//                    System.out.println( "size = " + ( tmpOutgoingShortcuts[node] != null ? tmpOutgoingShortcuts[node].size() : 0 ) );
                    next = tmpOutgoingShortcuts[node].get( position - graph.getOutgoingEdges( node ).length - size );
                }
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }

    private static class ShortcutLocator {

        private final int node;
        private final int sequenceIndex;

        public ShortcutLocator( int node, int sequenceIndex ) {
            this.node = node;
            this.sequenceIndex = sequenceIndex;
        }

        public int getNode() {
            return node;
        }

        public int getSequenceIndex() {
            return sequenceIndex;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + this.node;
            hash = 53 * hash + this.sequenceIndex;
            return hash;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( getClass() != obj.getClass() ) {
                return false;
            }
            final ShortcutLocator other = (ShortcutLocator) obj;
            if ( this.node != other.node ) {
                return false;
            }
            if ( this.sequenceIndex != other.sequenceIndex ) {
                return false;
            }
            return true;
        }

    }
}
