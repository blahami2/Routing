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
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private final Map<Node.Id, Integer> nodeRankMap;
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

    @Override
    public Path route( Node.Id from, Node.Id to ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Path route( Map<Node.Id, Distance> from, Map<Node.Id, Distance> to ) {
        // FROM dijkstra
        nodeDataStructure.clear();
        fromDistanceMap.clear();
        fromPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : from.entrySet() ) {
            fromDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
        }
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            int sourceRank = nodeRankMap.get( currentNode.getId() );
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
                Distance tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
                // replace is lower than actual
                if ( tmpNodeDistance.isLowerThan( fromDistanceMap.get( endNode.getId() ) ) ) {
                    fromDistanceMap.put( endNode.getId(), tmpNodeDistance );
                    fromPredecessorMap.put( endNode.getId(), edge );
                    nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                }
            }
        }

        // TO dijkstra
        nodeDataStructure.clear();
        toDistanceMap.clear();
        toPredecessorMap.clear();
        for ( Map.Entry<Node.Id, Distance> entry : to.entrySet() ) {
            toDistanceMap.put( entry.getKey(), entry.getValue() );
            nodeDataStructure.add( getGraph().getNode( entry.getKey() ), entry.getValue().getEvaluableValue() );
        }
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            int sourceRank = nodeRankMap.get( currentNode.getId() );
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
                Distance tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
                // replace is lower than actual
                if ( tmpNodeDistance.isLowerThan( toDistanceMap.get( endNode.getId() ) ) ) {
                    toDistanceMap.put( endNode.getId(), tmpNodeDistance );
                    toPredecessorMap.put( endNode.getId(), edge );
                    nodeDataStructure.notifyDataChange( endNode, tmpNodeDistance.getEvaluableValue() );
                }
            }
        }

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
        if ( minNodeId == null ) {
            return null;
        } else {
            // build path
        }
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}
