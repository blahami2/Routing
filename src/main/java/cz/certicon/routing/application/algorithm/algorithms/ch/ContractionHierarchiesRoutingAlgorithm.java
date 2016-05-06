/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.ch;

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
import cz.certicon.routing.utils.measuring.TimeMeasurement;
import cz.certicon.routing.utils.measuring.TimeUnits;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cz.certicon.routing.GlobalOptions.*;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private Map<Node.Id, Integer> nodeRankMap;
    private final Map<Node.Id, Distance> fromDistanceMap;
    private final Map<Node.Id, Edge> fromPredecessorMap;
    private final Map<Node.Id, Distance> toDistanceMap;
    private final Map<Node.Id, Edge> toPredecessorMap;
    private final NodeDataStructure<Node> nodeDataStructure;

    public ContractionHierarchiesRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory, Map<Node.Id, Integer> nodeRankMap ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeRankMap = nodeRankMap;
        this.fromDistanceMap = new HashMap<>();
        this.fromPredecessorMap = new HashMap<>();
        this.toDistanceMap = new HashMap<>();
        this.toPredecessorMap = new HashMap<>();
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
    }

    public Map<Node.Id, Integer> getNodeRankMap() {
        return nodeRankMap;
    }

    public void setNodeRankMap( Map<Node.Id, Integer> nodeRankMap ) {
        this.nodeRankMap = nodeRankMap;
    }

    @Override
    public Path route( Node.Id from, Node.Id to ) {
        Map<Node.Id, Distance> fromMap = new HashMap<>();
        fromMap.put( from, getDistanceFactory().createZeroDistance() );
        Map<Node.Id, Distance> toMap = new HashMap<>();
        toMap.put( to, getDistanceFactory().createZeroDistance() );
        return route( fromMap, toMap );
    }

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
        long executionTime = 0;
        int edgesOpposite = 0;
        int edgesLower = 0;
        if ( DEBUG_TIME ) {
            time.setTimeUnits( TimeUnits.NANOSECONDS );
            System.out.println( "Routing..." );
            time.start();
        }

        // FROM dijkstra
        nodeDataStructure.clear();
        fromDistanceMap.clear();
        fromPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            fromDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
//            System.out.println( entry.getKey() + " => " + entry.getValue().getEvaluableValue() );
        }
        if ( DEBUG_CORRECTNESS ) {
            System.out.println( "FROM" );
        }
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            if ( DEBUG_TIME ) {
                accTime.start();
            }
            Node currentNode = nodeDataStructure.extractMin();
            if ( DEBUG_TIME ) {
                extractMinTime += accTime.restart();
                visitedNodes++;
            }
//            System.out.println( "current node = " + currentNode.getId() + ", distance = " + fromDistanceMap.get( currentNode.getId() )+ ", rank = " + nodeRankMap.get( currentNode.getId()) );
            int sourceRank = nodeRankMap.get( currentNode.getId() );
            if ( DEBUG_TIME ) {
                nodeRankAccessTime += accTime.restart();
            }
            Distance currentDistance = fromDistanceMap.get( currentNode.getId() );
            if ( DEBUG_CORRECTNESS ) {
                System.out.println( "current node = " + currentNode.getId().getValue() + ", " + currentDistance.getEvaluableValue() );
            }
            if ( DEBUG_TIME ) {
                distanceAccessTime += accTime.restart();
                edgeTime.clear();
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getEdgesOf( currentNode ) ) {
                if ( DEBUG_TIME ) {
                    edgeProcessingTime += edgeTime.stop();
                    edgeTime.start();
                    vEdgeTime.start();
                    edgesCount++;
                }
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                if ( !edge.getSourceNode().equals( currentNode ) ) {
                    if ( DEBUG_TIME ) {
                        edgesOpposite++;
                    }
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                int endRank = nodeRankMap.get( endNode.getId() );
                if ( endRank <= sourceRank ) {
                    if ( DEBUG_TIME ) {
                        edgesLower++;
                    }
                    continue;
                }
                if ( DEBUG_TIME ) {
                    edgesVisited++;
                }
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = currentDistance.add( edge.getDistance() );
                // replace is lower than actual
                Distance dist = fromDistanceMap.get( endNode.getId() );
                if ( dist == null || tmpNodeDistance.isLowerThan( dist ) ) {
//                    System.out.println( "changing for " + endNode.getId() + " from: " + fromDistanceMap.get( endNode.getId()) + " to: " + tmpNodeDistance );
                    fromDistanceMap.put( endNode.getId(), tmpNodeDistance );
                    fromPredecessorMap.put( endNode.getId(), edge );
                    if ( !nodeDataStructure.contains( endNode ) ) {
                        nodeDataStructure.add( endNode, tmpNodeDistance.getEvaluableValue() );
                    } else {
                        nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                    }
                }
                if ( DEBUG_TIME ) {
                    visitedEdgeProcessingTime += vEdgeTime.stop();
                }
            }
        }
        if ( DEBUG_TIME ) {
            System.out.println( "From dijkstra done in " + time.getTimeString() );
            executionTime += time.restart();
        }

        // TO dijkstra
        nodeDataStructure.clear();
        toDistanceMap.clear();
        toPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : to.entrySet() ) {
            toDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
        }
        if ( DEBUG_CORRECTNESS ) {
            System.out.println( "TO" );
        }
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            if ( DEBUG_TIME ) {
                extractMinTime += accTime.restart();
                visitedNodes++;
            }
//            System.out.println( "current node = " + currentNode.getId() + ", distance = " + toDistanceMap.get( currentNode.getId() ) + ", rank = " + nodeRankMap.get( currentNode.getId()) );
//            System.out.println( "extracted: " + currentNode.getId() + ", " + toDistanceMap.get( currentNode.getId() ) );
            int sourceRank = nodeRankMap.get( currentNode.getId() );
            if ( DEBUG_TIME ) {
                nodeRankAccessTime += accTime.restart();
            }
            Distance currentDistance = toDistanceMap.get( currentNode.getId() );
            if ( DEBUG_CORRECTNESS ) {
                System.out.println( "current node = " + currentNode.getId().getValue() + ", " + currentDistance.getEvaluableValue() );
            }
            if ( DEBUG_TIME ) {
                distanceAccessTime += accTime.restart();
                edgeTime.clear();
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getEdgesOf( currentNode ) ) {
                if ( DEBUG_TIME ) {
                    edgeProcessingTime += edgeTime.stop();
                    edgeTime.start();
                    vEdgeTime.start();
                    edgesCount++;
                }
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                if ( !edge.getTargetNode().equals( currentNode ) ) {
                    if ( DEBUG_TIME ) {
                        edgesOpposite++;
                    }
                    continue;
                }
                if ( DEBUG_TIME ) {
                    edgesVisited++;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                int endRank = nodeRankMap.get( endNode.getId() );
                if ( endRank <= sourceRank ) {
                    if ( DEBUG_TIME ) {
                        edgesLower++;
                    }
                    continue;
                }
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = currentDistance.add( edge.getDistance() );
                // replace is lower than actual
                Distance dist = toDistanceMap.get( endNode.getId() );
                if ( dist == null || tmpNodeDistance.isLowerThan( dist ) ) {
                    toDistanceMap.put( endNode.getId(), tmpNodeDistance );
                    toPredecessorMap.put( endNode.getId(), edge );
                    if ( !nodeDataStructure.contains( endNode ) ) {
                        nodeDataStructure.add( endNode, tmpNodeDistance.getEvaluableValue() );
                    } else {
                        nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                    }
                }
                if ( DEBUG_TIME ) {
                    visitedEdgeProcessingTime += vEdgeTime.stop();
                }
            }
        }
        if ( DEBUG_TIME ) {
            System.out.println( "To dijkstra done in " + time.getTimeString() );
            executionTime += time.restart();
        }

        if ( DEBUG_TIME ) {
            System.out.println( "visited nodes: " + visitedNodes );
            System.out.println( "time per node: " + ( executionTime / visitedNodes ) );
            System.out.println( "extract min time: " + extractMinTime );
            System.out.println( "extract min time per node: " + ( extractMinTime / visitedNodes ) );
            System.out.println( "node rank access time: " + nodeRankAccessTime );
            System.out.println( "node rank access time per node: " + ( nodeRankAccessTime / visitedNodes ) );
            System.out.println( "distance access time: " + distanceAccessTime );
            System.out.println( "distance access time per node: " + ( distanceAccessTime / visitedNodes ) );
            System.out.println( "edges: " + edgesCount );
            System.out.println( "visited edges: " + edgesVisited );
            System.out.println( "visited edges ratio: " + ( 100 * edgesVisited / (double) edgesCount ) + "%" );
            System.out.println( "opposite edges: " + edgesOpposite );
            System.out.println( "opposite edges ratio: " + ( 100 * edgesOpposite / (double) edgesCount ) + "%" );
            System.out.println( "lower edges: " + edgesLower );
            System.out.println( "lower edges ratio: " + ( 100 * edgesLower / (double) edgesCount ) + "%" );
            System.out.println( "edge time: " + ( edgeProcessingTime ) );
            System.out.println( "edge time per edge: " + ( edgeProcessingTime / edgesCount ) );
            System.out.println( "edge visiting time: " + ( visitedEdgeProcessingTime ) );
            System.out.println( "edge visiting time per edge: " + ( visitedEdgeProcessingTime / edgesVisited ) );
            System.out.println( "edge not visited time: " + ( edgeProcessingTime - visitedEdgeProcessingTime ) );
            System.out.println( "edge not visited time per edge: " + ( ( edgeProcessingTime - visitedEdgeProcessingTime ) / ( edgesCount - edgesVisited ) ) );

            time.start();
        }

        long pathBuildingTime = 0;
        // find SP
        Distance minDistance = getDistanceFactory().createInfiniteDistance();
        Node.Id minNodeId = null;
        for ( Map.Entry<Node.Id, Distance> entry : fromDistanceMap.entrySet() ) {
            if ( toDistanceMap.containsKey( entry.getKey() ) ) {
                Distance tmpDistance = entry.getValue().add( toDistanceMap.get( entry.getKey() ) );
                if ( tmpDistance.isLowerThan( minDistance ) ) {
                    minDistance = tmpDistance;
                    minNodeId = entry.getKey();
                }
            }
        }
        if ( DEBUG_CORRECTNESS ) {
            System.out.println( "min node = " + getGraph().getNode( minNodeId ) );
        }
        if ( DEBUG_TIME ) {
            System.out.println( "Min node found in " + time.getTimeString() );
        }
//        System.out.println( "min node = " + minNodeId  + ", " + minDistance);
        if ( minNodeId == null ) {
            return null;
        } else {
            time.start();
//            System.out.println( "FROM" );
//            for ( Map.Entry<Node.Id, Distance> entry : fromDistanceMap.entrySet() ) {
//                System.out.println( "" + entry.getKey() + " => " + entry.getValue() );
//            }
//            System.out.println( "TO" );
//            for ( Map.Entry<Node.Id, Distance> entry : toDistanceMap.entrySet() ) {
//                System.out.println( "" + entry.getKey() + " => " + entry.getValue() );
//            }
            List<Edge> startEdges = new ArrayList<>();
            Node currentNode = getGraph().getNode( minNodeId );
            Node firstNode = currentNode;
//            System.out.println( "middle = " + minNodeId );
            while ( fromPredecessorMap.get( currentNode.getId() ) != null ) {
                startEdges.add( fromPredecessorMap.get( currentNode.getId() ) );
//                System.out.println( "adding: " + fromPredecessorMap.get( currentNode.getId() ).getId() );
                currentNode = getGraph().getOtherNodeOf( fromPredecessorMap.get( currentNode.getId() ), currentNode );
                firstNode = currentNode;
            }
            if ( DEBUG_TIME ) {
                System.out.println( "#1: " + time.getTimeString() );
                pathBuildingTime += time.restart();
            }
            List<Edge> endEdges = new ArrayList<>();
            currentNode = getGraph().getNode( minNodeId );
            while ( toPredecessorMap.get( currentNode.getId() ) != null ) {
                endEdges.add( toPredecessorMap.get( currentNode.getId() ) );
//                System.out.println( "adding: " + toPredecessorMap.get( currentNode.getId() ).getId() );
                currentNode = getGraph().getOtherNodeOf( toPredecessorMap.get( currentNode.getId() ), currentNode );
            }
            if ( DEBUG_TIME ) {
                System.out.println( "#2: " + time.getTimeString() );
                pathBuildingTime += time.restart();
            }
            Path path = getEntityAbstractFactory().createPathWithSource( getGraph(), firstNode );
//            System.out.println( "first node = " + firstNode );
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
            if ( DEBUG_TIME ) {
                System.out.println( "#3: " + time.getTimeString() );
                pathBuildingTime += time.restart();
                System.out.println( "Path construction done in " + pathBuildingTime + " ns" );
            }
            return path;
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
