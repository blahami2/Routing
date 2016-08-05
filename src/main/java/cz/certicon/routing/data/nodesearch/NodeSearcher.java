/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.certicon.routing.data.nodesearch;

import cz.certicon.routing.model.entity.Coordinate;
import cz.certicon.routing.model.entity.NodeSetBuilderFactory;
import java.io.IOException;

/**
 * An interface defining the search method for closest nodes to the given
 * coordinates. The distance is measured via distance to edge and then the edge
 * nodes are considered as sources/targets (edge target for source, edge source
 * for target).
 *
 * @author Michael Blaha {@literal <michael.blaha@certicon.cz>}
 */
public interface NodeSearcher {

    /**
     * Finds the closest nodes to the given coordinates, based on the edge to
     * node distance.
     *
     * @param <T> closest nodes structure type
     * @param nodeSetBuilderFactory factory providing a builder for the closest
     * nodes structure
     * @param source coordinates of source
     * @param target coordinates of target
     * @return closest nodes structure
     * @throws IOException thrown when an IO exception occurs
     * @throws EvaluableOnlyException thrown when the closest edge for the
     * source is the same as for the target =&gt; only simple calculation is
     * required then instead of complex routing
     */
    public <T> T findClosestNodes( NodeSetBuilderFactory<T> nodeSetBuilderFactory, Coordinate source, Coordinate target ) throws IOException, EvaluableOnlyException;

    public static enum SearchFor {
        SOURCE, TARGET;
    }
}
