/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphUtils {

    public static Path createPath( Graph graph, GraphEntityFactory graphEntityFactory, Node sourceNode, Node targetNode ) {
        Path path = graphEntityFactory.createPathWithTarget( graph, targetNode );
        Node currentNode = targetNode;
        while ( !currentNode.getCoordinates().equals( sourceNode.getCoordinates() ) ) {
//            System.out.println( "current node = " + currentNode );
//            System.out.println( "edge = " + currentNode.getPredecessorEdge() );
            path.addEdgeAsFirst( currentNode.getPredecessorEdge() );
            currentNode = graph.getOtherNodeOf( currentNode.getPredecessorEdge(), currentNode );
        }
        return path;
    }

    public static List<Edge> fillWithCoordinates( List<Edge> edges, Map<Edge, List<Coordinate>> coordinateMap ) {
        for ( Edge edge : edges ) {
            edge.setCoordinates( coordinateMap.get( edge ) );
        }
        return edges;
    }

    public static Graph subgraph( Graph graph, GraphEntityFactory graphEntityFactory, Node centerNode, int distance ) {
        Graph subgraph = graphEntityFactory.createGraph();
        Map<Node.Id, Boolean> visitedNodes = new HashMap<>();
        Map<Edge.Id, Boolean> visitedEdges = new HashMap<>();
        Queue<NodeContainer> nodeQueue = new LinkedList<>();
        subgraph.addNode( centerNode );
        visitedNodes.put( centerNode.getId(), Boolean.TRUE );
        nodeQueue.add( new NodeContainer( centerNode, 0 ) );
        while ( !nodeQueue.isEmpty() ) {
            NodeContainer currentNode = nodeQueue.poll();
            if ( currentNode.distance < distance ) {
                Set<Edge> edges = graph.getEdgesOf( currentNode.node );
                for ( Edge edge : edges ) {
                    if ( !visitedEdges.containsKey( edge.getId() ) ) {
                        Node otherNode = edge.getOtherNode( currentNode.node );
                        if ( !visitedNodes.containsKey( otherNode.getId() ) ) {
                            subgraph.addNode( otherNode );
                            visitedNodes.put( currentNode.node.getId(), Boolean.TRUE );
                            nodeQueue.add( new NodeContainer( otherNode, currentNode.distance + 1 ) );
                        }
                        subgraph.addEdge( edge );
                        visitedEdges.put( edge.getId(), Boolean.TRUE );
                    }
                }
            }
        }
        return subgraph;
    }

    private static class NodeContainer {

        public final Node node;
        public int distance;

        public NodeContainer( Node node, int distance ) {
            this.node = node;
            this.distance = distance;
        }

    }
}
