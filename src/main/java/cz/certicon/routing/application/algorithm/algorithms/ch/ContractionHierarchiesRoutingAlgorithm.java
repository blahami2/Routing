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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        TimeMeasurement time = new TimeMeasurement();
        System.out.println( "Routing..." );
        time.start();

        // FROM dijkstra
        nodeDataStructure.clear();
        fromDistanceMap.clear();
        fromPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            fromDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
//            System.out.println( entry.getKey() + " => " + entry.getValue().getEvaluableValue() );
        }
//        System.out.println( "DIJKSTRA FROM" );
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
//            System.out.println( "current node = " + currentNode.getId() + ", distance = " + fromDistanceMap.get( currentNode.getId() )+ ", rank = " + nodeRankMap.get( currentNode.getId()) );
            int sourceRank = nodeRankMap.get( currentNode.getId() );
            Distance currentDistance = fromDistanceMap.get( currentNode.getId() );
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                if ( !edge.getSourceNode().equals( currentNode ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                int endRank = nodeRankMap.get( endNode.getId() );
                if ( endRank <= sourceRank ) {
                    continue;
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
            }
        }
        System.out.println( "From dijkstra done in " + time.stop() + " ms" );
        time.start();

        // TO dijkstra
        nodeDataStructure.clear();
        toDistanceMap.clear();
        toPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : to.entrySet() ) {
            toDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
        }
//        System.out.println( "DIJKSTRA TO" );
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
//            System.out.println( "current node = " + currentNode.getId() + ", distance = " + toDistanceMap.get( currentNode.getId() ) + ", rank = " + nodeRankMap.get( currentNode.getId()) );
//            System.out.println( "extracted: " + currentNode.getId() + ", " + toDistanceMap.get( currentNode.getId() ) );
            int sourceRank = nodeRankMap.get( currentNode.getId() );
            Distance currentDistance = toDistanceMap.get( currentNode.getId() );
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                if ( !edge.getTargetNode().equals( currentNode ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                int endRank = nodeRankMap.get( endNode.getId() );
                if ( endRank <= sourceRank ) {
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
            }
        }
        System.out.println( "To dijkstra done in " + time.stop() + " ms" );
        time.start();

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
//        System.out.println( "min node = " + minNodeId  + ", " + minDistance);
        if ( minNodeId == null ) {
            return null;
        } else {

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
            System.out.println( "#1: " + time.stop() + " ms" );
        time.start();
            List<Edge> endEdges = new ArrayList<>();
            currentNode = getGraph().getNode( minNodeId );
            while ( toPredecessorMap.get( currentNode.getId() ) != null ) {
                endEdges.add( toPredecessorMap.get( currentNode.getId() ) );
//                System.out.println( "adding: " + toPredecessorMap.get( currentNode.getId() ).getId() );
                currentNode = getGraph().getOtherNodeOf( toPredecessorMap.get( currentNode.getId() ), currentNode );
            }
            System.out.println( "#2: " + time.restart() + " ms" );
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
            System.out.println( "#3: " + time.restart() + " ms" );
            System.out.println( "Path construction done in " + time.restart() + " ms" );
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
