/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing.ch;

import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.entity.Coordinates;
import cz.certicon.routing.model.entity.Edge;
import cz.certicon.routing.model.entity.EdgeAttributes;
import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.GraphEntityFactory;
import cz.certicon.routing.model.entity.Node;
import cz.certicon.routing.model.entity.Path;
import cz.certicon.routing.model.entity.Shortcut;
import cz.certicon.routing.model.entity.common.SimpleShortcut;
import cz.certicon.routing.utils.GraphUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesPreprocessor {

    private static final int DISTANCE = 5;

    /**
     * Returns ranked nodes by importance ordering
     *
     * @param graph an instance of {@link Graph} to calculate on
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * related with the given graph
     * @param distanceFactory an instance of {@link DistanceFactory} related
     * with the given graph
     * @return an instance of {@link HashMap} with {@link Integer} ranks
     */
    public Map<Node, Integer> preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        Map<Node, Integer> contractedNeighboursCountMap = new HashMap<>();
        NodeDataStructure<Node> priorityQueue = new JgraphtFibonacciDataStructure<>();
        DijkstraRoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, graphEntityFactory, distanceFactory );
        for ( Node node : graph.getNodes() ) {
            int degree = graph.getDegreeOf( node );
            int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, node );
            priorityQueue.add( node, numberOfShortcuts - degree );
            contractedNeighboursCountMap.put( node, 0 );
        }
        int rank = 1;
        Map<Node, Integer> rankMap = new HashMap<>();
        while ( !priorityQueue.isEmpty() ) {
            Node min = priorityQueue.extractMin();
            // shortcuts
            contraction( routingAlgorithm, graph, graphEntityFactory, distanceFactory, min );
            // Neighbours only heuristic + Spatial diversity heuristic
            for ( Edge edge : graph.getEdgesOf( min ) ) {
                Node neighbour = edge.getOtherNode( min );
                int count = 1 + contractedNeighboursCountMap.get( neighbour );
                int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, neighbour );
                priorityQueue.notifyDataChange( neighbour, count + numberOfShortcuts );
            }
            rankMap.put( min, rank++ );
        }
        return rankMap;
    }

    private int numberOfShortcuts( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, Node node ) {
        Graph subgraph = GraphUtils.subgraph( graph, graphEntityFactory, node, DISTANCE );
        routingAlgorithm.setGraph( subgraph );
        Map<Pair<Node, Node>, Distance> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = subgraph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            Node neighbourA = edge.getOtherNode( node );
            for ( Edge edge1 : edgesOf ) {
                Node neighbourB = edge1.getOtherNode( node );
                if ( !neighbourA.equals( neighbourB ) ) {
                    fromToDistanceMap.put( new Pair<>( neighbourA, neighbourB ), edge.getDistance().add( edge1.getDistance() ) );
                }
            }
        }
        subgraph.removeNode( node );
        int numOfShortcuts = 0;
        for ( Map.Entry<Pair<Node, Node>, Distance> entry : fromToDistanceMap.entrySet() ) {
            Path route = routingAlgorithm.route( entry.getKey().a.getId(), entry.getKey().b.getId() );
            if ( route != null && route.getDistance().isGreaterThan( entry.getValue() ) ) {
                subgraph.addEdge( graphEntityFactory.createEdge( Edge.Id.generateId(), entry.getKey().a, entry.getKey().b, entry.getValue() ) );
                numOfShortcuts++;
            }
        }
        return numOfShortcuts;
    }

    private Set<Edge> contraction( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, Node node ) {
        routingAlgorithm.setGraph( graph );
        Map<Pair<Pair<Node, Edge>, Pair<Node, Edge>>, Distance> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = graph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            Node neighbourA = edge.getOtherNode( node );
            for ( Edge edge1 : edgesOf ) {
                Node neighbourB = edge1.getOtherNode( node );
                if ( !neighbourA.equals( neighbourB ) ) {
                    fromToDistanceMap.put( new Pair<>( new Pair<>( neighbourA, edge ), new Pair<>( neighbourB, edge1 ) ), edge.getDistance().add( edge1.getDistance() ) );
                }
            }
        }
        graph.removeNode( node );
        Set<Edge> shortcuts = new HashSet<>();
        for ( Map.Entry<Pair<Pair<Node, Edge>, Pair<Node, Edge>>, Distance> entry : fromToDistanceMap.entrySet() ) {
            Node from = entry.getKey().a.a;
            Node to = entry.getKey().b.a;
            Path route = routingAlgorithm.route( from.getId(), to.getId() );
            if ( route != null && route.getDistance().isGreaterThan( entry.getValue() ) ) {
                Edge fromEdge = entry.getKey().a.b;
                Edge toEdge = entry.getKey().b.b;
                Shortcut shortcut  = new SimpleShortcut(Edge.Id.generateId(), fromEdge, toEdge );
                graph.addEdge( shortcut );
            }
        }
        return shortcuts;
    }
}
