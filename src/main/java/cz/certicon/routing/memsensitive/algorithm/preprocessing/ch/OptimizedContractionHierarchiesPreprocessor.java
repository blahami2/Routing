/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm.preprocessing.ch;

import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.memsensitive.algorithm.preprocessing.Preprocessor;
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
public class OptimizedContractionHierarchiesPreprocessor implements Preprocessor<PreprocessedData> {

    private static final int THREADS = 8;

    private static final double INIT_NODE_RANKING = 0.1;

    private static final double PRECISION = 10E-6;

    private double[] edgeLengthArray;
    private int[] edgeSourceArray;
    private int[] edgeTargetArray;
    private int[] contractedNeighboursCount;
    private int[] edArray;
    private long maxId;
    private long startId;
    private Graph graph;

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId ) {
        return preprocess( dataBuilder, graph, distanceType, startId, new EmptyProgressListener() );
    }

    @Override
    public PreprocessedData preprocess( ChDataBuilder<PreprocessedData> dataBuilder, Graph graph, DistanceType distanceType, long startId, ProgressListener progressListener ) {
        int nodeCount = graph.getNodeCount();
        this.graph = graph;
        this.startId = startId;
        contractedNeighboursCount = new int[nodeCount];
        edArray = new int[nodeCount];
        maxId = 0;

        Map<Integer, Integer> contractedNeighboursCountMap = new HashMap<>();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        NodeDataStructure<Integer> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        float[] nodeDistanceArray = new float[nodeCount];
        graph.resetNodeDistanceArray( nodeDistanceArray );
        TmpData data = new TmpData( graph );
        BitArray removedNodes = new LongBitArray( graph.getNodeCount() );
//
        progressListener.init( nodeCount, INIT_NODE_RANKING );
        for ( int node = 0; node < nodeCount; node++ ) {
            int degree = graph.getOutgoingEdges( node ).length;
            int numberOfShortcuts = calculateShortcuts( data, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray );
            priorityQueue.add( node, numberOfShortcuts - degree );
            progressListener.nextStep();
        }
        int rank = 1;
        progressListener.init( priorityQueue.size(), 1.0 - INIT_NODE_RANKING );
        while ( !priorityQueue.isEmpty() ) {
            Pair<Integer, Double> extractMin = extractMin( priorityQueue );
            int node = extractMin.a;
            // shortcuts
            contractNode( data, removedNodes, node, dijkstraPriorityQueue, nodeDistanceArray );
            Set<Integer> neighbours = new HashSet<>();
            TIntIterator it = graph.getOutgoingEdgesIterator( node );
            while ( it.hasNext() ) {
                neighbours.add( graph.getOtherNode( it.next(), node ) );
            }
            for ( int neighbour : neighbours ) {
                int count = 1 + contractedNeighboursCountMap.get( neighbour );
                int numberOfShortcuts = calculateShortcuts( data, removedNodes, neighbour, dijkstraPriorityQueue, nodeDistanceArray );
                if ( priorityQueue.contains( neighbour ) ) {
                    contractedNeighboursCountMap.put( neighbour, count );
                    priorityQueue.notifyDataChange( neighbour, count + numberOfShortcuts - graph.getOutgoingEdges( node ).length );
                }
            }
            dataBuilder.setRank( graph.getNodeOrigId( node ), rank++ );
            progressListener.nextStep();
        }
        for ( int i = 0; i < data.size(); i++ ) {
            dataBuilder.addShortcut( startId++, graph.getEdgeOrigId( data.startEdges.get( i ) ), graph.getEdgeOrigId( data.endEdges.get( i ) ) );
        }
        return dataBuilder.build();
    }

    private int calculateShortcuts( TmpData data, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray ) {
        removedNodes.set( node, true );
        Set<Integer> sources = new HashSet<>();
        Set<Integer> targets = new HashSet<>();
        Map<Integer, Float> upperBounds = new HashMap<>();
        Map<Pair<Integer, Integer>, Trinity<Integer, Integer, Float>> fromToDistanceMap = new HashMap<>();
        // for each pair of edges going through node:
        TIntIterator incomingIterator = data.getIncomingEdgesIterator( node );
        while ( incomingIterator.hasNext() ) {
            int incomingEdge = incomingIterator.next();
            TIntIterator outgoingIterator = data.getOutgoingEdgesIterator( node );
            while ( outgoingIterator.hasNext() ) {
                int outgoingEdge = outgoingIterator.next();
                if ( incomingEdge != outgoingEdge ) {
                    int sourceNode = data.getSource( incomingEdge );
                    int targetNode = data.getTarget( outgoingEdge );
                    //   calculate shortest distance
                    if ( sourceNode != targetNode ) {
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        float newDistance = data.getLength( incomingEdge ) + data.getLength( outgoingEdge );
                        if ( !fromToDistanceMap.containsKey( pair ) || newDistance < fromToDistanceMap.get( pair ).c ) {
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
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
                TIntIterator it = data.getOutgoingEdgesIterator( node );
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
                float distance = nodeDistanceArray[to];
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
                //   create shortcut if longer
                if ( shortcutDistanceT.c < distance - 10E-6 ) {
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
                    addedShortcuts.add( new Pair<>( from, to ) );
                }
            }
            // reset node distances
            for ( int visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Float.MAX_VALUE;
            }
        }
        // delete added shortcuts
        for ( Pair<Integer, Integer> addedShortcut : addedShortcuts ) {
            int source = addedShortcut.a;
            int target = addedShortcut.b;
            data.removeLastShortcut( source, target );
        }
        removedNodes.set( node, false );
        return addedShortcuts.size();
    }

    private void contractNode( TmpData data, BitArray removedNodes, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, float[] nodeDistanceArray ) {
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
            TIntIterator outgoingIterator = data.getOutgoingEdgesIterator( node );
            while ( outgoingIterator.hasNext() ) {
                int outgoingEdge = outgoingIterator.next();
                if ( incomingEdge != outgoingEdge ) {
                    int sourceNode = data.getSource( incomingEdge );
                    int targetNode = data.getTarget( outgoingEdge );
                    //   calculate shortest distance
                    if ( sourceNode != targetNode ) {
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        float newDistance = data.getLength( incomingEdge ) + data.getLength( outgoingEdge );
                        if ( !fromToDistanceMap.containsKey( pair ) || newDistance < fromToDistanceMap.get( pair ).c ) {
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            //   collect sources in a set
                            sources.add( sourceNode );
                            //   collect targets in a set
                            targets.add( targetNode );
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
                TIntIterator it = data.getOutgoingEdgesIterator( node );
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
                float distance = nodeDistanceArray[to];
                Trinity<Integer, Integer, Float> shortcutDistanceT = fromToDistanceMap.get( new Pair<>( from, to ) );
                //   create shortcut if longer
                if ( shortcutDistanceT.c < distance - 10E-6 ) {
                    data.addShortcut( shortcutDistanceT.a, shortcutDistanceT.b );
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

    private static class TmpData {

        public final ArrayList<Integer> sources = new ArrayList<>();
        public final ArrayList<Integer> targets = new ArrayList<>();
        public final ArrayList<Integer> startEdges = new ArrayList<>();
        public final ArrayList<Integer> endEdges = new ArrayList<>();
        public final ArrayList<Integer>[] incomingShortcuts;
        public final ArrayList<Integer>[] outgoingShortcuts;
        public final Graph graph;

        public TmpData( Graph graph ) {
            this.graph = graph;
            incomingShortcuts = new ArrayList[graph.getNodeCount()];
            outgoingShortcuts = new ArrayList[graph.getNodeCount()];
        }

        public void addShortcut( int startEdge, int endEdge ) {
            int source = getSource( startEdge );
            int target = getTarget( endEdge );
            sources.add( source );
            targets.add( target );
            startEdges.add( startEdge );
            endEdges.add( endEdge );
            if ( incomingShortcuts[target] == null ) {
                incomingShortcuts[target] = new ArrayList<>();
            }
            incomingShortcuts[target].add( sources.size() - 1 );
            if ( outgoingShortcuts[source] == null ) {
                outgoingShortcuts[source] = new ArrayList<>();
            }
            outgoingShortcuts[source].add( sources.size() - 1 );
        }

        public void removeLastShortcut( int source, int target ) {
            sources.remove( sources.size() - 1 );
            targets.remove( targets.size() - 1 );
            startEdges.remove( startEdges.size() - 1 );
            endEdges.remove( endEdges.size() - 1 );
            outgoingShortcuts[source].remove( outgoingShortcuts[source].size() - 1 );
            incomingShortcuts[target].remove( incomingShortcuts[target].size() - 1 );
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

        public float getLength( int shortcut ) {
            int start = startEdges.get( shortcut );
            int end = endEdges.get( shortcut );
            float length = 0;
            if ( start >= graph.getEdgeCount() ) {
                length += getLength( start - graph.getEdgeCount() );
            } else {
                length += graph.getLength( start );
            }
            if ( end >= graph.getEdgeCount() ) {
                length += getLength( end - graph.getEdgeCount() );
            } else {
                length += graph.getLength( end );
            }
            return length;
        }

        private class IncomingIterator implements TIntIterator {

            private final int node;
            private final Graph graph;
            private int position = -1;

            public IncomingIterator( Graph graph, int node ) {
                this.node = node;
                this.graph = graph;
            }

            @Override
            public boolean hasNext() { // ... see note at NeighbourListGraph
                return position + 1 < graph.getIncomingEdges( node ).length + incomingShortcuts[node].size();
            }

            @Override
            public int next() {
                if ( position + 1 < graph.getIncomingEdges( node ).length ) {
                    return graph.getIncomingEdges( node )[++position];
                } else {
                    return incomingShortcuts[node].get( ++position - graph.getIncomingEdges( node ).length );
                }
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
                this.node = node;
                this.graph = graph;
            }

            @Override
            public boolean hasNext() { // see above, analogically
                return position + 1 < graph.getOutgoingEdges( node ).length + outgoingShortcuts[node].size();
            }

            @Override
            public int next() {
                if ( position + 1 < graph.getOutgoingEdges( node ).length ) {
                    return graph.getOutgoingEdges( node )[++position];
                } else {
                    return outgoingShortcuts[node].get( ++position - graph.getOutgoingEdges( node ).length );
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
            }
        }
    }
}
