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
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.application.preprocessing.Preprocessor;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class BasicContractionHierarchiesPreprocessor implements ContractionHierarchiesPreprocessor {

    private static final int DISTANCE = 5;
    private static final double INIT_NODE_RANKING = 0.2;

    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graphInput, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        return preprocess( graphInput, graphEntityFactory, distanceFactory, new EmptyProgressListener(), 0 );
    }

    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, ProgressListener progressListener, long startId ) {
//        System.out.println( "**** EDGES ****" );
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( edge );
//        }
        int nodeCount = graph.getNodes().size();

        Map<Node, Integer> contractedNeighboursCountMap = new HashMap<>();
        NodeDataStructure<Node> priorityQueue = new JgraphtFibonacciDataStructure<>();
        DijkstraRoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, graphEntityFactory, distanceFactory );
        List<Node> nodes = new ArrayList<>( graph.getNodes() );

        progressListener.init( nodes.size(), INIT_NODE_RANKING );

        for ( Node node : nodes ) {
            int degree = graph.getDegreeOf( node );
            int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, node );
            priorityQueue.add( node, numberOfShortcuts - degree );
//            System.out.println( "inserting: " + node.getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = (" + numberOfShortcuts + " - " + degree + " )" );
            contractedNeighboursCountMap.put( node, 0 );

            if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                Log.dln( getClass().getSimpleName(), "inserting: " + node.getId().getValue() + " with value: " + ( numberOfShortcuts - degree ) + " = ( " + numberOfShortcuts + " - " + degree + " )" );
            }
            progressListener.nextStep();
//            System.out.println( "added: " + node.getId() + " with value: " + ( numberOfShortcuts - degree ) );
        }
        int rank = 1;
        Map<Node.Id, Integer> rankMap = new HashMap<>();
        List<Shortcut> shortcuts = new ArrayList<>();

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

        Set<Edge> removedEdges = new HashSet<>();
        Set<Node> removedNodes = new HashSet<>();
//        Set<Edge> addedEdges = new HashSet<>();
        while ( !priorityQueue.isEmpty() ) {
            if ( GlobalOptions.DEBUG_TIME ) {
                time.start();
            }
            Pair<Node, Double> extractMin = extractMin( priorityQueue );
            Node min = extractMin.a;
//            System.out.println( "extracted: " + extractMin.a.getId().getValue() + " with ED = " + extractMin.b );
            if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                Log.dln( getClass().getSimpleName(), "extracted: " + extractMin.a.getId().getValue() + " with ED = " + extractMin.b );
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                extractTime += time.restart();
            }
//            System.out.println( "contracting = " + min.getId() );
            // shortcuts
            shortcuts.addAll( contractNode( routingAlgorithm, graph, min, removedEdges, removedNodes, startId ) );
            if ( GlobalOptions.DEBUG_TIME ) {
                contractTime += time.restart();
            }
            // Neighbours only heuristic + Spatial diversity heuristic

            Set<Node> neighbours = new HashSet<>();
            for ( Edge edge : graph.getEdgesOf( min ) ) {
                neighbours.add( edge.getOtherNode( min ) );
            }
//            System.out.println( "#neighbours: " + neighbours.size() );
            for ( Node neighbour : neighbours ) {
                if ( GlobalOptions.DEBUG_TIME ) {
                    timeInner.start();
                }
//                System.out.println( "neighbour = " + neighbour );
//                System.out.println( "count = " +contractedNeighboursCountMap.get( neighbour ) );
                int count = 1 + contractedNeighboursCountMap.get( neighbour );
                int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, neighbour );
                if ( GlobalOptions.DEBUG_TIME ) {
                    neighbourCalculateShortcutTime += timeInner.stop();
                    neighbourCounter++;
                }
                if ( priorityQueue.contains( neighbour ) ) {
                    contractedNeighboursCountMap.put( neighbour, count );
//                    System.out.println( "changing: " + neighbour.getId().getValue() + " to value: " + ( count + numberOfShortcuts - graph.getDegreeOf( neighbour ) ) );
                    priorityQueue.notifyDataChange( neighbour, count + numberOfShortcuts - graph.getDegreeOf( neighbour ) );
//                    System.out.println( "changed value: " + neighbour.getId() + " to value: " + ( count + numberOfShortcuts - graph.getDegreeOf( neighbour )  ) );
                }
            }
            if ( GlobalOptions.DEBUG_TIME ) {
                neighboursProcessTime += time.restart();
            }
            rankMap.put( min.getId(), rank++ );

            progressListener.nextStep();
//            System.out.println( "ranked nodes: " + ( rank - 1 ) );
        }

        if ( GlobalOptions.DEBUG_TIME ) {
            System.out.println( "extract time per node: " + ( extractTime / nodeCount ) );
            System.out.println( "contract time per node: " + ( contractTime / nodeCount ) );
            System.out.println( "neighbours collecting time per node: " + ( neighboursBuildingTime / nodeCount ) );
            System.out.println( "neighbours processing time per node: " + ( neighboursProcessTime / nodeCount ) );
            System.out.println( "neighbours: " + neighbourCounter );
            System.out.println( "shortcut calculation time per neighbour: " + ( neighbourCalculateShortcutTime / neighbourCounter ) );
        }

        for ( Node removedNode : removedNodes ) {
            graph.addNode( removedNode );
        }
        removedEdges.removeAll( shortcuts );
        for ( Shortcut shortcut : shortcuts ) {
            graph.removeEdge( shortcut );
        }
        for ( Edge removedEdge : removedEdges ) {
            graph.addEdge( removedEdge );
        }

//        System.out.println( "======================== PRINT ========================" );
//
//        System.out.println( "**** NODES ****" );
//        for ( Node node : graph.getNodes() ) {
//            System.out.println( node );
//        }
//        System.out.println( "**** EDGES ****" );
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( edge );
//        }
//        System.out.println( "**** RANKS ****" );
//        for ( Map.Entry<Node.Id, Integer> entry : rankMap.entrySet() ) {
//            System.out.println( entry.getKey() + ": " + entry.getValue() );
//        }
//        System.out.println( "**** SHORTCUTS ****" );
//        for ( Shortcut shortcut : shortcuts ) {
//            System.out.println( shortcut );
//        }
        return new Pair<>( rankMap, shortcuts );
    }

    private int numberOfShortcuts( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, Node node ) {
//        System.out.println( "SHORTCUTS FOR : " + node.getId().getValue() );
        routingAlgorithm.setGraph( graph );
        Map<Pair<Node, Node>, Distance> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = graph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            if ( edge.getTargetNode().equals( node ) ) {
                Node neighbourA = edge.getSourceNode();
                for ( Edge edge1 : edgesOf ) {
                    if ( edge1.getSourceNode().equals( node ) ) {
                        Node neighbourB = edge1.getTargetNode();
                        if ( !neighbourA.equals( neighbourB ) ) {
                            Pair<Node, Node> pair = new Pair<>( neighbourA, neighbourB );
                            Distance dist = fromToDistanceMap.get( pair );
                            Distance newDist = edge.getDistance().add( edge1.getDistance() );
                            if ( dist == null || dist.isGreaterThan( newDist ) ) {
                                fromToDistanceMap.put( pair, newDist );
//                                System.out.println( "putting (via " + node.getId() + "): " + neighbourA.getId() + " to " + neighbourB.getId() + " dist: " + newDist.getEvaluableValue() );
                            }
                        }
                    }
                }
            }
        }
//        System.out.println( "#shortcuts: pairs = " + fromToDistanceMap.size() );
        List<Edge> addedEdges = new LinkedList<>();
        Set<Edge> removedEdges = graph.removeNode( node );
        int numOfShortcuts = 0;
        for ( Map.Entry<Pair<Node, Node>, Distance> entry : fromToDistanceMap.entrySet() ) {
            Path route = routingAlgorithm.route( entry.getKey().a.getId(), entry.getKey().b.getId() );
            if ( route == null || route.getDistance().isGreaterThan( entry.getValue() ) ) {
                Edge e = graphEntityFactory.createEdge( Edge.Id.generateId(), entry.getKey().a, entry.getKey().b, entry.getValue() );
                addedEdges.add( e );
                graph.addEdge( e );
                if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                    Log.dln( getClass().getSimpleName(), "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue().getEvaluableValue() + " < " + ( ( route != null ) ? route.getDistance().getEvaluableValue() : "inf" ) );
                }   //                System.out.println( "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue() );
                numOfShortcuts++;
            } else if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                Log.dln( getClass().getSimpleName(), "route: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + route.getDistance().getEvaluableValue() + " <= " + entry.getValue().getEvaluableValue() );
            } //                System.out.print( "route: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + route.getDistance() );
            //                System.out.print( ":::" );
            //                for ( Edge edge : route ) {
            //                    System.out.print( edge.getId().getValue() + ", " );
            //                }
            //                System.out.println( "" );
        }
//        System.out.println( "#shortcuts = " + numOfShortcuts );
        graph.addNode( node );
        for ( Edge addedEdge : addedEdges ) {
            graph.removeEdge( addedEdge );
        }
        for ( Edge removedEdge : removedEdges ) {
            graph.addEdge( removedEdge );
        }
        return numOfShortcuts;
    }

    private Set<Shortcut> contractNode( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, Node node, Set<Edge> removedEdges, Set<Node> removedNodes, long startId ) {
//        System.out.println( "CONTRACTING: " + node.getId() );
        routingAlgorithm.setGraph( graph );
        Map<Pair<Node, Node>, Trinity<Edge, Edge, Distance>> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = graph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            if ( edge.getTargetNode().equals( node ) ) {
                Node neighbourA = edge.getSourceNode();
                for ( Edge edge1 : edgesOf ) {
                    if ( edge1.getSourceNode().equals( node ) ) {
                        Node neighbourB = edge1.getTargetNode();
                        if ( !neighbourA.equals( neighbourB ) ) {
                            Pair<Node, Node> pair = new Pair<>( neighbourA, neighbourB );
                            Trinity<Edge, Edge, Distance> get = fromToDistanceMap.get( pair );
                            Distance newDist = edge.getDistance().add( edge1.getDistance() );
                            if ( get == null || get.c.isGreaterThan( newDist ) ) {
                                fromToDistanceMap.put( pair, new Trinity<>( edge, edge1, newDist ) );
                            }
                        }
                    }
                }
            }
        }
//        System.out.println( "#contract: pairs = " + fromToDistanceMap.size() );
        removedEdges.addAll( graph.removeNode( node ) );
        removedNodes.add( node );
        Set<Shortcut> shortcuts = new HashSet<>();
        for ( Map.Entry<Pair<Node, Node>, Trinity<Edge, Edge, Distance>> entry : fromToDistanceMap.entrySet() ) {
            Node from = entry.getKey().a;
            Node to = entry.getKey().b;
            Path route = routingAlgorithm.route( from.getId(), to.getId() );
            if ( route == null || route.getDistance().isGreaterThan( entry.getValue().c ) ) {
                Edge fromEdge = entry.getValue().a;
                Edge toEdge = entry.getValue().b;
                Shortcut shortcut = new SimpleShortcut( Edge.Id.createId(Edge.Id.generateId().getValue() + startId), fromEdge, toEdge );
//                System.out.println( "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue().c.getEvaluableValue() + " < " + ( ( route != null ) ? route.getDistance().getEvaluableValue() : "inf" ) );

                if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                    Log.dln( getClass().getSimpleName(), "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue().c.getEvaluableValue() + " < " + ( ( route != null ) ? route.getDistance().getEvaluableValue() : "inf" ) );
                }
                shortcuts.add( shortcut );
                graph.addEdge( shortcut );
            } else //                System.out.println( "route: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + route.getDistance().getEvaluableValue() + " < " + entry.getValue().c.getEvaluableValue() );
             if ( GlobalOptions.DEBUG_CORRECTNESS ) {
                    Log.dln( getClass().getSimpleName(), "route: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + route.getDistance().getEvaluableValue() + " <= " + entry.getValue().c.getEvaluableValue() );
                }
        }
        return shortcuts;
    }

    private Pair<Node, Double> extractMin( NodeDataStructure<Node> priorityQueue ) {
        double minValue = priorityQueue.minValue();
        double precision = 0.001;
        List<Node> mins = new ArrayList<>();
        Node minNode = null;
        while ( DoubleComparator.isEqualTo( priorityQueue.minValue(), minValue, precision ) ) {
            Node n = priorityQueue.extractMin();
            mins.add( n );
            if ( minNode == null || n.getId().getValue() < minNode.getId().getValue() ) {
                minNode = n;
            }
        }
        for ( Node min : mins ) {
            if ( !min.equals( minNode ) ) {
                priorityQueue.add( min, minValue );
            }
        }
        return new Pair<>( minNode, minValue );
    }
}
