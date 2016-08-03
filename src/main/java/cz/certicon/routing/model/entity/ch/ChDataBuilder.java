/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.DistanceType;

/**
 * Interface defining functionality of the CH data builder
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> CH data type
 */
public interface ChDataBuilder<T> {

    /**
     * Setter for the startId. StartId is the id of the first shortcut to be
     * created - all the shortcuts created will be equal or greater to this
     * startId.
     *
     * @param startId ideally last edge id + 1
     */
    public void setStartId( long startId );

    /**
     * Sets rank to a node
     *
     * @param nodeId id of the node
     * @param rank new rank of the node
     */
    public void setRank( long nodeId, int rank );

    /**
     * Adds shortcut - pair of two consequent shortcuts/edges, which together
     * create another shortcut
     *
     * @param shortcutId new shortcut id (greater or equal to startId)
     * @param sourceEdgeId id of the source edge/shortcut
     * @param targetEdgeId id of the target edge/shortcut
     */
    public void addShortcut( long shortcutId, long sourceEdgeId, long targetEdgeId );

    /**
     * Returns distance type of the data
     *
     * @return distance type of the data
     */
    public DistanceType getDistanceType();

    /**
     * Builds the data and returns them
     *
     * @return built data
     */
    public T build();
}
