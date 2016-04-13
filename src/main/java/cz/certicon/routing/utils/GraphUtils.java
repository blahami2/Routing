/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.utils;

import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.data.coordinates.CoordinateReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Graph utility class.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class GraphUtils {

    /**
     * Creates path in given graph from source to target based on the
     * predecessor edges of nodes (must be set!)
     *
     * @param graph an instance of {@link Graph} containing the two nodes
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * consistent with the given graph
     * @param targetNode a target {@link Node} of the path, must have
     * predecessor edge set
     * @return an instance of {@link Path} between the two nodes in the given
     * graph.
     */
    public static Path createPath( Graph graph, GraphEntityFactory graphEntityFactory, Node targetNode ) {
        Path path = graphEntityFactory.createPathWithTarget( graph, targetNode );
        Node currentNode = targetNode;
        while ( currentNode.getPredecessorEdge() != null ) {
            path.addEdgeAsFirst( currentNode.getPredecessorEdge() );
            currentNode = graph.getOtherNodeOf( currentNode.getPredecessorEdge(), currentNode );
        }
        return path;
    }

    /**
     * Fills edges with coordinates from the given map. Usually used with the
     * {@link CoordinateReader}
     *
     * @param edges list of {@link Edge}s
     * @param coordinateMap map of {@link Edge}s and their {@link Coordinates}s
     * @return given list of edges with the filled coordinates (geometry)
     */
    public static List<Edge> fillWithCoordinates( List<Edge> edges, Map<Edge, List<Coordinates>> coordinateMap ) {
        for ( Edge edge : edges ) {
            edge.setCoordinates( coordinateMap.get( edge ) );
        }
        return edges;
    }

    /**
     * Creates subgraph of the given graph with a given node in the center and
     * all the nodes/edges in the given distance from it.
     *
     * @param graph base {@link Graph}
     * @param graphEntityFactory graph-related {@link GraphEntityFactory}
     * @param centerNode {@link Node} in the center of the subgraph
     * @param distance rank of the farthest node
     * @return subgraph as an instance of {@link Graph}
     */
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
