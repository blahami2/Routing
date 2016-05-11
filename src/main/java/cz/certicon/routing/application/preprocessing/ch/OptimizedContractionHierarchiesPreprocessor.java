/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing.ch;

import cz.certicon.routing.GlobalOptions;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.application.preprocessing.Preprocessor;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.common.SimpleShortcut;
import cz.certicon.routing.model.utility.ProgressListener;
import cz.certicon.routing.model.utility.progress.EmptyProgressListener;
import cz.certicon.routing.utils.DoubleComparator;
import cz.certicon.routing.utils.debug.Log;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.utils.measuring.TimeUnits;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OptimizedContractionHierarchiesPreprocessor implements ContractionHierarchiesPreprocessor {

    private static final int DISTANCE = 20;

    private static final int THREADS = 8;

    private static final double INIT_NODE_RANKING = 0.1;

    private static final double PRECISION = 10E-6;

    private Map<Node.Id, Integer> nodePositionMap;
    private Map<Edge.Id, Integer> edgePositionMap;
    private Node[] origNodes;
    private Edge[] origEdges;
    private double[] edgeLengthArray;
    private int[] edgeSourceArray;
    private int[] edgeTargetArray;
    private List<Integer>[] outgoingEdgesArray;
    private List<Integer>[] incomingEdgesArray;
    private int[] contractedNeighboursCount;
    private int[] edArray;
    private double[] distancePrototype;
    private int[] predecessorPrototype;
    private long maxId;

    @Override
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graphInput, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        return preprocess( graphInput, graphEntityFactory, distanceFactory, new EmptyProgressListener() );
    }

    @Override
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, ProgressListener progressListener ) {
        int nodeCount = graph.getNodes().size();
        int edgeCount = graph.getEdges().size();
        nodePositionMap = new HashMap<>();
        edgePositionMap = new HashMap<>();
        origNodes = new Node[nodeCount];
        origEdges = new Edge[edgeCount];
        edgeLengthArray = new double[edgeCount];
        edgeSourceArray = new int[edgeCount];
        edgeTargetArray = new int[edgeCount];
        outgoingEdgesArray = new List[nodeCount];
        incomingEdgesArray = new List[nodeCount];
        contractedNeighboursCount = new int[nodeCount];
        edArray = new int[nodeCount];
        this.distancePrototype = new double[nodeCount];
        this.predecessorPrototype = new int[nodeCount];
        maxId = 0;
        int counter = 0;
        for ( Node node : graph.getNodes() ) {
            nodePositionMap.put( node.getId(), counter );
            origNodes[counter] = node;
            contractedNeighboursCount[counter] = 0;
            edArray[counter] = 0;
            distancePrototype[counter] = Double.MAX_VALUE;
            predecessorPrototype[counter] = -1;
            counter++;
        }
        counter = 0;
        for ( Edge edge : graph.getEdges() ) {
            if ( edge.getId().getValue() > maxId ) {
                maxId = edge.getId().getValue();
            }
            edgePositionMap.put( edge.getId(), counter );
            origEdges[counter] = edge;
            edgeLengthArray[counter] = edge.getDistance().getEvaluableValue();
            edgeSourceArray[counter] = nodePositionMap.get( edge.getSourceNode().getId() );
            edgeTargetArray[counter] = nodePositionMap.get( edge.getTargetNode().getId() );
            counter++;
        }
        maxId++;
        counter = 0;
        for ( Node node : graph.getNodes() ) {
            Set<Edge> incomingEdgesOf = graph.getIncomingEdgesOf( node );
            incomingEdgesArray[counter] = new LinkedList<>();
            for ( Edge edge : incomingEdgesOf ) {
                if ( edge.getTargetNode().equals( node ) ) {
                    incomingEdgesArray[counter].add( edgePositionMap.get( edge.getId() ) );
                }
            }
            Set<Edge> outgoingEdgesOf = graph.getOutgoingEdgesOf( node );
            outgoingEdgesArray[counter] = new LinkedList<>();
            for ( Edge edge : outgoingEdgesOf ) {
                if ( edge.getSourceNode().equals( node ) ) {
                    outgoingEdgesArray[counter].add( edgePositionMap.get( edge.getId() ) );
                }
            }
            counter++;
        }
        double[][] distanceArrays = new double[THREADS][nodeCount];
        NodeDataStructure<Integer>[] dijkstraPriorityQueues = new NodeDataStructure[THREADS];
        for ( int i = 0; i < THREADS; i++ ) {
            System.arraycopy( distancePrototype, 0, distanceArrays[i], 0, distancePrototype.length );
            dijkstraPriorityQueues[i] = new JgraphtFibonacciDataStructure<>();
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool( THREADS );
        List<Shortcut> shortcuts = new ArrayList<>();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        NodeDataStructure<Integer> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        double[] nodeDistanceArray = new double[origNodes.length];
        System.arraycopy( distancePrototype, 0, nodeDistanceArray, 0, distancePrototype.length );
        int[] nodePredecessorArray = new int[origNodes.length];
        System.arraycopy( predecessorPrototype, 0, nodePredecessorArray, 0, predecessorPrototype.length );

        progressListener.init( nodeCount, INIT_NODE_RANKING );
        for ( int i = 0; i < nodeCount; i++ ) {
            int degree = incomingEdgesArray[i].size() + outgoingEdgesArray[i].size();
            int numberOfShortcuts = calculateShortcuts( shortcuts, i, dijkstraPriorityQueue, nodeDistanceArray );

            priorityQueue.add( i, numberOfShortcuts - degree );
//            System.out.println( "inserting: " + origNodes[i].getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = (" + numberOfShortcuts + " - " + degree + " )" );

            if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                Log.dln( getClass().getSimpleName(), "inserting: " + origNodes[i].getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = ( " + numberOfShortcuts + " - " + degree + " )" );
            }
            progressListener.nextStep();
        }

        int rank = 1;
        Map<Node.Id, Integer> nodeRankMap = new HashMap<>();

        progressListener.init( priorityQueue.size(), 1.0 - INIT_NODE_RANKING );

        long extractTime = 0;
        long contractTime = 0;
        long neighboursBuildingTime = 0;
        long neighboursProcessTime = 0;
        long neighbourCalculateShortcutTime = 0;
        int neighbourCounter = 0;
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.NANOSECONDS );
        TimeMeasurement timeInner = new TimeMeasurement();
        timeInner.setTimeUnits( TimeUnits.NANOSECONDS );
        int size = priorityQueue.size();

        while ( !priorityQueue.isEmpty() ) {
            if ( GlobalOptions.DEBUG_TIME ) {
                time.start();
            }

            Pair<Integer, Double> extractMin = extractMin( priorityQueue );
            int currentNode = extractMin.a;
//            System.out.println( "extracted: " + origNodes[currentNode].getId().getValue() + " with ED = " + extractMin.b );

            if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                Log.dln( getClass().getSimpleName(), "extracted: " + origNodes[currentNode].getId().getValue() + " with ED = " + extractMin.b );
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                extractTime += time.restart();
            }
            contractNode( shortcuts, currentNode, dijkstraPriorityQueue, nodeDistanceArray );
            if ( GlobalOptions.DEBUG_TIME ) {
                contractTime += time.restart();
            }
            Set<Integer> neighbours = new HashSet<>();
            for ( int edge : incomingEdgesArray[currentNode] ) {
                int source = getSource( edge, shortcuts );
                neighbours.add( source );
            }
            for ( int edge : outgoingEdgesArray[currentNode] ) {
                int target = getTarget( edge, shortcuts );
                neighbours.add( target );
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                neighboursBuildingTime += time.restart();
            }
//            System.out.println( "#neighbours: " + neighbours.size() );
            for ( int neighbour : neighbours ) {
                if ( GlobalOptions.DEBUG_TIME ) {
                    timeInner.start();
                }
                int numOfShortcuts = calculateShortcuts( shortcuts, neighbour, dijkstraPriorityQueue, nodeDistanceArray );
                if ( GlobalOptions.DEBUG_TIME ) {
                    neighbourCalculateShortcutTime += timeInner.stop();
                    neighbourCounter++;
                }
                if ( priorityQueue.contains( neighbour ) ) {
                    contractedNeighboursCount[neighbour] += 1;
//                    System.out.println( "changing: " + origNodes[neighbour].getId().getValue() + " to value: " + ( contractedNeighboursCount[neighbour] + numOfShortcuts - ( incomingEdgesArray[neighbour].size() + outgoingEdgesArray[neighbour].size() ) ) );
                    priorityQueue.notifyDataChange( neighbour, contractedNeighboursCount[neighbour] + numOfShortcuts - ( incomingEdgesArray[neighbour].size() + outgoingEdgesArray[neighbour].size() ) );
                }
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                neighboursProcessTime += time.restart();
            }
            nodeRankMap.put( origNodes[currentNode].getId(), rank++ );
            progressListener.nextStep();
        }

        if ( GlobalOptions.DEBUG_TIME ) {
            System.out.println( "extract time per node: " + ( extractTime / nodeCount ) );
            System.out.println( "contract time per node: " + ( contractTime / nodeCount ) );
            System.out.println( "neighbours collecting time per node: " + ( neighboursBuildingTime / nodeCount ) );
            System.out.println( "neighbours processing time per node: " + ( neighboursProcessTime / nodeCount ) );
            System.out.println( "neighbours: " + neighbourCounter );
            System.out.println( "shortcut calculation time per neighbour: " + ( neighbourCalculateShortcutTime / neighbourCounter ) );
        }
        return new Pair<>( nodeRankMap, shortcuts );
    }

    private int getSource( int edge, List<Shortcut> shortcuts ) {
        if ( edge < edgeSourceArray.length ) {
            return edgeSourceArray[edge];
        } else {
            int idx = edge - edgeSourceArray.length;
            Shortcut shortcut = shortcuts.get( idx );
            Node node = shortcut.getSourceNode();
            return nodePositionMap.get( node.getId() );
        }
    }

    private int getTarget( int edge, List<Shortcut> shortcuts ) {
        if ( edge < edgeTargetArray.length ) {
            return edgeTargetArray[edge];
        } else {
            int idx = edge - edgeTargetArray.length;
            Shortcut shortcut = shortcuts.get( idx );
            Node node = shortcut.getTargetNode();
            return nodePositionMap.get( node.getId() );
        }
    }

    private double getLength( int edge, List<Shortcut> shortcuts ) {
        if ( edge < edgeLengthArray.length ) {
            return edgeLengthArray[edge];
        } else {
            int idx = edge - edgeLengthArray.length;
            Shortcut shortcut = shortcuts.get( idx );
            Distance distance = shortcut.getDistance();
            return distance.getEvaluableValue();
        }
    }

    private Edge getEdge( int edge, List<Shortcut> shortcuts ) {
        if ( edge < origEdges.length ) {
            return origEdges[edge];
        } else {
            int idx = edge - origEdges.length;
            return shortcuts.get( idx );
        }
    }

    private int calculateShortcuts( List<Shortcut> shortcuts, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
//        System.out.println( "SHORTCUTS FOR : " + origNodes[node].getId().getValue() );

        Set<Integer> sources = new HashSet<>();
        Set<Integer> targets = new HashSet<>();
        Map<Pair<Integer, Integer>, Double> fromToDistanceMap = new HashMap<>();
        for ( int incomingEdge : incomingEdgesArray[node] ) {
            for ( int outgoingEdge : outgoingEdgesArray[node] ) {
                if ( incomingEdge != outgoingEdge ) {
                    int sourceNode = getSource( incomingEdge, shortcuts );
                    int targetNode = getTarget( outgoingEdge, shortcuts );
                    if ( sourceNode != targetNode ) {
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        Double oldDistance = fromToDistanceMap.get( pair );
                        Double newDistance = getLength( incomingEdge, shortcuts ) + getLength( outgoingEdge, shortcuts );
                        if ( oldDistance == null || newDistance < oldDistance ) {
                            fromToDistanceMap.put( pair, newDistance );
                            sources.add( sourceNode );
                            targets.add( targetNode );
                        }
                    }
                }
            }
        }
        List<Integer> sourcesAddedTo = new LinkedList<>();
        List<Integer> targetsAddedTo = new LinkedList<>();
        Set<Integer> visitedNodes = new HashSet<>();
        List<Integer> edgeSources = new ArrayList<>();
        List<Integer> edgeTargets = new ArrayList<>();
        List<Double> edgeDistances = new ArrayList<>();
//        System.out.println( "#shortcuts: pairs = " + fromToDistanceMap.size() );

        int numOfShortcuts = 0;
        for ( int from : sources ) {
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            int step = 0;
            while ( !dijkstraPriorityQueue.isEmpty() && step++ < DISTANCE ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                double currentDistance = nodeDistanceArray[currentNode];
                for ( Integer e : outgoingEdgesArray[currentNode] ) {
                    int edge = e;
                    int target;
                    double targetDistance;
                    double newDistance;
                    if ( edge < edgeTargetArray.length + shortcuts.size() ) {
                        target = getTarget( edge, shortcuts );
                        targetDistance = nodeDistanceArray[target];
                        newDistance = currentDistance + getLength( edge, shortcuts );
                    } else {
                        target = edgeTargets.get( edge - origEdges.length - shortcuts.size() );
                        targetDistance = nodeDistanceArray[target];
                        newDistance = currentDistance + edgeDistances.get( edge - origEdges.length - shortcuts.size() );
                    }
                    if ( target != node && newDistance < targetDistance ) {
                        nodeDistanceArray[target] = newDistance;
                        visitedNodes.add( target );
                        dijkstraPriorityQueue.notifyDataChange( target, newDistance );
                    }
                }
            }
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }
                double distance = nodeDistanceArray[to];
                double shortcutDistance = fromToDistanceMap.get( new Pair<>( from, to ) );
                if ( DoubleComparator.isLowerThan( shortcutDistance, distance, PRECISION ) ) {
//            if ( entry.getValue() < distance ) {
                    edgeSources.add( from );
                    edgeTargets.add( to );
                    edgeDistances.add( shortcutDistance );
                    outgoingEdgesArray[from].add( origEdges.length + numOfShortcuts + shortcuts.size() );
                    incomingEdgesArray[to].add( origEdges.length + numOfShortcuts + shortcuts.size() );
                    sourcesAddedTo.add( from );
                    targetsAddedTo.add( to );
                    numOfShortcuts++;

                    if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                        Log.dln( getClass().getSimpleName(), "shortcut: " + origNodes[from].getId() + " to " + origNodes[to].getId() + " via " + origNodes[node].getId() + ", " + shortcutDistance + " < " + distance );
                    }
                } else if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                    Log.dln( getClass().getSimpleName(), "route: " + origNodes[from].getId() + " to " + origNodes[to].getId() + " via " + origNodes[node].getId() + ", " + distance + " <= " + shortcutDistance );
                }
            }
            // clean for next Dijkstra
            for ( Integer visitedNode : visitedNodes ) {
                nodeDistanceArray[visitedNode] = Double.MAX_VALUE;
            }
            visitedNodes.clear();
            dijkstraPriorityQueue.clear();
        }
        for ( Integer source : sourcesAddedTo ) {
            ( (LinkedList) outgoingEdgesArray[source] ).removeLast();
        }
        for ( Integer target : targetsAddedTo ) {
            ( (LinkedList) incomingEdgesArray[target] ).removeLast();
        }
        return numOfShortcuts;
    }

    public void contractNode( List<Shortcut> outShortcuts, int node, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
        Set<Integer> sources = new HashSet<>();
        Set<Integer> targets = new HashSet<>();
        Map<Pair<Integer, Integer>, Trinity<Integer, Integer, Double>> fromToDistanceMap = new HashMap<>();
        for ( Integer ie : incomingEdgesArray[node] ) {
            int incomingEdge = ie;
            for ( Integer oe : outgoingEdgesArray[node] ) {
                int outgoingEdge = oe;
                if ( incomingEdge != outgoingEdge ) {
                    int sourceNode = getSource( incomingEdge, outShortcuts );
                    int targetNode = getTarget( outgoingEdge, outShortcuts );
                    if ( sourceNode != targetNode ) {
                        Pair<Integer, Integer> pair = new Pair<>( sourceNode, targetNode );
                        Trinity<Integer, Integer, Double> oldDistance = fromToDistanceMap.get( pair );
                        Double newDistance = getLength( incomingEdge, outShortcuts ) + getLength( outgoingEdge, outShortcuts );
                        if ( oldDistance == null || newDistance < oldDistance.c ) {
                            fromToDistanceMap.put( pair, new Trinity<>( incomingEdge, outgoingEdge, newDistance ) );
                            sources.add( sourceNode );
                            targets.add( targetNode );
                        }
                    }
                }
            }
        }
//        System.out.println( "#contract: pairs = " + fromToDistanceMap.size() );
        for ( Integer edge : incomingEdgesArray[node] ) {
            int source = getSource( edge, outShortcuts );
            Iterator<Integer> iterator = outgoingEdgesArray[source].iterator();
            while ( iterator.hasNext() ) {
                if ( edge.equals( iterator.next() ) ) {
                    iterator.remove();
                    break;
                }
            }
        }
        for ( Integer edge : outgoingEdgesArray[node] ) {
            int target = getTarget( edge, outShortcuts );
            Iterator<Integer> iterator = incomingEdgesArray[target].iterator();
            while ( iterator.hasNext() ) {
                if ( edge.equals( iterator.next() ) ) {
                    iterator.remove();
                    break;
                }
            }
        }
        int[] preArray = new int[origNodes.length];
        Set<Integer> visitedNodes = new HashSet<>();
        for ( int from : sources ) {
            // route
            // if route worse, then create and add shortcut
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            int step = 0;
            while ( !dijkstraPriorityQueue.isEmpty() && step++ < DISTANCE ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                double currentDistance = nodeDistanceArray[currentNode];
                for ( Integer edge : outgoingEdgesArray[currentNode] ) {
                    int target = getTarget( edge, outShortcuts );
                    double targetDistance = nodeDistanceArray[target];
                    double newDistance = currentDistance + getLength( edge, outShortcuts );
                    if ( target != node && newDistance < targetDistance ) {
                        nodeDistanceArray[target] = newDistance;
                        visitedNodes.add( target );
                        preArray[target] = currentNode;
                        dijkstraPriorityQueue.notifyDataChange( target, newDistance );
                    }
                }
            }
            for ( int to : targets ) {
                if ( from == to ) {
                    continue;
                }

                double distance = nodeDistanceArray[to];
                Trinity<Integer, Integer, Double> shortcutData = fromToDistanceMap.get( new Pair<>( from, to ) );
                if ( DoubleComparator.isLowerThan( shortcutData.c, distance, PRECISION ) ) {
                    int fromEdge = shortcutData.a;
                    int toEdge = shortcutData.b;
                    int id = origEdges.length + outShortcuts.size();
                    outgoingEdgesArray[from].add( id );
                    incomingEdgesArray[to].add( id );
                    Shortcut shortcut = new SimpleShortcut( Edge.Id.createId( maxId + outShortcuts.size() ), getEdge( fromEdge, outShortcuts ), getEdge( toEdge, outShortcuts ) );
//                System.out.println( "shortcut: " + origNodes[entry.getKey().a].getId() + " to " + origNodes[entry.getKey().b].getId() + " via " + origNodes[node].getId() + ", " + entry.getValue() + " < " + distance  );
//                System.out.println( "shortcut: " + origNodes[entry.getKey().a].getId() + " to " + origNodes[entry.getKey().b].getId() + " via " + origNodes[node].getId() + ", " + entry.getValue().c + " < " + distance );

                    if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                        Log.dln( getClass().getSimpleName(), "shortcut: " + origNodes[from].getId() + " to " + origNodes[to].getId() + " via " + origNodes[node].getId() + ", " + shortcutData.c + " < " + distance );
                    }
                    outShortcuts.add( shortcut );
                } else if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                    Log.dln( getClass().getSimpleName(), "route: " + origNodes[from].getId() + " to " + origNodes[to].getId() + " via " + origNodes[node].getId() + ", " + distance + " <= " + shortcutData.c );
                }
            }
            //                System.out.print( origNodes[n].getId().getValue() + ", " );
            //                System.out.println( "" );
            for ( Integer visitedNode : visitedNodes ) {
//                System.out.println( "route: " + origNodes[entry.getKey().a].getId() + " to " + origNodes[entry.getKey().b].getId() + " via " + origNodes[node] + ", " + distance + " < " + entry.getValue() );
                nodeDistanceArray[visitedNode] = Double.MAX_VALUE;
            }
            // clean for next Dijkstra
            visitedNodes.clear();
            dijkstraPriorityQueue.clear();
        }
    }

    private Pair<Integer, Double> extractMin( NodeDataStructure<Integer> priorityQueue ) {
        double minValue = priorityQueue.minValue();
        double precision = 0.001;
        List<Integer> mins = new ArrayList<>();
        int minNode = -1;
        while ( DoubleComparator.isEqualTo( priorityQueue.minValue(), minValue, precision ) ) {
            int n = priorityQueue.extractMin();
            mins.add( n );
            if ( minNode == -1 || origNodes[n].getId().getValue() < origNodes[minNode].getId().getValue() ) {
                minNode = n;
            }
        }
        for ( Integer min : mins ) {
            if ( !min.equals( minNode ) ) {
                priorityQueue.add( min, minValue );
            }
        }
        return new Pair<>( minNode, minValue );
    }

    private static class ShortcutComputationTask extends RecursiveTask<IntegerArray> {

        private final OptimizedContractionHierarchiesPreprocessor preprocessor;
        private final List<Shortcut> shortcuts;
        private final int[] nodes;
        private final NodeDataStructure<Integer> dijkstraPriorityQueue;
        private final double[] nodeDistanceArray;

        public ShortcutComputationTask( OptimizedContractionHierarchiesPreprocessor preprocessor, List<Shortcut> shortcuts, int[] nodes, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
            this.preprocessor = preprocessor;
            this.shortcuts = shortcuts;
            this.nodes = nodes;
            this.dijkstraPriorityQueue = dijkstraPriorityQueue;
            this.nodeDistanceArray = nodeDistanceArray;
        }

        @Override
        protected IntegerArray compute() {
            IntegerArray results = new IntegerArray( nodes.length );
            for ( int i = 0; i < nodes.length; i++ ) {
                results.array[i] = preprocessor.calculateShortcuts( shortcuts, nodes[i], dijkstraPriorityQueue, nodeDistanceArray );
            }
            return results;
        }

    }

    private static class IntegerArray {

        final int[] array;

        public IntegerArray( int size ) {
            array = new int[size];
        }

    }
}
