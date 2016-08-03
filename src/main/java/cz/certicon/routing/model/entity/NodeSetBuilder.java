/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

import cz.certicon.routing.data.nodesearch.EvaluableOnlyException;
import cz.certicon.routing.model.entity.NodeSet.NodeCategory;

/**
 * Builder for node set.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> type of NodeSet
 */
public interface NodeSetBuilder<T> {

    /**
     * Adds given edge and node to the given category (source or target) with
     * the given distance (distance of the original location to the end of the
     * given edge (to the given node)).
     *
     * @param nodeCategory category to which this shall be added
     * @param edgeId edge's global id
     * @param nodeId node's global id
     * @param length distance in meters from node to the original location (on
     * the edge)
     * @param speed edge's maximal speed
     */
    public void addNode( NodeCategory nodeCategory, long nodeId, long edgeId, float length, float speed );

    /**
     * Adds node as a crossroad (without edge). The distance is implicitly zero.
     *
     * @param nodeCategory category to which this shall be added
     * @param nodeId node's global id
     */
    public void addCrossroad( NodeCategory nodeCategory, long nodeId );

    /**
     * Creates and returns the node set
     *
     * @return node set
     * @throws EvaluableOnlyException thrown when all the edges are the same,
     * therefore it is not a routing issue, just simple calculation.
     */
    public T build() throws EvaluableOnlyException;
}
