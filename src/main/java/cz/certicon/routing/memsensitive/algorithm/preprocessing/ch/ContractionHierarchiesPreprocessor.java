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
import cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.strategies.NeighboursOnlyRecalculationStrategy;
import cz.certicon.routing.memsensitive.model.entity.DistanceType;
import cz.certicon.routing.memsensitive.model.entity.Graph;
import cz.certicon.routing.memsensitive.model.entity.ch.PreprocessedData;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.ch.ChDataBuilder;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.model.utility.progress.EmptyProgressListener;
import cz.certicon.routing.utils.efficient.BitArray;
import cz.certicon.routing.utils.efficient.LongBitArray;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
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

    private static final int THREADS = 8;

    private static final double INIT_NODE_RANKING = 0.1;

    private static final double PRECISION = 10E-6;

    private NodeRecalculationStrategy nodeRecalculationStrategy;
    private EdgeDifferenceCalculator edgeDifferenceCalculator;

    public ContractionHierarchiesPreprocessor() {
        this.nodeRecalculationStrategy = new NeighboursOnlyRecalculationStrategy();
        this.edgeDifferenceCalculator = new BasicEdgeDifferenceCalculator();
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
    }

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId ) {
        return preprocess( dataBuilder, graph, distanceType, startId, new EmptyProgressListener() );
    }

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId, ProgressListener progressListener ) {
        this.nodeRecalculationStrategy.setEdgeDifferenceCalculator( edgeDifferenceCalculator );
        int nodeCount = graph.getNodeCount();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        NodeDataStructure<Integer> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        float[] nodeDistanceArray = new float[nodeCount];
        graph.resetNodeDistanceArray( nodeDistanceArray );
        ProcessingData data = new ProcessingData( graph );
        BitArray removedNodes = new LongBitArray( graph.getNodeCount() );
        BitArray calculatedNeighbours = new LongBitArray( graph.getNodeCount() );
        int[] nodeDegrees = new int[graph.getNodeCount()];
//
        progressListener.init( nodeCount, INIT_NODE_RANKING );

        for ( int node = 0; node < nodeCount; node++ ) {
            nodeDegrees[node] = graph.getNodeDegree( node );
        }
        for ( int node = 0; node < nodeCount; node++ ) {
            int numberOfShortcuts = calculateShortcuts( data, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray );
            int ed = nodeRecalculationStrategy.getEdgeDifferenceCalculator().calculate( -1, nodeDegrees, node, numberOfShortcuts );
//            System.out.println( "#" + node + " = " + ed );
            priorityQueue.add( node, ed );
            progressListener.nextStep();
        }
        int rank = 1;
        progressListener.init( priorityQueue.size(), 1.0 - INIT_NODE_RANKING );
        while ( !priorityQueue.isEmpty() ) {
            Pair<Integer, Double> extractMin = extractMin( priorityQueue );
            int node = extractMin.a;
            // shortcuts
//            System.out.println( "contracting: " + node );
            contractNode( data, nodeDegrees, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray );

            TIntIterator it = nodeRecalculationStrategy.recalculationIterator( graph, data, node, priorityQueue );
//            System.out.println( "#" + node + " - returned recalculation iterator" );
            while ( it.hasNext() ) {
                int n = it.next();
//                System.out.println( "#" + node + " - iterated to: " + n );
                // if not calculated yet and not removed yet
                if ( !calculatedNeighbours.get( n ) && !removedNodes.get( n ) ) {
                    int numberOfShortcuts = calculateShortcuts( data, removedNodes, n, dijkstraPriorityQueue, nodeDistanceArray );
                    nodeRecalculationStrategy.onShortcutsCalculated( graph, nodeDegrees, n, priorityQueue, numberOfShortcuts, node );
                    calculatedNeighbours.set( n, true );
                }
            }
            // reset calculated neighbours
            it = nodeRecalculationStrategy.recalculationIterator( graph, data, node, priorityQueue );
            while ( it.hasNext() ) {
                calculatedNeighbours.set( it.next(), false );
            }
            dataBuilder.setRank( graph.getNodeOrigId( node ), rank++ );
            progressListener.nextStep();
        }
        for ( int i = 0; i < data.size(); i++ ) {
//            System.out.println( "adding shortcut: #" + ( startId + i ) );
            dataBuilder.addShortcut( startId + i, data.getEdgeOrigId( data.startEdges.get( i ), startId ), data.getEdgeOrigId( data.endEdges.get( i ), startId ) );
        }
        return dataBuilder.build();
    }

    private int calculateShortcuts( ProcessingData data, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray ) {
        if ( removedNodes.get( node ) ) {
            return 0;
        }
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
                        if ( !fromToDistanceMap.containsKey( pair ) || newDistance < fromToDistanceMap.get( pair ).c ) {
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance );
                        }
                        //   find the longest distance for each source
                        if ( !upperBounds.containsKey( sourceNode ) || upperBounds.get( sourceNode ) < newDistance ) {
                            upperBounds.put( sourceNode, newDistance );
                        }
                    }
                }
            }
        }

        Set<Integer> visitedNodes = new HashSet<>();
        List<Pair<Integer, Integer>> addedShortcuts = new ArrayList<>();
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
                if ( upperBound < currentDistance ) {
                    dijkstraPriorityQueue.clear();
                    break;
                }
                // calculate SP ...
                TIntIterator it = data.getOutgoingEdgesIterator( currentNode );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    int target = data.getTarget( edge );
                    float targetDistance = nodeDistanceArray[target];
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && newDistance < targetDistance ) {
                        nodeDistanceArray[target] = newDistance;
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
                if ( shortcutDistanceT.c < distance ) {
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                    addedShortcuts.add( new Pair<>( from, to ) );
//                    System.out.println( "#" + node + " - creating temporary shortcut: " + from + " -> " + to );
                }
            }
            // reset node distances
            for ( int visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Float.MAX_VALUE;
            }
        }
        // delete added shortcuts
        for ( Pair<Integer, Integer> addedShortcut : addedShortcuts ) {
//            int source = addedShortcut.a;
//            int target = addedShortcut.b;
//            System.out.println( "#" + node + " - removing temporary shortcut: " + source + " -> " + target );
            data.removeLastShortcut();
        }
        removedNodes.set( node, false );
        return addedShortcuts.size();
    }

    private void contractNode( ProcessingData data, int[] nodeDegrees, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray ) {
        // disable node
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
                        nodeDegrees[sourceNode]--;
                        nodeDegrees[targetNode]--;
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        float newDistance = data.getLength( incomingEdge ) + data.getLength( outgoingEdge );
                        if ( !fromToDistanceMap.containsKey( pair ) || newDistance < fromToDistanceMap.get( pair ).c ) {
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
//                            System.out.println( "#" + node + " - neighbours: " + sourceNode + " -> " + targetNode + " = " + newDistance + "[" + incomingEdge + " -> " + outgoingEdge + "]" );
                        }
                        //   find the longest distance for each source
                        if ( !upperBounds.containsKey( sourceNode ) || upperBounds.get( sourceNode ) < newDistance ) {
                            upperBounds.put( sourceNode, newDistance );
                        }
                    }
                }
            }
        }

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
                if ( upperBound < currentDistance ) {
                    dijkstraPriorityQueue.clear();
                    break;
                }
                // calculate SP ...
                TIntIterator it = data.getOutgoingEdgesIterator( currentNode );
                while ( it.hasNext() ) {
                    int edge = it.next();
                    int target = data.getTarget( edge );
                    float targetDistance = nodeDistanceArray[target];
                    float newDistance = currentDistance + data.getLength( edge );
                    // if the target is active (removal simulation)
                    if ( !removedNodes.get( target ) && newDistance < targetDistance ) {
                        nodeDistanceArray[target] = newDistance;
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
                if ( shortcutDistanceT.c < distance ) {
//                    System.out.println( "#" + node + " - creating shortcut: " + from + " -> " + to );
                    nodeDegrees[from]++;
                    nodeDegrees[to]++;
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                } else {
//                    System.out.println( "#" + node + " - found path: " + distance );
                }
            }
            // reset node distances
            for ( int visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Float.MAX_VALUE;
            }
        }

    }

    private Pair<Integer, Double> extractMin( NodeDataStructure<Integer> priorityQueue ) {
        double minValue = priorityQueue.minValue();
        int minNode = priorityQueue.extractMin();
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
        private int shortcutCounter = 0;

        public ProcessingData( Graph graph ) {
            this.graph = graph;
            incomingShortcuts = new TIntArrayList[graph.getNodeCount()];
            outgoingShortcuts = new TIntArrayList[graph.getNodeCount()];
        }

        public void addShortcut( int startEdge, int endEdge ) {
            int source = getSource( startEdge );
            int target = getTarget( endEdge );
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
            shortcutCounter++;
//            System.out.println( "shortcut - outgoing[#" + source + "] = " + outgoingShortcuts[source] );
        }

        public void removeLastShortcut() {
            shortcutCounter--;
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
