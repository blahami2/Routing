/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.model.entity.ch;

import cz.certicon.routing.model.entity.DistanceType;
import cz.certicon.routing.model.basic.Pair;
import cz.certicon.routing.model.basic.Trinity;
import java.util.Iterator;

/**
 * Interface defining functionality of the CH data extractor - iterators for
 * ranks and shortcuts
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 * @param <T> CH data type
 */
public interface ChDataExtractor<T> {

    /**
     * Creates and returns iterator over nodes and their ranks - returning pairs
     * [nodeId, rank]
     *
     * @return iterator over [nodeId,rank]
     */
    public Iterator<Pair<Long, Integer>> getRankIterator();

    /**
     * Creates and returns iterator over shortcuts - returning trinities
     * [nodeId, startEdgeId, endEdgeId]
     *
     * @return iterator over [nodeId, startEdgeId, endEdgeId]
     */
    public Iterator<Trinity<Long, Long, Long>> getShortcutIterator();

    /**
     * Returns distance type
     *
     * @return distance type
     */
    public DistanceType getDistanceType();
}
