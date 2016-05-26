/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.dijkstra;

import cz.certicon.routing.GlobalOptions;
import static cz.certicon.routing.GlobalOptions.*;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.application.algorithm.algorithms.AbstractRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.utils.GraphUtils;
import cz.certicon.routing.utils.measuring.TimeLogger;
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.model.basic.TimeUnits;
import cz.certicon.routing.utils.measuring.StatsLogger;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Basic routing algorithm implementation using the optimal Dijkstra.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private static final boolean DEBUG_TIME = GlobalOptions.DEBUG_TIME;

    private NodeDataStructure<Node> nodeDataStructure;

    public DijkstraRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
//        System.out.println( "============ DIJKSTRA =============" );
//        for ( Node node : graph.getNodes() ) {
//            System.out.println( "node: " + node.getLabel() );
//        }
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( "edge: " + edge.getLabel() );
//        }
    }

    public void setNodeDataStructure( NodeDataStructure nodeDataStructure ) {
        this.nodeDataStructure = nodeDataStructure;
    }

//    static int cnt = 1000;
    @Override
    public Path route( Map<Node.Id, Distance> from, Map<Node.Id, Distance> to ) {
        /* DEBUG VARS */
        TimeMeasurement time = new TimeMeasurement();
        TimeMeasurement accTime = new TimeMeasurement();
        accTime.setTimeUnits( TimeUnits.NANOSECONDS );
        TimeMeasurement edgeTime = new TimeMeasurement();
        edgeTime.setTimeUnits( TimeUnits.NANOSECONDS );
        TimeMeasurement vEdgeTime = new TimeMeasurement();
        vEdgeTime.setTimeUnits( TimeUnits.NANOSECONDS );
        int visitedNodes = 0;
        int edgesCount = 0;
        int edgesVisited = 0;
        long extractMinTime = 0;
        long nodeRankAccessTime = 0;
        long distanceAccessTime = 0;
        long edgeProcessingTime = 0;
        long visitedEdgeProcessingTime = 0;
        int edgesOpposite = 0;
        int edgesLower = 0;
        if ( DEBUG_TIME ) {
            time.setTimeUnits( TimeUnits.NANOSECONDS );
            System.out.println( "Routing..." );
            time.start();
        }
        if ( MEASURE_STATS ) {
            StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.RESET );
            StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.RESET );
        }
        if ( MEASURE_TIME ) {
            TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.START );
        }
//        GraphPresenter gp = new GraphStreamPresenter();
//        gp.displayGraph( getGraph() );
//        try {
//            Thread.sleep(cnt);
//            cnt*=10;
//        } catch ( InterruptedException ex ) {
//            Logger.getLogger( DijkstraRoutingAlgorithm.class.getName() ).log( Level.SEVERE, null, ex );
//        }

        // clear the data structure
        nodeDataStructure.clear();
        Set<Node> closed = new HashSet<>();

        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            Node n = getGraph().getNode( entry.getKey() );
            n.setDistance( entry.getValue() );
            n.setPredecessorEdge( null );
            nodeDataStructure.add( n, entry.getValue().getEvaluableValue() );
        }
        Node finalNode = null;
        Distance finalDistance = getDistanceFactory().createInfiniteDistance();
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            if ( DEBUG_TIME ) {
                accTime.start();
            }
            Distance dist = currentNode.getDistance();
            if ( MEASURE_STATS ) {
                StatsLogger.log( StatsLogger.Statistic.NODES_EXAMINED, StatsLogger.Command.INCREMENT );
            }
            if ( DEBUG_TIME ) {
                distanceAccessTime += accTime.stop();
                visitedNodes++;
            }
            if ( dist.isGreaterThan( finalDistance ) ) {
                break;
            }
            if ( to.containsKey( currentNode.getId() ) ) {
                Distance wholeDistance = dist.add( to.get( currentNode.getId() ) );
                if ( wholeDistance.isLowerThan( finalDistance ) ) {
                    finalNode = currentNode;
                    finalDistance = wholeDistance;
                }
            }
//            System.out.println( "extracted: " + currentNode );
            closed.add( currentNode );
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getEdgesOf( currentNode ) ) {
                if ( DEBUG_TIME ) {
                    edgesCount++;
                }
                if ( !edge.getSourceNode().equals( currentNode ) ) {
                    continue;
                }
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                if ( closed.contains( endNode ) ) {
                    continue;
                }
                if ( DEBUG_TIME ) {
                    edgesVisited++;
                }
                if ( MEASURE_STATS ) {
                    StatsLogger.log( StatsLogger.Statistic.EDGES_EXAMINED, StatsLogger.Command.INCREMENT );
                }
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
                // replace is lower than actual
                if ( !nodeDataStructure.contains( endNode ) || tmpNodeDistance.isLowerThan( endNode.getDistance() ) ) {
//                    System.out.println( "is lower" );
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    if ( !nodeDataStructure.contains( endNode ) ) {
                        nodeDataStructure.add( endNode, tmpNodeDistance.getEvaluableValue() );
                    } else {
                        nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                    }
                }
            }
        }

        if ( finalNode != null ) {
            if ( MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.ROUTING, TimeLogger.Command.STOP );
            }
            if ( DEBUG_TIME ) {
                System.out.println( "Dijkstra done in " + time.getTimeString() );
                long fromExecutionTime = time.stop();

                System.out.println( "visited nodes: " + visitedNodes );
                System.out.println( "time per node: " + ( time.stop() / visitedNodes ) );
                System.out.println( "distance access time: " + distanceAccessTime );
                System.out.println( "distance access time per node: " + ( distanceAccessTime / visitedNodes ) );
                System.out.println( "edges: " + edgesCount );
                System.out.println( "visited edges: " + edgesVisited );
                System.out.println( "visited edges ratio: " + ( 100 * edgesVisited / (double) edgesCount ) + "%" );
                time.start();
            }
            // build path from predecessors and return
            if ( MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.START );
            }
            // build path from predecessors and return
            Path createPath = GraphUtils.createPath( getGraph(), getEntityAbstractFactory(), finalNode );
            if ( MEASURE_TIME ) {
                TimeLogger.log( TimeLogger.Event.ROUTE_BUILDING, TimeLogger.Command.STOP );
            }
            return GraphUtils.createPath( getGraph(), getEntityAbstractFactory(), finalNode );
        }
        return null;
    }

}
