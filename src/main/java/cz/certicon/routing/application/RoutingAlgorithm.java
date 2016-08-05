/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.application;

import cz.certicon.routing.model.entity.Graph;
import cz.certicon.routing.model.entity.NodeSet;
import cz.certicon.routing.model.entity.NodeSet.NodeEntry;
import java.util.Map;

/**
 * The root interface for routing algorithms. It's purpose is to find the
 * shortest path between two points. The distance between two points is abstract
 * (geographical distance, time, etc.).
 *
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <G> the graph class to be routed upon
 */
public interface RoutingAlgorithm<G> {

    /**
     * Find the shortest route (R) between a set of source points to a set of
     * target points
     *
     * @param <R> route return type, see {@link RouteBuilder}
     * @param routeBuilder builder for the result route
     * @param nodeSet holder of initial routing points
     * @return the shortest route of type R
     * @throws RouteNotFoundException thrown when no route was found between the
     * two points, see the {@link RouteNotFoundException} for more information
     */
    public <R> R route( RouteBuilder<R, G> routeBuilder, NodeSet<G> nodeSet ) throws RouteNotFoundException;

    public static class Utils {

        public static int getNodeId( Graph graph, NodeEntry nodeEntry ) {
            return nodeEntry.getNodeId();
        }

        public static int getEdgeId( Graph graph, NodeEntry nodeEntry ) {
            return nodeEntry.getEdgeId();
        }

        public static float getDistance( Graph graph, NodeEntry nodeEntry ) {
            return nodeEntry.getDistance();
        }

        public static void initArrays( Graph graph, float[] nodeDistanceArray, NodeDataStructure<Integer> nodeDataStructure, Map<Integer, NodeEntry> map ) {
            for ( NodeEntry nodeEntry : map.values() ) {
                int node = getNodeId( graph, nodeEntry );
                int edge = getEdgeId( graph, nodeEntry );
                float distance = getDistance( graph, nodeEntry );
                nodeDistanceArray[node] = distance;
                nodeDataStructure.add( node, distance );
            }

        }
    }
}
