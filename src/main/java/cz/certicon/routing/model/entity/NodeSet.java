/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeSet<G> {

    public void put( NodeCategory nodeCategory, long edgeId, long nodeId, float distance );

    public Iterator<NodeEntry> iterator( NodeCategory nodeCategory );
    
    public Map<Integer,Float> getMap(G graph, NodeCategory nodeCategory);

    public static class NodeEntry {

        private final long edgeId;
        private final long nodeId;
        private final float distance;

        public NodeEntry( long edgeId, long nodeId, float distance ) {
            this.edgeId = edgeId;
            this.nodeId = nodeId;
            this.distance = distance;
        }

        public long getEdgeId() {
            return edgeId;
        }

        public long getNodeId() {
            return nodeId;
        }

        public float getDistance() {
            return distance;
        }

    }

    public static enum NodeCategory {
        SOURCE, TARGET;
    }
}
