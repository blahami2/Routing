/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application.preprocessing.ch;

import com.sun.javafx.animation.TickCalculation;
import cz.certicon.routing.application.algorithm.Distance;
import cz.certicon.routing.application.algorithm.DistanceFactory;
import cz.certicon.routing.application.algorithm.NodeDataStructure;
import cz.certicon.routing.application.algorithm.RoutingAlgorithm;
import cz.certicon.routing.application.algorithm.algorithms.dijkstra.DijkstraRoutingAlgorithm;
import cz.certicon.routing.application.algorithm.datastructures.JgraphtFibonacciDataStructure;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public class ContractionHierarchiesPreprocessor {

    private static final int DISTANCE = 5;
    private static final double INIT_NODE_RANKING = 0.5;

    /**
     * Returns ranked nodes by importance ordering
     *
     * @param graphInput an instance of {@link Graph} to calculate on
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * related with the given graph
     * @param distanceFactory an instance of {@link DistanceFactory} related
     * with the given graph
     * @return an instance of {@link HashMap} with {@link Integer} ranks
     */
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graphInput, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory ) {
        return preprocess( graphInput, graphEntityFactory, distanceFactory, null );
    }

    /**
     * Returns ranked nodes by importance ordering
     *
     * @param graph an instance of {@link Graph} to calculate on
     * @param graphEntityFactory an instance of {@link GraphEntityFactory}
     * related with the given graph
     * @param distanceFactory an instance of {@link DistanceFactory} related
     * with the given graph
     * @param progressListener listener for progress update
     * @return an instance of {@link HashMap} with {@link Integer} ranks
     */
    public Pair<Map<Node.Id, Integer>, List<Shortcut>> preprocess( Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, ProgressListener progressListener ) {
//        System.out.println( "**** EDGES ****" );
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( edge );
//        }
        Map<Node, Integer> contractedNeighboursCountMap = new HashMap<>();
        NodeDataStructure<Node> priorityQueue = new JgraphtFibonacciDataStructure<>();
        DijkstraRoutingAlgorithm routingAlgorithm = new DijkstraRoutingAlgorithm( graph, graphEntityFactory, distanceFactory );
        List<Node> nodes = new ArrayList<>( graph.getNodes() );

        int interval = Integer.MAX_VALUE;
        int counter = 0;
        if ( progressListener != null ) {
            interval = (int) ( nodes.size() / ( progressListener.getNumOfUpdates() * INIT_NODE_RANKING ) );
            if ( interval <= 0 ) {
                interval = 1;
            }
        }

        for ( Node node : nodes ) {
            int degree = graph.getDegreeOf( node );
            int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, node );
            priorityQueue.add( node, numberOfShortcuts - degree );
            contractedNeighboursCountMap.put( node, 0 );

            if ( ++counter % interval == 0 && progressListener != null ) {
                progressListener.onProgressUpdate( counter / (double) interval / progressListener.getNumOfUpdates() );
            }
//            System.out.println( "added: " + node.getId() + " with value: " + ( numberOfShortcuts - degree ) );
        }
        int rank = 1;
        Map<Node.Id, Integer> rankMap = new HashMap<>();
        List<Shortcut> shortcuts = new ArrayList<>();

        if ( progressListener != null ) {
            interval = (int) Math.ceil( priorityQueue.size() / ( progressListener.getNumOfUpdates() * ( 1.0 - INIT_NODE_RANKING ) ) );
            counter = 0;
            if ( interval <= 0 ) {
                interval = 1;
            }
        }

        Set<Edge> removedEdges = new HashSet<>();
        Set<Node> removedNodes = new HashSet<>();
//        Set<Edge> addedEdges = new HashSet<>();
        while ( !priorityQueue.isEmpty() ) {
            Node min = priorityQueue.extractMin();
//            System.out.println( "contracting = " + min.getId() );
            // shortcuts
            shortcuts.addAll( contraction( routingAlgorithm, graph, min, removedEdges, removedNodes ) );
            // Neighbours only heuristic + Spatial diversity heuristic
            for ( Edge edge : graph.getEdgesOf( min ) ) {
                Node neighbour = edge.getOtherNode( min );
//                System.out.println( "neighbour = " + neighbour );
//                System.out.println( "count = " +contractedNeighboursCountMap.get( neighbour ) );
                int count = 1 + contractedNeighboursCountMap.get( neighbour );
                int numberOfShortcuts = numberOfShortcuts( routingAlgorithm, graph, graphEntityFactory, distanceFactory, neighbour );
                if ( priorityQueue.contains( neighbour ) ) {
                    contractedNeighboursCountMap.put( neighbour, count );
                    priorityQueue.notifyDataChange( neighbour, count + numberOfShortcuts - graph.getDegreeOf( neighbour ) );
//                    System.out.println( "changed value: " + neighbour.getId() + " to value: " + ( count + numberOfShortcuts - graph.getDegreeOf( neighbour )  ) );
                }
            }
            rankMap.put( min.getId(), rank++ );

            if ( ++counter % interval == 0 && progressListener != null ) {
//                System.out.println( "counter = " + counter + ", interval = " + interval + ", numofupdates = " + progressListener.getNumOfUpdates() );
                progressListener.onProgressUpdate( ( 1.0 - INIT_NODE_RANKING ) + ( counter / (double) interval / progressListener.getNumOfUpdates() ) );
            }
//            System.out.println( "ranked nodes: " + ( rank - 1 ) );
        }

        for ( Node removedNode : removedNodes ) {
            graph.addNode( removedNode );
        }
        removedEdges.removeAll( shortcuts );
        for ( Edge removedEdge : removedEdges ) {
            graph.addEdge( removedEdge );
        }

//        System.out.println( "======================== PRINT ========================" );
//
//        System.out.println( "**** NODES ****" );
//        for ( Node node : graph.getNodes() ) {
//            System.out.println( node );
//        }
//        System.out.println( "**** EDGES ****" );
//        for ( Edge edge : graph.getEdges() ) {
//            System.out.println( edge );
//        }
//        System.out.println( "**** RANKS ****" );
//        for ( Map.Entry<Node.Id, Integer> entry : rankMap.entrySet() ) {
//            System.out.println( entry.getKey() + ": " + entry.getValue() );
//        }
//        System.out.println( "**** SHORTCUTS ****" );
//        for ( Shortcut shortcut : shortcuts ) {
//            System.out.println( shortcut );
//        }
        return new Pair<>( rankMap, shortcuts );
    }

    private int numberOfShortcuts( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, GraphEntityFactory graphEntityFactory, DistanceFactory distanceFactory, Node node ) {
//        System.out.println( "SHORTCUTS FOR : " + node.getId() );
        routingAlgorithm.setGraph( graph );
        Map<Pair<Node, Node>, Distance> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = graph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            if ( edge.getTargetNode().equals( node ) ) {
                Node neighbourA = edge.getSourceNode();
                for ( Edge edge1 : edgesOf ) {
                    if ( edge1.getSourceNode().equals( node ) ) {
                        Node neighbourB = edge1.getTargetNode();
                        if ( !neighbourA.equals( neighbourB ) ) {
                            Pair<Node, Node> pair = new Pair<>( neighbourA, neighbourB );
                            Distance dist = fromToDistanceMap.get( pair );
                            Distance newDist = edge.getDistance().add( edge1.getDistance() );
                            if ( dist == null || dist.isGreaterThan( newDist ) ) {
                                fromToDistanceMap.put( pair, newDist );
//                                System.out.println( "putting (via " + node.getId() + "): " + neighbourA.getId() + " to " + neighbourB.getId() + " dist: " + newDist.getEvaluableValue() );
                            }
                        }
                    }
                }
            }
        }
        List<Edge> addedEdges = new LinkedList<>();
        Set<Edge> removedEdges = graph.removeNode( node );
        int numOfShortcuts = 0;
        for ( Map.Entry<Pair<Node, Node>, Distance> entry : fromToDistanceMap.entrySet() ) {
            Path route = routingAlgorithm.route( entry.getKey().a.getId(), entry.getKey().b.getId() );
            if ( route == null || route.getDistance().isGreaterThan( entry.getValue() ) ) {
                Edge e = graphEntityFactory.createEdge( Edge.Id.generateId(), entry.getKey().a, entry.getKey().b, entry.getValue() );
                addedEdges.add( e );
                graph.addEdge( e );
//                System.out.println( "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue() );
                numOfShortcuts++;
            } else {
//                System.out.println( "route: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + route.getDistance() );
            }
        }
//        System.out.println( "#shortcuts = " + numOfShortcuts );
        graph.addNode( node );
        for ( Edge addedEdge : addedEdges ) {
            graph.removeEdge( addedEdge );
        }
        for ( Edge removedEdge : removedEdges ) {
            graph.addEdge( removedEdge );
        }
        return numOfShortcuts;
    }

    private Set<Shortcut> contraction( DijkstraRoutingAlgorithm routingAlgorithm, Graph graph, Node node, Set<Edge> removedEdges, Set<Node> removedNodes ) {
//        System.out.println( "CONTRACTING: " + node.getId() );
        routingAlgorithm.setGraph( graph );
        Map<Pair<Node, Node>, Trinity<Edge, Edge, Distance>> fromToDistanceMap = new HashMap<>();
        Set<Edge> edgesOf = graph.getEdgesOf( node );
        for ( Edge edge : edgesOf ) {
            if ( edge.getTargetNode().equals( node ) ) {
                Node neighbourA = edge.getSourceNode();
                for ( Edge edge1 : edgesOf ) {
                    if ( edge1.getSourceNode().equals( node ) ) {
                        Node neighbourB = edge1.getTargetNode();
                        if ( !neighbourA.equals( neighbourB ) ) {
                            Pair<Node, Node> pair = new Pair<>( neighbourA, neighbourB );
                            Trinity<Edge, Edge, Distance> get = fromToDistanceMap.get( pair );
                            Distance newDist = edge.getDistance().add( edge1.getDistance() );
                            if ( get == null || get.c.isGreaterThan( newDist ) ) {
                                fromToDistanceMap.put( pair, new Trinity<>( edge, edge1, newDist ) );
                            }
                        }
                    }
                }
            }
        }
        removedEdges.addAll( graph.removeNode( node ) );
        removedNodes.add( node );
        Set<Shortcut> shortcuts = new HashSet<>();
        for ( Map.Entry<Pair<Node, Node>, Trinity<Edge, Edge, Distance>> entry : fromToDistanceMap.entrySet() ) {
            Node from = entry.getKey().a;
            Node to = entry.getKey().b;
            Path route = routingAlgorithm.route( from.getId(), to.getId() );
            if ( route == null || route.getDistance().isGreaterThan( entry.getValue().c ) ) {
                Edge fromEdge = entry.getValue().a;
                Edge toEdge = entry.getValue().b;
                Shortcut shortcut = new SimpleShortcut( Edge.Id.generateId(), fromEdge, toEdge );
//                System.out.println( "shortcut: " + entry.getKey().a.getId() + " to " + entry.getKey().b.getId() + " via " + node.getId() + ", " + entry.getValue().c );
                shortcuts.add( shortcut );
                graph.addEdge( shortcut );
            }
        }
        return shortcuts;
    }

    public static abstract class ProgressListener {

        private int numOfUpdates = 100;

        public abstract void onProgressUpdate( double done );

        public int getNumOfUpdates() {
            return numOfUpdates;
        }

        public void setNumOfUpdates( int numOfUpdates ) {
            this.numOfUpdates = numOfUpdates;
        }
    }
}
