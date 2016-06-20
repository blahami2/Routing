/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators.BasicEdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.calculators.SpatialHeuristicEdgeDifferenceCalculator;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.LazyRecalculationStrategy;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.NeighboursOnlyRecalculationStrategy;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
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
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    }

    @Override
    public void setNodeRecalculationStrategy( NodeRecalculationStrategy nodeRecalculationStrategy ) {
        this.nodeRecalculationStrategy = nodeRecalculationStrategy;
    }

    @Override
    public void setEdgeDifferenceCalculator( EdgeDifferenceCalculator edgeDifferenceCalculator ) {
        this.edgeDifferenceCalculator = edgeDifferenceCalculator;
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

        int nodeCount = graph.getNodeCount();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        NodeDataStructure<Integer> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        float[] nodeDistanceArray = new float[nodeCount];
        int[] nodePredecessorArray = new int[nodeCount];
        graph.resetNodeDistanceArray( nodeDistanceArray );
        graph.resetNodePredecessorArray( nodePredecessorArray );
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
//            out.println("node#" + node + " = " + graph.getNodeOrigId( node ));
            int numberOfShortcuts = calculateShortcuts( data, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray, nodePredecessorArray, graph );
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
            contractNode( data, nodeDegrees, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray, nodePredecessorArray, graph );

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
                    int numberOfShortcuts = calculateShortcuts( data, removedNodes, n, dijkstraPriorityQueue, nodeDistanceArray, nodePredecessorArray, graph );
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

    private int calculateShortcuts( ProcessingData data, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray, int[] nodePredecessorArray, Graph graph ) {
        if ( removedNodes.get( node ) ) {
            return 0;
        }
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
        Set<Integer> visitedNodes = new HashSet<>();
        List<Pair<Integer, Integer>> addedShortcuts = new ArrayList<>();
        // for each source calculate Dijkstra to all the targets {without using the node)
        for ( int from : sources ) {
            float upperBound = upperBounds.get( from );
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            // DEBUG
//            if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                out.println( "#" + graph.getNodeOrigId( node ) + "-Dijkstra for #" + graph.getNodeOrigId( from ) + ", upper bound = " + upperBound );
//            }
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                float currentDistance = nodeDistanceArray[currentNode];
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
                TIntIterator it = data.getOutgoingEdgesIterator( currentNode );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    if ( !data.isValidWay( node, edge, nodePredecessorArray ) ) {
                        continue;
                    }
                    int target = data.getTarget( edge );
                    float targetDistance = nodeDistanceArray[target];
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && DoubleComparator.isLowerThan( newDistance, targetDistance, PRECISION ) ) {
                        nodeDistanceArray[target] = newDistance;
                        nodePredecessorArray[target] = edge;
                        visitedNodes.add( target );
                        dijkstraPriorityQueue.notifyDataChange( target, newDistance );
                    }
                }
            }
            //   for each target compare calculated SP with the shortest distance
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }
//                System.out.println( "#" + node + " - path: " + from + " -> " + to );
                float distance = nodeDistanceArray[to];
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
                //   create shortcut if longer
                if ( DoubleComparator.isLowerThan( shortcutDistanceT.c, distance, PRECISION ) ) {
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                    addedShortcuts.add( new Pair<>( from, to ) );
                    // DEBUG
//                    if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//                        out.println( "#" + graph.getNodeOrigId( node ) + "- adding shortcut #" + graph.getNodeOrigId( from ) + " -> #" + graph.getNodeOrigId( to ) );
//                    }
//                    System.out.println( "#" + node + " - creating temporary shortcut: " + from + " -> " + to );
                }
            }
            // reset node distances
            for ( int visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Graph.DISTANCE_DEFAULT;
                nodePredecessorArray[visitedNode] = Graph.PREDECESSOR_DEFAULT;
            }
        }
        // delete added shortcuts
        for ( int i = 0; i < addedShortcuts.size(); i++ ) {
            data.removeLastShortcut();
        }
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
        return addedShortcuts.size();
    }

    private void contractNode( ProcessingData data, int[] nodeDegrees, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray, int[] nodePredecessorArray, Graph graph ) {
        // disable node
        removedNodes.set( node, true );

        // DEBUG
//        if ( graph.getNodeOrigId( node ) == nodeOfInterest || nodeOfInterest < 0 ) {
//            out.println( "contract for #" + graph.getNodeOrigId( node ) );
//        }
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
        Set<Integer> visitedNodes = new HashSet<>();
        // for each source calculate Dijkstra to all the targets {without using the node)
        for ( int from : sources ) {
            float upperBound = upperBounds.get( from );
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                float currentDistance = nodeDistanceArray[currentNode];
                //   use the longest distance as an upper bound
                if ( DoubleComparator.isLowerThan( upperBound, currentDistance, PRECISION ) ) {
                    dijkstraPriorityQueue.clear();
                    break;
                }
                // calculate SP ...
                TIntIterator it = data.getOutgoingEdgesIterator( currentNode );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    if ( !data.isValidWay( node, edge, nodePredecessorArray ) ) {
                        continue;
                    }
                    int target = data.getTarget( edge );
                    float targetDistance = nodeDistanceArray[target];
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && DoubleComparator.isLowerThan( newDistance, targetDistance, PRECISION ) ) {
                        nodeDistanceArray[target] = newDistance;
                        nodePredecessorArray[target] = edge;
                        visitedNodes.add( target );
                        dijkstraPriorityQueue.notifyDataChange( target, newDistance );
                    }
                }
            }
            //   for each target compare calculated SP with the shortest distance
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }
//                System.out.println( "#" + node + " - path: " + from + " -> " + to );
                float distance = nodeDistanceArray[to];
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
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
            for ( int visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Graph.DISTANCE_DEFAULT;
                nodePredecessorArray[visitedNode] = Graph.PREDECESSOR_DEFAULT;
            }
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
        public final Graph graph;
        public final TIntObjectMap<List<TIntList>> turnRestrictions = new TIntObjectHashMap<>();
        public final TIntObjectMap<List<Pair<Integer, Integer>>> shortcutsTrs = new TIntObjectHashMap<>(); // key = shortcut, value = list{a = node, b = sequence}
        private int shortcutCounter = 0;

        public ProcessingData( Graph graph ) {
            this.graph = graph;
            incomingShortcuts = new TIntArrayList[graph.getNodeCount()];
            outgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
            // add all shortcuts from graph
            if ( graph.getTurnRestrictions() != null ) {
                int[][][] tr = graph.getTurnRestrictions();
                for ( int i = 0; i < graph.getNodeCount(); i++ ) {
                    int[][] nodeTr = tr[i];
                    if ( nodeTr != null ) {
                        List<TIntList> sequences = turnRestrictions.get( i );
                        if ( sequences == null ) {
                            sequences = new ArrayList<>();
                            turnRestrictions.put( i, sequences );
                        }
                        for ( int j = 0; j < nodeTr.length; j++ ) {
                            sequences.add( new TIntArrayList( nodeTr[j] ) );
                            for ( int k = 0; k < nodeTr[j].length; k++ ) {
                                int edge = nodeTr[j][k];
                                List<Pair<Integer, Integer>> pairs = shortcutsTrs.get( edge );
                                if ( pairs == null ) {
                                    pairs = new ArrayList<>();
                                    shortcutsTrs.put( edge, pairs );
                                }
                                pairs.add( new Pair<>( i, sequences.size() - 1 ) );
                            }
                        }
                    }
                }
            }
        }

        public void addShortcut( int startEdge, int endEdge ) {
            int source = getSource( startEdge );
            int target = getTarget( endEdge );

            int thisId = sources.size() + graph.getEdgeCount();
            if ( thisId == source || thisId == target ) {
                throw new AssertionError( "shortcut #" + thisId + " = " + source + " -> " + target );
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
            incomingShortcuts[target].add( shortcutCounter + graph.getEdgeCount() );
//            System.out.println( "shortcut - incoming[#" + target + "] = " + incomingShortcuts[target] );
            if ( outgoingShortcuts[source] == null ) {
                outgoingShortcuts[source] = new TIntArrayList();
            }
            outgoingShortcuts[source].add( shortcutCounter + graph.getEdgeCount() );
//            System.out.println( "shortcut - outgoing[#" + source + "] = " + outgoingShortcuts[source] );

            // ADD TR if needed
            Set<Pair<Integer, Integer>> trSet = new HashSet<>();
            if ( shortcutsTrs.containsKey( startEdge ) ) {
                List<Pair<Integer, Integer>> pairs = shortcutsTrs.get( startEdge );
                for ( Pair<Integer, Integer> pair : pairs ) {
                    TIntList sequence = turnRestrictions.get( pair.a ).get( pair.b );
                    if ( endEdge == sequence.get( 0 ) ) {
                        trSet.add( pair );
                    } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) {
                        trSet.add( pair );
                    } else {
                        for ( int i = 0; i < sequence.size(); i++ ) {
                            int edge = sequence.get( i );
                            if ( edge == startEdge ) {
                                if ( ( i + 1 < sequence.size() && sequence.get( i + 1 ) == endEdge ) || i + 1 == sequence.size() ) {
                                    trSet.add( pair );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if ( shortcutsTrs.containsKey( endEdge ) ) {
                List<Pair<Integer, Integer>> pairs = shortcutsTrs.get( endEdge );
                for ( Pair<Integer, Integer> pair : pairs ) {
                    TIntList sequence = turnRestrictions.get( pair.a ).get( pair.b );
                    if ( endEdge == sequence.get( 0 ) ) {
                        trSet.add( pair );
                    } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) {
                        trSet.add( pair );
                    } else {
                        for ( int i = 0; i < sequence.size(); i++ ) {
                            int edge = sequence.get( i );
                            if ( edge == startEdge ) {
                                if ( ( i + 1 < sequence.size() && sequence.get( i + 1 ) == endEdge ) || i + 1 == sequence.size() ) {
                                    trSet.add( pair );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            for ( Pair<Integer, Integer> pair : trSet ) {
                int node = pair.a;
                int seqId = pair.b;
                TIntList sequence = turnRestrictions.get( node ).get( seqId );
                TIntArrayList seq = new TIntArrayList();
                int lastNode = -1;
                int lastEdge = -1;
                { // get the FIRST node independent of the direction
                    int e1 = sequence.get( 0 );
                    int s1 = getSource( e1 );
                    int t1 = getTarget( e1 );
                    int e2 = sequence.get( 1 );
                    int s2 = getSource( e2 );
                    int t2 = getTarget( e2 );
                    if ( t1 == s2 || t1 == t2 ) {
                        lastNode = s1;
                    } else if ( s1 == s2 || s1 == t2 ) {
                        lastNode = t1;
                    }
                }
                if ( endEdge == sequence.get( 0 ) ) {
                    seq.add( shortcutCounter + graph.getEdgeCount() );
                    for ( int i = 1; i < sequence.size(); i++ ) {
                        seq.add( sequence.get( i ) );
                    }
                    lastNode = node;
                } else if ( startEdge == sequence.get( sequence.size() - 1 ) ) {
                    for ( int i = 0; i < sequence.size() - 1; i++ ) {
                        seq.add( sequence.get( i ) );
                    }
                    seq.add( shortcutCounter + graph.getEdgeCount() );
                    lastNode = node;
                } else {
                    for ( int i = 0; i < sequence.size(); i++ ) { // for each part of this sequence
                        // add to the list until the start and edge are met - add them as one
                        // save last node
                        if ( lastEdge != -1 ) {
                            lastNode = getOtherNode( lastEdge, lastNode );
                        }
                        if ( sequence.get( i ) == startEdge && ( i + 1 == sequence.size() || sequence.get( i + 1 ) == endEdge ) ) {
                            lastEdge = shortcutCounter + graph.getEdgeCount();
                            i++; // move past the target edge
                        } else {
                            lastEdge = sequence.get( i );
                        }
                        seq.add( lastEdge );
                    }
                }
                List<TIntList> get = turnRestrictions.get( lastNode );
                get.add( seq );
                shortcutsTrs.get( shortcutCounter + graph.getEdgeCount() ).add( new Pair<>( lastNode, get.size() - 1 ) );
            }
            shortcutCounter++;
        }

        public void removeLastShortcut() {
            shortcutCounter--;
            int startEdge = startEdges.get( shortcutCounter );
            int endEdge = endEdges.get( shortcutCounter );
//            System.out.println( "#" + shortcutCounter + " - removing shortcut[edges] - " + startEdges.get( shortcutCounter ) + " -> " + endEdges.get( shortcutCounter ) );
            int source = sources.get( shortcutCounter );
            sources.removeAt( shortcutCounter );
            int target = targets.get( shortcutCounter );
            targets.removeAt( shortcutCounter );
            startEdges.removeAt( shortcutCounter );
            endEdges.removeAt( shortcutCounter );
            lengths.removeAt( shortcutCounter );
            outgoingShortcuts[source].removeAt( outgoingShortcuts[source].size() - 1 );
            incomingShortcuts[target].removeAt( incomingShortcuts[target].size() - 1 );
            if ( shortcutsTrs.containsKey( startEdge ) ) {
                List<Pair<Integer, Integer>> pairs = shortcutsTrs.get( startEdge );
                for ( Pair<Integer, Integer> pair : pairs ) {
                    turnRestrictions.get( pair.a ).remove( pair.b.intValue() );
                }
            }
            if ( shortcutsTrs.containsKey( endEdge ) ) {
                List<Pair<Integer, Integer>> pairs = shortcutsTrs.get( endEdge );
                for ( Pair<Integer, Integer> pair : pairs ) {
                    turnRestrictions.get( pair.a ).remove( pair.b.intValue() );
                }
            }
        }

        public int size() {
            return sources.size();
        }

        public boolean evaluableEdge( int edge ) {
            return edge < ( graph.getEdgeCount() + size() );
        }

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
            return sources.get( edge - graph.getEdgeCount() );
        }

        public int getTarget( int edge ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getTarget( edge );
            }
            return targets.get( edge - graph.getEdgeCount() );
        }

        public float getLength( int edge ) {
            if ( edge < graph.getEdgeCount() ) {
                return graph.getLength( edge );
            }
            return lengths.get( edge - graph.getEdgeCount() );
        }

        private boolean isValidWay( int node, int edge, int[] nodePredecessorArray ) {
            // what if predecessor is a shortcut...
            if ( turnRestrictions == null ) { // without turn restrictions, everything is valid
                return true;
            }
            if ( !turnRestrictions.containsKey( node ) ) { // without turn restrictions for the concrete node, every turn is valid
                return true;
            }
            List<TIntList> sequences = turnRestrictions.get( node );
            for ( int i = 0; i < sequences.size(); i++ ) { // for all restrictions for this node
                TIntList edgeSequence = sequences.get( i ); // load the edge sequence of this particular restrictions
                if ( edgeSequence.get( edgeSequence.size() - 1 ) == edge ) { // if the last edge of this sequence is the target edge
                    int currNode = node;
                    for ( int j = edgeSequence.size() - 2; j >= 0; j-- ) { // for every edge in the sequence (except for the last, it is already checked) compare it with the predecessor
                        int pred = nodePredecessorArray[currNode];
                        currNode = getOtherNode( pred, currNode );
                        if ( pred != edgeSequence.get( j ) ) { // this turn restriction edge sequence does not match the way
                            break;
                        }
                        if ( j == 0 ) { // all passed, the turn restriction edge sequence matches the way, therefore it is forbidden
                            return false;
                        }
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
//                System.out.println( "#" + node + " - IN counter = " + position );
                if ( incomingShortcuts[node] == null ) {
                    incomingShortcuts[node] = new TIntArrayList();
                }
//                System.out.println( "#" + node + " - IN comparison: " + ( position + 1 ) + " < " + graph.getIncomingEdges( node ).length + " + " + incomingShortcuts[node].size() );
//                System.out.println( "#" + node + " - IN has next = " + ( position + 1 < graph.getIncomingEdges( node ).length + incomingShortcuts[node].size() ) );
                return position + 1 < graph.getIncomingEdges( node ).length + incomingShortcuts[node].size();
            }

            @Override
            public int next() {
                int next;
                position++;
                if ( position < graph.getIncomingEdges( node ).length ) {
                    next = graph.getIncomingEdges( node )[position];
                } else {
                    next = incomingShortcuts[node].get( position - graph.getIncomingEdges( node ).length );
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
//                System.out.println( "#" + node + " - OUT counter = " + position );
                boolean hasNext;
                if ( outgoingShortcuts[node] == null ) {
                    outgoingShortcuts[node] = new TIntArrayList();
                }
                hasNext = position + 1 < graph.getOutgoingEdges( node ).length + outgoingShortcuts[node].size();
//                System.out.println( "#" + node + " - OUT comparison: " + ( position + 1 ) + " < " + graph.getOutgoingEdges( node ).length + " + " + outgoingShortcuts[node].size() );
//                System.out.println( "#" + node + " - OUT has next = " + hasNext );
                return hasNext;
            }

            @Override
            public int next() {
                int next;
                position++;
                if ( position < graph.getOutgoingEdges( node ).length ) {
                    next = graph.getOutgoingEdges( node )[position];
                } else {
                    next = outgoingShortcuts[node].get( position - graph.getOutgoingEdges( node ).length );
                }
//                System.out.println( "#" + node + " - next = " + next );
//                System.out.println( "#" + node + "outgoing shorctus " + outgoingShortcuts[node] );
                return next;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }
}
