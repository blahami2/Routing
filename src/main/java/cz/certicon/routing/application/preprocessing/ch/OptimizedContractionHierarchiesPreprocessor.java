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
import cz.certicon.routing.model.basic.TimeUnits;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OptimizedContractionHierarchiesPreprocessor implements ContractionHierarchiesPreprocessor {

    // DEBUG
    private PrintStream out = null;
    public List<Pair<Integer, String>> shortcutCounts = new ArrayList<>();
    public int nodeOfInterest = 71235;

    private static final int THREADS = 1;

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
    private long startId;

    public OptimizedContractionHierarchiesPreprocessor() {
        try {
            out = new PrintStream( new File( "C:\\Routing\\Testing\\original.txt" ) );
        } catch ( FileNotFoundException ex ) {
            Logger.getLogger( cz.certicon.routing.memsensitive.algorithm.preprocessing.ch.ContractionHierarchiesPreprocessor.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    @Override
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graphInput, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        return preprocess( graphInput, graphEntityFactory, distanceFactory, new EmptyProgressListener(), 0 );
    }

    @Override
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, ProgressListener progressListener, long startId ) {
        int nodeCount = graph.getNodes().size();
        int edgeCount = graph.getEdges().size();
        this.startId = startId;
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

        // DEBUG
        for ( int i = 0; i < origNodes.length; i++ ) {
            int degree = incomingEdgesArray[i].size() + outgoingEdgesArray[i].size();
            if ( origNodes[i].getId().getValue() == nodeOfInterest ) {
                out.println( "node degree #" + origNodes[i].getId().getValue() + " = " + degree );
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool( THREADS );
        List<Shortcut> shortcuts = new ArrayList<>();
        NodeDataStructure<Integer> priorityQueue = new JgraphtFibonacciDataStructure<>();
        NodeDataStructure<Integer> dijkstraPriorityQueue = new JgraphtFibonacciDataStructure<>();
        double[] nodeDistanceArray = new double[origNodes.length];
        System.arraycopy( distancePrototype, 0, nodeDistanceArray, 0, distancePrototype.length );
        int[] nodePredecessorArray = new int[origNodes.length];
        System.arraycopy( predecessorPrototype, 0, nodePredecessorArray, 0, predecessorPrototype.length );
//
        progressListener.init( nodeCount, INIT_NODE_RANKING );
        int bulkSize = 1 + nodeCount / THREADS;
        List<Future<IntegerArray>> futures = new ArrayList<>();
        for ( int i = 0; i < THREADS; i++ ) {
            int from = i * bulkSize;
            int to = Math.min( origNodes.length, ( i + 1 ) * bulkSize );
            Future<IntegerArray> future = executor.submit( new EdCalculationCallable( this, shortcuts, from, to, dijkstraPriorityQueues[i], distanceArrays[i] ) );
            futures.add( future );
        }
        for ( int i = 0; i < THREADS; i++ ) {
            int from = i * bulkSize;
            try {
                IntegerArray res = futures.get( i ).get();
                for ( int j = 0; j < res.array.length; j++ ) {
                    priorityQueue.add( from + j, res.array[j] );
//                    System.out.println( "#" + ( from + j ) + " = " + res.array[j] );
//                    if ( GlobalOptions.DEBUG_CORRECTNESS ) {
//                        Log.dln( getClass().getSimpleName(), "inserting: " + origNodes[from + j].getId().getValue() + " with value: " + ( res.array[j] ) );
//                    }
                    progressListener.nextStep();
                }
            } catch ( InterruptedException | ExecutionException ex ) {
                Logger.getLogger( OptimizedContractionHierarchiesPreprocessor.class.getName() ).log( Level.SEVERE, null, ex );
                return null;
            }
        }

        // DEBUG
        Collections.sort( shortcutCounts, new Comparator<Pair<Integer, String>>() {
            @Override
            public int compare( Pair<Integer, String> o1, Pair<Integer, String> o2 ) {
                return Integer.compare( o1.a, o2.a );
            }
        } );
        for ( Pair<Integer, String> shortcutCount : shortcutCounts ) {
            if ( shortcutCount.a == nodeOfInterest ) {
                out.println( "shortcut: #" + shortcutCount.a + ": " + shortcutCount.b );
            }
        }
//        if ( true ) {
//            return new Pair<Map<Node.Id, Integer>, List<Shortcut>>( new HashMap<Node.Id, Integer>(), new ArrayList<Shortcut>() );//Pair<Map<Node.Id, Integer>, List<Shortcut>>
//        }
//        for ( int i = 0; i < nodeCount; i++ ) {
//            int degree = incomingEdgesArray[i].size() + outgoingEdgesArray[i].size();
//            int numberOfShortcuts = calculateShortcuts( shortcuts, i, dijkstraPriorityQueue, nodeDistanceArray );
//
//            priorityQueue.add( i, numberOfShortcuts - degree );
////            System.out.println( "inserting: " + origNodes[i].getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = (" + numberOfShortcuts + " - " + degree + " )" );
//
//            if ( GlobalOptions.DEBUG_CORRECTNESS ) {
//                Log.dln( getClass().getSimpleName(), "inserting: " + origNodes[i].getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = ( " + numberOfShortcuts + " - " + degree + " )" );
//            }
//        }
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
            // DEBUG
//            if ( origNodes[currentNode].getId().getValue() == nodeOfInterest ) {
//                break;
//            }
//            System.out.println( "extracted: " + origNodes[currentNode].getId().getValue() + " with ED = " + extractMin.b );

//            System.out.println( "contracting: " + currentNode );
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
                if ( priorityQueue.contains( source ) ) {
                    neighbours.add( source );
                }
            }
            for ( int edge : outgoingEdgesArray[currentNode] ) {
                int target = getTarget( edge, shortcuts );
                if ( priorityQueue.contains( target ) ) {
                    neighbours.add( target );
                }
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                neighboursBuildingTime += time.restart();
            }

//            if ( neighbours.size() >= THREADS ) {
            int[] neighboursArray = new int[neighbours.size()];
            int cnt = 0;
            for ( int neighbour : neighbours ) {
                neighboursArray[cnt++] = neighbour;
                contractedNeighboursCount[neighbour] += 1;
            }
            bulkSize = (int) ( Math.ceil( neighboursArray.length / (double) THREADS ) + 0.0001 );
            futures = new ArrayList<>();
            for ( int i = 0; i < THREADS; i++ ) {
                int from = i * bulkSize;
                int to = Math.min( neighbours.size(), ( i + 1 ) * bulkSize );
                if ( from < to ) {
                    Future<IntegerArray> future = executor.submit( new EdCalculationCallable( this, shortcuts, neighboursArray, from, to, dijkstraPriorityQueues[i], distanceArrays[i] ) );
                    futures.add( future );
                }
            }
            for ( int i = 0; i < futures.size(); i++ ) {
                int from = i * bulkSize;
                try {
                    IntegerArray res = futures.get( i ).get();
                    for ( int j = 0; j < res.array.length; j++ ) {
                        if ( origNodes[neighboursArray[from + j]].getId().getValue() == nodeOfInterest ) {
                            out.println( "ED for #" + origNodes[neighboursArray[from + j]].getId().getValue() + " = " + res.array[j] );
                        }
                        priorityQueue.notifyDataChange( neighboursArray[from + j], res.array[j] );
//                        System.out.println( "#" + ( neighboursArray[from + j] ) + " = " + res.array[j] );
                        if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                            Log.dln( getClass().getSimpleName(), "inserting: " + origNodes[neighboursArray[from + j]].getId().getValue() + " with value: " + ( res.array[j] ) );
                        }
                    }
                } catch ( InterruptedException | ExecutionException ex ) {
                    Log.dln( getClass().getSimpleName(), ex.getMessage() );
                    return null;
                }
            }
//            } else {
//                for ( int neighbour : neighbours ) {
//                    if ( priorityQueue.contains( neighbour ) ) {
//                        if ( GlobalOptions.DEBUG_TIME ) {
//                            timeInner.start();
//                        }
//                        int numOfShortcuts = calculateShortcuts( shortcuts, neighbour, dijkstraPriorityQueue, nodeDistanceArray );
//                        if ( GlobalOptions.DEBUG_TIME ) {
//                            neighbourCalculateShortcutTime += timeInner.stop();
//                            neighbourCounter++;
//                        }
//                        contractedNeighboursCount[neighbour] += 1;
////                    System.out.println( "changing: " + origNodes[neighbour].getId().getValue() + " to value: " + ( contractedNeighboursCount[neighbour] + numOfShortcuts - ( incomingEdgesArray[neighbour].size() + outgoingEdgesArray[neighbour].size() ) ) );
//                        priorityQueue.notifyDataChange( neighbour, contractedNeighboursCount[neighbour] + numOfShortcuts - ( incomingEdgesArray[neighbour].size() + outgoingEdgesArray[neighbour].size() ) );
//                    }
//                }
//            }
            if ( GlobalOptions.DEBUG_TIME ) {
                neighboursProcessTime += time.restart();
            }
            nodeRankMap.put( origNodes[currentNode].getId(), rank++ );
            if ( progressListener.nextStep() ) {
//                Log.dln( getClass().getSimpleName(), "done: " + ( nodeCount - priorityQueue.size() ) + " out of " + nodeCount + " = " + ( 100 * ( nodeCount - priorityQueue.size() ) / nodeCount ) + "%" );
            }
        }

        if ( GlobalOptions.DEBUG_TIME ) {
            nodeCount = ( nodeCount > 0 ) ? nodeCount : 1;
            neighbourCounter = ( neighbourCounter > 0 ) ? neighbourCounter : 1;
            System.out.println( "extract time per node: " + ( extractTime / nodeCount ) );
            System.out.println( "contract time per node: " + ( contractTime / nodeCount ) );
            System.out.println( "neighbours collecting time per node: " + ( neighboursBuildingTime / nodeCount ) );
            System.out.println( "neighbours processing time per node: " + ( neighboursProcessTime / nodeCount ) );
            System.out.println( "neighbours: " + neighbourCounter );
            System.out.println( "shortcut calculation time per neighbour: " + ( neighbourCalculateShortcutTime / neighbourCounter ) );
        }
        executor.shutdown();
        // DEBUG
        out.flush();
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

        // DEBUG
        if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
            out.println( "shortcut for #" + origNodes[node].getId().getValue() );
        }
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
        Map<Integer, Double> fromMaxDistanceMap = new HashMap<>();
        for ( Map.Entry<Pair<Integer, Integer>, Double> entry : fromToDistanceMap.entrySet() ) {
            int from = entry.getKey().a;
            double distance = entry.getValue();
            if ( !fromMaxDistanceMap.containsKey( from ) || fromMaxDistanceMap.get( from ) < distance ) {
                fromMaxDistanceMap.put( from, distance );
            }
        }
        List<Integer> sourcesAddedTo = new LinkedList<>();
        List<Integer> targetsAddedTo = new LinkedList<>();
        Set<Integer> visitedNodes = new HashSet<>();
        List<Integer> edgeSources = new ArrayList<>();
        List<Integer> edgeTargets = new ArrayList<>();
        List<Double> edgeDistances = new ArrayList<>();
        Map<Integer, List<Integer>> outgoing = new HashMap<>();
//        System.out.println( "#shortcuts: pairs = " + fromToDistanceMap.size() );

        // DEBUG
        if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
            for ( int from : sources ) {
                out.println( "#" + origNodes[node].getId().getValue() + "-neighbour incoming: #" + origNodes[from].getId().getValue() );
            }
            for ( int to : targets ) {
                out.println( "#" + origNodes[node].getId().getValue() + "-neighbour outgoing: #" + origNodes[to].getId().getValue() );
            }
            for ( Map.Entry<Pair<Integer, Integer>, Double> entry : fromToDistanceMap.entrySet() ) {
                out.println( "#" + origNodes[node].getId().getValue() + "-path: from #" + origNodes[entry.getKey().a].getId().getValue() + " to #" + origNodes[entry.getKey().b].getId().getValue() + " in " + entry.getValue() );
            }
        }
        int numOfShortcuts = 0;
        for ( int from : sources ) {
            double maxDistance = fromMaxDistanceMap.get( from );
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            // DEBUG
            if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
                out.println( "#" + origNodes[node].getId().getValue() + "-Dijkstra for #" + origNodes[from].getId().getValue() );
            }
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                double currentDistance = nodeDistanceArray[currentNode];
                // DEBUG
                if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
                    out.println( "#" + origNodes[node].getId().getValue() + "-extracted #" + origNodes[currentNode].getId().getValue() + " -> " + currentDistance );
                }
                if ( maxDistance < currentDistance ) {
                    break;
                }
                for ( int edge : outgoingEdgesArray[currentNode] ) {
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
                if ( outgoing.containsKey( currentNode ) ) {
                    for ( int edge : outgoing.get( currentNode ) ) {
                        int target = edgeTargets.get( edge - origEdges.length - shortcuts.size() );
                        double targetDistance = nodeDistanceArray[target];
                        double newDistance = currentDistance + edgeDistances.get( edge - origEdges.length - shortcuts.size() );
                        if ( target != node && newDistance < targetDistance ) {
                            nodeDistanceArray[target] = newDistance;
                            visitedNodes.add( target );
                            dijkstraPriorityQueue.notifyDataChange( target, newDistance );
                        }
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
                    List<Integer> outEdges = outgoing.get( from );
                    if ( outEdges == null ) {
                        outEdges = new ArrayList<>();
                        outgoing.put( from, outEdges );
                    }
                    outEdges.add( origEdges.length + numOfShortcuts + shortcuts.size() );
                    sourcesAddedTo.add( from );
                    targetsAddedTo.add( to );
                    numOfShortcuts++;

                    // DEBUG
                    if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
                        out.println( "#" + origNodes[node].getId().getValue() + "- adding shortcut #" + origNodes[from].getId().getValue() + " -> #" + origNodes[to].getId().getValue() );
                    }
//                    System.out.println( "#" + node + " - creating shortcut: " + from + " -> " + to );
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
        // DEBUG
        if ( origNodes[node].getId().getValue() == nodeOfInterest ) {
            out.println( "shortcut for #" + origNodes[node].getId().getValue() + " = " + numOfShortcuts );
        }
        shortcutCounts.add( new Pair<>( (int) origNodes[node].getId().getValue(), "" + numOfShortcuts ) );
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
                    // DEBUG
                    if ( origNodes[source].getId().getValue() == nodeOfInterest ) {
                        out.println( "#" + origNodes[source].getId().getValue() + "-removing outgoing edge #" + ( ( edge < origEdges.length ) ? origEdges[edge].getId().getValue() : edge ) );
                    }
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
                    // DEBUG
                    if ( origNodes[target].getId().getValue() == nodeOfInterest ) {
                        out.println( "#" + origNodes[target].getId().getValue() + "-removing incoming edge #" + ( ( edge < origEdges.length ) ? origEdges[edge].getId().getValue() : edge ) );
                    }
                    iterator.remove();
                    break;
                }
            }
        }

        Map<Integer, Double> fromMaxDistanceMap = new HashMap<>();
        for ( Map.Entry<Pair<Integer, Integer>, Trinity<Integer, Integer, Double>> entry : fromToDistanceMap.entrySet() ) {
            int from = entry.getKey().a;
            double distance = entry.getValue().c;
            if ( !fromMaxDistanceMap.containsKey( from ) || fromMaxDistanceMap.get( from ) < distance ) {
                fromMaxDistanceMap.put( from, distance );
            }
        }
        int[] preArray = new int[origNodes.length];
        Set<Integer> visitedNodes = new HashSet<>();
        for ( int from : sources ) {
            double maxDistance = fromMaxDistanceMap.get( from );
            // route
            // if route worse, then create and add shortcut
            nodeDistanceArray[from] = 0;
            visitedNodes.add( from );
            dijkstraPriorityQueue.add( from, 0 );
            while ( !dijkstraPriorityQueue.isEmpty() ) {
                int currentNode = dijkstraPriorityQueue.extractMin();
                double currentDistance = nodeDistanceArray[currentNode];
                if ( maxDistance < currentDistance ) {
                    break;
                }
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
                    // DEBUG
                    if ( origNodes[from].getId().getValue() == nodeOfInterest ) {
                        out.println( "#" + origNodes[from].getId().getValue() + "-adding incoming edge #" + id );
                    }
                    if ( origNodes[to].getId().getValue() == nodeOfInterest ) {
                        out.println( "#" + origNodes[to].getId().getValue() + "-adding incoming edge #" + id );
                    }
                    outgoingEdgesArray[from].add( id );
                    incomingEdgesArray[to].add( id );
                    Shortcut shortcut = new SimpleShortcut( Edge.Id.createId( startId + maxId + outShortcuts.size() ), getEdge( fromEdge, outShortcuts ), getEdge( toEdge, outShortcuts ) );
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
//        int minNode = priorityQueue.extractMin();
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
        //DEBUG
        if ( origNodes[minNode].getId().getValue() == nodeOfInterest ) {
            out.println( "extracted: " + origNodes[minNode].getId().getValue() + ", " + minValue );
        }
        return new Pair<>( minNode, minValue );
    }

    private static class EdCalculationTask extends RecursiveTask<IntegerArray> {

        private final OptimizedContractionHierarchiesPreprocessor preprocessor;
        private final List<Shortcut> shortcuts;
        private final int from;
        private final int to;
        private final NodeDataStructure<Integer> dijkstraPriorityQueue;
        private final double[] nodeDistanceArray;

        public EdCalculationTask( OptimizedContractionHierarchiesPreprocessor preprocessor, List<Shortcut> shortcuts, int from, int to, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
            this.preprocessor = preprocessor;
            this.shortcuts = shortcuts;
            this.from = from;
            this.to = to;
            this.dijkstraPriorityQueue = dijkstraPriorityQueue;
            this.nodeDistanceArray = nodeDistanceArray;
        }

        @Override
        protected IntegerArray compute() {
            IntegerArray results = new IntegerArray( to - from );
            for ( int i = from; i < to; i++ ) {
                int degree = preprocessor.incomingEdgesArray[i].size() + preprocessor.outgoingEdgesArray[i].size();
                int numberOfShortcuts = preprocessor.calculateShortcuts( shortcuts, i, dijkstraPriorityQueue, nodeDistanceArray );
                results.array[i - from] = preprocessor.contractedNeighboursCount[i] + numberOfShortcuts - degree;
            }
            return results;
        }

    }

    private static class EdCalculationRunnable implements Runnable {

        private final OptimizedContractionHierarchiesPreprocessor preprocessor;
        private final List<Shortcut> shortcuts;
        private final int from;
        private final int to;
        private final NodeDataStructure<Integer> dijkstraPriorityQueue;
        private final double[] nodeDistanceArray;
        private IntegerArray results;

        public EdCalculationRunnable( OptimizedContractionHierarchiesPreprocessor preprocessor, List<Shortcut> shortcuts, int from, int to, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
            this.preprocessor = preprocessor;
            this.shortcuts = shortcuts;
            this.from = from;
            this.to = to;
            this.dijkstraPriorityQueue = dijkstraPriorityQueue;
            this.nodeDistanceArray = nodeDistanceArray;
        }

        @Override
        public void run() {
            results = new IntegerArray( to - from );
            for ( int i = from; i < to; i++ ) {
                int degree = preprocessor.incomingEdgesArray[i].size() + preprocessor.outgoingEdgesArray[i].size();
                int numberOfShortcuts = preprocessor.calculateShortcuts( shortcuts, i, dijkstraPriorityQueue, nodeDistanceArray );
                results.array[i - from] = preprocessor.contractedNeighboursCount[i] + numberOfShortcuts - degree;
            }
        }

        public IntegerArray getResult() {
            return results;
        }
    }

    private static class EdCalculationCallable implements Callable<IntegerArray> {

        private final OptimizedContractionHierarchiesPreprocessor preprocessor;
        private final List<Shortcut> shortcuts;
        private final int from;
        private final int to;
        private final NodeDataStructure<Integer> dijkstraPriorityQueue;
        private final double[] nodeDistanceArray;
        private final int[] neighbours;

        public EdCalculationCallable( OptimizedContractionHierarchiesPreprocessor preprocessor, List<Shortcut> shortcuts, int from, int to, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
            this.preprocessor = preprocessor;
            this.shortcuts = shortcuts;
            this.from = from;
            this.to = to;
            this.dijkstraPriorityQueue = dijkstraPriorityQueue;
            this.nodeDistanceArray = nodeDistanceArray;
            this.neighbours = null;
        }

        public EdCalculationCallable( OptimizedContractionHierarchiesPreprocessor preprocessor, List<Shortcut> shortcuts, int[] neighbours, int from, int to, NodeDataStructure<Integer> dijkstraPriorityQueue, double[] nodeDistanceArray ) {
            this.preprocessor = preprocessor;
            this.shortcuts = shortcuts;
            this.from = from;
            this.to = to;
            this.dijkstraPriorityQueue = dijkstraPriorityQueue;
            this.nodeDistanceArray = nodeDistanceArray;
            this.neighbours = neighbours;
        }

        @Override
        public IntegerArray call() throws Exception {
//            System.out.println( "#" + id + ": calculating from " + from + " to " + to );

            try {
                IntegerArray results = new IntegerArray( to - from );
                for ( int i = from; i < to; i++ ) {
                    int node = ( neighbours != null ) ? neighbours[i] : i;
                    int degree = preprocessor.incomingEdgesArray[node].size() + preprocessor.outgoingEdgesArray[node].size();
//                System.out.println( "#" + id + ": calling shortcuts for: " + i );
                    int numberOfShortcuts = preprocessor.calculateShortcuts( shortcuts, node, dijkstraPriorityQueue, nodeDistanceArray );
//                    System.out.println( "#" + node + " - calculate: " + preprocessor.contractedNeighboursCount[node] + " + " + numberOfShortcuts + " - " + degree );
                    // DEBUG
                    if ( preprocessor.origNodes[node].getId().getValue() == preprocessor.nodeOfInterest ) {
                        preprocessor.out.println( "#" + preprocessor.origNodes[node].getId().getValue() + " - calculate: " + preprocessor.contractedNeighboursCount[node] + " + " + numberOfShortcuts + " - " + degree );
                    }
                    results.array[i - from] = preprocessor.contractedNeighboursCount[node] + numberOfShortcuts - degree;
                }
                return results;
            } catch ( NegativeArraySizeException ex ) {
                System.out.println( "from = " + from + ", to = " + to );
                throw new AssertionError( "", ex );
            }
        }
    }

    private static class IntegerArray {

        final int[] array;

        public IntegerArray( int size ) {
            array = new int[size];
        }

    }
}
