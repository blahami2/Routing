/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.ch;

import cz.certicon.routing.GlobalOptions;
import static cz.certicon.routing.GlobalOptions.*;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.algorithms.AbstractRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.utils.measuring.StatsLogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class OptimizedContractionHierarchiesRoutingAlgorithmWithUB extends AbstractRoutingAlgorithm {

    private final Map<Node.Id, Integer> nodePositionMap;
    private final Map<Edge.Id, Integer> edgePositionMap;
    private final Node[] origNodes;
    private final Edge[] origEdges;

    private final int[] rankArray;
    private final double[] edgeLengthArray;
    private final int[] edgeSourceArray;
    private final int[] edgeTargetArray;
    private final int[][] outgoingEdgesArray;
    private final int[][] incomingEdgesArray;
    private final double[] distancePrototype;
    private final int[] predecessorPrototype;
    private final NodeDataStructure<Integer>[] nodeDataStructure;

    public OptimizedContractionHierarchiesRoutingAlgorithmWithUB( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory, List<Shortcut> shortcuts, Map<Node.Id, Integer> nodeRankMap ) {
        super( graph, entityAbstractFactory, distanceFactory );
        for ( Shortcut shortcut : shortcuts ) {
            graph.addEdge( shortcut );
        }
        int nodeCount = graph.getNodes().size();
        int edgeCount = graph.getEdges().size();
        this.nodePositionMap = new HashMap<>();
        this.edgePositionMap = new HashMap<>();
        this.origNodes = new Node[nodeCount];
        this.origEdges = new Edge[edgeCount];
        this.edgeLengthArray = new double[edgeCount];
        this.edgeSourceArray = new int[edgeCount];
        this.edgeTargetArray = new int[edgeCount];
        this.outgoingEdgesArray = new int[nodeCount][];
        this.incomingEdgesArray = new int[nodeCount][];
        this.rankArray = new int[nodeCount];
        this.distancePrototype = new double[nodeCount];
        this.predecessorPrototype = new int[nodeCount];
        int counter = 0;
        for ( Node node : graph.getNodes() ) {
            nodePositionMap.put( node.getId(), counter );
            origNodes[counter] = node;
            rankArray[counter] = nodeRankMap.get( node.getId() );
            distancePrototype[counter] = Double.MAX_VALUE;
            predecessorPrototype[counter] = -1;
            counter++;
        }
        counter = 0;
        for ( Edge edge : graph.getEdges() ) {
            edgePositionMap.put( edge.getId(), counter );
            origEdges[counter] = edge;
            edgeLengthArray[counter] = edge.getDistance().getEvaluableValue();
            edgeSourceArray[counter] = nodePositionMap.get( edge.getSourceNode().getId() );
            edgeTargetArray[counter] = nodePositionMap.get( edge.getTargetNode().getId() );
            counter++;
        }
        counter = 0;
        for ( Node node : graph.getNodes() ) {
            Set<Edge> incomingEdgesOf = graph.getIncomingEdgesOf( node );
            int i = 0;
            incomingEdgesArray[counter] = new int[incomingEdgesOf.size()];
            for ( Edge edge : incomingEdgesOf ) {
                incomingEdgesArray[counter][i++] = edgePositionMap.get( edge.getId() );
            }
            Set<Edge> outgoingEdgesOf = graph.getOutgoingEdgesOf( node );
            i = 0;
            outgoingEdgesArray[counter] = new int[outgoingEdgesOf.size()];
            for ( Edge edge : outgoingEdgesOf ) {
                outgoingEdgesArray[counter][i++] = edgePositionMap.get( edge.getId() );
            }
            counter++;
        }
        this.nodeDataStructure = new JgraphtFibonacciDataStructure[2];
        this.nodeDataStructure[0] = new JgraphtFibonacciDataStructure<>();
        this.nodeDataStructure[1] = new JgraphtFibonacciDataStructure<>();
        for ( Shortcut shortcut : shortcuts ) {
            graph.removeEdge( shortcut );
        }
    }

    @Override
    public Path route( Map<Node.Id, Distance> from, Map<Node.Id, Distance> to ) {
        TimeMeasurement time = new TimeMeasurement();
        time.setTimeUnits( TimeUnits.NANOSECONDS );
        if ( DEBUG_TIME ) {
            time.start();
        }
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }

        List<Integer> fromVisitedNodes = new ArrayList<>();
        List<Integer> toVisitedNodes = new ArrayList<>();
        double[] fromDistanceArray = new double[origNodes.length];
        double[] toDistanceArray = new double[origNodes.length];
        System.arraycopy( distancePrototype, 0, fromDistanceArray, 0, distancePrototype.length );
        System.arraycopy( distancePrototype, 0, toDistanceArray, 0, distancePrototype.length );
        int[] fromPredecessorArray = new int[origNodes.length];
        int[] toPredecessorArray = new int[origNodes.length];
        System.arraycopy( predecessorPrototype, 0, fromPredecessorArray, 0, predecessorPrototype.length );
        System.arraycopy( predecessorPrototype, 0, toPredecessorArray, 0, predecessorPrototype.length );
        nodeDataStructure[0].clear();
        nodeDataStructure[1].clear();
        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            int idx = nodePositionMap.get( entry.getKey() );
            double value = entry.getValue().getEvaluableValue();
            fromDistanceArray[idx] = value;
            nodeDataStructure[0].add( idx, value );
        }
        for ( Map.Entry<Node.Id, Distance> entry : to.entrySet() ) {
            int idx = nodePositionMap.get( entry.getKey() );
            double value = entry.getValue().getEvaluableValue();
            toDistanceArray[idx] = value;
            nodeDataStructure[1].add( idx, value );
        }

        if ( DEBUG_TIME ) {
            System.out.println( "Data prepared in " + time.getTimeString() );
            time.start();
        }
        if ( DEBUG_CORRECTNESS ) {
            System.out.println( "FROM" );
        }
        boolean fromRun = true; //false = to run
        double UB = Double.MAX_VALUE;
        while ( !nodeDataStructure[0].isEmpty() || !nodeDataStructure[1].isEmpty() ) {
            if ( nodeDataStructure[0].isEmpty() ) {
                fromRun = false;
            }
            if ( nodeDataStructure[1].isEmpty() ) {
                fromRun = true;
            }

            if ( fromRun ) {
                // extract node S with the minimal distance
                int currentNode = nodeDataStructure[0].extractMin();
                fromVisitedNodes.add( currentNode );
                int sourceRank = rankArray[currentNode];
                double currentDistance = fromDistanceArray[currentNode];
                if ( DEBUG_CORRECTNESS ) {
                    System.out.println( "current node = " + origNodes[currentNode].getId().getValue() + ", " + currentDistance );
                }
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                // foreach neighbour T of node S
                for ( int i = 0; i < outgoingEdgesArray[currentNode].length; i++ ) {
                    int edge = outgoingEdgesArray[currentNode][i];
                    int otherNode = edgeTargetArray[edge];
                    if ( rankArray[otherNode] > sourceRank ) {
                        if ( MEASURE_STATS ) {
                            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                        }
                        double otherNodeDistance = fromDistanceArray[otherNode];
                        double distance = currentDistance + edgeLengthArray[edge];
                        if ( distance < otherNodeDistance ) {
                            fromDistanceArray[otherNode] = distance;
                            fromPredecessorArray[otherNode] = edge;
                            if ( !nodeDataStructure[0].contains( otherNode ) ) {
                                nodeDataStructure[0].add( otherNode, distance );
                            } else {
                                nodeDataStructure[0].notifyDataChange( otherNode, distance );
                            }
                        }
                    }
                }

                fromRun = false;
                if ( toVisitedNodes.contains( currentNode ) ) {
                    double dist = fromDistanceArray[currentNode] + toDistanceArray[currentNode];
                    if ( dist < UB ) {
                        UB = dist;
                    }
                }
            } else {
                // extract node S with the minimal distance
                int currentNode = nodeDataStructure[1].extractMin();
                toVisitedNodes.add( currentNode );
                int sourceRank = rankArray[currentNode];
                double currentDistance = toDistanceArray[currentNode];
                if ( DEBUG_CORRECTNESS ) {
                    System.out.println( "current node = " + origNodes[currentNode].getId().getValue() + ", " + currentDistance );
                }
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                // foreach neighbour T of node S
                for ( int i = 0; i < incomingEdgesArray[currentNode].length; i++ ) {
                    int edge = incomingEdgesArray[currentNode][i];
                    int otherNode = edgeSourceArray[edge];
                    if ( rankArray[otherNode] > sourceRank ) {
                        double otherNodeDistance = toDistanceArray[otherNode];
                        double distance = currentDistance + edgeLengthArray[edge];
                        if ( distance < otherNodeDistance ) {
                            if ( MEASURE_STATS ) {
                                StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                            }
                            toDistanceArray[otherNode] = distance;
                            toPredecessorArray[otherNode] = edge;
                            if ( !nodeDataStructure[1].contains( otherNode ) ) {
                                nodeDataStructure[1].add( otherNode, distance );
                            } else {
                                nodeDataStructure[1].notifyDataChange( otherNode, distance );
                            }
                        }
                    }
                }

                fromRun = true;
                if ( fromVisitedNodes.contains( currentNode ) ) {
                    double dist = fromDistanceArray[currentNode] + toDistanceArray[currentNode];
                    if ( dist < UB ) {
                        UB = dist;
                    }
                }
            }

            if ( fromDistanceArray[nodeDataStructure[0].peek()] + toDistanceArray[nodeDataStructure[1].peek()] >= UB ) {
                break;
            }
        }

        if ( DEBUG_TIME ) {
            System.out.println( "Dijkstra done in " + time.getTimeString() );
            time.start();
        }

        double minDistance = Double.MAX_VALUE;
        int minNode = -1;
        for ( Integer fromVisitedNode : fromVisitedNodes ) {
            double dist = fromDistanceArray[fromVisitedNode] + toDistanceArray[fromVisitedNode];
            if ( 0 <= dist && dist < minDistance ) {
                minDistance = dist;
                minNode = fromVisitedNode;
            }
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.STOP );
        }

        System.out.println( "Min distance = " + minDistance );

        if ( DEBUG_TIME ) {
            System.out.println( "Min node found in " + time.getTimeString() );
            time.start();
        }
        if ( DEBUG_CORRECTNESS ) {
            System.out.println( "min node = " + origNodes[minNode] );
        }
        if ( minNode != -1 ) {
            if ( MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.START );
            }
            List<Edge> startEdges = new ArrayList<>();
            int currentNode = minNode;
            int firstNode = currentNode;
            int edge = fromPredecessorArray[currentNode];
            while ( edge != -1 ) {
                startEdges.add( origEdges[edge] );
                currentNode = edgeSourceArray[edge];
                firstNode = currentNode;
                edge = fromPredecessorArray[currentNode];
            }
            List<Edge> endEdges = new ArrayList<>();
            currentNode = minNode;
            edge = toPredecessorArray[currentNode];
            while ( edge != -1 ) {
                endEdges.add( origEdges[edge] );
                currentNode = edgeTargetArray[edge];
                edge = toPredecessorArray[currentNode];
            }
            Path path = getEntityAbstractFactory().createPathWithSource( getGraph(), origNodes[firstNode] );
            List<Edge> pathEdges = new ArrayList<>();
            for ( int i = startEdges.size() - 1; i >= 0; i-- ) {
                addEdges( pathEdges, startEdges.get( i ) );
            }
            for ( int i = 0; i < endEdges.size(); i++ ) {
                addEdges( pathEdges, endEdges.get( i ) );
            }
            for ( Edge pathEdge : pathEdges ) {
                if ( pathEdge instanceof Shortcut ) {
                    throw new AssertionError( "Cannot happen" );
                }
                path.addEdge( pathEdge );
            }

            if ( MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
            }
            if ( DEBUG_TIME ) {
                System.out.println( "Path built in " + time.getTimeString() );
                time.start();
            }
            return path;
        } else {
            return null;
        }
    }

    private void addEdges( List<Edge> edges, Edge edge ) {
        if ( edge instanceof Shortcut ) {
            Shortcut shortcut = (Shortcut) edge;
            addEdges( edges, shortcut.getSourceEdge() );
            addEdges( edges, shortcut.getTargetEdge() );
        } else {
            edges.add( edge );
        }
    }

}
