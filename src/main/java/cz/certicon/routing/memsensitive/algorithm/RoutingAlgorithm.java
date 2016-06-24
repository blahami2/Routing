/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.memsensitive.algorithm;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface RoutingAlgorithm<G> {

    public <R> R route( RouteBuilder<R, G> routeBuilder, Map<Integer, NodeEntry> from, Map<Integer, NodeEntry> to ) throws RouteNotFoundException;

    
    public static class NodeEntry {

        private final int edgeId;
        private final int nodeId;
        private final float distance;

        public NodeEntry( int edgeId, int nodeId, float distance ) {
            this.edgeId = edgeId;
            this.nodeId = nodeId;
            this.distance = distance;
        }

        public int getEdgeId() {
            return edgeId;
        }

        public int getNodeId() {
            return nodeId;
        }

        public float getDistance() {
            return distance;
        }
    }
}
