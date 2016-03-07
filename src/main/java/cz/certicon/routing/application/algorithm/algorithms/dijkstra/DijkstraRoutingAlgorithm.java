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
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.datastructures.TrivialNodeDataStructure;
import cz.certicon.routing.model.entity.NoPathException;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.utils.GraphUtils;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private NodeDataStructure nodeDataStructure;
    private EndCondition endCondition;

    public DijkstraRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new TrivialNodeDataStructure();
        this.endCondition = new EndCondition() {
            @Override
            public boolean isFinished( Graph graph, Node sourceNode, Node targetNode, Node currentNode ) {
                return targetNode.equals( currentNode );
            }

            @Override
            public Path getResult( Graph graph, GraphEntityFactory graphEntityFactory, Node sourceNode, Node targetNode ) {
                return GraphUtils.createPath( graph, graphEntityFactory, sourceNode, targetNode );
            }
        };
//        System.out.println( "============ DIJKSTRA =============" );
//        for ( Node node : graph.getNodes() ) {
//            System.out.println( "node: " + node.getLabel() );
//        }
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( "edge: " + edge.getLabel() );
//        }
    }

    public DijkstraRoutingAlgorithm setEndCondition( EndCondition endCondition ) {
        this.endCondition = endCondition;
        return this;
    }

    public DijkstraRoutingAlgorithm setNodeDataStructure( NodeDataStructure nodeDataStructure ) {
        this.nodeDataStructure = nodeDataStructure;
        return this;
    }

    @Override
    public Path route( Node from, Node to ) throws NoPathException {
//        System.out.println( "routing from: " + from.getLabel() + " to " + to.getLabel() );
        // clear the data structure
        nodeDataStructure.clear();
        Node nodeEqToFrom = from;
        Node nodeEqToTo = to;
        // foreach node in G
        for ( Node node : getGraph().getNodes() ) {
            if ( node.getCoordinates().equals( from.getCoordinates() ) ) {
                nodeDataStructure.add( node.setDistance( getDistanceFactory().createZeroDistance() ).setPredecessorEdge( null ) );
            } else { // set distance to infinity
                nodeDataStructure.add( node.setDistance( getDistanceFactory().createInfiniteDistance() ).setPredecessorEdge( null ) );
                if ( node.getCoordinates().equals( to.getCoordinates() ) ) {
                    nodeEqToTo = node;
                }
            }
//            System.out.println( "node (" + node.getLabel() + ") distance = " + node.getDistance() );
        }
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
//            System.out.println( "extracted node: " + currentNode.getLabel() );
//            System.out.println( "nodes left: " + nodeDataStructure.size() );
            if ( endCondition.isFinished( getGraph(), nodeEqToFrom, nodeEqToTo, currentNode ) ) {
//                System.out.println( "found, breaking" );
                // build path from predecessors and return
                return endCondition.getResult( getGraph(), getEntityAbstractFactory(), nodeEqToFrom, nodeEqToTo );
            }
            // foreach neighbour T of node S
            getGraph().getOutgoingEdgesOf( currentNode ).stream().forEach( ( edge ) -> {
//                {
//                    Coordinate first = new Coordinate( 50.077595, 14.4304993 ); // 352744338
//                    Coordinate second = new Coordinate( 50.0791829, 14.4327469 ); // 25936035
//                    Node sourceNode = edge.getSourceNode();
//                    Node targetNode = edge.getTargetNode();
//                    if ( ( sourceNode.getCoordinates().equals( first ) && targetNode.getCoordinates().equals( second ) )
//                            || ( sourceNode.getCoordinates().equals( second ) && targetNode.getCoordinates().equals( first ) ) ) {
//                        System.out.println( edge );
//                        System.out.println( "outgoing edges of: " + currentNode );
//                    }
//                }

//                System.out.println( "edge = " + edge.getLabel() );
//                System.out.println( "nodes: s = " + edge.getSourceNode().getLabel() + ", t = " + edge.getTargetNode().getLabel() );
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
//                System.out.println( "checking node: " + endNode.getLabel() + " with distance = " + endNode.getDistance() );
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance = getRoutingConfiguration().getNodeEvaluator().evaluate( currentNode, edge, endNode );
                // replace is lower than actual
                if ( tmpNodeDistance.isLowerThan( endNode.getDistance() ) ) {
                    endNode.setDistance( tmpNodeDistance );
                    endNode.setPredecessorEdge( edge );
                    nodeDataStructure.notifyDataChange( endNode );
                }
            } );
        }
        throw new NoPathException( from, to );
    }

}
