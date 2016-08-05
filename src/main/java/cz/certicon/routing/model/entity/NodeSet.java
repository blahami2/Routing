/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.model.basic.Pair;
import java.util.Iterator;
import java.util.Map;

/**
 * Interface representing the input set of nodes - closest to the starting
 * location and to the ending location.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <G> graph type
 */
public interface NodeSet<G> {

    /**
     * Adds given edge and node to the given category (source or target) with
     * the given distance (distance of the original location to the end of the
     * given edge (to the given node)).
     *
     * @param graph graph to search local ids from
     * @param nodeCategory category to which this shall be added
     * @param edgeId edge's global id
     * @param nodeId node's global id
     * @param distance distance in meters from node to the original location (on
     * the edge)
     */
    public void put( G graph, NodeCategory nodeCategory, long edgeId, long nodeId, float distance );

    /**
     * Returns iterator over all the entries for the given category
     *
     * @param nodeCategory category over which the iterator will iterate
     * @return iterator over all the entries for the given category
     */
    public Iterator<NodeEntry> iterator( NodeCategory nodeCategory );

    /**
     * Returns map with local ids as keys and distances as values. See
     * {@link #put(java.lang.Object, cz.certicon.routing.model.entity.NodeSet.NodeCategory, long, long, float) put()}
     * for more information about the distance.
     *
     * @param graph graph to search local ids from
     * @param nodeCategory category of the map
     * @return map[local_id, distance] for the given category
     */
    public Map<Integer, NodeEntry> getMap( G graph, NodeCategory nodeCategory );

    public void putUpperBound( NodeEntry source, NodeEntry target, float distance );

    /**
     * Returns true if the node-set contains upper bound of any sort, an upper
     * bound can be used to speed up computation. Existence of upper bound also
     * means that some path has already been found without routing - e.g.
     * starting and ending point are both on the same edge in the correct order
     *
     * @return true if the node-set contains upper bound, false otherwise
     */
    public boolean hasUpperBound();

    /**
     * Returns upper bound, see {@link #hasUpperBound() hasUpperBound()} for
     * more details
     *
     * @return upper bound (length of a known path)
     */
    public float getUpperBound();

    /**
     * Returns entries for the upper bound
     * ({@link #hasUpperBound() hasUpperBound()})
     *
     * @return entries for the upper bound
     */
    public Pair<NodeEntry, NodeEntry> getUpperBoundEntries();

    /**
     * Class representing a single node entry. It contains node's global id,
     * edge's global id (the edge close to the original location, on which the
     * node lies), distance (distance from point on the edge (closest to the
     * original location) to the node).
     */
    public static class NodeEntry {

        private final int edgeId;
        private final int nodeId;
        private final float distance;

        /**
         * Constructor
         *
         * @param edgeId local id of the edge close to the original location
         * @param nodeId local id of the node related to the edge
         * @param distance distance of the node to the original location (mapped
         * to edge)
         */
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

    /**
     * Enumeration of possible node categories: category for nodes around the
     * source location and category for nodes around the target location.
     */
    public static enum NodeCategory {
        SOURCE, TARGET;
    }
}
