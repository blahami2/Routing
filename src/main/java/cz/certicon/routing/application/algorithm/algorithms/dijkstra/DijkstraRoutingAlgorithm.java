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
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.presentation.GraphPresenter;
import cz.certicon.routing.presentation.graphstream.GraphStreamPresenter;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic routing algorithm implementation using the optimal Dijkstra.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class DijkstraRoutingAlgorithm extends AbstractRoutingAlgorithm {

    private NodeDataStructure<Node> nodeDataStructure;
    private EndCondition endCondition;

    public DijkstraRoutingAlgorithm( Graph graph, GraphEntityFactory entityAbstractFactory, DistanceFactory distanceFactory ) {
        super( graph, entityAbstractFactory, distanceFactory );
        this.nodeDataStructure = new JgraphtFibonacciDataStructure();
        this.endCondition = new EndCondition() {

            @Override
            public boolean isFinished( Graph graph, Map<Node.Id, Distance> targetSet, Node currentNode ) {
                return targetSet.containsKey( currentNode.getId() );
            }

            @Override
            public Path getResult( Graph graph, GraphEntityFactory graphEntityFactory, Node targetNode ) {
                return GraphUtils.createPath( graph, graphEntityFactory, targetNode );
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

    public void setEndCondition( EndCondition endCondition ) {
        this.endCondition = endCondition;
    }

    public void setNodeDataStructure( NodeDataStructure nodeDataStructure ) {
        this.nodeDataStructure = nodeDataStructure;
    }

//    static int cnt = 1000;
    
    @Override
    public Path route( Map<Node.Id, Distance> from, Map<Node.Id, Distance> to ) {
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
        // set source node distance to zero
        // while the data structure is not empty (or while the target node is not found)
        while ( !nodeDataStructure.isEmpty() ) {
            // extract node S with the minimal distance
            Node currentNode = nodeDataStructure.extractMin();
//            System.out.println( "extracted: " + currentNode );
            closed.add( currentNode );
            if ( endCondition.isFinished( getGraph(), to, currentNode ) ) {
                // build path from predecessors and return
                return endCondition.getResult( getGraph(), getEntityAbstractFactory(), currentNode );
            }
            // foreach neighbour T of node S
            for ( Edge edge : getGraph().getOutgoingEdgesOf( currentNode ) ) {
                if ( !getRoutingConfiguration().getEdgeValidator().validate( edge ) ) {
                    continue;
                }
                Node endNode = getGraph().getOtherNodeOf( edge, currentNode );
                if ( closed.contains( endNode ) ) {
                    continue;
                }
                // calculate it's distance S + path from S to T
                Distance tmpNodeDistance;
                if ( to.containsKey( currentNode.getId() ) ) {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode, to.get( currentNode.getId() ) );
                } else {
//                    try {
                    tmpNodeDistance = getRoutingConfiguration().getDistanceEvaluator().evaluate( currentNode, edge, endNode );
//                    } catch ( NullPointerException ex ) {
//                        System.out.println( "current node = " + currentNode );
//                        System.out.println( "current edge = " + edge );
//                        throw ex;
//                    }
                }
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
        return null;
    }

}
