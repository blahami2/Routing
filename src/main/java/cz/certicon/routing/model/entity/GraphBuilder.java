/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity;

/**
 * Builder for the graph.
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> Graph class
 */
public interface GraphBuilder<T> {

    /**
     * Adds node to the graph
     *
     * @param id node's id
     * @param dataId id of the node's data
     * @param osmId original node id in OSM
     * @param latitude node's latitude
     * @param longitude node's longitude
     */
    public void addNode( long id, long dataId, long osmId, double latitude, double longitude );

    /**
     * Adds edge to the graph
     *
     * @param id edge's id
     * @param dataId id of the edge's data
     * @param osmId original edge's id in OSM
     * @param sourceId id of the source node
     * @param targetId id of the target node
     * @param length edge's length
     * @param speed edge's maximal allowed speed
     * @param isPaid true if the edge has somehow paid passage, false otherwise
     */
    public void addEdge( long id, long dataId, long osmId, long sourceId, long targetId, double length, double speed, boolean isPaid );

    /**
     * Builds and returns the complete graph
     *
     * @return complete graph
     */
    public T build();
}
