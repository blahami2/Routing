/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.algorithm.algorithms.dijkstra;

import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.application.algorithm.algorithms.AbstractRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.AlgorithmConfiguration;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.RoutingConfiguration;
import cz.certicon.routing.model.entity.NoPathException;
import cz.certicon.routing.model.entity.GraphEntityFactory;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private final AlgorithmConfiguration algorithmConfiguration;
    private final RoutingConfiguration routingConfiguration;
    private final NodeDataStructure nodeDataStructure;
    private final DistanceFactory distanceFactory;

    public DijkstraRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, NodeDataStructure nodeDataStructure, AlgorithmConfiguration algorithmConfiguration, RoutingConfiguration routingConfiguration, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory );
        this.nodeDataStructure = nodeDataStructure;
        this.algorithmConfiguration = algorithmConfiguration;
        this.routingConfiguration = routingConfiguration;
        this.distanceFactory = distanceFactory;
    }

    @Override
    public Path route( Node from, Node to ) throws NoPathException {
        System.out.println( "routing from: " + from.getLabel() + " to " + to.getLabel() );
        // clear the data structure
        nodeDataStructure.clear();
        Node nodeEqToFrom = from;
        Node nodeEqToTo = to;
        // foreach node in G
        for ( Node node : getGraph().getNodes() ) {
            if ( node.getCoordinates().equals( from.getCoordinates() ) ) {
                nodeEqToFrom = node;
            } else { // set distance to infinity
                nodeDataStructure.add( node.setDistance( distanceFactory.createInfiniteDistance() ).setPredecessorEdge( null ) );
                if ( node.getCoordinates().equals( to.getCoordinates() ) ) {
                    nodeEqToTo = node;
                }
            }
        }
        // set source node distance to zero
        nodeDataStructure.add( nodeEqToFrom.setDistance( distanceFactory.createZeroDistance() ).setPredecessorEdge( null ) );
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
            System.out.println( "extracted node: " + currentNode.getLabel() );
            System.out.println( "nodes left: " + nodeDataStructure.size() );
            if ( currentNode.equals( nodeEqToTo ) ) {
                System.out.println( "found, breaking" );
                break;
            }
            // foreach neighbour T of node S
            getGraph().getOutgoingEdgesOf( currentNode ).stream().forEach( ( edge ) -> {
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                System.out.println( "checking node: " + endNode.getLabel() );
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = routingConfiguration.getNodeEvaluator().evaluate( currentNode, edge, endNode );
                // replace is lower than actual
                if ( tmpNodeDistance.isLowerThan( endNode.getDistance() ) ) {
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    nodeDataStructure.notifyDataChange( endNode );
                }
            } );
        }
        if ( nodeEqToTo.getPredecessorEdge() == null ) {
            throw new NoPathException( from, to );
        }
        // build path from predecessors
        Path path = getEntityAbstractFactory().createPathWithTarget( getGraph(), nodeEqToTo );
        Node currentNode = nodeEqToTo;
        while ( !currentNode.equals( nodeEqToFrom ) ) {
            System.out.println( "backtracking: " + currentNode.getLabel() );
            path.addEdgeAsFirst( currentNode.getPredecessorEdge() );
            currentNode = getGraph().getOtherNodeOf( currentNode.getPredecessorEdge(), currentNode );
        }
        return path;
    }

}
